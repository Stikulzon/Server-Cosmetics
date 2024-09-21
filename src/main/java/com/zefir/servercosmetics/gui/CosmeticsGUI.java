package com.zefir.servercosmetics.gui;

import com.mojang.brigadier.context.CommandContext;
import com.zefir.servercosmetics.CosmeticsData;
import com.zefir.servercosmetics.config.CosmeticsGUIConfig;
import com.zefir.servercosmetics.ext.CosmeticSlotExt;
import com.zefir.servercosmetics.util.GUIUtils;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SignGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.NbtComponent;
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
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;


// TODO: remove mutable variables usage and map operations
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

    private static void drawCosmeticItems(ServerPlayerEntity player, MutableInt filterRegime) {
        var num = new MutableInt();
        var creator = new MutableObject<Supplier<SimpleGui>>();

        creator.setValue(() -> {
            num.increment();
            int pageNumber = num.getValue();
            var previousGui = GuiHelpers.getCurrentGui(player);
            var gui = new CosmeticsScreen(player, pageNumber, (SimpleGui) previousGui, filterRegime);

            gui.setTitle(CosmeticsGUIConfig.getCosmeticsGUIName());

            Map<Integer, AbstractMap.SimpleEntry<String, ItemStack>> cosmeticsItemsMap = getFilteredCosmetics(player, filterRegime);

            gui.drawCosmeticItems(cosmeticsItemsMap, pageNumber);
            gui.setupNavigationButtons(creator, (SimpleGui) previousGui, pageNumber, cosmeticsItemsMap);
            gui.setupCosmeticFilterButtons(filterRegime);
            gui.setupPageIndicator(pageNumber, cosmeticsItemsMap);

            return gui;
        });

        creator.getValue().get().open();
    }

    private static Map<Integer, AbstractMap.SimpleEntry<String, ItemStack>> getFilteredCosmetics(ServerPlayerEntity player, MutableInt filterRegime) {
        Map<Integer, AbstractMap.SimpleEntry<AbstractMap.SimpleEntry<String, String>, ItemStack>> originalCosmeticsItemsMap = CosmeticsGUIConfig.getCosmeticsItemsMap();
        Map<Integer, AbstractMap.SimpleEntry<String, ItemStack>> allCosmeticsItemsMap = new HashMap<>();

        for (Map.Entry<Integer, AbstractMap.SimpleEntry<AbstractMap.SimpleEntry<String, String>, ItemStack>> entry : originalCosmeticsItemsMap.entrySet()) {
            Integer key = entry.getKey();
            AbstractMap.SimpleEntry<String, ItemStack> newEntry = getStringItemStackSimpleEntry(entry);

            // Put the new entry in the converted map
            allCosmeticsItemsMap.put(key, newEntry);
        }


        if (filterRegime.getValue() == 0) {
            return allCosmeticsItemsMap;
        } else {
            return filterUnlockedCosmetics(player, allCosmeticsItemsMap);
        }
    }

    private static AbstractMap.@NotNull SimpleEntry<String, ItemStack> getStringItemStackSimpleEntry(Map.Entry<Integer, AbstractMap.SimpleEntry<AbstractMap.SimpleEntry<String, String>, ItemStack>> entry) {
        AbstractMap.SimpleEntry<AbstractMap.SimpleEntry<String, String>, ItemStack> value = entry.getValue();

        String extractedString2 = value.getKey().getKey();
        ItemStack itemStack = value.getValue();
        return new AbstractMap.SimpleEntry<>(extractedString2, itemStack);
    }

    private static Map<Integer, AbstractMap.SimpleEntry<String, ItemStack>> filterUnlockedCosmetics(ServerPlayerEntity player, Map<Integer, AbstractMap.SimpleEntry<String, ItemStack>> allCosmeticsItemsMap) {
        Map<Integer, AbstractMap.SimpleEntry<String, ItemStack>> cosmeticsItemsMap = new HashMap<>();
        int itemId = 0;
        for (Map.Entry<Integer, AbstractMap.SimpleEntry<String, ItemStack>> entry : allCosmeticsItemsMap.entrySet()) {
            AbstractMap.SimpleEntry<String, ItemStack> itemEntry = entry.getValue();
            String permission = itemEntry.getKey();
            ItemStack itemStack = itemEntry.getValue();

            if (Permissions.check(player, permission)) {
                cosmeticsItemsMap.put(itemId, new AbstractMap.SimpleEntry<>(permission, itemStack.copy()));
                itemId++;
            }
        }
        return cosmeticsItemsMap;
    }

    private static void colorPicker(ServerPlayerEntity player, ItemStack hatItemStack) {

        try {

            var creator = new MutableObject<Supplier<SimpleGui>>();
            creator.setValue(() -> {
                var selectedColorSlot = new MutableInt();
                var saturation = new MutableFloat(100F);
                var isAlreadyGenerated = new MutableBoolean(false);
                var viewSwitch = new MutableBoolean(true);

                var gui = new ColorPickerScreen(player, selectedColorSlot, saturation, isAlreadyGenerated, viewSwitch, hatItemStack);

                gui.setTitle(CosmeticsGUIConfig.getColorPickerGUIName());
                gui.setSlot(CosmeticsGUIConfig.getColorInputSlot(), GuiElementBuilder.from(hatItemStack));
                gui.drawColorSlots(hatItemStack);
                gui.setupBrightnessButtons();
                gui.setupViewToggleButtons();
                gui.setupColorInputButton(hatItemStack);

                return gui;
            });

            creator.getValue().get().open();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void colorInput(ServerPlayerEntity player, ItemStack is) {
        try {
            SignGui gui = new ColorInputSign(player, is);
            gui.open();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class ColorInputSign extends SignGui {
        private final ItemStack is;

        public ColorInputSign(ServerPlayerEntity player, ItemStack is) {
            super(player);
            this.is = is;
            this.setSignType(Registries.BLOCK.get(Identifier.of(CosmeticsGUIConfig.getSignType())));
            this.setColor(CosmeticsGUIConfig.getSignColor());
            List<String> lines = CosmeticsGUIConfig.getTextLines();
            for (int i = 0; i < lines.size(); i++) {
                this.setLine(i + 1, Text.literal(lines.get(i)));
            }
            this.setAutoUpdate(false);
        }

        @Override
        public void onClose() {
            try {
                String colorString = this.getLine(0).getString();

                if (colorString != null) {
                    if (colorString.length() == 6) {
                        colorString = "#" + colorString;
                    }
                    Color color = Color.decode(colorString);

                    ItemStack is2 = is.copy();
                    is.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(color.getRGB(), true));

                    this.player.sendMessage(CosmeticsGUIConfig.getSuccessColorChangeMessage(), false);

                    CosmeticsData.setHeadCosmetics(player.getUuid(), is2);
                    ((CosmeticSlotExt) player.playerScreenHandler).setHeadCosmetics(is2);
                    player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(player.playerScreenHandler.syncId, player.playerScreenHandler.nextRevision(), 5, is2));
                }
            } catch (NumberFormatException e) {
                this.player.sendMessage(CosmeticsGUIConfig.getErrorColorChangeMessage(), false);
            }
        }
    }

    private static class CosmeticsScreen extends SimpleGui {

        public CosmeticsScreen(ServerPlayerEntity player, int pageNumber, SimpleGui previousGui, MutableInt filterRegime) {
            super(ScreenHandlerType.GENERIC_9X6, player, CosmeticsGUIConfig.isReplaceInventory());
        }

        public void drawCosmeticItems(Map<Integer, AbstractMap.SimpleEntry<String, ItemStack>> cosmeticsItemsMap, int pageNumber) {
            for (int i = 0; i < Math.min(Math.min(cosmeticsItemsMap.size(), CosmeticsGUIConfig.getCosmeticSlots().length), (cosmeticsItemsMap.size() - CosmeticsGUIConfig.getCosmeticSlots().length * (pageNumber - 1))); i++) {
                int finalI = Math.min(i + (CosmeticsGUIConfig.getCosmeticSlots().length * (pageNumber - 1)), cosmeticsItemsMap.size() - 1);
                ItemStack is = cosmeticsItemsMap.get(finalI).getValue().copy(); // IMPORTANT TO USE .copy()!!!

                String permission = cosmeticsItemsMap.get(finalI).getKey();
                if (Permissions.check(player, permission)) {
                    // loading unlocked item
                    this.setSlot(CosmeticsGUIConfig.getCosmeticSlots()[i], GuiElementBuilder.from(is)
                            .addLoreLine(CosmeticsGUIConfig.getTextUnlocked())
                            .setCallback(() -> {
                                if (Objects.equals(is.getItem().toString(), Items.LEATHER_HORSE_ARMOR.toString())) {

                                    colorPicker(player, is);
                                } else {
                                    this.close();
                                    CosmeticsData.setHeadCosmetics(player.getUuid(), is);
                                    ((CosmeticSlotExt) player.playerScreenHandler).setHeadCosmetics(is);
                                    player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(player.playerScreenHandler.syncId, player.playerScreenHandler.nextRevision(), 5, is));
                                }
                            })
                    );
                } else {
                    // loading locked item
                    this.setSlot(CosmeticsGUIConfig.getCosmeticSlots()[i], GuiElementBuilder.from(is)
                            .addLoreLine(CosmeticsGUIConfig.getTextLocked()));
                }
            }
        }

        public void setupNavigationButtons(MutableObject<Supplier<SimpleGui>> creator, SimpleGui previousGui, int pageNumber, Map<Integer, AbstractMap.SimpleEntry<String, ItemStack>> cosmeticsItemsMap) {
            if (CosmeticsGUIConfig.getCosmeticSlots().length < (cosmeticsItemsMap.size() - CosmeticsGUIConfig.getCosmeticSlots().length * (pageNumber - 1))) {
                GUIUtils.setUpButton(this, CosmeticsGUIConfig::getButtonConfig, "next", () -> {
                    var next = new MutableObject<SimpleGui>();
                    if (next.getValue() == null) {
                        next.setValue(creator.getValue().get());
                    }
                    next.getValue().open();
                });
            }

            if (pageNumber > 1 && previousGui != null) {
                GUIUtils.setUpButton(this, CosmeticsGUIConfig::getButtonConfig, "previous", previousGui::open);
            }

            GUIUtils.setUpButton(this, CosmeticsGUIConfig::getButtonConfig, "removeItem", () -> {
                this.close();
                CosmeticsData.setHeadCosmetics(player.getUuid(), ItemStack.EMPTY);
                ((CosmeticSlotExt) player.playerScreenHandler).setHeadCosmetics(ItemStack.EMPTY);
                player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(player.playerScreenHandler.syncId, player.playerScreenHandler.nextRevision(), 5, ItemStack.EMPTY));
            });
        }

        public void setupCosmeticFilterButtons(MutableInt filterRegime) {
            if (filterRegime.getValue() == 0) {
                GUIUtils.setUpButton(this, CosmeticsGUIConfig::getButtonConfig, "cosmeticFilter.show-all-skins", () -> {
                    filterRegime.setValue(1);
                    CosmeticsGUI.drawCosmeticItems(player, filterRegime);
                });
            } else if (filterRegime.getValue() == 1) {
                GUIUtils.setUpButton(this, CosmeticsGUIConfig::getButtonConfig, "cosmeticFilter.show-owned-skins", () -> {
                    filterRegime.setValue(0);
                    CosmeticsGUI.drawCosmeticItems(player, filterRegime);
                });
            }
        }

        public void setupPageIndicator(int pageNumber, Map<Integer, AbstractMap.SimpleEntry<String, ItemStack>> cosmeticsItemsMap) {
            if (CosmeticsGUIConfig.getIsPageIndicatorEnabled()) {
                GUIUtils.setUpButton(this, CosmeticsGUIConfig::getButtonConfig, "pageIndicator", () -> {});
                this.setSlot(53, new GuiElementBuilder(Items.STICK).setCount(pageNumber));
            }
        }
    }

    private static class ColorPickerScreen extends SimpleGui {
        private final MutableInt selectedColorSlot;
        private final MutableFloat saturation;
        private final MutableBoolean isAlreadyGenerated;
        private final MutableBoolean viewSwitch;
        private final ItemStack hatItemStack;

        public ColorPickerScreen(ServerPlayerEntity player, MutableInt selectedColorSlot, MutableFloat saturation, MutableBoolean isAlreadyGenerated, MutableBoolean viewSwitch, ItemStack hatItemStack) {
            super(ScreenHandlerType.GENERIC_9X5, player, true);
            this.selectedColorSlot = selectedColorSlot;
            this.saturation = saturation;
            this.isAlreadyGenerated = isAlreadyGenerated;
            this.viewSwitch = viewSwitch;
            this.hatItemStack = hatItemStack;
        }

        public void drawColorSlots(ItemStack hatItemStack) {
            ItemStack is;
            if (viewSwitch.getValue()) {
                is = new ItemStack(Items.LEATHER_HORSE_ARMOR);
                is.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(CosmeticsGUIConfig.getPaintItemCMD()));
            } else {
                is = hatItemStack;
            }

            int[] colorSlots = CosmeticsGUIConfig.getColorSlots();
            String[] colorHexValues = CosmeticsGUIConfig.getColorHexValues();
            for (int i = 0; i < colorSlots.length; i++) {

                int decimal = Integer.parseInt(colorHexValues[i], 16);

                is.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(decimal, true));
                int finalI = i;
                is.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, comp -> comp.apply(nbt -> nbt.putInt("index", finalI)));

                this.setSlot(colorSlots[i], GuiElementBuilder.from(is)
                        .setCallback((index, clickType, actionType) -> {
                            selectedColorSlot.setValue(index);

                            drawGradientSlots(selectedColorSlot);
                        })
                );

                if (!isAlreadyGenerated.getValue()) {
                    selectedColorSlot.setValue(colorSlots[i]);
                    drawGradientSlots(selectedColorSlot);
                    isAlreadyGenerated.setValue(true);
                }

            }
        }

        private void drawGradientSlots(MutableInt selectedColorSlot) {
            ItemStack currentColorItemStack = new ItemStack(Items.LEATHER_HORSE_ARMOR);
            ItemStack sourceStack = Objects.requireNonNull(this.getSlot(selectedColorSlot.getValue())).getItemStack();
            currentColorItemStack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, comp -> comp.apply(nbt -> {
                NbtComponent sourceCustomData = sourceStack.get(DataComponentTypes.CUSTOM_DATA);
                if (sourceCustomData != null) {
                    NbtCompound sourceNbt = sourceCustomData.copyNbt();
                    if (sourceNbt.contains("itemSkinsID")) {
                        nbt.putString("itemSkinsID", sourceNbt.getString("itemSkinsID"));
                    }
                    if (sourceNbt.contains("index")) {
                        nbt.putInt("index", sourceNbt.getInt("index"));
                    }
                }
            }));
            currentColorItemStack.set(DataComponentTypes.CUSTOM_MODEL_DATA, sourceStack.getOrDefault(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(0)));

            int slotIndex = currentColorItemStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT)
                    .copyNbt().getInt("index");

            int decimal = (Integer.parseInt(CosmeticsGUIConfig.getColorHexValues()[slotIndex], 16));


            Color color = new Color(decimal);

            float[] hsv = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);

            int[] colorGradientSlots = CosmeticsGUIConfig.getColorGradientSlots();
            for (int j = 0; j < colorGradientSlots.length; j++) {

                float luminance = Math.min(
                        (1F / (7F + 1F) * ((float) j + 1)),
                        1F);

                Color color2;
                if (color.getRed() == color.getGreen() && color.getRed() == color.getBlue()) {
                    color2 = new Color(Color.HSBtoRGB(hsv[0], hsv[1], saturation.getValue() / 100F));
                } else {
                    color2 = new Color(Color.HSBtoRGB(hsv[0], 1F - luminance, saturation.getValue() / 100F));
                }
                int[] rgb = {color2.getRed(), color2.getGreen(), color2.getBlue()};


                ItemStack is = currentColorItemStack.copy();
                int decimal2 = rgb[0];
                decimal2 = (decimal2 << 8) + rgb[1];
                decimal2 = (decimal2 << 8) + rgb[2];

                is.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(decimal2, true));


                int finalDecimal2 = decimal2;
                this.setSlot(colorGradientSlots[j], GuiElementBuilder.from(is)

                        .setCallback(() -> {
                            ItemStack is4 = hatItemStack.copy();
                            is4.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(finalDecimal2, true));

                            this.setSlot(CosmeticsGUIConfig.getColorOutputSlot(), GuiElementBuilder.from(is4)

                                    .setCallback(() -> {
                                        this.close();
                                        CosmeticsData.setHeadCosmetics(player.getUuid(), is4);
                                        ((CosmeticSlotExt) player.playerScreenHandler).setHeadCosmetics(is4);
                                        player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(player.playerScreenHandler.syncId, player.playerScreenHandler.nextRevision(), 5, is4));
                                    })
                            );
                        })
                );
            }
        }

        public void setupBrightnessButtons() {
            GUIUtils.setUpButton(this, CosmeticsGUIConfig::getButtonConfig, "decreaseBrightness", () -> {
                if (saturation.getValue() > CosmeticsGUIConfig.getSaturationAdjustmentValue()) {
                    saturation.subtract(CosmeticsGUIConfig.getSaturationAdjustmentValue());
                    saturation.setValue(Math.max(saturation.getValue(), 0F));
                    drawGradientSlots(selectedColorSlot);
                }
            });

            GUIUtils.setUpButton(this, CosmeticsGUIConfig::getButtonConfig, "increaseBrightness", () -> {
                if (saturation.getValue() < 100F) {
                    saturation.add(CosmeticsGUIConfig.getSaturationAdjustmentValue());
                    saturation.setValue(Math.min(saturation.getValue(), 100F));
                    drawGradientSlots(selectedColorSlot);
                }
            });
        }

        public void setupViewToggleButtons() {
            GUIUtils.setUpButton(this, CosmeticsGUIConfig::getButtonConfig, "toggleColorView", () -> {
                viewSwitch.setValue(!viewSwitch.getValue());
                drawColorSlots(hatItemStack);
            });
        }

        public void setupColorInputButton(ItemStack hatItemStack) {
            GUIUtils.setUpButton(this, CosmeticsGUIConfig::getButtonConfig, "enterColor", () -> colorInput(player, hatItemStack));
        }
    }
}