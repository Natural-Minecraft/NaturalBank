package id.naturalsmp.naturalbank.placeholders.list;

import id.naturalsmp.naturalbank.bankSystem.BankRegistry;
import id.naturalsmp.naturalbank.bankSystem.BankUtils;
import id.naturalsmp.naturalbank.placeholders.BPPlaceholder;
import id.naturalsmp.naturalbank.values.ConfigValues;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class CapacityPlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "capacity";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!BankUtils.exist(target)) return bankDoesNotExist;

        BigDecimal capacity = BankUtils.getCapacity(BankRegistry.getBank(target), p);
        if (capacity.compareTo(BigDecimal.ZERO) <= 0) return ConfigValues.getInfiniteCapacityText();
        return getFormat(identifier, capacity);
    }
}