package id.naturalsmp.naturalbank.commands.list;

import id.naturalsmp.naturalbank.bankSystem.Bank;
import id.naturalsmp.naturalbank.bankSystem.BankRegistry;
import id.naturalsmp.naturalbank.bankSystem.BankUtils;
import id.naturalsmp.naturalbank.commands.NBCmdExecution;
import id.naturalsmp.naturalbank.commands.NBCommand;
import id.naturalsmp.naturalbank.economy.NBEconomy;
import id.naturalsmp.naturalbank.utils.NBUtils;
import id.naturalsmp.naturalbank.utils.texts.NBArgs;
import id.naturalsmp.naturalbank.utils.texts.NBMessages;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class WithdrawCmd extends NBCommand {

    public WithdrawCmd(FileConfiguration commandsConfig, String commandID) {
        super(commandsConfig, commandID);
    }

    public WithdrawCmd(FileConfiguration commandsConfig, String commandID, String... aliases) {
        super(commandsConfig, commandID, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Collections.singletonList("%prefix% Usage: /bank deposit [amount/half/all] [bankName]");
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
        return true;
    }

    @Override
    public boolean skipUsage() {
        return false;
    }

    @Override
    public NBCmdExecution onExecution(CommandSender s, String[] args) {
        String amount = args[1].toLowerCase();
        if (NBUtils.isInvalidNumber(amount, s)) return NBCmdExecution.invalidExecution();

        Bank bank = BankRegistry.getBank(getPossibleBank(args, 2));
        if (!BankUtils.exist(bank, s)) return NBCmdExecution.invalidExecution();

        Player p = (Player) s;
        if (!BankUtils.isAvailable(bank, p)) {
            NBMessages.sendIdentifier(s, "Cannot-Access-Bank");
            return NBCmdExecution.invalidExecution();
        }

        return new NBCmdExecution() {
            @Override
            public void execute() {
                BigDecimal result, h = BigDecimal.valueOf(100);
                NBEconomy economy = bank.getBankEconomy();
                if (!amount.contains("%")) result = new BigDecimal(amount);
                else {
                    BigDecimal percentage = new BigDecimal(amount.replace("%", ""));

                    // If the percentage is <= 0 or > 100 return.
                    if (percentage.compareTo(BigDecimal.ZERO) <= 0 || percentage.compareTo(h) > 0) {
                        NBMessages.sendIdentifier(p, "Invalid-Number");
                        return;
                    }

                    // Start from the player bank balance, then modify it if it's not 100%.
                    result = economy.getBankBalance(p);

                    // Do that only if the % is < 100% to avoid odd numbers bug.
                    if (percentage.compareTo(h) < 0) result = result.divide(h).multiply(percentage);
                }
                economy.withdraw(p, result);
            }
        };
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 2)
            return NBArgs.getArgs(args, "1", "2", "3", "10%", "20%");

        if (args.length == 3)
            return NBArgs.getArgs(args, BankUtils.getAvailableBankNames((Player) s));

        return null;
    }
}