package id.naturalsmp.naturalbank.listeners;

import id.naturalsmp.naturalbank.economy.NBEconomy;
import id.naturalsmp.naturalbank.economy.TransactionType;
import id.naturalsmp.naturalbank.events.NBAfterTransactionEvent;
import id.naturalsmp.naturalbank.events.NBPreTransactionEvent;
import id.naturalsmp.naturalbank.utils.NBLogger;
import id.naturalsmp.naturalbank.utils.NBUtils;
import id.naturalsmp.naturalbank.utils.texts.NBFormatter;
import id.naturalsmp.naturalbank.utils.texts.NBMessages;
import id.naturalsmp.naturalbank.values.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class NBTransactionListener implements Listener {

    public record Pair<K, V>(K key, V value) {

    }

    private final HashMap<UUID, Pair<BigDecimal, Double>> logHolder = new HashMap<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTransactionStart(NBPreTransactionEvent e) {
        if (ConfigValues.isLoggingTransactions())
            logHolder.put(e.getPlayer().getUniqueId(), new Pair<>(e.getCurrentBalance(), e.getCurrentVaultBalance()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTransactionEnd(NBAfterTransactionEvent e) {
        if (ConfigValues.isLoggingTransactions()) log(e);

        NBEconomy economy = NBEconomy.get(e.getBankName());
        if (economy == null) return;

        OfflinePlayer p = e.getPlayer();
        BigDecimal debt = economy.getDebt(p);
        if (debt.compareTo(BigDecimal.ZERO) <= 0) return;

        BigDecimal removed = economy.removeBankBalance(p, debt, true);
        if (removed.compareTo(BigDecimal.ZERO) <= 0) return;

        BigDecimal newDebt = debt.subtract(removed);
        economy.setDebt(p, newDebt);

        List<String> replacers = NBUtils.placeValues(e.getTransactionAmount().min(debt));
        replacers.addAll(NBUtils.placeValues(newDebt, "debt"));
        NBMessages.sendIdentifier(Bukkit.getPlayer(p.getUniqueId()), "Debt-Money-Taken", replacers);
    }

    private void log(NBAfterTransactionEvent e) {
        TransactionType type = e.getTransactionType();
        StringBuilder builder = new StringBuilder(e.getPlayer().getName() + " -> " + type.name());

        if (type.equals(TransactionType.DEPOSIT)) {
            BigDecimal taxes = ConfigValues.getDepositTaxes();
            if (taxes.doubleValue() > 0) builder.append(" (Taxes: ").append(taxes).append("%)");
        }

        if (type.equals(TransactionType.WITHDRAW)) {
            BigDecimal taxes = ConfigValues.getWithdrawTaxes();
            if (taxes.doubleValue() > 0) builder.append(" (Taxes: ").append(taxes).append("%)");
        }

        Pair<BigDecimal, Double> pair = logHolder.get(e.getPlayer().getUniqueId());
        builder.append(
                " - %1 [%2] Bank: [%3 -> %4] Vault: [%5 -> %6]\n"
                        .replace("%1", NBFormatter.styleBigDecimal(e.getTransactionAmount()))
                        .replace("%2", e.getBankName())
                        .replace("%3", NBFormatter.styleBigDecimal(pair.key()))
                        .replace("%4", NBFormatter.styleBigDecimal(e.getNewBalance()))
                        .replace("%5", pair.value() + "")
                        .replace("%6", e.getNewVaultBalance() + "")
        );

        NBLogger.LogsFile.log(builder.toString());
    }
}