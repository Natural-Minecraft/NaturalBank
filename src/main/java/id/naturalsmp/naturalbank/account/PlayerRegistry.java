package id.naturalsmp.naturalbank.account;

import id.naturalsmp.naturalbank.NaturalBank;
import id.naturalsmp.naturalbank.bankSystem.Bank;
import id.naturalsmp.naturalbank.bankSystem.BankRegistry;
import id.naturalsmp.naturalbank.economy.NBEconomy;
import id.naturalsmp.naturalbank.sql.NBSQL;
import id.naturalsmp.naturalbank.utils.NBLogger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class PlayerRegistry {

    private static final HashMap<UUID, NBPlayer> players = new HashMap<>();

    public static boolean isPlayerLoaded(OfflinePlayer p) {
        return players.containsKey(p.getUniqueId());
    }

    /**
     * Load the specified player to all registered banks.
     *
     * @param p The player to load.
     * @return The instance of the loaded player.
     */
    public static NBPlayer loadPlayer(Player p) {
        return loadPlayer(p, true);
    }

    /**
     * Load the specified player to all registered banks.
     *
     * @param p             The player to load.
     * @param wasRegistered Specify if the player was already registered or not.
     * @return The instance of the loaded player.
     */
    public static NBPlayer loadPlayer(Player p, boolean wasRegistered) {
        NBPlayer nbPlayer = new NBPlayer(p);

        for (Bank bank : BankRegistry.getBanks().values())
            nbPlayer = loadPlayer(p, bank, wasRegistered);

        return nbPlayer;
    }

    /**
     * Load the player only to the specified bank (useful when just needing to load
     * it in new registered banks)
     *
     * @param p             The player to load.
     * @param bank          The bank where to load the player.
     * @param wasRegistered Specify if the player was already registered or not.
     * @return The instance of the loaded player.
     */
    public static NBPlayer loadPlayer(Player p, Bank bank, boolean wasRegistered) {
        NBPlayer nbPlayer;

        UUID uuid = p.getUniqueId();
        if (!players.containsKey(uuid))
            nbPlayer = new NBPlayer(p);
        else
            nbPlayer = players.get(uuid);

        if (bank == null) {
            NBLogger.Console.error("Cannot load player " + p.getName() + " because the bank specified is null");
            return nbPlayer;
        }

        Bukkit.getScheduler().runTaskAsynchronously(NaturalBank.INSTANCE(),
                () -> bank.getBankEconomy().loadPlayer(p, wasRegistered));
        players.putIfAbsent(uuid, nbPlayer);
        return nbPlayer;
    }

    /**
     * Method to remove the NBPlayer instance from the registry and unloading the
     * player from all economies.
     *
     * @param p The player to unload.
     * @return The instance removed from the registry.
     */
    public static NBPlayer unloadPlayer(OfflinePlayer p) {
        for (NBEconomy economy : NBEconomy.list())
            economy.unloadPlayer(p);
        return players.remove(p.getUniqueId());
    }

    public static NBPlayer get(OfflinePlayer p) {
        return get(p.getUniqueId());
    }

    public static NBPlayer get(UUID uuid) {
        return players.get(uuid);
    }
}
