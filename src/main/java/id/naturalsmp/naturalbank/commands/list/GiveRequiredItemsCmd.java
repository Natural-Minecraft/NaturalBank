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
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class GiveRequiredItemsCmd extends NBCommand {

    public GiveRequiredItemsCmd(FileConfiguration commandsConfig, String commandID, String... aliases) {
        super(commandsConfig, commandID, aliases);
    }

    public GiveRequiredItemsCmd(FileConfiguration commandsConfig, String commandID) {
        super(commandsConfig, commandID);
    }

    @Override
    public List<String> defaultUsage() {
        return Collections.singletonList("%prefix% Usage: /bank giveRequiredItems <player> [bankName] [bankLevel] [itemName/all]");
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

        if (args.length <= 3) {
            NBMessages.sendIdentifier(s, "Specify-Number");
            return NBCmdExecution.invalidExecution();
        }

        String levelString = args[3];
        if (NBUtils.isInvalidNumber(levelString, s)) return NBCmdExecution.invalidExecution();

        int level = Integer.parseInt(levelString);
        if (!BankUtils.hasLevel(bank, level)) {
            NBMessages.sendIdentifier(s, "Invalid-Bank-Level");
            return NBCmdExecution.invalidExecution();
        }

        HashMap<String, Bank.RequiredItem> requiredItems = BankUtils.getRequiredItems(bank, level);
        Set<Bank.RequiredItem> givenItems = new HashSet<>(requiredItems.values());

        if (requiredItems.isEmpty()) {
            NBMessages.sendIdentifier(s, "No-Available-Items");
            return NBCmdExecution.invalidExecution();
        }

        if (args.length > 4) {
            String choose = args[4];
            if (!choose.equalsIgnoreCase("all")) {
                if (!requiredItems.containsKey(choose)) {
                    NBMessages.sendIdentifier(s, "Invalid-Required-Item");
                    return NBCmdExecution.invalidExecution();
                }

                givenItems.clear();
                givenItems.add(requiredItems.get(choose));
            }
        }

        return new NBCmdExecution() {
            @Override
            public void execute() {
                for (Bank.RequiredItem requiredItem : givenItems) target.getInventory().addItem(requiredItem.item);
            }
        };
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {

        if (args.length == 2)
            return NBArgs.getOnlinePlayers(args);

        if (args.length == 3)
            return NBArgs.getBanks(args);

        Bank bank = BankRegistry.getBank(args[2]);

        if (args.length == 4) {
            List<String> levelsWithItems = new ArrayList<>();
            for (String level : BankUtils.getLevels(bank))
                if (!BankUtils.getRequiredItems(bank, Integer.parseInt(level)).isEmpty()) levelsWithItems.add(level);

            return NBArgs.getArgs(args, levelsWithItems);
        }

        if (args.length == 5) {
            HashMap<String, Bank.RequiredItem> requiredItems = BankUtils.getRequiredItems(bank, Integer.parseInt(args[3]));
            if (requiredItems.isEmpty()) return null;

            List<String> choose = new ArrayList<>();
            choose.add("all");
            choose.addAll(requiredItems.keySet());

            return NBArgs.getArgs(args, choose);
        }

        return null;
    }
}
