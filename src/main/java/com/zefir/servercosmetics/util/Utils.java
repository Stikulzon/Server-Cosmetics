package com.zefir.servercosmetics.util;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class Utils {
    public static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder().hexColors().useUnusualXRepeatedCharacterHexFormat().build();
    public static final MiniMessage MINI_MESSAGE = MiniMessage.builder().tags(StandardTags.defaults()).build();
    public static @NotNull String parseStringToString(String parsed) {
        return SERIALIZER.serialize(MINI_MESSAGE.deserialize(parsed));
    }

    public static Text miniMessageFormatter(String st) {
        String formatted = SERIALIZER.serialize(MINI_MESSAGE.deserialize(st));
        return Text.of(formatted);
    }

    public static Text formatDisplayName(String st) {
        StringBuilder sb = new StringBuilder(st.length());

        for (int i = 0; i < st.length(); i++) {
            char ch = st.charAt(i);
            if (ch == '\\' && i < st.length() - 1) {
                char nextChar = st.charAt(i + 1);
                if (nextChar == 'u') {
                    String hex = st.substring(i + 2, i + 6);
                    ch = (char) Integer.parseInt(hex, 16);
                    i += 5;
                }
            }
            sb.append(ch);
        }
        return Text.of(sb.toString().replace("&", "§"));
    }
}
