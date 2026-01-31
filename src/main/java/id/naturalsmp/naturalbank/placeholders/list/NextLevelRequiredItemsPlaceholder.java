package id.naturalsmp.naturalbank.placeholders.list;

import id.naturalsmp.naturalbank.bankSystem.Bank;
import id.naturalsmp.naturalbank.bankSystem.BankRegistry;
import id.naturalsmp.naturalbank.bankSystem.BankUtils;
import id.naturalsmp.naturalbank.placeholders.BPPlaceholder;
import id.naturalsmp.naturalbank.utils.BPUtils;
import id.naturalsmp.naturalbank.values.ConfigValues;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;

public class NextLevelRequiredItemsPlaceholder extends BPPlaceholder {

    @Override
    public String getIdentifier() {
        return "next_level_required_items";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!BankUtils.exist(target)) return bankDoesNotExist;

        Bank bank = BankRegistry.getBank(target);
        if (!BankUtils.hasNextLevel(bank, p)) return ConfigValues.getUpgradesMaxedPlaceholder();

        HashMap<String, Bank.RequiredItem> requiredItems = BankUtils.getRequiredItems(bank, BankUtils.getCurrentLevel(bank, p) + 1);

        if (identifier.contains("[") && identifier.contains("]")) {
            int position;
            try {
                position = Integer.parseInt(identifier.substring(identifier.indexOf("[") + 1, identifier.indexOf("]")));
            } catch (NumberFormatException e) {
                return "Invalid item position.";
            }

            int i = 0;
            ItemStack choice = null;
            for (String itemName : requiredItems.keySet()) {
                choice = requiredItems.get(itemName).item;
                if (i >= position - 1) break;
            }

            if (choice == null) return "Invalid selected required item.";

            ItemMeta meta = choice.getItemMeta();
            Component displayname;
            if (meta != null && meta.hasDisplayName()) displayname = meta.displayName();
            else displayname = choice.displayName();

            return choice.getAmount() + " " + MiniMessage.miniMessage().serialize(displayname);
        }

        if (requiredItems.isEmpty()) return ConfigValues.getNoUpgradeItemsMessage();
        return BPUtils.getRequiredItemsFormatted(requiredItems.values());
    }
}