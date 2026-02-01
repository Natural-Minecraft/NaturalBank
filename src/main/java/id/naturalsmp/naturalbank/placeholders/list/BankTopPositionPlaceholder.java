package id.naturalsmp.naturalbank.placeholders.list;

import id.naturalsmp.naturalbank.bankTop.NBBankTop;
import id.naturalsmp.naturalbank.placeholders.NBPlaceholder;
import id.naturalsmp.naturalbank.values.ConfigValues;
import org.bukkit.entity.Player;

public class BankTopPositionPlaceholder extends NBPlaceholder {

    @Override
    public String getIdentifier() {
        return "banktop_position";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!ConfigValues.isBankTopEnabled())
            return bankTopNotEnabled;
        return NBBankTop.getPlayerBankTopPosition(p) + "";
    }
}