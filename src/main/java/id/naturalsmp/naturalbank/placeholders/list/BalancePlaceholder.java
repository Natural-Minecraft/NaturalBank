package id.naturalsmp.naturalbank.placeholders.list;

import id.naturalsmp.naturalbank.bankSystem.BankUtils;
import id.naturalsmp.naturalbank.economy.BPEconomy;
import id.naturalsmp.naturalbank.placeholders.BPPlaceholder;
import org.bukkit.entity.Player;

public class BalancePlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "balance";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!BankUtils.exist(target)) return bankDoesNotExist;
        return getFormat(identifier, BPEconomy.get(target).getBankBalance(p));
    }
}