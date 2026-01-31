package id.naturalsmp.naturalbank.commands;

import id.naturalsmp.naturalbank.bankTop.BPBankTop;
import id.naturalsmp.naturalbank.utils.BPLogger;
import id.naturalsmp.naturalbank.utils.BPUtils;
import id.naturalsmp.naturalbank.utils.texts.BPChat;
import id.naturalsmp.naturalbank.utils.texts.BPFormatter;
import id.naturalsmp.naturalbank.utils.texts.BPMessages;
import id.naturalsmp.naturalbank.values.ConfigValues;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.List;

public class BankTopCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender s, Command command, String label, String[] args) {
        if (!ConfigValues.isBankTopEnabled()) {
            BPMessages.sendIdentifier(s, "BankTop-Disabled");
            return false;
        }
        if (!BPUtils.hasPermission(s, "NaturalBank.banktop")) return false;

        List<String> format = ConfigValues.getBankTopFormat();
        for (String line : format) s.sendMessage(BPChat.color(placeName(placeMoney(line))));
        return true;
    }

    private String placeMoney(String message) {
        if (!message.contains("%NaturalBank_banktop_money_")) return message;

        String split = message.split("%NaturalBank_banktop_money_")[1];
        int i = split.indexOf("%");
        String numbers = split.substring(0, i);
        int position;
        try {
            position = Integer.parseInt(numbers);
        } catch (NumberFormatException e) {
            BPLogger.Console.error("Invalid number for the BankTop money placeholder!");
            BPLogger.Console.error("Message: " + message);
            return message;
        }
        if (position > ConfigValues.getBankTopSize()) {
            BPLogger.Console.error("Limit of the BankTop: " + ConfigValues.getBankTopSize());
            BPLogger.Console.error("Message: " + message);
            return message;
        }

        String stringToReplace;
        BigDecimal money = BPBankTop.getBankTopBalancePlayer(position);
        switch (ConfigValues.getBankTopMoneyFormat()) {
            case "default_amount":
                stringToReplace = BPFormatter.formatCommas(money);
                break;
            case "amount_long":
                stringToReplace = String.valueOf(money);
                break;
            default:
                stringToReplace = BPFormatter.formatPrecise(money);
                break;
            case "amount_formatted_long":
                stringToReplace = BPFormatter.formatLong(money);
                break;
        }
        return message.replace("%NaturalBank_banktop_money_" + position + "%", stringToReplace);
    }

    private String placeName(String message) {
        if (!message.contains("%NaturalBank_banktop_name_")) return message;

        String split = message.split("%NaturalBank_banktop_name_")[1];
        int i = split.indexOf("%");
        String numbers = split.substring(0, i);
        int position;
        try {
            position = Integer.parseInt(numbers);
        } catch (NumberFormatException e) {
            BPLogger.Console.error("Invalid number for the BankTop name placeholder!");
            BPLogger.Console.error("Message: " + message);
            return message;
        }
        if (position > ConfigValues.getBankTopSize()) {
            BPLogger.Console.error("Limit of the BankTop: " + ConfigValues.getBankTopSize());
            BPLogger.Console.error("Message: " + message);
            return message;
        }

        String name = BPBankTop.getBankTopNamePlayer(position);
        return message.replace("%NaturalBank_banktop_name_" + position + "%", name);
    }
}