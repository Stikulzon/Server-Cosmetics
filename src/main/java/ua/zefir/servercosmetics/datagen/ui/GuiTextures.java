package ua.zefir.servercosmetics.datagen.ui;

import static ua.zefir.servercosmetics.datagen.ui.UiResourceCreator.*;

import java.util.function.Function;
import net.minecraft.network.chat.Component;

public class GuiTextures {
  public static final Function<Component, Component> COLOR_PICKER_MENU =
      background("color_picker_menu");
  public static final Function<Component, Component> COSMETICS_MENU = background("cosmetics_menu");
  public static final Function<Component, Component> ITEM_SKINS_MENU =
      background("item_skins_menu");
  public static final Function<Component, Component> SEARCH_MENU = background("search_menu");

  public static void register() {}
}
