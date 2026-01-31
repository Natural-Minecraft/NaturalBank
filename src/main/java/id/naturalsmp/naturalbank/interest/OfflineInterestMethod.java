package id.naturalsmp.naturalbank.interest;

import id.naturalsmp.naturalbank.NaturalBank;
import id.naturalsmp.naturalbank.bankSystem.Bank;
import id.naturalsmp.naturalbank.bankSystem.BankRegistry;
import id.naturalsmp.naturalbank.bankSystem.BankUtils;
import id.naturalsmp.naturalbank.economy.BPEconomy;
import id.naturalsmp.naturalbank.economy.TransactionType;
import id.naturalsmp.naturalbank.sql.BPSQL;
import id.naturalsmp.naturalbank.utils.BPUtils;
import id.naturalsmp.naturalbank.values.ConfigValues;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OfflineInterestMethod extends BPInterest.InterestMethod {

    @Override
    public void giveInterest(OfflinePlayer p) {
        if (BPInterest.InterestMethod.offlineTimeExpired(p)) return;

        String offlinePermission = ConfigValues.getInterestOfflinePermission();
        if (!offlinePermission.isEmpty() && !BPUtils.hasOfflinePermission(p, offlinePermission)) return;

        List<Bank> availableBanks = new ArrayList<>(); // List of available banks for the player.
        for (Bank bank : BankRegistry.getBanks().values())
            // If the bank gives interest even if not available, still add it.
            if (bank.isGiveInterestIfNotAvailable() || BankUtils.isAvailable(bank, p)) availableBanks.add(bank);

        if (availableBanks.isEmpty()) return;

        boolean interestToVault = ConfigValues.isGivingInterestOnVaultBalance();
        for (Bank bank : availableBanks) {
            BPEconomy economy = bank.getBankEconomy();
            if (economy.getBankBalance(p).compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal interestMoney = getInterestMoney(bank, p,BankUtils.getOfflineInterestRate(bank, p));

            BigDecimal maxAmount = BankUtils.getMaxInterestAmount(bank, p);
            if (maxAmount.compareTo(BigDecimal.ZERO) > 0) interestMoney = interestMoney.min(maxAmount);

            BigDecimal added = interestMoney;
            if (!interestToVault) added = economy.addBankBalance(p, interestMoney, TransactionType.INTEREST);
            else NaturalBank.INSTANCE().getVaultEconomy().depositPlayer(p, interestMoney.doubleValue());

            // Since interest needs to be updated only when the player is offline to notify it when joining, directly updated it from the database.
            // This method is already called asynchronously.
            BPSQL.setInterest(p, bank.getIdentifier(), BPSQL.getInterest(p, bank.getIdentifier()).add(added));
        }
    }
}
