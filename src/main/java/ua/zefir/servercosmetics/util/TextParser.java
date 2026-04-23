package ua.zefir.servercosmetics.util;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.parsers.TagParser;
import net.minecraft.text.Text;

public class TextParser {
    private static final TagParser PARSER = TagParser.QUICK_TEXT;

    public static Text format(String content) {
        return PARSER.parseText(content, ParserContext.of());
    }
}
