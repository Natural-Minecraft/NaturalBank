package id.naturalsmp.naturalbank.placeholders.list;

import id.naturalsmp.naturalbank.NaturalBank;
import id.naturalsmp.naturalbank.placeholders.BPPlaceholder;
import id.naturalsmp.naturalbank.values.ConfigValues;
import org.bukkit.entity.Player;

public class InterestCooldownMillisPlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "interest_cooldown_millis";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        return ConfigValues.isInterestEnabled() ? String.valueOf(NaturalBank.INSTANCE().getInterest().getInterestCooldownMillis()) : "Interest disabled.";
    }
}