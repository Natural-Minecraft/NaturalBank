package id.naturalsmp.naturalbank.listeners.playerChat;

import io.papermc.paper.event.player.AsyncChatEvent;
import id.naturalsmp.naturalbank.NaturalBank;
import id.naturalsmp.naturalbank.account.NBPlayer;
import id.naturalsmp.naturalbank.account.PlayerRegistry;
import id.naturalsmp.naturalbank.bankSystem.Bank;
import id.naturalsmp.naturalbank.bankSystem.BankGui;
import id.naturalsmp.naturalbank.economy.NBEconomy;
import id.naturalsmp.naturalbank.utils.texts.NBMessages;
import id.naturalsmp.naturalbank.values.ConfigValues;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;

public class PlayerChatMethod {

    public static void process(AsyncChatEvent e) {
        Player p = e.getPlayer();
        NBPlayer NBPlayer = PlayerRegistry.get(p);
        if (NBPlayer == null) return;

        if (!NBPlayer.isDepositing() && !NBPlayer.isWithdrawing()) return;
        e.setCancelled(true);
        e.viewers().clear(); // Try stopping chat plugins from still sending the message.

        Bank openedBank = NBPlayer.getOpenedBank();
        if (openedBank == null) {
            removeFromTyping(NBPlayer);
            return;
        }

        MiniMessage mm = MiniMessage.miniMessage();
        String text = mm.serialize(e.message());
        // If some chat format plugin is adding a "." at the end, remove it.
        if (text.endsWith(".")) text = text.substring(0, text.length() - 1);

        if (hasTypedExit(text, p)) reopenBank(NBPlayer, openedBank.getBankGui());
        else {
            BigDecimal amount;
            try {
                amount = new BigDecimal(text);
            } catch (NumberFormatException ex) {
                NBMessages.sendIdentifier(p, "Invalid-Number");
                return;
            }

            NBEconomy economy = openedBank.getBankEconomy();
            if (NBPlayer.isDepositing()) economy.deposit(p, amount);
            else economy.withdraw(p, amount);

            reopenBank(NBPlayer, openedBank.getBankGui());
        }
    }

    private static boolean hasTypedExit(String message, Player p) {
        if (!message.toLowerCase().contains(ConfigValues.getChatExitMessage().toLowerCase())) return false;
        executeExitCommands(p);
        return true;
    }

    private static void removeFromTyping(NBPlayer NBPlayer) {
        NBPlayer.setDepositing(false);
        NBPlayer.setWithdrawing(false);
    }

    public static void reopenBank(NBPlayer NBPlayer, BankGui openedBankGui) {
        Bukkit.getScheduler().runTask(NaturalBank.INSTANCE(), () -> {
            BukkitTask task = NBPlayer.getClosingTask();
            if (task != null) task.cancel();

            removeFromTyping(NBPlayer);
            if (ConfigValues.isReopeningBankAfterChat() && ConfigValues.isGuiModuleEnabled()) openedBankGui.openBankGui(NBPlayer.getPlayer(), true);
        });
    }

    private static void executeExitCommands(Player p) {
        Bukkit.getScheduler().runTask(NaturalBank.INSTANCE(), () -> {
            for (String cmd : ConfigValues.getExitCommands()) {
                if (cmd.startsWith("[CONSOLE]")) {
                    String s = cmd.replace("[CONSOLE] ", "").replace("%player%", p.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s);
                }
                if (cmd.startsWith("[PLAYER]")) {
                    String s = cmd.replace("[PLAYER] ", "");
                    p.chat("/" + s);
                }
            }
        });
    }
}
