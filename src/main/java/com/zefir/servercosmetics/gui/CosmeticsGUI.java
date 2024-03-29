package com.zefir.servercosmetics.gui;

import com.mojang.brigadier.context.CommandContext;
import com.zefir.servercosmetics.CosmeticsData;
import com.zefir.servercosmetics.config.CosmeticsGUIConfig;
import com.zefir.servercosmetics.ext.CosmeticSlotExt;
import com.zefir.servercosmetics.util.GUIUtils;
import com.zefir.servercosmetics.util.IEntityDataSaver;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SignGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

public class CosmeticsGUI {

    public static int openGui(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFeedback(() -> Text.literal("Player not find"), false);
            return 1;
        }

        try {
            var filterRegime = new MutableInt(0);
            drawCosmeticItems(player, filterRegime);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
    private static void drawCosmeticItems(ServerPlayerEntity player, MutableInt filterRegime){
        var num = new MutableInt();
        var creator = new MutableObject<Supplier<SimpleGui>>();

        creator.setValue(() -> {
            num.increment();
            int pageNumber = num.getValue();
            var previousGui = GuiHelpers.getCurrentGui(player);
            var gui = new SimpleGui(ScreenHandlerType.GENERIC_9X6, player, CosmeticsGUIConfig.getReplaceInventory());
            var next = new MutableObject<SimpleGui>();
            gui.setTitle(CosmeticsGUIConfig.getCosmeticsGUIName());
            Map<Integer, AbstractMap.SimpleEntry<String, ItemStack>> allCosmeticsItemsMap = CosmeticsGUIConfig.getAllCosmeticItems();
            Map<Integer, AbstractMap.SimpleEntry<String, ItemStack>> cosmeticsItemsMap;

            // filtering all/unlocked
            if (filterRegime.getValue() == 0){
                cosmeticsItemsMap = allCosmeticsItemsMap;
            } else {
                cosmeticsItemsMap = new HashMap<>();
                int itemId = 0;
                for (Map.Entry<Integer, AbstractMap.SimpleEntry<String, ItemStack>> entry : allCosmeticsItemsMap.entrySet()) {
                    AbstractMap.SimpleEntry<String, ItemStack> itemEntry = entry.getValue();
                    String permission = itemEntry.getKey();
                    ItemStack itemStack = itemEntry.getValue();

                    // Check if the player has permission for this item (unlocked)
                    if (Permissions.check(player, permission)) {
                        cosmeticsItemsMap.put(itemId, new AbstractMap.SimpleEntry<>(permission, itemStack.copy()));
                        itemId++;
                    }
                }
            }

            for (int i = 0; i < Math.min(Math.min(cosmeticsItemsMap.size(), CosmeticsGUIConfig.getCosmeticSlots().length), (cosmeticsItemsMap.size() - CosmeticsGUIConfig.getCosmeticSlots().length * (pageNumber - 1))); i++) {
                int finalI = Math.min(i + (CosmeticsGUIConfig.getCosmeticSlots().length * (pageNumber - 1)), cosmeticsItemsMap.size() - 1);
                ItemStack is = cosmeticsItemsMap.get(finalI).getValue().copy(); // IMPORTANT TO USE .copy()!!!

                String permission = cosmeticsItemsMap.get(finalI).getKey();
                if (Permissions.check(player, permission)) {
                    // loading unlocked item
                    gui.setSlot(CosmeticsGUIConfig.getCosmeticSlots()[i], GuiElementBuilder.from(is)
                            .addLoreLine(CosmeticsGUIConfig.getTextUnlocked())
                            .setCallback(() -> {
                                if (Objects.equals(is.getItem().toString(), "leather_horse_armor")) {

                                    colorPicker(player, is);
                                } else {
                                    gui.close();
                                    CosmeticsData.setHeadCosmetics((IEntityDataSaver) player, is);
                                    ((CosmeticSlotExt) player.playerScreenHandler).setHeadCosmetics(is);
                                    player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(player.playerScreenHandler.syncId, player.playerScreenHandler.nextRevision(), 5, is));
                                }
                            })
                    );
                } else {
                    // loading locked item
                    gui.setSlot(CosmeticsGUIConfig.getCosmeticSlots()[i], GuiElementBuilder.from(is)
                            .addLoreLine(CosmeticsGUIConfig.getTextLocked()));
                }
            }

            if(CosmeticsGUIConfig.getCosmeticSlots().length < (cosmeticsItemsMap.size() - CosmeticsGUIConfig.getCosmeticSlots().length*(pageNumber-1))) {
                GUIUtils.setUpButton(gui, CosmeticsGUIConfig::getButtonConfig, "next", () -> {
                    if (next.getValue() == null) {
                        next.setValue(creator.getValue().get());
                    }
                    next.getValue().open();
                });
            }

            if (num.getValue() > 1 && previousGui != null) {
                GUIUtils.setUpButton(gui, CosmeticsGUIConfig::getButtonConfig, "previous", previousGui::open);
            }

            GUIUtils.setUpButton(gui, CosmeticsGUIConfig::getButtonConfig, "removeItem", () -> {
                gui.close();
                CosmeticsData.setHeadCosmetics((IEntityDataSaver) player, ItemStack.EMPTY);
                ((CosmeticSlotExt) player.playerScreenHandler).setHeadCosmetics(ItemStack.EMPTY);
                player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(player.playerScreenHandler.syncId, player.playerScreenHandler.nextRevision(), 5, ItemStack.EMPTY));
            });

            if(filterRegime.getValue() == 0) {
                GUIUtils.setUpButton(gui, CosmeticsGUIConfig::getButtonConfig, "cosmeticFilter.show-all-skins", () -> {
                    filterRegime.setValue(1);
                    drawCosmeticItems(player, filterRegime);
                });
            } else if (filterRegime.getValue() == 1) {
                GUIUtils.setUpButton(gui, CosmeticsGUIConfig::getButtonConfig, "cosmeticFilter.show-owned-skins", () -> {
                    filterRegime.setValue(0);
                    drawCosmeticItems(player, filterRegime);
                });
            }

            if(CosmeticsGUIConfig.getIsPageIndicatorEnabled()) {
                GUIUtils.setUpButton(gui, CosmeticsGUIConfig::getButtonConfig, "pageIndicator", () -> {});
                gui.setSlot(53, new GuiElementBuilder(Items.STICK).setCount(pageNumber));
            }


            return gui;
        });

