package id.naturalsmp.naturalbank.placeholders.list;

import id.naturalsmp.naturalbank.bankSystem.BankRegistry;
import id.naturalsmp.naturalbank.bankSystem.BankUtils;
import id.naturalsmp.naturalbank.placeholders.BPPlaceholder;
import org.bukkit.entity.Player;

public class LevelPlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "level";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!BankUtils.exist(target)) return bankDoesNotExist;
        return String.valueOf(BankUtils.getCurrentLevel(BankRegistry.getBank(target), p));
    }
}