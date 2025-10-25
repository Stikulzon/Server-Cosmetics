package com.zefir.servercosmetics.gui;

import com.mojang.brigadier.context.CommandContext;
import com.zefir.servercosmetics.ServerCosmetics;
import com.zefir.servercosmetics.config.ItemSkinsGUIConfig;
import com.zefir.servercosmetics.gui.actions.ApplySkinAction;
import com.zefir.servercosmetics.gui.filters.PermissionFilter;
import com.zefir.servercosmetics.gui.filters.SelectedItemFilter;
import com.zefir.servercosmetics.gui.providers.ItemSkinProvider;
import com.zefir.servercosmetics.util.GUIUtils;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static com.zefir.servercosmetics.config.ConfigManager.ITEM_SKINS_GUI_CONFIG;

public class ItemSkinsGUI {
    public static int openItemSkinsGui(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFeedback(() -> Text.literal("This command can only be run by a player."), false);
            return 1;
        }

        try {
            ItemStack handStack = player.getMainHandStack();
            var config = ITEM_SKINS_GUI_CONFIG;
            var provider = new ItemSkinProvider();
            var action = new ApplySkinAction(handStack, ItemSkinsGUIConfig.getItemSlot());
            PagedItemDisplayGui gui = new PagedItemDisplayGui(player, config, provider, action) {
                @Override
                public boolean onAnyClick(int idx, ClickType ct, SlotActionType sa) {
                    if (idx >= this.getVirtualSize()) {
                        ItemStack newClicked = this.player.currentScreenHandler.getSlot(idx).getStack();
                        if (!newClicked.isEmpty()) {
                            GuiHelpers.sendPlayerScreenHandler(this.player);
                            var newProvider = new ItemSkinProvider();
                            var newAction = new ApplySkinAction(newClicked, ItemSkinsGUIConfig.getItemSlot());

                            setupDynamicSlots(this, newClicked);
                            this.reinitialize(newProvider, newAction);
                        }
                    }
                    return super.onAnyClick(idx, ct, sa);
                }
            };
            gui.getFilterManager().addFilter(
                    "permission",
                    new PermissionFilter(player),
                    config.getButtonConfig("filter.show-owned-skins-disabled"),
                    config.getButtonConfig("filter.show-owned-skins-enabled"),
                    false
            );
            gui.getFilterManager().addFilter(
                    "selected-item",
                    new SelectedItemFilter(),
                    config.getButtonConfig("filter.show-skins-for-selected-item-disabled"),
                    config.getButtonConfig("filter.show-skins-for-selected-item-enabled"),
                    true,
                    true
            );

            setupDynamicSlots(gui, handStack);

            gui.reinitialize(provider, action);

            gui.setLockPlayerInventory(true);
            gui.open();
        } catch (Exception e) {
            ctx.getSource().sendError(Text.literal("An error occurred opening the Item Skins GUI. See console for details."));
            ServerCosmetics.LOGGER.error("Failed to open item skins GUI for player {}", player.getName().getString(), e);
        }
        return 0;
    }


    private static void setupDynamicSlots(PagedItemDisplayGui gui, ItemStack targetStack) {
        if(targetStack.getItem() == Items.AIR || targetStack.isEmpty() || targetStack.getItem() == null) {
            gui.setSlot(ItemSkinsGUIConfig.getItemSlot(), new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.literal("Select item"))
                    .addLoreLine(Text.literal("Click on the item in your inventory below.")));
            ((SelectedItemFilter) (gui.getFilterManager().getFilter("selected-item").filter())).setSelectedItem(null);
            return;
        }

        gui.setSlot(ItemSkinsGUIConfig.getItemSlot(),
                new GuiElementBuilder(targetStack.copy())
                .setCallback(
                        () -> setupDynamicSlots(gui, ItemStack.EMPTY)
                )
        );

        ((SelectedItemFilter) (gui.getFilterManager().getFilter("selected-item").filter())).setSelectedItem(targetStack.getItem());

        GUIUtils.setUpButton(gui, ITEM_SKINS_GUI_CONFIG.getButtonConfig("removeSkin"), () -> {
                targetStack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, comp -> comp.apply(nbt -> nbt.remove("cosmeticItemId")));
                targetStack.remove(DataComponentTypes.CUSTOM_MODEL_DATA);

                gui.setSlot(ItemSkinsGUIConfig.getItemSlot(), targetStack.copy());
        });
    }
}