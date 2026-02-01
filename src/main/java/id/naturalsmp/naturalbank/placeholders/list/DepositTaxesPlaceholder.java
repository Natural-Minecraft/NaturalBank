package id.naturalsmp.naturalbank.placeholders.list;

import id.naturalsmp.naturalbank.placeholders.NBPlaceholder;
import id.naturalsmp.naturalbank.utils.texts.NBFormatter;
import id.naturalsmp.naturalbank.values.ConfigValues;
import org.bukkit.entity.Player;

public class DepositTaxesPlaceholder extends NBPlaceholder {

    @Override
    public String getIdentifier() {
        return "deposit_taxes";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        return NBFormatter.styleBigDecimal(ConfigValues.getDepositTaxes());
    }
}