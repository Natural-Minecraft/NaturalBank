package id.naturalsmp.naturalbank.commands.list;

import id.naturalsmp.naturalbank.bankSystem.Bank;
import id.naturalsmp.naturalbank.bankSystem.BankRegistry;
import id.naturalsmp.naturalbank.bankSystem.BankUtils;
import id.naturalsmp.naturalbank.commands.BPCmdExecution;
import id.naturalsmp.naturalbank.commands.BPCommand;
import id.naturalsmp.naturalbank.utils.texts.BPArgs;
import id.naturalsmp.naturalbank.utils.texts.BPMessages;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class UpgradeCmd extends BPCommand {

    public UpgradeCmd(FileConfiguration commandsConfig, String commandID) {
        super(commandsConfig, commandID);
    }

    public UpgradeCmd(FileConfiguration commandsConfig, String commandID, String... aliases) {
        super(commandsConfig, commandID, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Collections.singletonList("%prefix% Usage: /bank upgrade [bankName]");
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

    @Override
    public BPCmdExecution onExecution(CommandSender s, String[] args) {
        Bank bank = BankRegistry.getBank(getPossibleBank(args, 1));
        if (!BankUtils.exist(bank, s)) return BPCmdExecution.invalidExecution();

        Player p = (Player) s;
        if (!BankUtils.isAvailable(bank, p)) {
            BPMessages.sendIdentifier(s, "Cannot-Access-Bank");
            return BPCmdExecution.invalidExecution();
        }

        return new BPCmdExecution() {
            @Override
            public void execute() {
                BankUtils.upgradeBank(bank, p);
            }
        };
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        Player p = (Player) s;

        if (args.length == 2)
            return BPArgs.getArgs(args, BankUtils.getAvailableBankNames(p));
        return null;
    }
}