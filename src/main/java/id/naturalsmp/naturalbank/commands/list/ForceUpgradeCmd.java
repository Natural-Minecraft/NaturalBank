package id.naturalsmp.naturalbank.commands.list;

import id.naturalsmp.naturalbank.bankSystem.Bank;
import id.naturalsmp.naturalbank.bankSystem.BankRegistry;
import id.naturalsmp.naturalbank.bankSystem.BankUtils;
import id.naturalsmp.naturalbank.commands.NBCmdExecution;
import id.naturalsmp.naturalbank.commands.NBCommand;
import id.naturalsmp.naturalbank.utils.texts.NBArgs;
import id.naturalsmp.naturalbank.utils.texts.NBMessages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ForceUpgradeCmd extends NBCommand {

    public ForceUpgradeCmd(FileConfiguration commandsConfig, String commandID) {
        super(commandsConfig, commandID);
    }

    public ForceUpgradeCmd(FileConfiguration commandsConfig, String commandID, String... aliases) {
        super(commandsConfig, commandID, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Collections.singletonList("%prefix% Usage: /bank forceOpen [player] [bankName]");
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
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            NBMessages.sendIdentifier(s, "Invalid-Player");
            return NBCmdExecution.invalidExecution();
        }

        Bank bank = BankRegistry.getBank(getPossibleBank(args, 2));
        if (!BankUtils.exist(bank, s)) return NBCmdExecution.invalidExecution();

        if (!BankUtils.isAvailable(bank, target)) {
            NBMessages.sendIdentifier(s, "Cannot-Access-Bank-Others", "%player%$" + target.getName());
            return NBCmdExecution.invalidExecution();
        }

        return new NBCmdExecution() {
            @Override
            public void execute() {
                BankUtils.upgradeBank(bank, target);
                if (!isSilent(args)) NBMessages.sendIdentifier(s, "Force-Upgrade", "%player%$" + target.getName(), "%bank%$" + bank.getIdentifier());
            }
        };
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 3)
            return NBArgs.getArgs(args, BankUtils.getAvailableBankNames(Bukkit.getPlayer(args[1])));

        if (args.length == 4)
            return NBArgs.getArgs(args, "silent=true", "silent=false");
        return null;
    }
}