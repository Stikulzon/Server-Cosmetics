package com.zefir.servercosmetics.gui;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.zefir.servercosmetics.ServerCosmetics;
import com.zefir.servercosmetics.config.entries.CustomItemEntry;
import com.zefir.servercosmetics.config.entries.CustomItemRegistry;
import com.zefir.servercosmetics.database.DatabaseManager;
import com.zefir.servercosmetics.config.CosmeticsGUIConfig;
import com.zefir.servercosmetics.ext.CosmeticSlotExt;
import com.zefir.servercosmetics.gui.actions.EquipCosmeticAction;
import com.zefir.servercosmetics.gui.filters.PermissionFilter;
import com.zefir.servercosmetics.gui.providers.StandaloneCosmeticProvider;
import com.zefir.servercosmetics.util.GUIUtils;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;


public class CosmeticsGUI {

    public static int openGui(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFeedback(() -> Text.literal("This command can only be run by a player."), false);
            return 1;
        }

        try {
            var config = CosmeticsGUIConfig.get();
            var provider = new StandaloneCosmeticProvider();
            var action = new EquipCosmeticAction();

            PagedItemDisplayGui gui = new PagedItemDisplayGui(player, config, provider, action);

                gui.getFilterManager().addFilter(
                        "permission",
                        new PermissionFilter(),
                        config.getButtonConfig("filter.show-all-skins"),
                        config.getButtonConfig("filter.show-owned-skins"),
                        false
                );

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

            gui.open();

        } catch (Exception e) {
            ctx.getSource().sendError(Text.literal("An error occurred opening the Cosmetics GUI. See console for details."));
            ServerCosmetics.LOGGER.error("Failed to open cosmetics GUI for player {}", player.getName().getString(), e);
        }
        return 0;
    }

    public static int wearCosmeticById(CommandContext<ServerCommandSource> context) {
        final ServerPlayerEntity player;
        try {
            player = EntityArgumentType.getPlayer(context, "player");
        } catch (CommandSyntaxException e) {
            context.getSource().sendError(Text.literal("Invalid player specified."));
            return 1;
        }

        String id = StringArgumentType.getString(context, "cosmeticId");
        if (id == null || id.isEmpty()) {
            context.getSource().sendError(Text.literal("Invalid cosmetic ID."));
            return 1;
        }

        CustomItemEntry entry = CustomItemRegistry.getStandaloneCosmetic(id);

        if (entry == null) {
            context.getSource().sendFeedback(() -> Text.literal("Cosmetic not found with ID: " + id), false);
            return 1;
        }

        if (!Permissions.check(player, entry.permission())) {
            context.getSource().sendFeedback(() -> Text.literal("Selected player does not have permission to use this cosmetic."), false);
            return 1;
        }

        ItemStack cosmeticItem = entry.itemStack();
        if (cosmeticItem == null || cosmeticItem.isEmpty()) {
            context.getSource().sendError(Text.literal("Cosmetic item definition is empty or invalid."));
            return 1;
        }

        boolean isColorable = Items.LEATHER_HORSE_ARMOR.equals(
                Registries.ITEM.get(Identifier.tryParse(entry.baseItemForModel()))
        );

        if (isColorable) {
            ItemStack itemForColorPicker = cosmeticItem.copy();
            itemForColorPicker.remove(DataComponentTypes.DYED_COLOR);

            new ColorPickerComponent(player, itemForColorPicker, (coloredStack) -> {
                var finalEntry = new CustomItemEntry(
                        entry.id(),
                        entry.permission(),
                        entry.displayName(),
                        Collections.emptyList(),
                        coloredStack,
                        entry.type(),
                        entry.baseItemForModel()
                );
                new EquipCosmeticAction().execute(player, finalEntry, null);
                context.getSource().sendFeedback(() -> Text.literal("Equipped colored cosmetic: " + finalEntry.displayName().getString()), false);
            }).open();

        } else {
            new EquipCosmeticAction().execute(player, entry, null);
            context.getSource().sendFeedback(() -> Text.literal("Equipped cosmetic: " + entry.displayName().getString()), false);
        }
        return 0;
    }
}