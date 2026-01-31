package id.naturalsmp.naturalbank.placeholders.list;

import id.naturalsmp.naturalbank.placeholders.BPPlaceholder;
import id.naturalsmp.naturalbank.utils.texts.BPFormatter;
import id.naturalsmp.naturalbank.values.ConfigValues;
import org.bukkit.entity.Player;

public class WithdrawTaxesPlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "withdraw_taxes";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        return BPFormatter.styleBigDecimal(ConfigValues.getWithdrawTaxes());
    }
}