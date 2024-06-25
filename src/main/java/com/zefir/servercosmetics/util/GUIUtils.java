package com.zefir.servercosmetics.util;

import com.zefir.servercosmetics.config.ConfigManager;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class GUIUtils {
    public static void setUpButton(SimpleGui gui, Function<String, ConfigManager.NavigationButton> getConfigFunction, String buttonKey, Runnable callback) {
        ConfigManager.NavigationButton buttonConfig = getConfigFunction.apply(buttonKey);
        if (buttonConfig != null) {
            String itemString = buttonConfig.item().contains(":") ? buttonConfig.item() : "minecraft:" + buttonConfig.item().toLowerCase();

            GuiElementBuilder builder = new GuiElementBuilder(Registries.ITEM.get(Identifier.of(itemString)))
                    .setName(buttonConfig.name())
                    .setLore(buttonConfig.lore().stream().map(Utils::formatDisplayName).toList())
                    .setCallback((index, clickType, actionType) -> callback.run());

            if (buttonConfig.customModelData() >= 0) {
                builder.setCustomModelData(buttonConfig.customModelData());
            }

            gui.setSlot(buttonConfig.slotIndex(), builder);
        }
    }
}
