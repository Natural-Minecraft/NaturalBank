package id.naturalsmp.naturalbank.debug;

import id.naturalsmp.naturalbank.NaturalBank;
import id.naturalsmp.naturalbank.bankTop.BankTopPlayer;
import id.naturalsmp.naturalbank.economy.NBEconomy;
import id.naturalsmp.naturalbank.utils.texts.NBMessages;
import id.naturalsmp.naturalbank.values.ConfigValues;
import org.bukkit.Bukkit;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Debug {

    public static void debugBankTop(Object debugReceiver) {
        NBMessages.sendMessage(debugReceiver, "Simulating a banktop update:");
        NBMessages.sendMessage(debugReceiver, " ");

        Bukkit.getScheduler().runTaskAsynchronously(NaturalBank.INSTANCE(), () -> {
            HashMap<String, BigDecimal> balances = NBEconomy.getAllEconomiesBankBalances();
            NBMessages.sendMessage(debugReceiver, "|  All player balances: " + balances.size());

            List<BankTopPlayer> players = new ArrayList<>();

            NBMessages.sendMessage(debugReceiver, "|  Players with 0 balance skipped.");
            for (String name : balances.keySet()) {
                BigDecimal balance = balances.get(name);
                if (balance.compareTo(BigDecimal.ZERO) <= 0) continue;

                BankTopPlayer bankTopPlayer = new BankTopPlayer();
                bankTopPlayer.setName(name);
                bankTopPlayer.setBalance(balance);

                players.add(bankTopPlayer);

                NBMessages.sendMessage(debugReceiver, "|    Player \"" + name + "\" loaded with \"" + balance + "\" total balance.");
            }

            NBMessages.sendMessage(debugReceiver, "|  BankTopPlayer instances loaded: " + players.size());
            NBMessages.sendMessage(debugReceiver, " ");
            NBMessages.sendMessage(debugReceiver, "|  Sorting the first " + ConfigValues.getBankTopSize() + " players with the highest balance.");
            for (int i = 1; i <= ConfigValues.getBankTopSize(); i++) {
                BankTopPlayer highestPlayerBal = players.getFirst();

                for (BankTopPlayer player : players)
                    if (player.getBalance().compareTo(highestPlayerBal.getBalance()) > 0)
                        highestPlayerBal = player;

                players.remove(highestPlayerBal);
                NBMessages.sendMessage(debugReceiver, "|    BankTop Position #" + i + ": " + highestPlayerBal.getName() + " with " + highestPlayerBal.getBalance() + " balance.");
            }

            NBMessages.sendMessage(debugReceiver, " ");
            NBMessages.sendMessage(debugReceiver, "BankTop simulation ended.");
        });
    }
}
