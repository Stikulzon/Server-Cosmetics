package ua.zefir.servercosmetics.util;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import ua.zefir.servercosmetics.config.ButtonConfig;

public class GuiUtils {
  public static void setUpButton(SimpleGui gui, ButtonConfig buttonConfig, Runnable callback) {
    setUpButton(gui, buttonConfig, callback, buttonConfig.slotIndex());
  }

  public static void setUpButton(
      SimpleGui gui, ButtonConfig buttonConfig, Runnable callback, int slotIndex) {
    if (buttonConfig != null) {
      ItemStack itemStack = new ItemStack(buttonConfig.baseItem());
      if (buttonConfig.modelPath() != null) {
        itemStack.set(DataComponents.ITEM_MODEL, buttonConfig.modelPath());
      }

      GuiElementBuilder builder =
          new GuiElementBuilder(itemStack)
              .setName(buttonConfig.name())
              .setLore(buttonConfig.lore().stream().map(Utils::formatDisplayName).toList())
              .setCallback((index, clickType, actionType, slotGui) -> callback.run());

      if (buttonConfig.modelPath() != null) {
        builder.model(buttonConfig.modelPath());
      }

      gui.setSlot(slotIndex, builder);
    }
  }
}
