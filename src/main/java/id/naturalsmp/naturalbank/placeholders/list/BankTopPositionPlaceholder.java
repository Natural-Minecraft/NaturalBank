package id.naturalsmp.naturalbank.placeholders.list;

import id.naturalsmp.naturalbank.bankTop.BPBankTop;
import id.naturalsmp.naturalbank.placeholders.BPPlaceholder;
import id.naturalsmp.naturalbank.values.ConfigValues;
import org.bukkit.entity.Player;

public class BankTopPositionPlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "banktop_position";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!ConfigValues.isBankTopEnabled()) return bankTopNotEnabled;
        return BPBankTop.getPlayerBankTopPosition(p) + "";
    }
}