package id.naturalsmp.naturalbank.commands.list;

import id.naturalsmp.naturalbank.bankSystem.BankRegistry;
import id.naturalsmp.naturalbank.bankSystem.BankUtils;
import id.naturalsmp.naturalbank.commands.NBCmdExecution;
import id.naturalsmp.naturalbank.commands.NBCommand;
import id.naturalsmp.naturalbank.utils.texts.NBArgs;
import id.naturalsmp.naturalbank.utils.texts.NBMessages;
import id.naturalsmp.naturalbank.values.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ForceOpenCmd extends NBCommand {

    public ForceOpenCmd(FileConfiguration commandsConfig, String commandID) {
        super(commandsConfig, commandID);
    }

    public ForceOpenCmd(FileConfiguration commandsConfig, String commandID, String... aliases) {
        super(commandsConfig, commandID, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Collections.singletonList("%prefix% Usage: /bank forceopen [player] [bankName]");
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
        if (!ConfigValues.isGuiModuleEnabled()) {
            NBMessages.sendIdentifier(s, "Gui-Module-Disabled");
            return NBCmdExecution.invalidExecution();
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            NBMessages.sendIdentifier(s, "Invalid-Player");
            return NBCmdExecution.invalidExecution();
        }

        String bankName = getPossibleBank(args, 2);
        if (!BankUtils.exist(bankName, s)) return NBCmdExecution.invalidExecution();

        return new NBCmdExecution() {
            @Override
            public void execute() {
                BankRegistry.getBank(bankName).getBankGui().openBankGui(target, true);
                if (!isSilent(args)) NBMessages.sendIdentifier(s, "Force-Open", "%player%$" + target.getName(), "%bank%$" + bankName);
            }
        };
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 3)
            return NBArgs.getBanks(args);

        if (args.length == 4)
            return NBArgs.getArgs(args, "silent=true", "silent=false");
        return null;
    }
}