package id.naturalsmp.naturalbank.utils.texts;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class BPChat {

    public static final String PREFIX = "<b><gradient:green:blue:green>NaturalBank</gradient></b>";

    public static Component color(String message) {
        return MiniMessage.miniMessage().deserialize(message);
    }
}