        creator.getValue().get().open();
    }

    private static void colorPicker(ServerPlayerEntity player, ItemStack hatItemStack) {

        try {

            var creator = new MutableObject<Supplier<SimpleGui>>();
            creator.setValue(() -> {
                var selectedColorSlot = new MutableInt();
                var saturation = new MutableFloat(100F);
                var isAlreadyGenerated = new MutableBoolean(false);
                var viewSwitch = new MutableBoolean(true);

                var gui = new SimpleGui(ScreenHandlerType.GENERIC_9X5, player, true);

                gui.setTitle(CosmeticsGUIConfig.getColorPickerGUIName());

                gui.setSlot(CosmeticsGUIConfig.getColorInputSlot(), GuiElementBuilder.from(hatItemStack));

                drawColorSlots(gui, hatItemStack, isAlreadyGenerated, viewSwitch, selectedColorSlot, saturation, player);

                GUIUtils.setUpButton(gui, CosmeticsGUIConfig::getButtonConfig, "decreaseBrightness", () -> {
                    if (saturation.getValue() > CosmeticsGUIConfig.getSaturationAdjustmentValue()) {
                        saturation.subtract(CosmeticsGUIConfig.getSaturationAdjustmentValue());
                        saturation.setValue(Math.max(saturation.getValue(), 0F));
                        drawGradientSlots(gui, selectedColorSlot, hatItemStack, saturation, player);
                    }
                });

                GUIUtils.setUpButton(gui, CosmeticsGUIConfig::getButtonConfig, "increaseBrightness", () -> {
                    if (saturation.getValue() < 100F) {
                        saturation.add(CosmeticsGUIConfig.getSaturationAdjustmentValue());
                        saturation.setValue(Math.min(saturation.getValue(), 100F));
                        drawGradientSlots(gui, selectedColorSlot, hatItemStack, saturation, player);
                    }
                });

                GUIUtils.setUpButton(gui, CosmeticsGUIConfig::getButtonConfig, "toggleColorView", () -> {
                    viewSwitch.setValue(!viewSwitch.getValue());
                    drawColorSlots(gui, hatItemStack, isAlreadyGenerated, viewSwitch, selectedColorSlot, saturation, player);
                });

                GUIUtils.setUpButton(gui, CosmeticsGUIConfig::getButtonConfig, "enterColor", () -> colorInput(player, hatItemStack));

                return gui;
            });

            creator.getValue().get().open();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static void drawColorSlots(SimpleGui gui,
                                       ItemStack hatItemStack,
                                       MutableBoolean isAlreadyGenerated,
                                       MutableBoolean viewSwitch,
                                       MutableInt selectedColorSlot,
                                       MutableFloat saturation,
                                       ServerPlayerEntity player){
        ItemStack is;
        if(viewSwitch.getValue()) {
            is = new ItemStack(Registries.ITEM.get(new Identifier("minecraft", "leather_horse_armor")));
            NbtCompound nbtData = is.getOrCreateNbt();
            nbtData.putInt("CustomModelData", CosmeticsGUIConfig.getPaintItemCMD());
        } else {
            is = hatItemStack;
        }

        int[] colorSlots = CosmeticsGUIConfig.getColorSlots();
        String[] colorHexValues = CosmeticsGUIConfig.getColorHexValues();
        for (int i = 0; i < colorSlots.length; i++) {

            int decimal = Integer.parseInt(colorHexValues[i], 16);

            NbtCompound nbtDataDisplay = is.getOrCreateSubNbt("display");
            nbtDataDisplay.putInt("color", decimal);

            NbtCompound nbtDataDisplay2 = is.getOrCreateNbt();
            nbtDataDisplay2.putInt("index", i);

            gui.setSlot(colorSlots[i], GuiElementBuilder.from(is)
                    .setCallback((index, clickType, actionType) -> {
                        selectedColorSlot.setValue(index);

                        drawGradientSlots(gui, selectedColorSlot, hatItemStack, saturation, player);
                    })
            );

            if(!isAlreadyGenerated.getValue()) {
                selectedColorSlot.setValue(colorSlots[i]);
                drawGradientSlots(gui, selectedColorSlot, hatItemStack, saturation, player);
                isAlreadyGenerated.setValue(true);
            }

        }
    }

    private static void drawGradientSlots(SimpleGui gui,
                                          MutableInt selectedColorSlot,
                                          ItemStack hatItemStack,
                                          MutableFloat saturation,
                                          ServerPlayerEntity player){

        ItemStack currentColorItemStack = new ItemStack(Items.LEATHER_HORSE_ARMOR);
        currentColorItemStack.setNbt(Objects.requireNonNull(gui.getSlot(selectedColorSlot.getValue()).getItemStack().copy().getNbt()).copy());
        assert currentColorItemStack.getNbt() != null;
        final int slotIndex = currentColorItemStack.getNbt().getInt("index");

        int decimal = (Integer.parseInt(CosmeticsGUIConfig.getColorHexValues()[slotIndex], 16));


        Color color = new Color(decimal);

        float[] hsv = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);

        int[] colorGradientSlots = CosmeticsGUIConfig.getColorGradientSlots();
        for (int j = 0; j < colorGradientSlots.length; j++) {

            float luminance = Math.min(
                    (1F / (7F+1F) * ((float)j + 1)),
                    1F);

            Color color2;
            if(color.getRed() == color.getGreen() && color.getRed() == color.getBlue()) {
                color2 = new Color(Color.HSBtoRGB(hsv[0], hsv[1], saturation.getValue() / 100F));
            } else {
                color2 = new Color(Color.HSBtoRGB(hsv[0], 1F - luminance, saturation.getValue() / 100F));
            }
            int[] rgb = {color2.getRed(), color2.getGreen(), color2.getBlue()};


            ItemStack is = currentColorItemStack.copy();
            int decimal2 = rgb[0];
            decimal2 = (decimal2 << 8) + rgb[1];
            decimal2 = (decimal2 << 8) + rgb[2];
            NbtCompound nbtData2 = is.getNbt();
            assert nbtData2 != null;
            NbtCompound nbtData3 = nbtData2.getCompound("display");
            assert nbtData3 != null;
            nbtData3.putInt("color", decimal2);
            nbtData2.put("display", nbtData3);
            is.setNbt(nbtData2);



            int finalDecimal2 = decimal2;
            gui.setSlot(colorGradientSlots[j], GuiElementBuilder.from(is)

                    .setCallback(() -> {
                        ItemStack is4 = hatItemStack.copy();
                        NbtCompound nbtData4 = is4.getOrCreateSubNbt("display");
                        nbtData4.putInt("color", finalDecimal2);

                        gui.setSlot(CosmeticsGUIConfig.getColorOutputSlot(), GuiElementBuilder.from(is4)

                                .setCallback(() -> {
                                    gui.close();
                                    CosmeticsData.setHeadCosmetics((IEntityDataSaver) player, is4);
                                    ((CosmeticSlotExt) player.playerScreenHandler).setHeadCosmetics(is4);
                                    player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(player.playerScreenHandler.syncId, player.playerScreenHandler.nextRevision(), 5, is4));
                                })
                        );
                    })
            );
        }
    }

    private static void colorInput(ServerPlayerEntity player, ItemStack is) {
        try {
            SignGui gui = new SignGui(player) {

                {
                    this.setSignType(Registries.BLOCK.get(new Identifier(CosmeticsGUIConfig.getSignType())));
                    this.setColor(CosmeticsGUIConfig.getSignColor());
                    List<String> lines = CosmeticsGUIConfig.getTextLines();
                    for (int i = 0; i < lines.size(); i++) {
                        this.setLine(i+1, Text.literal(lines.get(i)));
                    }
                    this.setAutoUpdate(false);
                }

                @Override
                public void onClose() {
                    try {
                        String colorString = this.getLine(0).getString();

                        if(colorString != null){
                            if(colorString.length() == 6){
                                colorString = "#" + colorString;
                            }
                            Color color = Color.decode(colorString);

                            ItemStack is2 = is.copy();
                            NbtCompound nbtData4 = is2.getOrCreateSubNbt("display");
                            nbtData4.putInt("color", color.getRGB());

                            this.player.sendMessage(CosmeticsGUIConfig.getSuccessColorChangeMessage(), false);

                            CosmeticsData.setHeadCosmetics((IEntityDataSaver) player, is2);
                            ((CosmeticSlotExt) player.playerScreenHandler).setHeadCosmetics(is2);
                            player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(player.playerScreenHandler.syncId, player.playerScreenHandler.nextRevision(), 5, is2));
                        }
                    } catch (NumberFormatException e) {
                        this.player.sendMessage(CosmeticsGUIConfig.getErrorColorChangeMessage(), false);
                    }
                }
            };
            gui.open();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
