package ua.zefir.servercosmetics.util;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.parsers.TagParser;
import net.minecraft.network.chat.Component;

public class TextParser {
  private static final TagParser PARSER = TagParser.QUICK_TEXT_WITH_STF;

  public static Component format(String content) {
    return PARSER.parseComponent(content, ParserContext.of());
  }
}
