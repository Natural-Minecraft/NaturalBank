package id.naturalsmp.naturalbank.placeholders.list;

import id.naturalsmp.naturalbank.NaturalBank;
import id.naturalsmp.naturalbank.placeholders.BPPlaceholder;
import id.naturalsmp.naturalbank.utils.texts.BPFormatter;
import id.naturalsmp.naturalbank.values.ConfigValues;
import org.bukkit.entity.Player;

public class InterestCooldownPlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "interest_cooldown";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        return ConfigValues.isInterestEnabled() ? BPFormatter.formatTime(NaturalBank.INSTANCE().getInterest().getInterestCooldownMillis()) : "Interest disabled.";
    }
}