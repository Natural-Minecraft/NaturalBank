package id.naturalsmp.naturalbank.loanSystem;

import id.naturalsmp.naturalbank.NaturalBank;
import id.naturalsmp.naturalbank.bankSystem.Bank;
import id.naturalsmp.naturalbank.bankSystem.BankRegistry;
import id.naturalsmp.naturalbank.bankSystem.BankUtils;
import id.naturalsmp.naturalbank.economy.NBEconomy;
import id.naturalsmp.naturalbank.economy.TransactionType;
import id.naturalsmp.naturalbank.utils.NBLogger;
import id.naturalsmp.naturalbank.utils.NBUtils;
import id.naturalsmp.naturalbank.utils.SavesFile;
import id.naturalsmp.naturalbank.utils.texts.NBMessages;
import id.naturalsmp.naturalbank.values.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class NBLoanRegistry {

    private static final List<NBLoan> loans = new ArrayList<>();
    private static final HashMap<UUID, LoanRequest> requests = new HashMap<>();

    /**
     * List to track each loan made by players.
     *
     * @return A list of loans.
     */
    public static List<NBLoan> getLoans() {
        return loans;
    }

    /**
     * K = The player UUID that initialized the request.
     * V = The loan request object.
     */
    public static HashMap<UUID, LoanRequest> getRequests() {
        return requests;
    }

    /**
     * Loan storage style:
     * loans:
     * loan-receiver-UUID:
     * sender: loan-sender-UUID
     * loan-values ....
     */
    public static void loadAllLoans() {
        FileConfiguration saves = NaturalBank.INSTANCE().getConfigs().getConfig("saves.yml");

        ConfigurationSection section = saves.getConfigurationSection("loans");
        if (section == null) return;

        for (String receiverUUID : section.getKeys(false)) {
            ConfigurationSection values = section.getConfigurationSection(receiverUUID);
            if (values == null) continue;

            OfflinePlayer receiver, sender = null;

            try {
                receiver = Bukkit.getOfflinePlayer(UUID.fromString(receiverUUID));

                String senderUUID = values.getString("sender");
                if (senderUUID != null) sender = Bukkit.getOfflinePlayer(UUID.fromString(senderUUID));
            } catch (IllegalArgumentException e) {
                NBLogger.Console.warn(e, "Could not load \"" + receiverUUID + "\" loan! (Invalid UUID specified)");
                continue;
            }

            String moneyToReturn = values.getString("money-to-return");
            if (moneyToReturn == null || NBUtils.isInvalidNumber(moneyToReturn)) {
                NBLogger.Console.warn("Could not load \"" + receiverUUID + "\" loan! (An invalid money-to-return amount has been specified)");
                continue;
            }

            NBLoan loan;

            String requestedBank = values.getString("requested-bank");
            if (requestedBank != null) {
                if (!BankUtils.exist(requestedBank)) {
                    NBLogger.Console.warn("The loan \"" + receiverUUID + "\" specified an invalid bank to take the money, using the main bank.");
                    requestedBank = ConfigValues.getMainGuiName();
                }

                loan = new NBLoan(receiver, BankRegistry.getBank(requestedBank));
            } else {

                String fromBank = ConfigValues.getMainGuiName(), toBank = ConfigValues.getMainGuiName();
                String fromBankString = values.getString("from"), toBankString = values.getString("to");

                if (fromBankString == null) NBLogger.Console.warn("The loan \"" + receiverUUID + "\" did not specify a bank to take the money, using the main bank.");
                else if (!BankUtils.exist(fromBankString)) {
                    NBLogger.Console.warn("The loan \"" + receiverUUID + "\" specified an invalid bank to take the money, using the main bank.");
                    fromBank = fromBankString;
                }

                if (toBankString == null) NBLogger.Console.warn("The loan \"" + receiverUUID + "\" did not specify a bank to give the money, using the main bank.");
                else if (!BankUtils.exist(toBankString)) {
                    NBLogger.Console.warn("The loan \"" + receiverUUID + "\" specified an invalid bank to give the money, using the main bank.");
                    toBank = toBankString;
                }

                loan = new NBLoan(sender, receiver, BankRegistry.getBank(fromBank), BankRegistry.getBank(toBank));
            }

            loan.setMoneyToReturn(new BigDecimal(moneyToReturn));
            loan.setInstalments(values.getInt("instalments"));
            loan.setInstalmentsPoint(values.getInt("instalments-point"));

            registerLoan(loan);
        }
    }

    /**
     * Save all currently registered loans.
     * <p>
     * Save format:
     * <pre>
     * loans:
     *   Receiver-UUID:
     *     money-to-return: The total amount of money to return.
     *     instalments: The number of instalments of this loan.
     *     instalments-point: The current instalment.
     *     time-left: The time left before the next instalment.
     *     from: The name of the bank where to take the money.
     *     to: The name of the bank where to send the money.
     *     requested-bank: Not always present, in case a loan has been requested from a back, this will be the giver bank.
     * </pre>
     */
    public static void saveAllLoans() {
        File file = SavesFile.getFile();
        FileConfiguration config = SavesFile.getConfig();
        for (NBLoan loan : loans) {
            String path = "loans." + loan.getReceiver().getUniqueId() + ".";

            if (loan.getSender() != null) config.set(path + "sender", loan.getSender().getUniqueId());
            config.set(path + "money-to-return", loan.getMoneyToReturn());
            config.set(path + "instalments", loan.getInstalments());
            config.set(path + "instalments-point", loan.getInstalmentsPoint());
            config.set(path + "time-left", loan.getTimeLeft());
            config.set(path + "from", loan.getSenderBank().getIdentifier());
            config.set(path + "to", loan.getReceiverBank().getIdentifier());
            config.set(path + "requested-bank", loan.getRequestedBank().getIdentifier());
        }

        try {
            config.save(file);
        } catch (IOException e) {
            NBLogger.Console.warn("Could not save loans to saves.yml file: " + e.getMessage());
        }
    }

    /**
     * Register the loan to the loan registry and start the returning task.
     * * Note: This method does not give the initial money to the player, you'll have to add them manually.
     *
     * @param loan The loan to register.
     */
    public static void registerLoan(NBLoan loan) {
        loans.add(loan);

        // Check that because not every loan is brand new, there could be loans that have a different time left.
        int delay = loan.getTimeLeft() <= 0 ? ConfigValues.getLoanDelay() : NBUtils.millisecondsInTicks(loan.getTimeLeft());
        loan.setTask(Bukkit.getScheduler().runTaskLater(NaturalBank.INSTANCE(), () -> advanceReturningTask(loan), delay));
    }

    /**
     * Queue the loan request for the specified time.
     *
     * @param sender      The sender of the request.
     * @param loanRequest The loan request.
     */
    public static void queueLoanRequest(Player sender, LoanRequest loanRequest) {
        UUID uuid = sender.getUniqueId();
        requests.put(uuid, loanRequest);
        Bukkit.getScheduler().runTaskLater(NaturalBank.INSTANCE(), () -> requests.remove(uuid), ConfigValues.getLoanAcceptTime() * 20L);
    }

    private static void advanceReturningTask(NBLoan loan) {
        loan.setTimeLeft(System.currentTimeMillis() + NBUtils.ticksInMilliseconds(ConfigValues.getLoanDelay()));
        loan.setInstalmentsPoint(loan.getInstalmentsPoint() + 1);
        int instalments = loan.getInstalments();

        BigDecimal currentTaskAmount = loan.getMoneyToReturn().divide(BigDecimal.valueOf(instalments));
        OfflinePlayer sender = loan.getSender(), receiver = loan.getReceiver();
        boolean isBankToPlayer = sender == null;

        Bank senderBank = loan.getSenderBank() == null ? loan.getRequestedBank() : loan.getSenderBank(),
                receiverBank = loan.getReceiverBank() == null ? loan.getRequestedBank() : loan.getReceiverBank();

        // Means that the loan has been requested from a player to another player, so we need to give back the money.
        // If the sender == null, means that the loan has been requested from a player to a bank, so we just remove the money to the player receiver.
        if (!isBankToPlayer) {
            // Add back a part of the amount to the sender of the loan.
            BigDecimal addedToSender = senderBank.getBankEconomy().addBankBalance(sender, currentTaskAmount, TransactionType.LOAN), extra = currentTaskAmount.subtract(addedToSender);
            if (extra.compareTo(BigDecimal.ZERO) <= 0) NBMessages.sendIdentifier(sender.getPlayer(), "Loan-Payback", NBUtils.placeValues(receiver, addedToSender));
            else {
                List<String> replacers = NBUtils.placeValues(currentTaskAmount);
                replacers.addAll(NBUtils.placeValues(receiver, extra, "extra"));
                NBMessages.sendIdentifier(sender.getPlayer(), "Loan-Payback-Full", replacers);
                NaturalBank.INSTANCE().getVaultEconomy().depositPlayer(sender, extra.doubleValue());
            }
        }

        // Remove a part of the amount from the receiver of the loan.
        NBEconomy receiverBankEconomy = receiverBank.getBankEconomy();
        BigDecimal removedToReceiver = receiverBankEconomy.removeBankBalance(receiver, currentTaskAmount, TransactionType.LOAN), debt = currentTaskAmount.subtract(removedToReceiver);
        if (debt.doubleValue() <= 0D) {
            if (isBankToPlayer) NBMessages.sendIdentifier(receiver.getPlayer(), "Loan-Returned-Bank", NBUtils.placeValues(loan.getRequestedBank().getIdentifier(), currentTaskAmount));
            else NBMessages.sendIdentifier(receiver.getPlayer(), "Loan-Returned", NBUtils.placeValues(sender, currentTaskAmount));
        } else {
            BigDecimal newDebt = receiverBankEconomy.getDebt(receiver).add(debt);
            if (isBankToPlayer) NBMessages.sendIdentifier(receiver.getPlayer(), "Loan-Returned-Debt-Bank", NBUtils.placeValues(loan.getRequestedBank().getIdentifier(), newDebt));
            else NBMessages.sendIdentifier(receiver.getPlayer(), "Loan-Returned-Debt", NBUtils.placeValues(sender, newDebt));
            receiverBankEconomy.setDebt(receiver, newDebt);
        }

        // Was the loan at his final instalment?
        if (loan.getInstalmentsPoint() >= instalments) loans.remove(loan);
        else loan.setTask(Bukkit.getScheduler().runTaskLater(NaturalBank.INSTANCE(), () -> advanceReturningTask(loan), ConfigValues.getLoanDelay()));
    }

    public static class LoanRequest {
        private boolean senderIsReceiver;
        private Player sender, receiver;
        private NBLoan loan;

        /**
         * Check if the sender of that request will be the receiver of the money (in cases the player executed "/NB loan request")
         */
        public boolean senderIsReceiver() {
            return senderIsReceiver;
        }

        public Player getSender() {
            return sender;
        }

        public Player getReceiver() {
            return receiver;
        }

        public NBLoan getLoan() {
            return loan;
        }

        public void setSenderIsReceiver(boolean senderIsReceiver) {
            this.senderIsReceiver = senderIsReceiver;
        }

        public void setSender(Player sender) {
            this.sender = sender;
        }

        public void setReceiver(Player receiver) {
            this.receiver = receiver;
        }

        public void setLoan(NBLoan loan) {
            this.loan = loan;
        }
    }
}