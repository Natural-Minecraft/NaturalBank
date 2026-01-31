package id.naturalsmp.naturalbank.external;

import id.naturalsmp.naturalbank.NaturalBank;
import id.naturalsmp.naturalbank.utils.texts.BPChat;
import id.naturalsmp.naturalbank.values.ConfigValues;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateChecker implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (!ConfigValues.isUpdateCheckerEnabled() || (!p.isOp() && !p.hasPermission("NaturalBank.notify"))) return;

        String message;
        if (!NaturalBank.INSTANCE().isUpdated())
            message = BPChat.PREFIX + " <green>A new update is available! " +
                    "<hover:show_text:'<aqua>Click to download it!'>" +
                    "<click:open_url:https://www.spigotmc.org/resources/%E2%9C%A8-NaturalBank-%E2%9C%A8.93130/>" +
                    "<aqua>[INFO]";
        else if (NaturalBank.isAlphaVersion())
            message = BPChat.PREFIX + " <aqua>You are using an alpha " +
                    "version of the plugin, if you find any bug make sure to report it in my discord server. Thanks! :)";
        else message = null;

        if (message != null)
            Bukkit.getScheduler().runTaskLater(NaturalBank.INSTANCE(), () -> {
                p.sendMessage(" ");
                p.sendMessage(MiniMessage.miniMessage().deserialize(message));
                p.sendMessage(" ");
            }, 80);
    }
}