package com.zefir.servercosmetics.gui;

import com.mojang.brigadier.context.CommandContext;
import com.zefir.servercosmetics.config.ItemSkinsGUIConfig;
import com.zefir.servercosmetics.util.GUIUtils;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

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

    private static void drawItemSkins(ServerPlayerEntity player, MutableInt currentItemStack, MutableInt filterRegime){
        var creator = new MutableObject<Supplier<SimpleGui>>();
        var num = new MutableInt();

        creator.setValue(() -> {
            num.increment();
            int pageNumber = num.getValue();
            SimpleGui gui = getSimpleGui(player, currentItemStack, filterRegime);
            gui.setTitle(ItemSkinsGUIConfig.getItemSkinsGuiName());
            var previousGui = GuiHelpers.getCurrentGui(player);
            var next = new MutableObject<SimpleGui>();
            int[] cosmeticSlots = ItemSkinsGUIConfig.getCosmeticSlots();

            // filtering all/unlocked
            if(currentItemStack.getValue() != -1) {
                Map<Integer, AbstractMap.SimpleEntry<String, AbstractMap.SimpleEntry<String, ItemStack>>> allItemSkinsMap = ItemSkinsGUIConfig.getItemSkinsItems(player.currentScreenHandler.getSlot(currentItemStack.getValue()).getStack().getItem());

                if (allItemSkinsMap != null) {
                    Map<Integer, AbstractMap.SimpleEntry<String, AbstractMap.SimpleEntry<String, ItemStack>>> itemSkinsMap = new HashMap<>();
                    if (filterRegime.getValue() == 0){
                        itemSkinsMap = allItemSkinsMap;
                    } else {
                        int itemSkinId = 0;
                        for (Map.Entry<Integer, AbstractMap.SimpleEntry<String, AbstractMap.SimpleEntry<String, ItemStack>>> entry : allItemSkinsMap.entrySet()) {
                            AbstractMap.SimpleEntry<String, AbstractMap.SimpleEntry<String, ItemStack>> skinEntry = entry.getValue();
                            String skinId = skinEntry.getKey();
                            String permission = skinEntry.getValue().getKey();
                            ItemStack itemStack = skinEntry.getValue().getValue();

                            // Check if the player has permission for this item (unlocked)
                            if (Permissions.check(player, permission, 4)) {
                                itemSkinsMap.put(itemSkinId, new AbstractMap.SimpleEntry<>(skinId, new AbstractMap.SimpleEntry<>(permission, itemStack.copy())));
                                itemSkinId++;
                            }
                        }
                    }

                    for (int i = 0; i < Math.min(Math.min(itemSkinsMap.size(), cosmeticSlots.length), (itemSkinsMap.size() - cosmeticSlots.length * (pageNumber - 1))); i++) {
                        int finalI = Math.min(i + (cosmeticSlots.length * (pageNumber - 1)), itemSkinsMap.size() - 1);
                        ItemStack is = itemSkinsMap.get(finalI).getValue().getValue().copy(); // IMPORTANT TO USE .copy()!!!

                        String permission = itemSkinsMap.get(finalI).getValue().getKey();
                        if (Permissions.check(player, permission, 4)) {
                            // loading unlocked item
                            Map<Integer, AbstractMap.SimpleEntry<String, AbstractMap.SimpleEntry<String, ItemStack>>> finalItemSkinsMap = itemSkinsMap;
                            gui.setSlot(cosmeticSlots[i], GuiElementBuilder.from(is)
                                    .addLoreLine(ItemSkinsGUIConfig.getMessageUnlocked())
                                    .setCallback((e) -> {
                                        int customModelData = Objects.requireNonNull(is.copy().getNbt()).getInt("CustomModelData");
                                        String itemSkinsID = finalItemSkinsMap.get(finalI).getKey();
                                        ItemStack playerItemsStack = player.currentScreenHandler.getSlot(currentItemStack.getValue()).getStack();
                                        NbtCompound nbtData = Objects.requireNonNull(playerItemsStack.getNbt());
                                        nbtData.putInt("CustomModelData", customModelData);
                                        nbtData.putString("itemSkinsID", itemSkinsID);
                                        playerItemsStack.setNbt(nbtData);

                                        player.currentScreenHandler.getSlot(currentItemStack.getValue()).setStack(playerItemsStack);
                                        gui.setSlot(ItemSkinsGUIConfig.getItemSlot(), GuiElementBuilder.from(playerItemsStack));
                                    })
                            );
                        } else {
                                // loading locked item
                                gui.setSlot(cosmeticSlots[i], GuiElementBuilder.from(is)
                                        .addLoreLine(ItemSkinsGUIConfig.getMessageLocked()));
                        }
                    }
                    if (cosmeticSlots.length < (itemSkinsMap.size() - cosmeticSlots.length * (pageNumber - 1))) {
                        GUIUtils.setUpButton(gui, ItemSkinsGUIConfig::getButtonConfig, "next", () -> {
                            if (next.getValue() == null) {
                                next.setValue(creator.getValue().get());
                            }
                            next.getValue().open();
                        });
                    }

                    if (num.getValue() > 1 && previousGui != null) {
                        GUIUtils.setUpButton(gui, ItemSkinsGUIConfig::getButtonConfig, "previous", previousGui::open);
                    }
                }
                gui.setSlot(ItemSkinsGUIConfig.getItemSlot(), GuiElementBuilder.from(player.currentScreenHandler.getSlot(currentItemStack.getValue()).getStack()));

                GUIUtils.setUpButton(gui, ItemSkinsGUIConfig::getButtonConfig, "removeItem", () -> {
                    ItemStack playerItemsStack = player.playerScreenHandler.getSlot(currentItemStack.getValue() - 53 + 8).getStack();
                    NbtCompound nbtData = playerItemsStack.getNbt();
                    if (nbtData != null) {
                        nbtData.remove("CustomModelData");
                        nbtData.remove("itemSkinsID");
                        playerItemsStack.setNbt(nbtData);

                        player.currentScreenHandler.getSlot(currentItemStack.getValue()).setStack(playerItemsStack);
                    }
                });
            }

            if(filterRegime.getValue() == 0) {
                GUIUtils.setUpButton(gui, ItemSkinsGUIConfig::getButtonConfig, "skinFilter.show-all-skins", () -> {
                    filterRegime.setValue(1);
                    drawItemSkins(player, currentItemStack, filterRegime);
                });
            } else if (filterRegime.getValue() == 1) {
                GUIUtils.setUpButton(gui, ItemSkinsGUIConfig::getButtonConfig, "skinFilter.show-owned-skins", () -> {
                    filterRegime.setValue(0);
                    drawItemSkins(player, currentItemStack, filterRegime);
                });
            }



            if(ItemSkinsGUIConfig.getIsPageIndicatorEnabled()) {
                GUIUtils.setUpButton(gui, ItemSkinsGUIConfig::getButtonConfig, "pageIndicator", () -> {
                });
            }

            return gui;
        });
        creator.getValue().get().open();
    }

    @NotNull
    private static SimpleGui getSimpleGui(ServerPlayerEntity player, MutableInt currentItemStack, MutableInt filterRegime) {
        SimpleGui gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, false) {
            @Override
            public boolean onClick(int index, ClickType type, SlotActionType action, GuiElementInterface element) {
                return super.onClick(index, type, action, element);
            }

            @Override
            public boolean onAnyClick(int index, ClickType type, SlotActionType action){
                if(index>53) {
                    GuiHelpers.sendPlayerScreenHandler(this.player);
                    currentItemStack.setValue(index);
                    drawItemSkins(player, currentItemStack, filterRegime);
                }
                return true;
            }
        };
        gui.setLockPlayerInventory(true);
        return gui;
    }
}