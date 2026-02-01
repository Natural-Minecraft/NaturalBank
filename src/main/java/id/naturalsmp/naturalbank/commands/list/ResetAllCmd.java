package id.naturalsmp.naturalbank.commands.list;

import id.naturalsmp.naturalbank.NaturalBank;
import id.naturalsmp.naturalbank.commands.NBCmdExecution;
import id.naturalsmp.naturalbank.commands.NBCommand;
import id.naturalsmp.naturalbank.economy.NBEconomy;
import id.naturalsmp.naturalbank.economy.EconomyUtils;
import id.naturalsmp.naturalbank.utils.texts.NBArgs;
import id.naturalsmp.naturalbank.utils.texts.NBMessages;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ResetAllCmd extends NBCommand {

    private final Economy vaultEconomy;

    public ResetAllCmd(FileConfiguration commandsConfig, String commandID) {
        super(commandsConfig, commandID);
        vaultEconomy = NaturalBank.INSTANCE().getVaultEconomy();
    }

    public ResetAllCmd(FileConfiguration commandsConfig, String commandID, String... aliases) {
        super(commandsConfig, commandID, aliases);
        vaultEconomy = NaturalBank.INSTANCE().getVaultEconomy();
    }

    @Override
    public List<String> defaultUsage() {
        return Arrays.asList(
                "%prefix% Usage: /bank resetall [mode]",
                "",
                "Specify a mode between <aqua>\"delete\"</aqua> and <aqua>\"maintain\"</aqua>.",
                "- delete: Will remove all players money and they will be lost",
                "- maintain: Will move all players money from their banks to their vault balance."
        );
    }

    @Override
    public int defaultConfirmCooldown() {
        return 5;
    }

    @Override
    public List<String> defaultConfirmMessage() {
        return Collections.singletonList(
                "%prefix% <red>Warning, this command is going to reset everyone's bank balance," +
                        " based on the mode you choose, this action is not reversible, type the" +
                        " command again within 5 seconds to confirm."
        );
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
        String mode = args[1];
        if (!mode.equalsIgnoreCase("delete") && !mode.equalsIgnoreCase("maintain")) {
            NBMessages.sendMessage(s, "%prefix% Invalid reset mode! Choose one between <aqua>\"delete\"</aqua> and <aqua>\"maintain\"</aqua>.");
            return NBCmdExecution.invalidExecution();
        }

        return new NBCmdExecution() {
            @Override
            public void execute() {
                NBMessages.sendMessage(s, "%prefix% Successfully reset all players money! <dark_gray>(</dark_gray>With <white>" + mode + "</white> mode<dark_gray>)");
                resetAll(Arrays.asList(Bukkit.getOfflinePlayers()), mode);
            }
        };
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        if (args.length == 2)
            return NBArgs.getArgs(args, "delete", "maintain");
        return null;
    }

    private void resetAll(List<OfflinePlayer> offlinePlayers, String mode) {
        List<OfflinePlayer> copy = new ArrayList<>(offlinePlayers);
        List<NBEconomy> economies = NBEconomy.list();

        for (int i = 0; i < 50; i++) {
            if (copy.isEmpty()) {
                EconomyUtils.saveEveryone(true);
                return;
            }
            OfflinePlayer p = copy.removeFirst();

            if (mode.equalsIgnoreCase("maintain")) vaultEconomy.depositPlayer(p, NBEconomy.getBankBalancesSum(p).doubleValue());

            for (NBEconomy economy : economies) {
                economy.setBankBalance(p, BigDecimal.ZERO);
                economy.setDebt(p, BigDecimal.ZERO);
                economy.setBankLevel(p, 1);
            }
        }

        Bukkit.getScheduler().runTaskLater(NaturalBank.INSTANCE(), () -> resetAll(copy, mode), 1);
    }
}