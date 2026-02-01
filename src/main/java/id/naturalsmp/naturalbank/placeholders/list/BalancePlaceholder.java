package id.naturalsmp.naturalbank.placeholders.list;

import id.naturalsmp.naturalbank.bankSystem.BankUtils;
import id.naturalsmp.naturalbank.economy.NBEconomy;
import id.naturalsmp.naturalbank.placeholders.NBPlaceholder;
import org.bukkit.entity.Player;

public class BalancePlaceholder extends NBPlaceholder {

    @Override
    public String getIdentifier() {
        return "balance";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!BankUtils.exist(target))
            return bankDoesNotExist;
        return getFormat(identifier, NBEconomy.get(target).getBankBalance(p));
    }
}