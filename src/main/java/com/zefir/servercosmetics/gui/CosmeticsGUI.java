package com.zefir.servercosmetics.gui;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.zefir.servercosmetics.ServerCosmetics;
import com.zefir.servercosmetics.config.ItemSkinsGUIConfig;
import com.zefir.servercosmetics.config.entries.CustomItemEntry;
import com.zefir.servercosmetics.config.entries.CustomItemRegistry;
import com.zefir.servercosmetics.database.DatabaseManager;
import com.zefir.servercosmetics.config.CosmeticsGUIConfig;
import com.zefir.servercosmetics.ext.CosmeticSlotExt;
import com.zefir.servercosmetics.util.GUIUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SignGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.EntityArgumentType;
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

import java.awt.Color;
import java.util.*;
import java.util.stream.Collectors;


public class CosmeticsGUI {
    public static int openGui(CommandContext<ServerCommandSource> ctx) {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFeedback(() -> Text.literal("Player not found"), false);
            return 1;
        }

        try {
            // filterRegime: 0 for all, 1 for owned
            drawCosmeticItems(player, new MutableInt(0), new MutableInt(0)); // Start with page 0, filter all
        } catch (Exception e) {
            ctx.getSource().sendError(Text.literal("An error occurred opening the Cosmetics GUI."));
            throw new RuntimeException(e);
        }
        return 0;
    }

    // Added currentPageNum parameter
    private static void drawCosmeticItems(ServerPlayerEntity player, MutableInt filterRegime, MutableInt currentPageNum) {
        SimpleGui gui = new CosmeticsScreen(player, filterRegime, currentPageNum, CosmeticsGUI::drawCosmeticItems);
        gui.setTitle(GuiTextures.COSMETICS_MENU.apply(CosmeticsGUIConfig.get().getGuiName()));

        gui.open();
    }


    public static void colorPicker(ServerPlayerEntity player, ItemStack hatItemStack) {
        try {
            ColorPickerScreen gui = new ColorPickerScreen(player, hatItemStack);
            gui.setTitle(GuiTextures.COLOR_PICKER_MENU.apply(CosmeticsGUIConfig.getColorPickerGUIName()));
            gui.open();
        } catch (Exception e) {
            player.sendMessage(Text.literal("Error opening color picker."), false);
            throw new RuntimeException(e);
        }
    }

    private static void colorInput(ServerPlayerEntity player, ItemStack itemToColor) {
        try {
            ColorInputSign gui = new ColorInputSign(player, itemToColor);
            gui.open();
        } catch (Exception e) {
            player.sendMessage(Text.literal("Error opening color input."), false);
            throw new RuntimeException(e);
        }
    }

    private static class ColorInputSign extends SignGui {
        private final ItemStack itemToColor;

        public ColorInputSign(ServerPlayerEntity player, ItemStack itemToColor) {
            super(player);
            this.itemToColor = itemToColor;
            this.setSignType(Registries.BLOCK.get(Identifier.of(CosmeticsGUIConfig.getSignType())));
            this.setColor(CosmeticsGUIConfig.getSignColor());
            List<String> lines = CosmeticsGUIConfig.getTextLines();
            for (int i = 0; i < lines.size() && i < 4; i++) {
                this.setLine(i, Text.literal(lines.get(i)));
            }
        }

        @Override
        public void onClose() {
            String colorString = this.getLine(0).getString().trim();

            if (!colorString.isEmpty()) {
                try {
                    if (colorString.length() == 6 && !colorString.startsWith("#")) {
                        colorString = "#" + colorString;
                    }
                    Color color = Color.decode(colorString);

                    ItemStack coloredStack = itemToColor.copy();
                    coloredStack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(color.getRGB(), true));

                    this.player.sendMessage(CosmeticsGUIConfig.getSuccessColorChangeMessage(), false);

                    DatabaseManager.setHeadCosmetics(player.getUuid(), coloredStack);
                    ((CosmeticSlotExt) player.playerScreenHandler).setHeadCosmetics(coloredStack);
                    player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(player.playerScreenHandler.syncId, player.playerScreenHandler.nextRevision(), 5, coloredStack));

                } catch (NumberFormatException e) {
                    this.player.sendMessage(CosmeticsGUIConfig.getErrorColorChangeMessage(), false);
                }
            }
        }
    }

    @FunctionalInterface
    interface GuiDrawer {
        void draw(ServerPlayerEntity player, MutableInt filterRegime, MutableInt currentPageNum);
    }

    private static class CosmeticsScreen extends SimpleGui {
        private final ServerPlayerEntity player;
        private final MutableInt filterRegime;
        private final MutableInt currentPageNum;
        private final GuiDrawer redrawCallback;
        private final int itemsPerPage;

        public CosmeticsScreen(ServerPlayerEntity player, MutableInt filterRegime, MutableInt currentPageNum, GuiDrawer redrawCallback) {
            super(ScreenHandlerType.GENERIC_9X6, player, CosmeticsGUIConfig.isReplaceInventory());
            this.player = player;
            this.filterRegime = filterRegime;
            this.currentPageNum = currentPageNum;
            this.redrawCallback = redrawCallback;
            this.itemsPerPage = CosmeticsGUIConfig.get().getDisplaySlots().length;

            populateGui();
        }

        private void populateGui() {
            List<CustomItemEntry> displayableCosmetics = getFilteredAndSortedCosmetics();
            drawCosmeticItems(displayableCosmetics);
            setupNavigationButtons(displayableCosmetics.size());
            setupCosmeticFilterButtons();
            setupPageIndicator(displayableCosmetics.size());
        }

        private List<CustomItemEntry> getFilteredAndSortedCosmetics() {
            Collection<CustomItemEntry> allCosmetics = CustomItemRegistry.getAllStandaloneCosmetics();
            List<CustomItemEntry> filteredCosmetics;

            if (filterRegime.getValue() == 0) { // Show all
                filteredCosmetics = new ArrayList<>(allCosmetics);
            } else {
                filteredCosmetics = allCosmetics.stream()
                        .filter(entry -> Permissions.check(player, entry.permission()))
                        .collect(Collectors.toList());
            }

            filteredCosmetics.sort(Comparator.comparing(CustomItemEntry::id));
            return filteredCosmetics;
        }

        public void drawCosmeticItems(List<CustomItemEntry> cosmeticsToDisplay) {
            int[] displaySlots = CosmeticsGUIConfig.get().getDisplaySlots();
            int startIndex = currentPageNum.getValue() * itemsPerPage;

            for (int i = 0; i < itemsPerPage; i++) {
                int cosmeticIndex = startIndex + i;
                if (cosmeticIndex < cosmeticsToDisplay.size()) {
                    CustomItemEntry entry = cosmeticsToDisplay.get(cosmeticIndex);
                    ItemStack displayStack = entry.itemStack().copy();

                    GuiElementBuilder element = GuiElementBuilder.from(displayStack);
                    boolean hasPermission = Permissions.check(player, entry.permission());

                    if (hasPermission) {
                        element.addLoreLine(CosmeticsGUIConfig.get().getMessageUnlocked());
                        element.setCallback(() -> {
                            String baseMaterialId = entry.baseItemForModel();
                            if (Items.LEATHER_HORSE_ARMOR.equals(Registries.ITEM.get(Identifier.tryParse(baseMaterialId)))) {
                                colorPicker(player, displayStack);
                            } else {
                                this.close();
                                DatabaseManager.setHeadCosmetics(player.getUuid(), displayStack);
                                ((CosmeticSlotExt) player.playerScreenHandler).setHeadCosmetics(displayStack);
                                player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(player.playerScreenHandler.syncId, player.playerScreenHandler.nextRevision(), 5, displayStack));
                            }
                        });
                    } else {
                        element.addLoreLine(CosmeticsGUIConfig.get().getMessageLocked());
                    }
                    this.setSlot(displaySlots[i], element);
                } else {
                    this.clearSlot(displaySlots[i]);
                }
            }
        }

        public void setupNavigationButtons(int totalFilteredItems) {
            // Next Button
            if ((currentPageNum.getValue() + 1) * itemsPerPage < totalFilteredItems) {
                GUIUtils.setUpButton(this, CosmeticsGUIConfig.get().getButtonConfig("next"), () -> {
                    currentPageNum.increment();
                    redrawCallback.draw(player, filterRegime, currentPageNum);
                });
            }

            // Previous Button
            if (currentPageNum.getValue() > 0) {
                GUIUtils.setUpButton(this, CosmeticsGUIConfig.get().getButtonConfig("previous"), () -> {
                    currentPageNum.decrement();
                    redrawCallback.draw(player, filterRegime, currentPageNum);
                });
            }

            // Remove Item Button
            GUIUtils.setUpButton(this, CosmeticsGUIConfig.get().getButtonConfig("removeItem"), () -> {
                this.close();
                DatabaseManager.setHeadCosmetics(player.getUuid(), ItemStack.EMPTY);
                ((CosmeticSlotExt) player.playerScreenHandler).setHeadCosmetics(ItemStack.EMPTY);
                player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(player.playerScreenHandler.syncId, player.playerScreenHandler.nextRevision(), 5, ItemStack.EMPTY));
            });
        }

        public void setupCosmeticFilterButtons() {
            if (filterRegime.getValue() == 0) { // Currently showing all
                GUIUtils.setUpButton(this, CosmeticsGUIConfig.get().getButtonConfig("cosmeticFilter.show-all-skins"), () -> {
                    filterRegime.setValue(1); // Set to "owned"
                    currentPageNum.setValue(0); // Reset to first page
                    redrawCallback.draw(player, filterRegime, currentPageNum);
                });
            } else { // Currently showing owned
                GUIUtils.setUpButton(this, CosmeticsGUIConfig.get().getButtonConfig("cosmeticFilter.show-owned-skins"), () -> {
                    filterRegime.setValue(0); // Set to "all"
                    currentPageNum.setValue(0);
                    redrawCallback.draw(player, filterRegime, currentPageNum);
                });
            }
        }

        public void setupPageIndicator(int totalFilteredItems) {
            if (CosmeticsGUIConfig.get().isPageIndicatorEnabled()) {
//                int totalPages = (totalFilteredItems == 0) ? 1 : (int) Math.ceil((double) totalFilteredItems / itemsPerPage);
//                totalPages = Math.max(1, totalPages);
//                int displayPageNum = currentPageNum.getValue() + 1;

                if(ItemSkinsGUIConfig.get().isPageIndicatorEnabled()) {
                    GUIUtils.setUpButton(this, ItemSkinsGUIConfig.get().getButtonConfig("pageIndicator"), () -> {});
                }
            }
        }
    }

    private static class ColorPickerScreen extends SimpleGui {
        private final ServerPlayerEntity player;
        private final ItemStack hatItemStack;
        private final MutableFloat saturation = new MutableFloat(100F);
        private final MutableInt selectedBaseColorSlotIndex = new MutableInt(0);
        private boolean initialGradientDrawn = false;
        private final MutableBoolean usePaintBrushView = new MutableBoolean(true);

        public ColorPickerScreen(ServerPlayerEntity player, ItemStack hatItemStack) {
            super(ScreenHandlerType.GENERIC_9X5, player, true);
            this.player = player;
            this.hatItemStack = hatItemStack.copy();

            populateGui();
        }

        private void populateGui() {
            this.setSlot(CosmeticsGUIConfig.getColorInputSlot(), GuiElementBuilder.from(hatItemStack));
            drawBaseColorSlots();
            if (!initialGradientDrawn && CosmeticsGUIConfig.getColorSlots().length > 0) {
                selectedBaseColorSlotIndex.setValue(CosmeticsGUIConfig.getColorSlots()[0]);
                drawGradientSlots();
                initialGradientDrawn = true;
            }
            setupBrightnessButtons();
            setupViewToggleButtons();
            setupColorInputButton();
        }


        public void drawBaseColorSlots() {
            ItemStack templateStack;
            if (usePaintBrushView.getValue() && CosmeticsGUIConfig.getPaintItemPolymerModelData() != null) {
                templateStack = new ItemStack(Items.LEATHER_HORSE_ARMOR);
                templateStack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(CosmeticsGUIConfig.getPaintItemPolymerModelData().value()));
            } else {
                templateStack = hatItemStack.copy();
                templateStack.remove(DataComponentTypes.DYED_COLOR);
            }

            int[] baseColorDisplaySlots = CosmeticsGUIConfig.getColorSlots();
            String[] colorHexValues = CosmeticsGUIConfig.getColorHexValues();

            for (int i = 0; i < baseColorDisplaySlots.length && i < colorHexValues.length; i++) {
                ItemStack displayColorStack = templateStack.copy();
                try {
                    int decimalColor = Integer.parseInt(colorHexValues[i], 16);
                    displayColorStack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(decimalColor, true));

                    NbtCompound nbt = new NbtCompound();
                    nbt.putInt("baseColorHexIndex", i);
                    displayColorStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));


                    this.setSlot(baseColorDisplaySlots[i], GuiElementBuilder.from(displayColorStack)
                            .setCallback((clickIndex, clickType, actionType) -> {
                                ItemStack clickedStack = Objects.requireNonNull(this.getSlot(clickIndex)).getItemStack();
                                NbtCompound clickedNbt = clickedStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
                                if(clickedNbt.contains("baseColorHexIndex")){
                                    selectedBaseColorSlotIndex.setValue(clickedNbt.getInt("baseColorHexIndex"));
                                } else {
                                    for(int k=0; k < baseColorDisplaySlots.length; k++){
                                        if(baseColorDisplaySlots[k] == clickIndex){
                                            selectedBaseColorSlotIndex.setValue(k);
                                            break;
                                        }
                                    }
                                }
                                drawGradientSlots();
                            })
                    );
                } catch (NumberFormatException e) {
                    ServerCosmetics.LOGGER.warn("Invalid hex color in config: {}", colorHexValues[i]);
                }
            }
            if (!initialGradientDrawn && baseColorDisplaySlots.length > 0) {
                selectedBaseColorSlotIndex.setValue(0);
                drawGradientSlots();
                initialGradientDrawn = true;
            }
        }

        private void drawGradientSlots() {
            if (selectedBaseColorSlotIndex.getValue() < 0 || selectedBaseColorSlotIndex.getValue() >= CosmeticsGUIConfig.getColorHexValues().length) {
                return;
            }

            String baseHex = CosmeticsGUIConfig.getColorHexValues()[selectedBaseColorSlotIndex.getValue()];
            Color baseColor;
            try {
                baseColor = new Color(Integer.parseInt(baseHex, 16));
            } catch (NumberFormatException e) {
                ServerCosmetics.LOGGER.warn("Invalid base hex for gradient: {}", baseHex);
                return;
            }

            ItemStack gradientTemplateStack = hatItemStack.copy();

            CustomModelDataComponent modelData = hatItemStack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
            if (modelData != null) {
                gradientTemplateStack.set(DataComponentTypes.CUSTOM_MODEL_DATA, modelData);
            }

            NbtComponent originalCustomData = hatItemStack.get(DataComponentTypes.CUSTOM_DATA);
            if (originalCustomData != null) {
                gradientTemplateStack.set(DataComponentTypes.CUSTOM_DATA, originalCustomData);
            }


            float[] hsv = Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), null);
            int[] gradientDisplaySlots = CosmeticsGUIConfig.getColorGradientSlots();

            for (int j = 0; j < gradientDisplaySlots.length; j++) {

                float brightnessFactor = (1.0f / (gradientDisplaySlots.length +1 )) * (j + 1.0f);
                brightnessFactor = Math.min(Math.max(brightnessFactor, 0.1f), 1.0f);

                Color gradientStepColor;

                if (baseColor.getRed() == baseColor.getGreen() && baseColor.getRed() == baseColor.getBlue()) {
                    gradientStepColor = new Color(Color.HSBtoRGB(hsv[0], 0, brightnessFactor));
                } else {
                    gradientStepColor = new Color(Color.HSBtoRGB(hsv[0], saturation.getValue() / 100F, brightnessFactor));
                }


                ItemStack gradientItem = gradientTemplateStack.copy();
                int stepColorRgb = gradientStepColor.getRGB();
                gradientItem.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(stepColorRgb, true));

                this.setSlot(gradientDisplaySlots[j], GuiElementBuilder.from(gradientItem)
                        .setCallback(() -> {
                            ItemStack finalColoredHat = hatItemStack.copy();
                            finalColoredHat.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(stepColorRgb, true));

                            this.setSlot(CosmeticsGUIConfig.getColorOutputSlot(), GuiElementBuilder.from(finalColoredHat.copy())
                                    .setCallback(() -> {
                                        this.close();
                                        DatabaseManager.setHeadCosmetics(player.getUuid(), finalColoredHat);
                                        ((CosmeticSlotExt) player.playerScreenHandler).setHeadCosmetics(finalColoredHat);
                                        player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(player.playerScreenHandler.syncId, player.playerScreenHandler.nextRevision(), 5, finalColoredHat));
                                    })
                            );
                        })
                );
            }
        }


        public void setupBrightnessButtons() {
            GUIUtils.setUpButton(this, CosmeticsGUIConfig.get().getButtonConfig("decreaseBrightness"), () -> {
                if (saturation.getValue() > CosmeticsGUIConfig.getSaturationAdjustmentValue()) {
                    saturation.subtract(CosmeticsGUIConfig.getSaturationAdjustmentValue());
                } else {
                    saturation.setValue(0F);
                }
                drawGradientSlots();
            });

            GUIUtils.setUpButton(this, CosmeticsGUIConfig.get().getButtonConfig("increaseBrightness"), () -> {
                if (saturation.getValue() < 100F - CosmeticsGUIConfig.getSaturationAdjustmentValue()) {
                    saturation.add(CosmeticsGUIConfig.getSaturationAdjustmentValue());
                } else {
                    saturation.setValue(100F);
                }
                drawGradientSlots();
            });
        }

        public void setupViewToggleButtons() {
            GUIUtils.setUpButton(this, CosmeticsGUIConfig.get().getButtonConfig("toggleColorView"), () -> {
                usePaintBrushView.setValue(!usePaintBrushView.getValue());
                drawBaseColorSlots();
            });
        }

        public void setupColorInputButton() {
            GUIUtils.setUpButton(this, CosmeticsGUIConfig.get().getButtonConfig("enterColor"), () -> colorInput(player, hatItemStack));
        }
    }

    public static int wearCosmeticById(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player;
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

        if (entry != null) {
            if (!Permissions.check(player, entry.permission())) {
                context.getSource().sendFeedback(() -> Text.literal("Selected player does not have permission to use this cosmetic."), false);
                return 1;
            }

            ItemStack cosmeticItem = entry.itemStack();
            if (cosmeticItem == null || cosmeticItem.isEmpty()) {
                context.getSource().sendError(Text.literal("Cosmetic item definition is empty or invalid."));
                return 1;
            }

            String baseMaterialId = entry.baseItemForModel();
            if (Items.LEATHER_HORSE_ARMOR.equals(Registries.ITEM.get(Identifier.tryParse(baseMaterialId)))) {
                ItemStack itemForColorPicker = cosmeticItem.copy();
                itemForColorPicker.remove(DataComponentTypes.DYED_COLOR);
                colorPicker(player, itemForColorPicker);
            } else {
                DatabaseManager.setHeadCosmetics(player.getUuid(), cosmeticItem);
                ((CosmeticSlotExt) player.playerScreenHandler).setHeadCosmetics(cosmeticItem);
                player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(player.playerScreenHandler.syncId, player.playerScreenHandler.nextRevision(), 5, cosmeticItem));
                context.getSource().sendFeedback(() -> Text.literal("Equipped cosmetic: " + entry.displayName().getString()), false);
            }
            return 0;
        } else {
            context.getSource().sendFeedback(() -> Text.literal("Cosmetic not found with ID: " + id), false);
            return 1;
        }
    }
}