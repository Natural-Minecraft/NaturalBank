package id.naturalsmp.naturalbank.economy;

import id.naturalsmp.naturalbank.NaturalBank;
import id.naturalsmp.naturalbank.account.PlayerRegistry;
import id.naturalsmp.naturalbank.managers.NBTaskManager;
import id.naturalsmp.naturalbank.sql.NBSQL;
import id.naturalsmp.naturalbank.utils.texts.NBFormatter;
import id.naturalsmp.naturalbank.values.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EconomyUtils {

    /**
     * Save the selected player information of all bank economies.
     *
     * @param p The player.
     */
    public static void savePlayer(OfflinePlayer p, boolean unload) {
        for (NBEconomy economy : NBEconomy.list())
            NBSQL.savePlayer(p, economy);

        if (unload)
            PlayerRegistry.unloadPlayer(p);
    }

    /**
     * Load all the online players balance.
     */
    public static void loadEveryone() {
        for (Player p : Bukkit.getOnlinePlayers())
            PlayerRegistry.loadPlayer(p);
    }

    /**
     * Save everyone's balance.
     *
     * @param async Choose if saving asynchronously to have less impact on the
     *              server performance. DON'T USE ON SERVER SHUTDOWN
     */
    public static void saveEveryone(boolean async) {
        Set<UUID> loadedUUIDs = new HashSet<>();
        for (NBEconomy economy : NBEconomy.list()) // It's safe to call #addAll since Sets don't allow duplicated
                                                   // entries.
            loadedUUIDs.addAll(economy.getLoadedPlayers());

        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(NaturalBank.INSTANCE(), () -> {
                for (UUID uuid : loadedUUIDs) {
                    OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
                    savePlayer(p, !p.isOnline());
                }
            });
            return;
        }

        for (UUID uuid : loadedUUIDs) {
            OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
            savePlayer(p, !p.isOnline());
        }
    }

    /**
     * Restart the saving cooldown.
     */
    public static void restartSavingInterval() {
        long delay = ConfigValues.getSaveDelay();
        if (delay <= 0)
            return;

        long minutes = delay * 1200L;
        NBTaskManager.setTask(NBTaskManager.MONEY_SAVING_TASK,
                Bukkit.getScheduler().runTaskTimer(NaturalBank.INSTANCE(), () -> saveEveryone(true), minutes, minutes));
    }
}