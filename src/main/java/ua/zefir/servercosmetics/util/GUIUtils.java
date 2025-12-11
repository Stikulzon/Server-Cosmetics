package ua.zefir.servercosmetics.util;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import ua.zefir.servercosmetics.config.ConfigManager;

public class GUIUtils {
  public static void setUpButton(
      SimpleGui gui, ConfigManager.NavigationButton buttonConfig, Runnable callback) {
    setUpButton(gui, buttonConfig, callback, buttonConfig.slotIndex());
  }

  public static void setUpButton(
      SimpleGui gui,
      ConfigManager.NavigationButton buttonConfig,
      Runnable callback,
      int slotIndex) {
    if (buttonConfig != null) {
      ItemStack itemStack;
      if (buttonConfig.polymerModelData() != null) {
        itemStack = new ItemStack(buttonConfig.polymerModelData().item());
      } else {
        itemStack = new ItemStack(buttonConfig.baseItem());
      }

      GuiElementBuilder builder =
          new GuiElementBuilder(itemStack)
              .setName(buttonConfig.name())
              .setLore(buttonConfig.lore().stream().map(Utils::formatDisplayName).toList())
              .setCallback((index, clickType, actionType) -> callback.run());

      if (buttonConfig.polymerModelData() != null) {
        builder.setCustomModelData(buttonConfig.polymerModelData().value());
      }

      gui.setSlot(slotIndex, builder);
    }
  }
}
