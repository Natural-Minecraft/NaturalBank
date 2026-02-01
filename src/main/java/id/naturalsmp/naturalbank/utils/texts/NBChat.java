package id.naturalsmp.naturalbank.utils.texts;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class NBChat {

    public static final String PREFIX = "<b><gradient:blue:green>NaturalBank</gradient></b>";

    public static Component color(String message) {
        return MiniMessage.miniMessage().deserialize(message);
    }
}