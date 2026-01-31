package id.naturalsmp.naturalbank.placeholders.list;

import id.naturalsmp.naturalbank.bankTop.BPBankTop;
import id.naturalsmp.naturalbank.placeholders.BPPlaceholder;
import id.naturalsmp.naturalbank.values.ConfigValues;
import org.bukkit.entity.Player;

public class BankTopNamePlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "banktop_name_";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!ConfigValues.isBankTopEnabled()) return bankTopNotEnabled;

        String number = identifier.replace("banktop_name_", "");
        int position;
        try {
            position = Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return "Invalid banktop number!";
        }

        if (position > ConfigValues.getBankTopSize())
            return "The banktop limit is " + ConfigValues.getBankTopSize() + "!";

        return BPBankTop.getBankTopNamePlayer(position);
    }
}