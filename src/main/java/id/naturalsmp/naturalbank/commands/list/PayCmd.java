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
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PayCmd extends NBCommand {

    public PayCmd(FileConfiguration commandsConfig, String commandID) {
        super(commandsConfig, commandID);
    }

    public PayCmd(FileConfiguration commandsConfig, String commandID, String... aliases) {
        super(commandsConfig, commandID, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Arrays.asList(
                "%prefix% Usage: /bank pay [player] [amount] [fromBankName] [toBankName]",
                "",
                "- fromBankName: The bank from where to take the money.",
                "- toBankName: The bank where to send the money."
        );
    }

    @Override
    public int defaultConfirmCooldown() {
        return 5;
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
        return Collections.singletonList("%prefix% <green>Type again within 5 seconds to confirm your payment.");
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
        Player sender = (Player) s, target = Bukkit.getPlayerExact(args[1]);
        if (target == null || target.equals(s)) {
            NBMessages.sendIdentifier(s, "Invalid-Player");
            return NBCmdExecution.invalidExecution();
        }

        if (args.length == 2) {
            NBMessages.sendIdentifier(s, "Specify-Number");
            return NBCmdExecution.invalidExecution();
        }

        String num = args[2];
        if (NBUtils.isInvalidNumber(num, s)) return NBCmdExecution.invalidExecution();

        Bank fromBank = BankRegistry.getBank(getPossibleBank(args, 3));

        if (!BankUtils.exist(fromBank, s)) return NBCmdExecution.invalidExecution();
        if (!BankUtils.isAvailable(fromBank, sender)) {
            NBMessages.sendIdentifier(s, "Cannot-Access-Bank");
            return NBCmdExecution.invalidExecution();
        }

        Bank toBank = BankRegistry.getBank(getPossibleBank(args, 4));

        if (!BankUtils.exist(toBank, s)) return NBCmdExecution.invalidExecution();
        if (!BankUtils.isAvailable(toBank, target)) {
            NBMessages.sendIdentifier(s, "Cannot-Access-Bank-Others", "%player%$" + target.getName());
            return NBCmdExecution.invalidExecution();
        }

        return new NBCmdExecution() {
            @Override
            public void execute() {
                fromBank.getBankEconomy().pay(sender, target, new BigDecimal(num), toBank);
            }
        };
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 3)
            return NBArgs.getArgs(args, "1", "2", "3");

        if (args.length == 4)
            return NBArgs.getArgs(args, BankUtils.getAvailableBankNames((Player) s));

        if (args.length == 5)
            return NBArgs.getArgs(args, BankUtils.getAvailableBankNames(Bukkit.getPlayerExact(args[1])));
        return null;
    }
}