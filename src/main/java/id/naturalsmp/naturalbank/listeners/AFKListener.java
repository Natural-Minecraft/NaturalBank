package id.naturalsmp.naturalbank.listeners;

import id.naturalsmp.naturalbank.NaturalBank;
import id.naturalsmp.naturalbank.utils.BPUtils;
import id.naturalsmp.naturalbank.values.ConfigValues;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class AFKListener implements Listener {

    private final NaturalBank plugin;

    public AFKListener(NaturalBank plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!ConfigValues.isIgnoringAfkPlayers() || ConfigValues.isUsingEssentialsXAFK()) return;

        Player p = e.getPlayer();
        long time = System.currentTimeMillis() + BPUtils.minutesInMilliseconds(ConfigValues.getAfkPlayersTime());
        plugin.getAfkManager().getAfkCooldown().put(p.getUniqueId(), time);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!ConfigValues.isIgnoringAfkPlayers() || ConfigValues.isUsingEssentialsXAFK()) return;

        Player p = e.getPlayer();
        long time = System.currentTimeMillis() + BPUtils.minutesInMilliseconds(ConfigValues.getAfkPlayersTime());
        plugin.getAfkManager().getAfkCooldown().put(p.getUniqueId(), time);
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (!ConfigValues.isIgnoringAfkPlayers() || ConfigValues.isUsingEssentialsXAFK()) return;

        Player p = e.getPlayer();
        long time = System.currentTimeMillis() + BPUtils.minutesInMilliseconds(ConfigValues.getAfkPlayersTime());
        plugin.getAfkManager().getAfkCooldown().put(p.getUniqueId(), time);
    }
}