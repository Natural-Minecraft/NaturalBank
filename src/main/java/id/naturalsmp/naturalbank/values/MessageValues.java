package id.naturalsmp.naturalbank.values;

import id.naturalsmp.naturalbank.NaturalBank;
import id.naturalsmp.naturalbank.utils.texts.NBMessages;
import org.bukkit.configuration.file.FileConfiguration;

public class MessageValues {

    private static boolean isTitleCustomAmountEnabled, isInterestBroadcastEnabled;
    private static String customDepositTitle, customWithdrawTitle;
    private static String interestMoney, interestNoMoney, multiInterestMoney, interestBankFull;

    public static void setupValues() {
        NBMessages.loadMessages();

        FileConfiguration messages = NaturalBank.INSTANCE().getConfigs().getConfig("messages.yml");
        isTitleCustomAmountEnabled = messages.getBoolean("Title-Custom-Transaction.Enabled");
        isInterestBroadcastEnabled = messages.getBoolean("Interest-Broadcast.Enabled");
        customDepositTitle = messages.getString("Title-Custom-Transaction.Title-Deposit");
        customWithdrawTitle = messages.getString("Title-Custom-Transaction.Title-Withdraw");
        interestMoney = messages.getString("Interest-Broadcast.Message");
        multiInterestMoney = messages.getString("Interest-Broadcast.Multi-Message");
        interestNoMoney = messages.getString("Interest-Broadcast.No-Money");
        interestBankFull = messages.getString("Interest-Broadcast.Bank-Full");
    }

    public static boolean isTitleCustomAmountEnabled() {
        return isTitleCustomAmountEnabled;
    }

    public static boolean isInterestBroadcastEnabled() {
        return isInterestBroadcastEnabled;
    }

    public static String getCustomDepositTitle() {
        return customDepositTitle;
    }

    public static String getCustomWithdrawTitle() {
        return customWithdrawTitle;
    }

    public static String getInterestMoney() {
        return interestMoney;
    }

    public static String getMultiInterestMoney() {
        return multiInterestMoney;
    }

    public static String getInterestNoMoney() {
        return interestNoMoney;
    }

    public static String getInterestBankFull() {
        return interestBankFull;
    }
}