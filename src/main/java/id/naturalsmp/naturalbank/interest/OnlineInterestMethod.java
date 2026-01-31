package id.naturalsmp.naturalbank.interest;

import id.naturalsmp.naturalbank.NaturalBank;
import id.naturalsmp.naturalbank.bankSystem.Bank;
import id.naturalsmp.naturalbank.bankSystem.BankRegistry;
import id.naturalsmp.naturalbank.bankSystem.BankUtils;
import id.naturalsmp.naturalbank.economy.BPEconomy;
import id.naturalsmp.naturalbank.economy.TransactionType;
import id.naturalsmp.naturalbank.utils.BPLogger;
import id.naturalsmp.naturalbank.utils.BPUtils;
import id.naturalsmp.naturalbank.utils.texts.BPMessages;
import id.naturalsmp.naturalbank.values.ConfigValues;
import id.naturalsmp.naturalbank.values.MessageValues;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OnlineInterestMethod extends BPInterest.InterestMethod {

    public static final String ONLINE_INTEREST_PERMISSION = "NaturalBank.receive.interest";

    @Override
    public void giveInterest(OfflinePlayer offlinePlayer) {
        Player p = offlinePlayer.getPlayer();
        if (p == null) {
            BPLogger.Console.warn("Could not process interest for player \"" + offlinePlayer.getName() + "\". (The player resulted as null)");
            return;
        }
        if (!p.hasPermission(ONLINE_INTEREST_PERMISSION)) return;

        List<Bank> availableBanks = new ArrayList<>(); // List of available banks for the player.
        for (Bank bank : BankRegistry.getBanks().values())
            // If the bank gives interest even if not available, still add it.
            if (bank.isGiveInterestIfNotAvailable() || BankUtils.isAvailable(bank, p)) availableBanks.add(bank);
        if (availableBanks.isEmpty()) return;

        boolean isAfk = NaturalBank.INSTANCE().getAfkManager().isAFK(p);
        // If AFK system is enabled and AFK players do not receive interest, return.
        if (ConfigValues.isIgnoringAfkPlayers() && isAfk) return;

        BigDecimal interestAmount = BigDecimal.ZERO;
        boolean interestToVault = ConfigValues.isGivingInterestOnVaultBalance();

        for (Bank bank : availableBanks) {
            BPEconomy economy = bank.getBankEconomy();
            if (economy.getBankBalance(p).compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal interestMoney = getInterestMoney(bank, p, isAfk ? BankUtils.getAfkInterestRate(bank, p) : BankUtils.getOnlineInterestRate(bank, p));

            BigDecimal maxAmount = BankUtils.getMaxInterestAmount(bank, p); // Max interest amount.
            if (maxAmount.compareTo(BigDecimal.ZERO) > 0) interestMoney = interestMoney.min(maxAmount);

            BigDecimal added = interestMoney;
            if (!interestToVault) added = economy.addBankBalance(p, interestMoney, TransactionType.INTEREST);
            else NaturalBank.INSTANCE().getVaultEconomy().depositPlayer(p, interestMoney.doubleValue());

            interestAmount = interestAmount.add(added);
        }

        if (!MessageValues.isInterestBroadcastEnabled()) return;

        BigDecimal skipAmount = ConfigValues.getInterestMessageSkipAmount();
        if (skipAmount.compareTo(BigDecimal.ZERO) > 0 && skipAmount.compareTo(interestAmount) > 0) return;

        if (availableBanks.size() > 1) BPMessages.sendMessage(p, MessageValues.getMultiInterestMoney(), BPUtils.placeValues(p, interestAmount));
        else {
            if (BankUtils.isFull(availableBanks.getFirst(), p) && !ConfigValues.isGivingInterestOnVaultBalance()) {
                BPMessages.sendMessage(p, MessageValues.getInterestBankFull(), BPUtils.placeValues(p, interestAmount));
                return;
            }

            if (interestAmount.compareTo(BigDecimal.ZERO) <= 0) { // If interest earned is 0.
                BPMessages.sendMessage(p, MessageValues.getInterestNoMoney(), BPUtils.placeValues(p, interestAmount));
                return;
            }

            BPMessages.sendMessage(p, MessageValues.getInterestMoney(), BPUtils.placeValues(p, interestAmount));
        }
    }
}
