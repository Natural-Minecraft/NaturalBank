package id.naturalsmp.naturalbank.commands.list;

import id.naturalsmp.naturalbank.NaturalBank;
import id.naturalsmp.naturalbank.commands.NBCmdExecution;
import id.naturalsmp.naturalbank.commands.NBCommand;
import id.naturalsmp.naturalbank.utils.texts.NBMessages;
import id.naturalsmp.naturalbank.values.ConfigValues;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.List;

public class RestartInterestCmd extends NBCommand {

    public RestartInterestCmd(FileConfiguration commandsConfig, String commandID) {
        super(commandsConfig, commandID);
    }

    public RestartInterestCmd(FileConfiguration commandsConfig, String commandID, String... aliases) {
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
    public NBCmdExecution onExecution(CommandSender s, String[] args) {
        if (!ConfigValues.isInterestEnabled()) {
            NBMessages.sendIdentifier(s, "Interest-Disabled");
            return NBCmdExecution.invalidExecution();
        }

        return new NBCmdExecution() {
            @Override
            public void execute() {
                NaturalBank.INSTANCE().getInterest().restartInterest(false);
                NBMessages.sendMessage(s, "%prefix% Interest cooldown restarted!");
            }
        };
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        return null;
    }
}