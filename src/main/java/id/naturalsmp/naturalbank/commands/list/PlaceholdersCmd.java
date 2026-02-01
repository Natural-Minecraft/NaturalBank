package id.naturalsmp.naturalbank.commands.list;

import id.naturalsmp.naturalbank.NaturalBank;
import id.naturalsmp.naturalbank.bankSystem.BankUtils;
import id.naturalsmp.naturalbank.commands.NBCmdExecution;
import id.naturalsmp.naturalbank.commands.NBCommand;
import id.naturalsmp.naturalbank.utils.texts.NBArgs;
import id.naturalsmp.naturalbank.utils.texts.NBMessages;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class PlaceholdersCmd extends NBCommand {

    public PlaceholdersCmd(FileConfiguration commandsConfig, String commandID) {
        super(commandsConfig, commandID);
    }

    public PlaceholdersCmd(FileConfiguration commandsConfig, String commandID, String... aliases) {
        super(commandsConfig, commandID, aliases);
    }

    @Override
    public List<String> defaultUsage() {
        return Collections.emptyList();
    }

    @Override
    public int defaultConfirmCooldown() {
        return 0;
    }

    @Override
    public List<String> defaultConfirmMessage() {
        return Collections.emptyList();
    }

    @Override
    public int defaultCooldown() {
        return 0;
    }

    @Override
    public List<String> defaultCooldownMessage() {
        return Collections.emptyList();
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public boolean skipUsage() {
        return true;
    }

    @Override
    public NBCmdExecution onExecution(CommandSender s, String[] args) {
        return new NBCmdExecution() {
            @Override
            public void execute() {
                List<String> placeholders = NaturalBank.INSTANCE().getNbPlaceholders().getRegisteredPlaceholders();
                int size = placeholders.size();

                NBMessages.sendMessage(s, "%prefix% Currently registered placeholders <dark_gray>(<aqua>" + size
                        + "</aqua>)</dark_gray>:");

                StringBuilder builder = new StringBuilder(" <dark_gray>* ");
                for (int i = 0; i < size; i++) {
                    builder.append("<dark_gray>[<green>").append(placeholders.get(i));

                    if (i + 1 >= size)
                        builder.append("<dark_gray>]<gray>.");
                    else
                        builder.append("<dark_gray>]<gray>, ");
                }
                NBMessages.sendMessage(s, builder.toString());
            }
        };
    }

    @Override
    public List<String> tabCompletion(CommandSender s, String[] args) {
        Player p = (Player) s;

        if (args.length == 2)
            return NBArgs.getArgs(args, BankUtils.getAvailableBankNames(p));
        return null;
    }
}