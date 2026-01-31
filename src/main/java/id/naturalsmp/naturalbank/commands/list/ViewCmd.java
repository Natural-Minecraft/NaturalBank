package id.naturalsmp.naturalbank.commands.list;

import id.naturalsmp.naturalbank.NaturalBank;
import id.naturalsmp.naturalbank.bankSystem.Bank;
import id.naturalsmp.naturalbank.bankSystem.BankRegistry;
import id.naturalsmp.naturalbank.bankSystem.BankUtils;
import id.naturalsmp.naturalbank.commands.BPCmdExecution;
import id.naturalsmp.naturalbank.commands.BPCommand;
import id.naturalsmp.naturalbank.economy.BPEconomy;
import id.naturalsmp.naturalbank.utils.BPUtils;
import id.naturalsmp.naturalbank.utils.texts.BPArgs;
import id.naturalsmp.naturalbank.utils.texts.BPMessages;
import id.naturalsmp.naturalbank.values.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ViewCmd extends BPCommand {

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
    public BPCmdExecution onExecution(CommandSender s, String[] args) {

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (!target.hasPlayedBefore()) {
            BPMessages.sendIdentifier(s, "Invalid-Player");
            return BPCmdExecution.invalidExecution();
        }

        return new BPCmdExecution() {
            @Override
            public void execute() {
                Bukkit.getScheduler().runTaskAsynchronously(NaturalBank.INSTANCE(), () -> {
                    // Do that on the cmd execution because to check offline
                    // permissions we need to run it asynchronously.
                    List<Bank> banks = new ArrayList<>();
                    if (args.length == 2) {
                        banks.addAll(BankUtils.getAvailableBanks(target));
                        if (banks.isEmpty()) {
                            BPMessages.sendIdentifier(s, "No-Available-Banks-Others", "%player%$" + target.getName());
                            return;
                        }
                    } else {
                        Bank bank = BankRegistry.getBank(args[2]);
                        if (!BankUtils.exist(bank, s)) return;

                        if (!BankUtils.isAvailable(bank, target)) {
                            BPMessages.sendIdentifier(s, "Cannot-Access-Bank-Others", "%player%$" + target.getName());
                            return;
                        }
                        banks.add(bank);
                    }

                    if (banks.size() > 1)
                        BPMessages.sendIdentifier(
                                s,
                                "Multiple-Bank-Others",
                                BPUtils.placeValues(target, BPEconomy.getBankBalancesSum(target))
                        );
                    else {
                        Bank bank = banks.getFirst();
                        BPMessages.sendIdentifier(
                                s,
                                "Bank-Others",
                                BPUtils.placeValues(target, bank.getBankEconomy().getBankBalance(target), BankUtils.getCurrentLevel(bank, target))
                        );
                    }

                    if (s instanceof Player p && ConfigValues.isViewSoundEnabled()) BPUtils.playSound(ConfigValues.getPersonalSound(), p);
                });
            }
        };
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 3)
            return BPArgs.getArgs(args, BankUtils.getAvailableBankNames(Bukkit.getOfflinePlayer(args[1])));
        return null;
    }
}