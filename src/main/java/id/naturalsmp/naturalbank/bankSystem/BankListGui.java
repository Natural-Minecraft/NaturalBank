package id.naturalsmp.naturalbank.bankSystem;

import me.clip.placeholderapi.PlaceholderAPI;
import id.naturalsmp.naturalbank.NaturalBank;
import id.naturalsmp.naturalbank.account.BPPlayer;
import id.naturalsmp.naturalbank.account.PlayerRegistry;
import id.naturalsmp.naturalbank.utils.BPUtils;
import id.naturalsmp.naturalbank.utils.texts.BPChat;
import id.naturalsmp.naturalbank.values.ConfigValues;
import id.naturalsmp.naturalbank.values.MultipleBanksValues;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;

/**
 * This class is used to create different instances of the bank list gui based on the player-accessible banks.
 * It must be initialized, every gui can be different depending on the player.
 */
public class BankListGui extends BankGui {

    private final HashMap<Integer, Bank> bankListGuiClickHolder = new HashMap<>();

    public BankListGui() {
        super(null);
    }

    @Override
    public void openBankGui(Player p) {
        this.openBankGui(p, true);
    }

    @Override
    public void openBankGui(Player p, boolean bypass) {
        BPPlayer player = PlayerRegistry.get(p);

        BukkitTask updating = player.getBankUpdatingTask();
        if (updating != null) updating.cancel();

        if (MultipleBanksValues.isDirectlyOpenIf1IsAvailable()) {
            List<Bank> availableBanks = BankUtils.getAvailableBanks(p);
            if (availableBanks.size() == 1) {
                availableBanks.getFirst().getBankGui().openBankGui(p);
                return;
            }
        }

        Component title = MultipleBanksValues.getBanksGuiTitle();
        if (NaturalBank.INSTANCE().isPlaceholderApiHooked())
            title = BPChat.color(PlaceholderAPI.setPlaceholders(p, MiniMessage.miniMessage().serialize(title)));

        Inventory bankListInventory = Bukkit.createInventory(new BankHolder(), MultipleBanksValues.getBankListGuiLines(), title);
        placeContent(getBankItems(), bankListInventory, p);
        updateBankGuiMeta(bankListInventory, p);

        long delay = MultipleBanksValues.getUpdateDelay();
        if (delay >= 0) player.setBankUpdatingTask(Bukkit.getScheduler().runTaskTimer(NaturalBank.INSTANCE(), () -> updateBankGuiMeta(bankListInventory, p), delay, delay));

        player.setOpenedBank(getOriginBank());
        if (ConfigValues.isPersonalSoundEnabled()) BPUtils.playSound(ConfigValues.getPersonalSound(), p);
        p.openInventory(bankListInventory);
    }

    @Override
    public void placeContent(HashMap<Integer, BPGuiItem> items, Inventory bankInventory, Player p) {
        int slot = 0;

        for (Bank bank : BankRegistry.getBanks().values()) {
            if (MultipleBanksValues.isShowNotAvailableBanks() && !BankUtils.isAvailable(bank, p)) continue;

            BankGui gui = bank.getBankGui();
            BPGuiItem showBankItem = BankUtils.isAvailable(bank, p) ? gui.getAvailableBankListItem() : gui.getUnavailableBankListItem();

            bankInventory.setItem(slot, showBankItem.getItem());
            bankListGuiClickHolder.put(slot, bank);
            getBankItems().put(slot, showBankItem);
            slot++;
        }
    }

    public HashMap<Integer, Bank> getBankListGuiClickHolder() {
        return bankListGuiClickHolder;
    }
}