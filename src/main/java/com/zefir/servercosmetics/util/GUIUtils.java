package com.zefir.servercosmetics.util;

import com.zefir.servercosmetics.config.ConfigManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;

public class GUIUtils {
    public static void setUpButton(SimpleGui gui, ConfigManager.NavigationButton buttonConfig, Runnable callback) {
        if (buttonConfig != null) {
            ItemStack itemStack;
            if(buttonConfig.polymerModelData() != null) {
                itemStack = new ItemStack(buttonConfig.polymerModelData().item());
            } else {
                itemStack = new ItemStack(buttonConfig.baseItem());
            }

            GuiElementBuilder builder = new GuiElementBuilder(itemStack)
                    .setName(buttonConfig.name())
                    .setLore(buttonConfig.lore().stream().map(Utils::formatDisplayName).toList())
                    .setCallback((index, clickType, actionType) -> callback.run());

            if (buttonConfig.polymerModelData() != null) {
                builder.setCustomModelData(buttonConfig.polymerModelData().value());
            }

            gui.setSlot(buttonConfig.slotIndex(), builder);
        }
    }
}
