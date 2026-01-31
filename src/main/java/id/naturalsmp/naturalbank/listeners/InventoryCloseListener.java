package id.naturalsmp.naturalbank.listeners;

import id.naturalsmp.naturalbank.account.BPPlayer;
import id.naturalsmp.naturalbank.account.PlayerRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.scheduler.BukkitTask;

public class InventoryCloseListener implements Listener {

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();

        BPPlayer player = PlayerRegistry.get(p);
        if (player == null) return;

        BukkitTask updating = player.getBankUpdatingTask();
        if (updating != null) updating.cancel();

        player.setOpenedBank(null);
    }
}