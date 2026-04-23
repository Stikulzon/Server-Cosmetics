package ua.zefir.servercosmetics.util;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import ua.zefir.servercosmetics.config.ConfigManager;

public class GuiUtils {
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
      ItemStack itemStack = new ItemStack(buttonConfig.baseItem());
      if (buttonConfig.modelPath() != null) {
        itemStack.set(DataComponentTypes.ITEM_MODEL, buttonConfig.modelPath());
      }

      GuiElementBuilder builder =
          new GuiElementBuilder(itemStack)
              .setName(buttonConfig.name())
              .setLore(buttonConfig.lore().stream().map(Utils::formatDisplayName).toList())
              .setCallback((index, clickType, actionType) -> callback.run());

      if (buttonConfig.modelPath() != null) {
        builder.model(buttonConfig.modelPath());
      }

      gui.setSlot(slotIndex, builder);
    }
  }
}
