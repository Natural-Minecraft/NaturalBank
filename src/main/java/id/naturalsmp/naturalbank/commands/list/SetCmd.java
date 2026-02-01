package id.naturalsmp.naturalbank.commands.list;

import id.naturalsmp.naturalbank.bankSystem.BankUtils;
import id.naturalsmp.naturalbank.commands.NBCmdExecution;
import id.naturalsmp.naturalbank.commands.NBCommand;
import id.naturalsmp.naturalbank.economy.NBEconomy;
import id.naturalsmp.naturalbank.utils.NBUtils;
import id.naturalsmp.naturalbank.utils.texts.NBArgs;
import id.naturalsmp.naturalbank.utils.texts.NBMessages;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class SetCmd extends NBCommand {

    public SetCmd(FileConfiguration commandsConfig, String commandID) {
        super(commandsConfig, commandID);
    }

    public SetCmd(FileConfiguration commandsConfig, String commandID, String... aliases) {
        super(commandsConfig, commandID, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Collections.singletonList("%prefix% Usage: /bank set [player] [amount] [bankName]");
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
        OfflinePlayer p = Bukkit.getOfflinePlayer(args[1]);
        if (!p.hasPlayedBefore()) {
            NBMessages.sendIdentifier(s, "Invalid-Player");
            return NBCmdExecution.invalidExecution();
        }

        if (args.length == 2) {
            NBMessages.sendIdentifier(s, "Specify-Number");
            return NBCmdExecution.invalidExecution();
        }

        String amount = args[2];
        if (NBUtils.isInvalidNumber(amount, s)) return NBCmdExecution.invalidExecution();

        String bankName = getPossibleBank(args, 3);
        if (!BankUtils.exist(bankName, s)) return NBCmdExecution.invalidExecution();

        return new NBCmdExecution() {
            @Override
            public void execute() {
                BigDecimal set = NBEconomy.get(bankName).setBankBalance(p, new BigDecimal(amount));
                if (!isSilent(args)) NBMessages.sendIdentifier(s, "Set-Message", NBUtils.placeValues(p, set));
            }
        };
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 3)
            return NBArgs.getArgs(args, "1", "2", "3");

        if (args.length == 4)
            return NBArgs.getBanks(args);

        if (args.length == 5)
            return NBArgs.getArgs(args, "silent=true", "silent=false");
        return null;
    }
}