package us.polarismc.polarisduels.utils;

import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public Utils() {}

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("(#[A-Fa-f0-9]{6})");

    public String chat (String s){
        Matcher matcher = HEX_COLOR_PATTERN.matcher(s);
        while (matcher.find())
            s = s.replace("&#" + matcher.group(), "<#" + matcher.group() + ">");
        return JSONComponentSerializer.json().serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(s));
    }

    public String prefix = chat("&bDuels &7» &r");
}
