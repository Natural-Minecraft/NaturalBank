package id.naturalsmp.naturalbank.commands;

import id.naturalsmp.naturalbank.bankSystem.Bank;
import id.naturalsmp.naturalbank.bankSystem.BankListGui;
import id.naturalsmp.naturalbank.bankSystem.BankRegistry;
import id.naturalsmp.naturalbank.bankSystem.BankUtils;
import id.naturalsmp.naturalbank.economy.NBEconomy;
import id.naturalsmp.naturalbank.utils.NBUtils;
import id.naturalsmp.naturalbank.utils.texts.NBMessages;
import id.naturalsmp.naturalbank.values.ConfigValues;
import id.naturalsmp.naturalbank.values.MultipleBanksValues;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static id.naturalsmp.naturalbank.commands.NBCmdRegistry.commands;

public class MainCmd implements CommandExecutor, TabCompleter {

    public boolean onCommand(CommandSender s, Command command, String label, String[] args) {
        if (!ConfigValues.getWorldsBlacklist().isEmpty() && s instanceof Player p) {
            if (ConfigValues.getWorldsBlacklist().contains(p.getWorld().getName())
                    && !p.hasPermission("NaturalBank.worlds.blacklist.bypass")) {
                NBMessages.sendIdentifier(p, "Cannot-Use-Bank-Here");
                return true;
            }
        }

        if (args.length == 0) {
            if (!NBUtils.hasPermission(s, "NaturalBank.use"))
                return true;

            if (s instanceof Player p) {
                if (BankUtils.getAvailableBankNames((Player) s).isEmpty()) {
                    if (ConfigValues.isShowingHelpWhenNoBanksAvailable())
                        NBMessages.sendIdentifier(s, "Help-Message");
                    else
                        NBMessages.sendIdentifier(s, "No-Available-Banks");
                    return true;
                }
            } else {
                NBMessages.sendIdentifier(s, "Help-Message");
                return true;
            }

            if (ConfigValues.isGuiModuleEnabled()) {
                if (!MultipleBanksValues.enableMultipleBanksModule())
                    BankRegistry.getBank(ConfigValues.getMainGuiName()).getBankGui().openBankGui(p);
                else {
                    if (!MultipleBanksValues.isDirectlyOpenIf1IsAvailable())
                        new BankListGui().openBankGui(p);
                    else {
                        List<Bank> availableBanks = BankUtils.getAvailableBanks(p);
                        if (availableBanks.size() == 1)
                            availableBanks.getFirst().getBankGui().openBankGui(p);
                        else
                            new BankListGui().openBankGui(p);
                    }
                }
            } else {
                NBMessages.sendIdentifier(p, "Multiple-Personal-Bank",
                        NBUtils.placeValues(p, NBEconomy.getBankBalancesSum(p)));
                if (ConfigValues.isPersonalSoundEnabled())
                    NBUtils.playSound(ConfigValues.getPersonalSound(), p);
            }
            return true;
        }

        String identifier = args[0].toLowerCase();

        if (!commands.containsKey(identifier)) {
            NBMessages.sendIdentifier(s, "Unknown-Command");
            return true;
        }

        NBCommand cmd = commands.get(identifier);
        cmd.execute(s, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command command, String alias, String[] args) {
        if (args.length == 0)
            return null;

        String args0 = args[0].toLowerCase();

        if (args.length == 1) {
            List<String> cmds = new ArrayList<>();
            for (NBCommand cmd : commands.values())
                if (s.hasPermission(cmd.permission))
                    cmds.add(cmd.commandID);

            List<String> result = new ArrayList<>();
            for (String arg : cmds)
                if (arg.toLowerCase().startsWith(args0))
                    result.add(arg);
            return result;
        }

        NBCommand cmd = commands.get(args0);
        if (cmd == null || !s.hasPermission(cmd.permission) || (cmd.playerOnly() && !(s instanceof Player)))
            return null;

        return cmd.tabCompletion(s, args);
    }
}