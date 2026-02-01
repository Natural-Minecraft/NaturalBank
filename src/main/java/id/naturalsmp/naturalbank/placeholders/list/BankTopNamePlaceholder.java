package id.naturalsmp.naturalbank.placeholders.list;

import id.naturalsmp.naturalbank.bankTop.NBBankTop;
import id.naturalsmp.naturalbank.placeholders.NBPlaceholder;
import id.naturalsmp.naturalbank.values.ConfigValues;
import org.bukkit.entity.Player;

public class BankTopNamePlaceholder extends NBPlaceholder {

    @Override
    public String getIdentifier() {
        return "banktop_name_";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!ConfigValues.isBankTopEnabled())
            return bankTopNotEnabled;

        String number = identifier.replace("banktop_name_", "");
        int position;
        try {
            position = Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return "Invalid banktop number!";
        }

        if (position > ConfigValues.getBankTopSize())
            return "The banktop limit is " + ConfigValues.getBankTopSize() + "!";

        return NBBankTop.getBankTopNamePlayer(position);
    }
}