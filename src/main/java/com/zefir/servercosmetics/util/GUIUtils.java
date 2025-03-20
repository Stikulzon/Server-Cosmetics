package com.zefir.servercosmetics.util;

import com.zefir.servercosmetics.ServerCosmetics;
import com.zefir.servercosmetics.config.ConfigManager;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class GUIUtils {
    public static void setUpButton(SimpleGui gui, Function<String, ConfigManager.NavigationButton> getConfigFunction, String buttonKey, Runnable callback) {
        ConfigManager.NavigationButton buttonConfig = getConfigFunction.apply(buttonKey);
        if (buttonConfig != null) {
            String itemString = buttonConfig.item().contains(":") ? buttonConfig.item() : "minecraft:" + buttonConfig.item().toLowerCase();

            PolymerModelData polymerModel = PolymerResourcePackUtils.requestModel(Registries.ITEM.get(Identifier.of(itemString)), Identifier.of(ServerCosmetics.MOD_ID, "ui/" + buttonConfig.textureName()));
            ItemStack itemStack = new ItemStack(polymerModel.item());

            GuiElementBuilder builder = new GuiElementBuilder(itemStack)
                    .setName(buttonConfig.name())
                    .setLore(buttonConfig.lore().stream().map(Utils::formatDisplayName).toList())
                    .setCallback((index, clickType, actionType) -> callback.run());

            if (buttonConfig.customModelData() >= 0) {
                builder.setCustomModelData(polymerModel.value());
            }

            gui.setSlot(buttonConfig.slotIndex(), builder);
        }
    }
}
