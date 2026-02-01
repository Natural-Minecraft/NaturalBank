package id.naturalsmp.naturalbank.placeholders.list;

import id.naturalsmp.naturalbank.bankSystem.Bank;
import id.naturalsmp.naturalbank.bankSystem.BankRegistry;
import id.naturalsmp.naturalbank.bankSystem.BankUtils;
import id.naturalsmp.naturalbank.placeholders.NBPlaceholder;
import id.naturalsmp.naturalbank.values.ConfigValues;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class NextLevelCostPlaceholder extends NBPlaceholder {

    @Override
    public String getIdentifier() {
        return "next_level_cost";
    }

    @Override
    public String getPlaceholder(Player p, String target, String identifier) {
        if (!BankUtils.exist(target))
            return bankDoesNotExist;

        Bank bank = BankRegistry.getBank(target);
        if (!BankUtils.hasNextLevel(bank, p))
            return ConfigValues.getUpgradesMaxedPlaceholder();

        BigDecimal cost = BankUtils.getLevelCost(bank, BankUtils.getCurrentLevel(bank, p) + 1);
        return getFormat(identifier, cost);
    }
}