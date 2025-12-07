package ua.zefir.servercosmetics.datagen.ui;

import static ua.zefir.servercosmetics.datagen.ui.UiResourceCreator.*;

import java.util.function.Function;
import net.minecraft.text.Text;

public class GuiTextures {
  public static final Function<Text, Text> COLOR_PICKER_MENU = background("color_picker_menu");
  public static final Function<Text, Text> COSMETICS_MENU = background("cosmetics_menu");
  public static final Function<Text, Text> ITEM_SKINS_MENU = background("item_skins_menu");

  public static void register() {}
}
