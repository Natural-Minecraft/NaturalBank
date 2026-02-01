package id.naturalsmp.naturalbank.placeholders.list;

import id.naturalsmp.naturalbank.bankTop.NBBankTop;
import id.naturalsmp.naturalbank.placeholders.NBPlaceholder;
import id.naturalsmp.naturalbank.utils.texts.NBFormatter;
import id.naturalsmp.naturalbank.values.ConfigValues;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class BankTopMoneyPlaceholder extends NBPlaceholder {

    @Override
    public String getIdentifier() {
        return "banktop_money_";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!ConfigValues.isBankTopEnabled())
            return bankTopNotEnabled;

        String number = identifier.replace("banktop_money_", "");
        int position;
        try {
            position = Integer.parseInt(number);
        } catch (NumberFormatException e) {
            return "Invalid banktop number!";
        }

        if (position > ConfigValues.getBankTopSize())
            return "The banktop limit is " + ConfigValues.getBankTopSize() + "!";

        BigDecimal money = NBBankTop.getBankTopBalancePlayer(position);
        switch (ConfigValues.getBankTopMoneyFormat()) {
            case "default_amount":
                return NBFormatter.formatCommas(money);
            case "amount_long":
                return money.toPlainString();
            default:
                return NBFormatter.formatPrecise(money);
            case "amount_formatted_long":
                return NBFormatter.formatLong(money);
        }
    }
}