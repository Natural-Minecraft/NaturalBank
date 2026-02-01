package id.naturalsmp.naturalbank.commands.list;

import id.naturalsmp.naturalbank.NaturalBank;
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
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ViewCmd extends NBCommand {

    public ViewCmd(FileConfiguration commandsConfig, String commandID) {
        super(commandsConfig, commandID);
    }

    public ViewCmd(FileConfiguration commandsConfig, String commandID, String... aliases) {
        super(commandsConfig, commandID, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Collections.singletonList("%prefix% Usage: /bank view [player] [bankName]");
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

        return new NBCmdExecution() {
            @Override
            public void execute() {
                Bukkit.getScheduler().runTaskAsynchronously(NaturalBank.INSTANCE(), () -> {
                    // Do that on the cmd execution because to check offline
                    // permissions we need to run it asynchronously.
                    List<Bank> banks = new ArrayList<>();
                    if (args.length == 2) {
                        banks.addAll(BankUtils.getAvailableBanks(target));
                        if (banks.isEmpty()) {
                            NBMessages.sendIdentifier(s, "No-Available-Banks-Others", "%player%$" + target.getName());
                            return;
                        }
                    } else {
                        Bank bank = BankRegistry.getBank(args[2]);
                        if (!BankUtils.exist(bank, s)) return;

                        if (!BankUtils.isAvailable(bank, target)) {
                            NBMessages.sendIdentifier(s, "Cannot-Access-Bank-Others", "%player%$" + target.getName());
                            return;
                        }
                        banks.add(bank);
                    }

                    if (banks.size() > 1)
                        NBMessages.sendIdentifier(
                                s,
                                "Multiple-Bank-Others",
                                NBUtils.placeValues(target, NBEconomy.getBankBalancesSum(target))
                        );
                    else {
                        Bank bank = banks.getFirst();
                        NBMessages.sendIdentifier(
                                s,
                                "Bank-Others",
                                NBUtils.placeValues(target, bank.getBankEconomy().getBankBalance(target), BankUtils.getCurrentLevel(bank, target))
                        );
                    }

                    if (s instanceof Player p && ConfigValues.isViewSoundEnabled()) NBUtils.playSound(ConfigValues.getPersonalSound(), p);
                });
            }
        };
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 3)
            return NBArgs.getArgs(args, BankUtils.getAvailableBankNames(Bukkit.getOfflinePlayer(args[1])));
        return null;
    }
}