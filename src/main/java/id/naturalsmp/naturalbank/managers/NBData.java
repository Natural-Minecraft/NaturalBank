package id.naturalsmp.naturalbank.managers;

import id.naturalsmp.naturalbank.NaturalBank;
import id.naturalsmp.naturalbank.account.NBPlayer;
import id.naturalsmp.naturalbank.account.PlayerRegistry;
import id.naturalsmp.naturalbank.bankSystem.BankRegistry;
import id.naturalsmp.naturalbank.bankTop.NBBankTop;
import id.naturalsmp.naturalbank.commands.NBCmdRegistry;
import id.naturalsmp.naturalbank.commands.BankTopCmd;
import id.naturalsmp.naturalbank.commands.MainCmd;
import id.naturalsmp.naturalbank.economy.EconomyUtils;
import id.naturalsmp.naturalbank.external.UpdateChecker;
import id.naturalsmp.naturalbank.external.bStats;
import id.naturalsmp.naturalbank.interest.NBInterest;
import id.naturalsmp.naturalbank.listeners.AFKListener;
import id.naturalsmp.naturalbank.listeners.NBTransactionListener;
import id.naturalsmp.naturalbank.listeners.InventoryCloseListener;
import id.naturalsmp.naturalbank.listeners.PlayerServerListener;
import id.naturalsmp.naturalbank.listeners.bankListener.*;
import id.naturalsmp.naturalbank.listeners.playerChat.*;
import id.naturalsmp.naturalbank.loanSystem.NBLoanRegistry;
import id.naturalsmp.naturalbank.sql.NBSQL;
import id.naturalsmp.naturalbank.utils.NBLogger;
import id.naturalsmp.naturalbank.utils.texts.NBChat;
import id.naturalsmp.naturalbank.values.ConfigValues;
import id.naturalsmp.naturalbank.values.MessageValues;
import id.naturalsmp.naturalbank.values.MultipleBanksValues;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

public class NBData {

    private boolean start = true;

    private final NaturalBank plugin;

    public NBData(NaturalBank plugin) {
        this.plugin = plugin;
    }

    public void setupPlugin() {
        long startTime = System.currentTimeMillis();

        NBLogger.Console.log("");
        NBLogger.Console.log("    " + NBChat.PREFIX + " <green>Enabling plugin...");
        NBLogger.Console
                .log("    <green>Running on version <white>" + plugin.getDescription().getVersion() + "</white>!");
        NBLogger.Console.log("    <green>Detected server version: <white>" + NaturalBank.getServerVersion());
        NBLogger.Console.log("    <green>Setting up the plugin...");
        NBLogger.Console.log("");

        new bStats(plugin);
        plugin.getConfigs().setupConfigs();
        reloadPlugin();

        NBLoanRegistry.loadAllLoans();

        registerEvents();
        setupCommands();

        NBLogger.Console
                .log("    <green>Done! <dark_gray>(<aqua>" + (System.currentTimeMillis() - startTime) + "ms</aqua>)");
        NBLogger.Console.log("");

        if (ConfigValues.isBankTopEnabled())
            NBBankTop.updateBankTop();
        start = false;
    }

    public void shutdownPlugin() {
        EconomyUtils.saveEveryone(false);
        if (ConfigValues.isInterestEnabled())
            plugin.getInterest().saveInterest();
        NBLoanRegistry.saveAllLoans();

        NBLogger.Console.log("");
        NBLogger.Console.log("    " + NBChat.PREFIX + " <red>Plugin successfully disabled!");
        NBLogger.Console.log("");
    }

    public boolean reloadPlugin() {
        boolean success = true;
        try {
            ConfigValues.setupValues();
            MessageValues.setupValues();
            MultipleBanksValues.setupValues();

            NBCmdRegistry.registerPluginCommands();
            NBLogger.LogsFile.setupLoggerFile();

            if (ConfigValues.isIgnoringAfkPlayers())
                plugin.getAfkManager().startCountdown();
            if (ConfigValues.isBankTopEnabled() && !NBTaskManager.contains(NBTaskManager.BANKTOP_BROADCAST_TASK))
                NBBankTop.restartBankTopUpdateTask();

            NBAFK nbAfk = plugin.getAfkManager();
            if (!nbAfk.isPlayerCountdownActive())
                nbAfk.startCountdown();

            NBInterest interest = plugin.getInterest();
            if (ConfigValues.isInterestEnabled() && interest.wasDisabled())
                interest.restartInterest(start);

            // Load the banks to the registry before to make MySQL able to create the
            // tables.
            BankRegistry.loadBanks();

            NBSQL.disconnect();
            if (ConfigValues.isMySqlEnabled())
                NBSQL.MySQL.connect();
            else
                NBSQL.SQLite.connect();

            // Do this check to avoid restarting the saving interval if another one is
            // finishing.
            if (!NBTaskManager.contains(NBTaskManager.MONEY_SAVING_TASK))
                EconomyUtils.restartSavingInterval();

            Bukkit.getOnlinePlayers().forEach(p -> {
                NBPlayer player = PlayerRegistry.get(p);
                if (player != null && player.getOpenedBank() != null)
                    p.closeInventory();
            });
        } catch (Exception e) {
            NBLogger.Console.warn(e, "Something went wrong while trying to reload the plugin.");
            success = false;
        }
        return success;
    }

    private void registerEvents() {
        PluginManager plManager = plugin.getServer().getPluginManager();

        plManager.registerEvents(new PlayerServerListener(), plugin);
        plManager.registerEvents(new UpdateChecker(), plugin);
        plManager.registerEvents(new AFKListener(plugin), plugin);
        plManager.registerEvents(new InventoryCloseListener(), plugin);
        plManager.registerEvents(new NBTransactionListener(), plugin);

        String chatPriority = ConfigValues.getPlayerChatPriority();
        if (chatPriority == null)
            plManager.registerEvents(new PlayerChatNormal(), plugin);
        else
            switch (chatPriority) {
                case "LOWEST":
                    plManager.registerEvents(new PlayerChatLowest(), plugin);
                    break;
                case "LOW":
                    plManager.registerEvents(new PlayerChatLow(), plugin);
                    break;
                case "HIGH":
                    plManager.registerEvents(new PlayerChatHigh(), plugin);
                    break;
                case "HIGHEST":
                    plManager.registerEvents(new PlayerChatHighest(), plugin);
                    break;
                default:
                    plManager.registerEvents(new PlayerChatNormal(), plugin);
                    break;
            }

        String bankClickPriority = ConfigValues.getBankClickPriority();
        if (bankClickPriority == null)
            plManager.registerEvents(new BankClickNormal(), plugin);
        else
            switch (bankClickPriority) {
                case "LOWEST":
                    plManager.registerEvents(new BankClickLowest(), plugin);
                    break;
                case "LOW":
                    plManager.registerEvents(new BankClickLow(), plugin);
                    break;
                case "HIGH":
                    plManager.registerEvents(new BankClickHigh(), plugin);
                    break;
                case "HIGHEST":
                    plManager.registerEvents(new BankClickHighest(), plugin);
                    break;
                default:
                    plManager.registerEvents(new BankClickNormal(), plugin);
                    break;
            }
    }

    private void setupCommands() {
        plugin.getCommand("naturalbank").setExecutor(new MainCmd());
        plugin.getCommand("naturalbank").setTabCompleter(new MainCmd());
        plugin.getCommand("banktop").setExecutor(new BankTopCmd());
    }
}
