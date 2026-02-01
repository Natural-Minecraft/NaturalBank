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
import id.naturalsmp.naturalbank.values.ConfigValues;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BalanceCmd extends NBCommand {

    public BalanceCmd(FileConfiguration commandsConfig, String commandID) {
        super(commandsConfig, commandID);
    }

    public BalanceCmd(FileConfiguration commandsConfig, String commandID, String... aliases) {
        super(commandsConfig, commandID, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Collections.singletonList("%prefix% Usage: /bank balance [bankName]");
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
        return true;
    }

    public NBCmdExecution onExecution(CommandSender s, String[] args) {
        Player p = (Player) s;

        List<Bank> banks = new ArrayList<>();
        if (args.length == 1) {
            banks.addAll(BankUtils.getAvailableBanks(p));
            if (banks.isEmpty()) {
                NBMessages.sendIdentifier(p, "No-Available-Banks");
                return NBCmdExecution.invalidExecution();
            }

        } else {
            Bank bank = BankRegistry.getBank(args[1]);
            if (!BankUtils.exist(bank, s)) return NBCmdExecution.invalidExecution();

            if (!BankUtils.isAvailable(bank, p)) {
                NBMessages.sendIdentifier(s, "Cannot-Access-Bank");
                return NBCmdExecution.invalidExecution();
            }
            banks.add(bank);
        }

        return new NBCmdExecution() {
            @Override
            public void execute() {
                if (banks.size() > 1)
                    NBMessages.sendIdentifier(
                            p,
                            "Multiple-Personal-Bank",
                            NBUtils.placeValues(p, NBEconomy.getBankBalancesSum(p))
                    );
                else {
                    Bank bank = banks.getFirst();
                    NBMessages.sendIdentifier(
                            p,
                            "Personal-Bank",
                            NBUtils.placeValues(p, bank.getBankEconomy().getBankBalance(p), BankUtils.getCurrentLevel(bank, p))
                    );
                }

                if (ConfigValues.isViewSoundEnabled()) NBUtils.playSound(ConfigValues.getPersonalSound(), p);
            }
        };
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 2) return NBArgs.getArgs(args, BankUtils.getAvailableBankNames((Player) s));
        return null;
    }
}