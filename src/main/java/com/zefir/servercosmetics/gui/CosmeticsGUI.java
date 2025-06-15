package com.zefir.servercosmetics.gui;

import com.mojang.brigadier.context.CommandContext;
import com.zefir.servercosmetics.ServerCosmetics;
import com.zefir.servercosmetics.config.entries.ItemType;
import com.zefir.servercosmetics.database.DatabaseManager;
import com.zefir.servercosmetics.ext.CosmeticSlotExt;
import com.zefir.servercosmetics.gui.actions.EquipCosmeticAction;
import com.zefir.servercosmetics.gui.filters.ItemTypeFilter;
import com.zefir.servercosmetics.gui.filters.PermissionFilter;
import com.zefir.servercosmetics.gui.providers.StandaloneCosmeticProvider;
import com.zefir.servercosmetics.util.GUIUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Arrays;

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
                    config.getButtonConfig("filter.show-all-skins"),
                    config.getButtonConfig("filter.show-owned-skins"),
                    false
            );

            gui.getFilterManager().addFilter(
                    "hat",
                    new ItemTypeFilter(ItemType.HAT),
                    config.getButtonConfig("filter.hats-disabled"),
                    config.getButtonConfig("filter.hats-enabled"),
                    true
            );

            gui.getFilterManager().addFilter(
                    "body-cosmetic",
                    new ItemTypeFilter(ItemType.BODY_COSMETIC),
                    config.getButtonConfig("filter.body-cosmetics-disabled"),
                    config.getButtonConfig("filter.body-cosmetics-enabled"),
                    false
            );

            gui.getFilterManager().canBeActiveOnlyOne(Arrays.asList("body-cosmetic", "hat"));

            GUIUtils.setUpButton(gui, config.getButtonConfig("removeSkin"), () -> {
                gui.close();
                DatabaseManager.setHeadCosmetics(player.getUuid(), ItemStack.EMPTY);
                ((CosmeticSlotExt) player.playerScreenHandler).setHeadCosmetics(ItemStack.EMPTY);
                player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(
                        player.playerScreenHandler.syncId,
                        player.playerScreenHandler.nextRevision(),
                        5, // Head Slot
                        ItemStack.EMPTY
                ));
            });

            gui.reinitialize(provider, action);
            gui.open();

        } catch (Exception e) {
            ctx.getSource().sendError(Text.literal("An error occurred opening the Cosmetics GUI. See console for details."));
            ServerCosmetics.LOGGER.error("Failed to open cosmetics GUI for player {}", player.getName().getString(), e);
        }
        return 0;
    }
}