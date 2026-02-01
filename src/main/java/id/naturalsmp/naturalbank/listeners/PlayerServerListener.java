package id.naturalsmp.naturalbank.listeners;

import id.naturalsmp.naturalbank.NaturalBank;
import id.naturalsmp.naturalbank.account.NBPlayer;
import id.naturalsmp.naturalbank.account.PlayerRegistry;
import id.naturalsmp.naturalbank.economy.NBEconomy;
import id.naturalsmp.naturalbank.economy.EconomyUtils;
import id.naturalsmp.naturalbank.sql.NBSQL;
import id.naturalsmp.naturalbank.utils.NBLogger;
import id.naturalsmp.naturalbank.utils.NBUtils;
import id.naturalsmp.naturalbank.utils.texts.NBMessages;
import id.naturalsmp.naturalbank.values.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;

public class PlayerServerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        Bukkit.getScheduler().runTaskAsynchronously(NaturalBank.INSTANCE(), () -> {
            boolean wasRegistered = NBSQL.isRegistered(p, ConfigValues.getMainGuiName());
            if (!wasRegistered && ConfigValues.isNotifyingNewPlayer())
                    NBLogger.Console.info("Successfully registered " + p.getName() + "!");

            NBSQL.fillRecords(p);

            int loadDelay = ConfigValues.getLoadDelay();
            if (loadDelay <= 0) PlayerRegistry.loadPlayer(p, wasRegistered);
            else Bukkit.getScheduler().runTaskLater(NaturalBank.INSTANCE(), () -> PlayerRegistry.loadPlayer(p, wasRegistered), loadDelay);

            if (!ConfigValues.isNotifyingOfflineInterest()) return;

            BigDecimal amount = BigDecimal.ZERO;
            for (NBEconomy economy : NBEconomy.list()) {
                BigDecimal offlineInterest = NBSQL.getInterest(p, economy.getOriginBank().getIdentifier());
                if (offlineInterest.compareTo(BigDecimal.ZERO) <= 0) continue;

                amount = amount.add(offlineInterest);
                NBSQL.setInterest(p, economy.getOriginBank().getIdentifier(), BigDecimal.ZERO);
            }

            BigDecimal finalAmount = amount;
            String mess = NBMessages.applyMessagesPrefix(ConfigValues.getOfflineInterestMessage());
            if (finalAmount.compareTo(BigDecimal.ZERO) > 0)
                Bukkit.getScheduler().runTaskLater(NaturalBank.INSTANCE(), () ->
                                NBMessages.sendMessage(p, mess, NBUtils.placeValues(finalAmount)),
                        ConfigValues.getNotifyOfflineInterestDelay() * 20L);
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        NBPlayer player = PlayerRegistry.get(p);
        if (player != null) {
            BukkitTask updating = player.getBankUpdatingTask();
            if (updating != null) updating.cancel();

            player.setDepositing(false);
            player.setWithdrawing(false);
        }

        if (ConfigValues.isSavingOnQuit()) EconomyUtils.savePlayer(p, true);
    }
}