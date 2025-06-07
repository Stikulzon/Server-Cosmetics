package com.zefir.servercosmetics.gui;

import com.mojang.brigadier.context.CommandContext;
import com.zefir.servercosmetics.config.ItemSkinsGUIConfig;
import com.zefir.servercosmetics.config.entries.CustomItemEntry;
import com.zefir.servercosmetics.util.GUIUtils;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ItemSkinsGUI {
    public static int openIsGui(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFeedback(() -> Text.literal("Player not find"), false);
            return 1;
        }

        try {
            var currentItemStack = new MutableInt(-1);
            var filterRegime = new MutableInt(0);
            drawItemSkins(player, currentItemStack, filterRegime);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    private static void drawItemSkins(ServerPlayerEntity player, MutableInt currentItemStackSlotIndex, MutableInt filterRegime) {
        var pageCreator = new MutableObject<Supplier<SimpleGui>>();
        var currentPageNumber = new MutableInt(0);

        pageCreator.setValue(() -> {
            currentPageNumber.increment();
            int pageNumForCalc = currentPageNumber.getValue() - 1;

            SimpleGui gui = createBaseGui(player, currentItemStackSlotIndex, filterRegime);
            gui.setTitle(GuiTextures.ITEM_SKINS_MENU.apply(ItemSkinsGUIConfig.get().getGuiName()));

            var previousGuiScreen = GuiHelpers.getCurrentGui(player);
            var nextGuiScreen = new MutableObject<SimpleGui>();

            int[] displaySlots = ItemSkinsGUIConfig.getCosmeticSlots();
            int itemsPerPage = displaySlots.length;

            // --- Item Skin Loading and Filtering ---
            if (currentItemStackSlotIndex.getValue() != -1) {
                ItemStack targetPlayerItemStack = player.currentScreenHandler.getSlot(currentItemStackSlotIndex.getValue()).getStack();
                if (targetPlayerItemStack == null || targetPlayerItemStack.isEmpty()) {

                } else {
                    Item targetItem = targetPlayerItemStack.getItem();

                    Map<String, CustomItemEntry> allSkinsForMaterial = ItemSkinsGUIConfig.getAllSkinsForMaterial(targetItem);

                    List<CustomItemEntry> filteredAndSortedSkins;

                    if (filterRegime.getValue() == 0) {
                        filteredAndSortedSkins = new ArrayList<>(allSkinsForMaterial.values());
                    } else { // Show owned
                        filteredAndSortedSkins = allSkinsForMaterial.values().stream()
                                .filter(entry -> Permissions.check(player, entry.permission()))
                                .collect(Collectors.toList());
                    }

                    filteredAndSortedSkins.sort(Comparator.comparing(CustomItemEntry::id));

                    // Pagination logic
                    int totalSkinsToShow = filteredAndSortedSkins.size();
                    int startIndex = pageNumForCalc * itemsPerPage;

                    for (int i = 0; i < itemsPerPage; i++) {
                        int skinIndexInList = startIndex + i;
                        if (skinIndexInList < totalSkinsToShow) {
                            CustomItemEntry skinEntry = filteredAndSortedSkins.get(skinIndexInList);
                            ItemStack displayStack = skinEntry.itemStack().copy();

                            GuiElementBuilder elementBuilder = GuiElementBuilder.from(displayStack);
                            boolean hasPermission = Permissions.check(player, skinEntry.permission());

                            if (hasPermission) {
                                elementBuilder.addLoreLine(ItemSkinsGUIConfig.get().getMessageUnlocked());
                                elementBuilder.setCallback((clickIndex, clickType, slotActionType) -> {
                                    // Apply the skin
                                    ItemStack playerItemToSkin = player.currentScreenHandler.getSlot(currentItemStackSlotIndex.getValue()).getStack();
                                    if (playerItemToSkin != null && !playerItemToSkin.isEmpty()) {
                                        if (playerItemToSkin.getItem() == targetItem) {
                                            playerItemToSkin.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, comp -> comp.apply(nbt -> nbt.putString("cosmeticItemId", skinEntry.id())));
                                            playerItemToSkin.set(DataComponentTypes.CUSTOM_MODEL_DATA, displayStack.getOrDefault(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(0)));

                                            player.currentScreenHandler.getSlot(currentItemStackSlotIndex.getValue()).setStack(playerItemToSkin);

                                            gui.setSlot(ItemSkinsGUIConfig.getItemSlot(), GuiElementBuilder.from(playerItemToSkin.copy()));
                                        } else {
                                            player.sendMessage(Text.literal("The item in the slot changed! Please re-select."), false);
                                            gui.close();
                                        }
                                    }
                                });
                            } else {
                                elementBuilder.addLoreLine(ItemSkinsGUIConfig.get().getMessageLocked());
                            }
                            gui.setSlot(displaySlots[i], elementBuilder);
                        }
                    }

                    // Next button
                    if ((pageNumForCalc + 1) * itemsPerPage < totalSkinsToShow) {
                        GUIUtils.setUpButton(gui, ItemSkinsGUIConfig.get().getButtonConfig("next"), () -> {
                            if (nextGuiScreen.getValue() == null) {
                                nextGuiScreen.setValue(pageCreator.getValue().get());
                            }
                            nextGuiScreen.getValue().open();
                        });
                    }

                    // Previous button
                    if (pageNumForCalc > 0 && previousGuiScreen != null) {
                        GUIUtils.setUpButton(gui, ItemSkinsGUIConfig.get().getButtonConfig("previous"), () -> {
                            if (previousGuiScreen instanceof SimpleGui && previousGuiScreen != gui) {
                                previousGuiScreen.open();
                            } else {
                                gui.close();
                            }
                        });
                    }
                    gui.setSlot(ItemSkinsGUIConfig.getItemSlot(), GuiElementBuilder.from(targetPlayerItemStack.copy()));
                }


                // Remove Skin Button
                GUIUtils.setUpButton(gui, ItemSkinsGUIConfig.get().getButtonConfig("removeItem"), () -> {
                    ItemStack playerItemToModify = player.currentScreenHandler.getSlot(currentItemStackSlotIndex.getValue()).getStack();
                    if (playerItemToModify != null && !playerItemToModify.isEmpty()) {
                        playerItemToModify.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, comp -> comp.apply(nbt -> nbt.remove("cosmeticItemId")));
                        playerItemToModify.remove(DataComponentTypes.CUSTOM_MODEL_DATA);
                        player.currentScreenHandler.getSlot(currentItemStackSlotIndex.getValue()).setStack(playerItemToModify);
                        gui.setSlot(ItemSkinsGUIConfig.getItemSlot(), GuiElementBuilder.from(playerItemToModify.copy()));
                        drawItemSkins(player, currentItemStackSlotIndex, filterRegime);
                    }
                });
            } else {
                gui.setSlot(ItemSkinsGUIConfig.getItemSlot(),
                        GuiElementBuilder.from(new ItemStack(net.minecraft.item.Items.BARRIER))
                                .setName(Text.literal("Place an item here"))
                                .addLoreLine(Text.literal("Click an item in your inventory below.")));
            }

            // Filter Toggle Button
            if (filterRegime.getValue() == 0) {
                GUIUtils.setUpButton(gui, ItemSkinsGUIConfig.get().getButtonConfig("skinFilter.show-all-skins"), () -> {
                    filterRegime.setValue(1);
                    currentPageNumber.setValue(0);
                    drawItemSkins(player, currentItemStackSlotIndex, filterRegime);
                });
            } else {
                GUIUtils.setUpButton(gui, ItemSkinsGUIConfig.get().getButtonConfig("skinFilter.show-owned-skins"), () -> {
                    filterRegime.setValue(0);
                    currentPageNumber.setValue(0);
                    drawItemSkins(player, currentItemStackSlotIndex, filterRegime);
                });
            }

            // Page Indicator
            if (ItemSkinsGUIConfig.get().isPageIndicatorEnabled()) {/*
                int totalPages = 1; // Default to 1 page
                if (currentItemStackSlotIndex.getValue() != -1 && !player.currentScreenHandler.getSlot(currentItemStackSlotIndex.getValue()).getStack().isEmpty()) {
                    Item targetItem = player.currentScreenHandler.getSlot(currentItemStackSlotIndex.getValue()).getStack().getItem();
                    Map<String, CustomItemEntry> allSkins = ItemSkinsGUIConfig.getAllSkinsForMaterial(targetItem);
                    List<CustomItemEntry> relevantSkins;
                    if (filterRegime.getValue() == 0) {
                        relevantSkins = new ArrayList<>(allSkins.values());
                    } else {
                        relevantSkins = allSkins.values().stream()
                                .filter(entry -> Permissions.check(player, entry.getPermission()))
                                .collect(Collectors.toList());
                    }
                    if (!relevantSkins.isEmpty()) {
                        totalPages = (int) Math.ceil((double) relevantSkins.size() / itemsPerPage);
                    }
                }
                final int finalTotalPages = Math.max(1, totalPages); // Ensure at least 1 page*/

            if(ItemSkinsGUIConfig.get().isPageIndicatorEnabled()) {
                GUIUtils.setUpButton(gui, ItemSkinsGUIConfig.get().getButtonConfig("pageIndicator"), () -> {});
            }
            }

            return gui;
        });

        pageCreator.getValue().get().open();
    }

    @NotNull
    private static SimpleGui createBaseGui(ServerPlayerEntity player, MutableInt currentItemStackSlotIndex, MutableInt filterRegime) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false) {
            @Override
            public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
                return super.onClick(index, type, action, element);
            }

            @Override
            public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
                if (index >= 54 && index < 54 + 36) {
                    ItemStack clickedStack = this.player.currentScreenHandler.getSlot(index).getStack();
                    if (clickedStack != null && !clickedStack.isEmpty()) {
                        // GuiHelpers.sendPlayerScreenHandler(this.player);
                        currentItemStackSlotIndex.setValue(index);
                        drawItemSkins(player, currentItemStackSlotIndex, filterRegime);
                        return true;
                    }
                }
                return super.onAnyClick(index, type, action);
            }

            @Override
            public void onClose() {
                super.onClose();
            }
        };
        gui.setLockPlayerInventory(true);
        return gui;
    }
}