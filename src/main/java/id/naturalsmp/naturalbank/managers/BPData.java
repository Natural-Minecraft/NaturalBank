package id.naturalsmp.naturalbank.managers;

import id.naturalsmp.naturalbank.NaturalBank;
import id.naturalsmp.naturalbank.account.BPPlayer;
import id.naturalsmp.naturalbank.account.PlayerRegistry;
import id.naturalsmp.naturalbank.bankSystem.BankRegistry;
import id.naturalsmp.naturalbank.bankTop.BPBankTop;
import id.naturalsmp.naturalbank.commands.BPCmdRegistry;
import id.naturalsmp.naturalbank.commands.BankTopCmd;
import id.naturalsmp.naturalbank.commands.MainCmd;
import id.naturalsmp.naturalbank.economy.EconomyUtils;
import id.naturalsmp.naturalbank.external.UpdateChecker;
import id.naturalsmp.naturalbank.external.bStats;
import id.naturalsmp.naturalbank.interest.BPInterest;
import id.naturalsmp.naturalbank.listeners.AFKListener;
import id.naturalsmp.naturalbank.listeners.BPTransactionListener;
import id.naturalsmp.naturalbank.listeners.InventoryCloseListener;
import id.naturalsmp.naturalbank.listeners.PlayerServerListener;
import id.naturalsmp.naturalbank.listeners.bankListener.*;
import id.naturalsmp.naturalbank.listeners.playerChat.*;
import id.naturalsmp.naturalbank.loanSystem.BPLoanRegistry;
import id.naturalsmp.naturalbank.sql.BPSQL;
import id.naturalsmp.naturalbank.utils.BPLogger;
import id.naturalsmp.naturalbank.utils.texts.BPChat;
import id.naturalsmp.naturalbank.values.ConfigValues;
import id.naturalsmp.naturalbank.values.MessageValues;
import id.naturalsmp.naturalbank.values.MultipleBanksValues;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

public class BPData {

    private boolean start = true;

    private final NaturalBank plugin;

    public BPData(NaturalBank plugin) {
        this.plugin = plugin;
    }

    public void setupPlugin() {
        long startTime = System.currentTimeMillis();

        BPLogger.Console.log("");
        BPLogger.Console.log("    " + BPChat.PREFIX + " <green>Enabling plugin...");
        BPLogger.Console.log("    <green>Running on version <white>" + plugin.getDescription().getVersion() + "</white>!");
        BPLogger.Console.log("    <green>Detected server version: <white>" + NaturalBank.getServerVersion());
        BPLogger.Console.log("    <green>Setting up the plugin...");
        BPLogger.Console.log("");

        new bStats(plugin);
        plugin.getConfigs().setupConfigs();
        reloadPlugin();

        BPLoanRegistry.loadAllLoans();

        registerEvents();
        setupCommands();

        BPLogger.Console.log("    <green>Done! <dark_gray>(<aqua>" + (System.currentTimeMillis() - startTime) + "ms</aqua>)");
        BPLogger.Console.log("");

        if (ConfigValues.isBankTopEnabled()) BPBankTop.updateBankTop();
        start = false;
    }

    public void shutdownPlugin() {
        EconomyUtils.saveEveryone(false);
        if (ConfigValues.isInterestEnabled()) plugin.getInterest().saveInterest();
        BPLoanRegistry.saveAllLoans();

        BPLogger.Console.log("");
        BPLogger.Console.log("    " + BPChat.PREFIX + " <red>Plugin successfully disabled!");
        BPLogger.Console.log("");
    }

    public boolean reloadPlugin() {
        boolean success = true;
        try {
            ConfigValues.setupValues();
            MessageValues.setupValues();
            MultipleBanksValues.setupValues();

            BPCmdRegistry.registerPluginCommands();
            BPLogger.LogsFile.setupLoggerFile();

            if (ConfigValues.isIgnoringAfkPlayers()) plugin.getAfkManager().startCountdown();
            if (ConfigValues.isBankTopEnabled() && !BPTaskManager.contains(BPTaskManager.BANKTOP_BROADCAST_TASK)) BPBankTop.restartBankTopUpdateTask();

            BPAFK BPAFK = plugin.getAfkManager();
            if (!BPAFK.isPlayerCountdownActive()) BPAFK.startCountdown();

            BPInterest interest = plugin.getInterest();
            if (ConfigValues.isInterestEnabled() && interest.wasDisabled()) interest.restartInterest(start);

            // Load the banks to the registry before to make MySQL able to create the tables.
            BankRegistry.loadBanks();

            BPSQL.disconnect();
            if (ConfigValues.isMySqlEnabled()) BPSQL.MySQL.connect();
            else BPSQL.SQLite.connect();

            // Do this check to avoid restarting the saving interval if another one is finishing.
            if (!BPTaskManager.contains(BPTaskManager.MONEY_SAVING_TASK)) EconomyUtils.restartSavingInterval();

            Bukkit.getOnlinePlayers().forEach(p -> {
                BPPlayer player = PlayerRegistry.get(p);
                if (player != null && player.getOpenedBank() != null) p.closeInventory();
            });
        } catch (Exception e) {
            BPLogger.Console.warn(e, "Something went wrong while trying to reload the plugin.");
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
        plManager.registerEvents(new BPTransactionListener(), plugin);

        String chatPriority = ConfigValues.getPlayerChatPriority();
        if (chatPriority == null) plManager.registerEvents(new PlayerChatNormal(), plugin);
        else switch (chatPriority) {
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
        if (bankClickPriority == null) plManager.registerEvents(new BankClickNormal(), plugin);
        else switch (bankClickPriority) {
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
        plugin.getCommand("NaturalBank").setExecutor(new MainCmd());
        plugin.getCommand("NaturalBank").setTabCompleter(new MainCmd());
        plugin.getCommand("banktop").setExecutor(new BankTopCmd());
    }
}