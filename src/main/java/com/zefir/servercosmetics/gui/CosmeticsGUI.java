package com.zefir.servercosmetics.gui;

import com.mojang.brigadier.context.CommandContext;
import com.zefir.servercosmetics.ServerCosmetics;
import com.zefir.servercosmetics.config.ItemSkinsGUIConfig;
import com.zefir.servercosmetics.data.ItemType;
import com.zefir.servercosmetics.gui.actions.EquipCosmeticAction;
import com.zefir.servercosmetics.gui.filters.ItemTypeFilter;
import com.zefir.servercosmetics.gui.filters.PermissionFilter;
import com.zefir.servercosmetics.gui.providers.StandaloneCosmeticProvider;
import com.zefir.servercosmetics.util.GUIUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

import static com.zefir.servercosmetics.config.ConfigManager.COSMETICS_GUI_CONFIG;


public class CosmeticsGUI {

    public static int openGui(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFeedback(() -> Text.literal("This command can only be run by a player."), false);
            return 1;
        }

        try {
            var config = COSMETICS_GUI_CONFIG;
            var provider = new StandaloneCosmeticProvider();
            var action = new EquipCosmeticAction();

            PagedItemDisplayGui gui = new PagedItemDisplayGui(player, config, provider, action);

            gui.getFilterManager().addFilter(
                    "permission",
                    new PermissionFilter(player),
                    config.getButtonConfig("filter.show-owned-skins-disabled"),
                    config.getButtonConfig("filter.show-owned-skins-enabled"),
                    false
            );

            gui.getFilterManager().addFilter(
                    "hat",
                    new ItemTypeFilter(List.of(ItemType.HAT)),
                    config.getButtonConfig("filter.hats-disabled"),
                    config.getButtonConfig("filter.hats-enabled"),
                    true
            );

            gui.getFilterManager().addFilter(
                    "body-cosmetic",
                    new ItemTypeFilter(List.of(ItemType.BODY_COSMETIC)),
                    config.getButtonConfig("filter.body-cosmetics-disabled"),
                    config.getButtonConfig("filter.body-cosmetics-enabled"),
                    false
            );
            gui.getFilterManager().addFilter(
                    "chestplate",
                    new ItemTypeFilter(List.of(ItemType.HELMET, ItemType.CHESTPLATE, ItemType.LEGGINGS, ItemType.BOOTS, ItemType.HAT_BODY_COSMETIC, ItemType.CHESTPLATE_BODY_COSMETIC, ItemType.LEGGINGS_BODY_COSMETIC, ItemType.BOOTS_BODY_COSMETIC)),
                    config.getButtonConfig("filter.chestplate-cosmetics-disabled"),
                    config.getButtonConfig("filter.chestplate-cosmetics-enabled"),
                    false
            );

            GUIUtils.setUpButton(gui, config.getButtonConfig("removeSkin"), () -> {
                gui.close();
                for (ItemType type : gui.getFilterManager().getTargetTypes()) {
                    action.execute(player, ItemStack.EMPTY, type);
                }
            });

            gui.setSlot(ItemSkinsGUIConfig.getItemSlot(), new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("Select an Item"))
                    .addLoreLine(Text.literal("Click an item in your inventory below.")));

            gui.reinitialize(provider, action);
            gui.open();

        } catch (Exception e) {
            ctx.getSource().sendError(Text.literal("An error occurred opening the Cosmetics GUI. See console for details."));
            ServerCosmetics.LOGGER.error("Failed to open cosmetics GUI for player {}", player.getName().getString(), e);
        }
        return 0;
    }
}