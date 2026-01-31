package id.naturalsmp.naturalbank.placeholders.list;

import id.naturalsmp.naturalbank.bankSystem.Bank;
import id.naturalsmp.naturalbank.bankSystem.BankRegistry;
import id.naturalsmp.naturalbank.bankSystem.BankUtils;
import id.naturalsmp.naturalbank.interest.BPInterest;
import id.naturalsmp.naturalbank.placeholders.BPPlaceholder;
import org.bukkit.entity.Player;

public class NextInterestPlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "next_interest";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!BankUtils.exist(target)) return bankDoesNotExist;
        Bank bank = BankRegistry.getBank(target);
        return getFormat(identifier, BPInterest.InterestMethod.getInterestMoney(bank, p, BankUtils.getOnlineInterestRate(bank, p)));
    }
}