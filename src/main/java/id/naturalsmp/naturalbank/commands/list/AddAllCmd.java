package id.naturalsmp.naturalbank.commands.list;

import id.naturalsmp.naturalbank.NaturalBank;
import id.naturalsmp.naturalbank.bankSystem.BankUtils;
import id.naturalsmp.naturalbank.commands.NBCmdExecution;
import id.naturalsmp.naturalbank.commands.NBCommand;
import id.naturalsmp.naturalbank.economy.NBEconomy;
import id.naturalsmp.naturalbank.utils.NBUtils;
import id.naturalsmp.naturalbank.utils.texts.NBArgs;
import id.naturalsmp.naturalbank.utils.texts.NBMessages;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AddAllCmd extends NBCommand {

    public AddAllCmd(FileConfiguration commandsConfig, String commandID, String... aliases) {
        super(commandsConfig, commandID, aliases);
    }

    public AddAllCmd(FileConfiguration commandsConfig, String commandID) {
        super(commandsConfig, commandID);
    }

    @Override
    public List<String> defaultUsage() {
        return Collections.singletonList("%prefix% Usage: /bank addall [amount] [bankName]");
    }

    @Override
    public int defaultConfirmCooldown() {
        return 5;
    }

    @Override
    public List<String> defaultConfirmMessage() {
        return Collections.singletonList(
                "%prefix% Type the command again within 5 seconds to add to every online player (%server_online% players) the specified amount.");
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
        String amount = args[1];
        if (NBUtils.isInvalidNumber(amount, s))
            return NBCmdExecution.invalidExecution();

        String bankName = getPossibleBank(args, 2);
        if (!BankUtils.exist(bankName, s))
            return NBCmdExecution.invalidExecution();

        return new NBCmdExecution() {
            @Override
            public void execute() {
                NBMessages.sendMessage(s,
                        "%prefix% Successfully added <white>" + amount + "</white> money to all online players!");
                addAll(Bukkit.getOnlinePlayers(), new BigDecimal(amount), NBEconomy.get(bankName));
            }
        };
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 2)
            return NBArgs.getArgs(args, "1", "2", "3");

        if (args.length == 3)
            return NBArgs.getBanks(args);
        return null;
    }

    /**
     * Method to add to each online player the selected amount, split
     * the task every 50 players to avoid freezing in bigger servers.
     *
     * @param onlinePlayers The initial online players list.
     * @param amount        The selected amount to add.
     * @param economy       The bank economy where to add the money.
     */
    private void addAll(Collection<? extends Player> onlinePlayers, BigDecimal amount, NBEconomy economy) {
        List<Player> copy = new ArrayList<>(onlinePlayers);

        for (int i = 0; i < 50; i++) {
            if (copy.isEmpty())
                return;
            economy.addBankBalance(copy.removeFirst(), amount);
        }

        Bukkit.getScheduler().runTaskLater(NaturalBank.INSTANCE(), () -> addAll(copy, amount, economy), 1);
    }
}