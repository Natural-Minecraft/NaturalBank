package id.naturalsmp.naturalbank.commands.list;

import id.naturalsmp.naturalbank.bankSystem.Bank;
import id.naturalsmp.naturalbank.bankSystem.BankRegistry;
import id.naturalsmp.naturalbank.bankSystem.BankUtils;
import id.naturalsmp.naturalbank.commands.NBCmdExecution;
import id.naturalsmp.naturalbank.commands.NBCommand;
import id.naturalsmp.naturalbank.utils.NBUtils;
import id.naturalsmp.naturalbank.utils.texts.NBArgs;
import id.naturalsmp.naturalbank.utils.texts.NBMessages;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;
import java.util.List;

public class SetLevelCmd extends NBCommand {

    public SetLevelCmd(FileConfiguration commandsConfig, String commandID) {
        super(commandsConfig, commandID);
    }

    public SetLevelCmd(FileConfiguration commandsConfig, String commandID, String... aliases) {
        super(commandsConfig, commandID, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Collections.singletonList("%prefix% Usage: /bank setLevel [level] [bankName]");
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
        return false;
    }

    @Override
    public NBCmdExecution onExecution(CommandSender s, String[] args) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore()) {
            NBMessages.sendIdentifier(s, "Invalid-Player");
            return NBCmdExecution.invalidExecution();
        }

        if (args.length == 2) {
            NBMessages.sendIdentifier(s, "Specify-Number");
            return NBCmdExecution.invalidExecution();
        }

        String level = args[2];
        if (NBUtils.isInvalidNumber(level, s)) return NBCmdExecution.invalidExecution();

        Bank bank = BankRegistry.getBank(getPossibleBank(args, 3));
        if (!BankUtils.exist(bank, s)) return NBCmdExecution.invalidExecution();

        if (!BankUtils.hasLevel(bank, level)) {
            NBMessages.sendIdentifier(s, "Invalid-Bank-Level");
            return NBCmdExecution.invalidExecution();
        }

        return new NBCmdExecution() {
            @Override
            public void execute() {
                BankUtils.setLevel(bank, target, Integer.parseInt(level));
                NBMessages.sendIdentifier(s, "Set-Level-Message", "%player%$" + target.getName(), "%level%$" + level);
            }
        };
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 3)
            return NBArgs.getArgs(args, "1", "2", "3");

        if (args.length == 4)
            return NBArgs.getBanks(args);
        return null;
    }
}