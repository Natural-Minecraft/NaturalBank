package me.pulsi_.bankplus.managers;

import com.earth2me.essentials.Essentials;
import me.pulsi_.bankplus.BankPlus;
import me.pulsi_.bankplus.values.ConfigValues;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class BPAFK {

    private final Map<UUID, Long> afkCooldown = new HashMap<>();
    private final List<Player> afkPlayers = new ArrayList<>();
    private boolean isPlayerCountdownActive = false;

    private final BankPlus plugin;

    public BPAFK(BankPlus plugin) {
        this.plugin = plugin;
    }

    public boolean isAFK(Player p) {
        if (ConfigValues.isUsingEssentialsXAFK()) {
            return Essentials.getPlugin(Essentials.class).getUser(p).isAfk();
        }
        if (ConfigValues.isUsingCmiAfk()) {
            return getCmiAfkStatus(p);
        }
        return afkPlayers.contains(p);
    }

    private boolean getCmiAfkStatus(Player p) {
        try {
            Class<?> cmiClass = Class.forName("com.Zrips.CMI.CMI");
            Object cmi = cmiClass.getMethod("getInstance").invoke(null);
            Object playerManager = cmi.getClass().getMethod("getPlayerManager").invoke(cmi);
            Object user = playerManager.getClass().getMethod("getUser", Player.class).invoke(playerManager, p);
            return (boolean) user.getClass().getMethod("isAfk").invoke(user);
        } catch (Exception e) {
            return false;
        }
    }

    public void startCountdown() {
        if (!ConfigValues.isIgnoringAfkPlayers() || ConfigValues.isUsingEssentialsXAFK()
                || ConfigValues.isUsingCmiAfk()) {
            isPlayerCountdownActive = false;
            return;
        }
        isPlayerCountdownActive = true;

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!afkCooldown.containsKey(p.getUniqueId()))
                continue;
            if (afkCooldown.get(p.getUniqueId()) > System.currentTimeMillis())
                afkPlayers.remove(p);
            else {
                if (!afkPlayers.contains(p))
                    afkPlayers.add(p);
            }
        }

        Bukkit.getScheduler().runTaskLater(plugin, this::startCountdown, 20L);
    }

    public Map<UUID, Long> getAfkCooldown() {
        return afkCooldown;
    }

    public boolean isPlayerCountdownActive() {
        return isPlayerCountdownActive;
    }
}