package id.naturalsmp.naturalbank;

import id.naturalsmp.naturalbank.interest.NBInterest;
import id.naturalsmp.naturalbank.managers.NBAFK;
import id.naturalsmp.naturalbank.managers.NBConfigs;
import id.naturalsmp.naturalbank.managers.NBData;
import id.naturalsmp.naturalbank.placeholders.NBPlaceholders;
import id.naturalsmp.naturalbank.utils.NBLogger;
import id.naturalsmp.naturalbank.utils.NBVersions;
import id.naturalsmp.naturalbank.utils.texts.NBChat;
import id.naturalsmp.naturalbank.values.ConfigValues;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

public final class NaturalBank extends JavaPlugin {

    public static final String actualVersion = "1.0.1";

    private static String serverVersion;
    private static NaturalBank INSTANCE;

    private Economy vaultEconomy = null;
    private Permission perms = null;

    private NBPlaceholders nbPlaceholders;
    private NBConfigs nbConfigs;
    private NBData nbData;
    private NBAFK nbAfk;
    private NBInterest interest;

    private boolean isPlaceholderApiHooked = false, isEssentialsXHooked = false, isCmiHooked = false, isUpdated;

    private int tries = 1;

    @Override
    public void onEnable() {
        INSTANCE = this;

        PluginManager plManager = Bukkit.getPluginManager();
        if (plManager.getPlugin("Vault") == null) {
            NBLogger.Console.log("");
            NBLogger.Console.log("<red>Cannot load " + NBChat.PREFIX + ", Vault is not installed.");
            NBLogger.Console.log("<red>Please download it in order to use this plugin.");
            NBLogger.Console.log("");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!setupEconomy()) {
            if (tries < 4) {
                NBLogger.Console.warn(
                        "NaturalBank didn't find any economy plugin on this server, the plugin will re-search in 2 seconds. ("
                                + tries + " try)");
                Bukkit.getScheduler().runTaskLater(this, this::onEnable, 40);
                tries++;
                return;
            }
            NBLogger.Console.log("");
            NBLogger.Console.log("<red>Cannot load " + NBChat.PREFIX + ", No economy plugin found.");
            NBLogger.Console.log("<red>Please download an economy plugin to use this plugin.");
            NBLogger.Console.log("");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        String v = getServer().getVersion();
        serverVersion = v.substring(v.lastIndexOf("MC:"), v.length() - 1).replace("MC: ", "");

        this.nbConfigs = new NBConfigs(this);
        this.nbData = new NBData(this);
        this.nbAfk = new NBAFK(this);
        this.interest = new NBInterest();

        if (!NBConfigs.isUpdated()) {
            NBVersions.renameInterestMoneyGiveToRate();
            NBVersions.convertPlayerFilesToNewStyle();
            NBVersions.changeBankUpgradesSection();
        }

        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp != null)
            perms = rsp.getProvider();

        nbData.setupPlugin();

        if (plManager.getPlugin("PlaceholderAPI") != null) {
            NBLogger.Console.info("Hooked into PlaceholderAPI!");
            nbPlaceholders = new NBPlaceholders();
            nbPlaceholders.registerPlaceholders();
            nbPlaceholders.register();
            isPlaceholderApiHooked = true;
        }
        if (plManager.getPlugin("Essentials") != null) {
            NBLogger.Console.info("Hooked into Essentials!");
            isEssentialsXHooked = true;
        }
        if (plManager.getPlugin("CMI") != null) {
            NBLogger.Console.info("Hooked into CMI!");
            isCmiHooked = true;
        }

        if (ConfigValues.isUpdateCheckerEnabled())
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> isUpdated = isPluginUpdated(), 0,
                    (8 * 1200) * 60 /* 8 hours */);
    }

    @Override
    public void onDisable() {
        nbData.shutdownPlugin();
    }

    public static NaturalBank INSTANCE() {
        return INSTANCE;
    }

    public static String getServerVersion() {
        return serverVersion;
    }

    public static boolean isAlphaVersion() {
        return INSTANCE.getDescription().getVersion().toLowerCase().contains("-alpha");
    }

    public Economy getVaultEconomy() {
        return vaultEconomy;
    }

    public Permission getPermissions() {
        return perms;
    }

    public boolean isPlaceholderApiHooked() {
        return isPlaceholderApiHooked;
    }

    public boolean isEssentialsXHooked() {
        return isEssentialsXHooked;
    }

    public boolean isCmiHooked() {
        return isCmiHooked;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public NBConfigs getConfigs() {
        return nbConfigs;
    }

    public NBData getDataManager() {
        return nbData;
    }

    public NBAFK getAfkManager() {
        return nbAfk;
    }

    public NBInterest getInterest() {
        return interest;
    }

    public NBPlaceholders getNbPlaceholders() {
        return nbPlaceholders;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null)
            return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
            return false;
        vaultEconomy = rsp.getProvider();
        return true;
    }

    private boolean isPluginUpdated() {
        String newVersion = getDescription().getVersion();
        boolean updated = true;
        try {
            newVersion = new BufferedReader(new InputStreamReader(
                    URI.create("https://api.spigotmc.org/legacy/update.php?resource=93130").toURL().openConnection()
                            .getInputStream()))
                    .readLine();

            updated = true; // effectively disable legacy checking
        } catch (Exception e) {
            NBLogger.Console.warn("Could not check for updates. (No internet connection)");
        }

        if (isAlphaVersion() && !ConfigValues.isSilentInfoMessages())
            NBLogger.Console.info(
                    "You are using an alpha version of the plugin, please report any bug or problem found in my discord!");

        if (updated) {
            if (!ConfigValues.isSilentInfoMessages())
                NBLogger.Console.info("The plugin is updated!");
        } else {
            // Even if the info is disabled, notify when there is a new update
            // because it is important to keep users at the latest version.
            NBLogger.Console.info("New version of the plugin available! (v" + newVersion + ").");
            NBLogger.Console.info(
                    "Please download the latest version from our official source.");
        }
        return updated;
    }
}