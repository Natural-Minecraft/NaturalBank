package id.naturalsmp.naturalbank.commands;

import id.naturalsmp.naturalbank.bankTop.NBBankTop;
import id.naturalsmp.naturalbank.utils.NBLogger;
import id.naturalsmp.naturalbank.utils.NBUtils;
import id.naturalsmp.naturalbank.utils.texts.NBChat;
import id.naturalsmp.naturalbank.utils.texts.NBFormatter;
import id.naturalsmp.naturalbank.utils.texts.NBMessages;
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
            NBMessages.sendIdentifier(s, "BankTop-Disabled");
            return false;
        }
        if (!NBUtils.hasPermission(s, "NaturalBank.banktop"))
            return false;

        List<String> format = ConfigValues.getBankTopFormat();
        for (String line : format)
            s.sendMessage(NBChat.color(placeName(placeMoney(line))));
        return true;
    }

    private String placeMoney(String message) {
        if (!message.contains("%NaturalBank_banktop_money_"))
            return message;

        String split = message.split("%NaturalBank_banktop_money_")[1];
        int i = split.indexOf("%");
        String numbers = split.substring(0, i);
        int position;
        try {
            position = Integer.parseInt(numbers);
        } catch (NumberFormatException e) {
            NBLogger.Console.error("Invalid number for the BankTop money placeholder!");
            NBLogger.Console.error("Message: " + message);
            return message;
        }
        if (position > ConfigValues.getBankTopSize()) {
            NBLogger.Console.error("Limit of the BankTop: " + ConfigValues.getBankTopSize());
            NBLogger.Console.error("Message: " + message);
            return message;
        }

        String stringToReplace;
        BigDecimal money = NBBankTop.getBankTopBalancePlayer(position);
        switch (ConfigValues.getBankTopMoneyFormat()) {
            case "default_amount":
                stringToReplace = NBFormatter.formatCommas(money);
                break;
            case "amount_long":
                stringToReplace = String.valueOf(money);
                break;
            default:
                stringToReplace = NBFormatter.formatPrecise(money);
                break;
            case "amount_formatted_long":
                stringToReplace = NBFormatter.formatLong(money);
                break;
        }
        return message.replace("%NaturalBank_banktop_money_" + position + "%", stringToReplace);
    }

    private String placeName(String message) {
        if (!message.contains("%NaturalBank_banktop_name_"))
            return message;

        String split = message.split("%NaturalBank_banktop_name_")[1];
        int i = split.indexOf("%");
        String numbers = split.substring(0, i);
        int position;
        try {
            position = Integer.parseInt(numbers);
        } catch (NumberFormatException e) {
            NBLogger.Console.error("Invalid number for the BankTop name placeholder!");
            NBLogger.Console.error("Message: " + message);
            return message;
        }
        if (position > ConfigValues.getBankTopSize()) {
            NBLogger.Console.error("Limit of the BankTop: " + ConfigValues.getBankTopSize());
            NBLogger.Console.error("Message: " + message);
            return message;
        }

        String name = NBBankTop.getBankTopNamePlayer(position);
        return message.replace("%NaturalBank_banktop_name_" + position + "%", name);
    }
}