package id.naturalsmp.naturalbank.values;

import id.naturalsmp.naturalbank.utils.NBLogger;
import id.naturalsmp.naturalbank.utils.NBUtils;
import id.naturalsmp.naturalbank.utils.texts.NBFormatter;
import org.bukkit.configuration.file.FileConfiguration;

import java.math.BigDecimal;

public class ValueLoader {

    /**
     * Simplify the process of getting a BigDecimal amount
     * from the config with an automatic warn of invalid number.
     *
     * @param config The config.
     * @param path   The path.
     * @return A BigDecimal.
     */
    protected static BigDecimal getBigDecimal(FileConfiguration config, String path) {
        String amount = config.getString(path);
        if (NBUtils.isInvalidNumber(amount))
            NBLogger.Console.warn("\"" + path + "\" is an invalid number, please correct it in the config.yml.");
        return NBFormatter.getStyledBigDecimal(amount);
    }

    /**
     * Get the delay from the given path.
     * 
     * @param config The config.
     * @param path   The path.
     * @return The delay in milliseconds.
     */
    protected static long getDelayMilliseconds(FileConfiguration config, String path) {
        long delayMillis = 0;
        String time = config.getString(path);
        if (time == null)
            return delayMillis;
        if (!time.contains(" "))
            return NBUtils.minutesInMilliseconds(Integer.parseInt(time));

        String[] split = time.split(" ");
        int delay;
        try {
            delay = Integer.parseInt(split[0]);
        } catch (NumberFormatException e) {
            NBLogger.Console.warn("\"" + path + "\" is an invalid number, please correct it in the config.yml.");
            return NBUtils.minutesInMilliseconds(5);
        }

        String delayType = split[1];
        switch (delayType) {
            case "s":
                return NBUtils.secondsInMilliseconds(delay);
            default:
                return NBUtils.minutesInMilliseconds(delay);
            case "h":
                return NBUtils.hoursInMilliseconds(delay);
            case "d":
                return NBUtils.daysInMilliseconds(delay);
        }
    }
}
