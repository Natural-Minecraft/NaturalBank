package id.naturalsmp.naturalbank.commands.list;

import id.naturalsmp.naturalbank.commands.BPCmdExecution;
import id.naturalsmp.naturalbank.commands.BPCommand;
import id.naturalsmp.naturalbank.economy.EconomyUtils;
import id.naturalsmp.naturalbank.utils.BPLogger;
import id.naturalsmp.naturalbank.utils.texts.BPMessages;
import id.naturalsmp.naturalbank.values.ConfigValues;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.List;

public class SaveAllDataCmd extends BPCommand {

    public SaveAllDataCmd(FileConfiguration commandsConfig, String commandID) {
        super(commandsConfig, commandID);
    }

    public SaveAllDataCmd(FileConfiguration commandsConfig, String commandID, String... aliases) {
        super(commandsConfig, commandID, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Collections.emptyList();
    }

    @Override
    public int defaultConfirmCooldown() {
        return 0;
    }

    @Override
    public List<String> defaultConfirmMessage() {
        return Collections.emptyList();
    }

    @Override
    public int defaultCooldown() {
        return 0;
    }

    @Override
    public List<String> defaultCooldownMessage() {
        return Collections.emptyList();
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public boolean skipUsage() {
        return true;
    }

    @Override
    public BPCmdExecution onExecution(CommandSender s, String[] args) {
        return new BPCmdExecution() {
            @Override
            public void execute() {
                EconomyUtils.saveEveryone(true);
                EconomyUtils.restartSavingInterval();

                if (ConfigValues.isBroadcastingSaves()) BPLogger.Console.info("All player data have been saved!");
                BPMessages.sendMessage(s, "%prefix% Successfully saved all player data!");
            }
        };
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        return null;
    }
}