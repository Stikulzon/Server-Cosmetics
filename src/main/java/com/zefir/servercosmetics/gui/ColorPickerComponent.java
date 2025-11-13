package com.zefir.servercosmetics.gui;

import com.zefir.servercosmetics.ServerCosmetics;
import com.zefir.servercosmetics.config.CosmeticsGUIConfig;
import com.zefir.servercosmetics.gui.resources.GuiTextures;
import com.zefir.servercosmetics.util.GUIUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SignGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;

import java.awt.Color;
import java.util.List;
import java.util.function.Consumer;

import static com.zefir.servercosmetics.config.ConfigManager.COSMETICS_GUI_CONFIG;

// TODO: remove mutable variables
public class ColorPickerComponent {

    private final ServerPlayerEntity player;
    private final ItemStack itemToColor;
    private final Consumer<ItemStack> onColorSelectCallback;

    public ColorPickerComponent(ServerPlayerEntity player, ItemStack itemToColor, Consumer<ItemStack> onColorSelectCallback) {
        this.player = player;
        this.itemToColor = itemToColor.copy();
        this.onColorSelectCallback = onColorSelectCallback;
    }

    public void open() {
        try {
            new ColorPickerScreen(player, itemToColor, onColorSelectCallback).open();
        } catch (Exception e) {
            player.sendMessage(Text.literal("Error opening color picker."), false);
            ServerCosmetics.LOGGER.error("Failed to open ColorPickerComponent", e);
        }
    }

    private static class ColorPickerScreen extends SimpleGui {
        private final ServerPlayerEntity player;
        private final ItemStack hatItemStack;
        private final Consumer<ItemStack> onColorSelectCallback;

        private final MutableFloat saturation = new MutableFloat(100F);
        private final MutableInt selectedBaseColorSlotIndex = new MutableInt(0);
        private final MutableBoolean usePaintBrushView = new MutableBoolean(true);
        private boolean initialGradientDrawn = false;


        public ColorPickerScreen(ServerPlayerEntity player, ItemStack hatItemStack, Consumer<ItemStack> onColorSelectCallback) {
            super(ScreenHandlerType.GENERIC_9X5, player, true);
            this.player = player;
            this.hatItemStack = hatItemStack.copy();
            this.onColorSelectCallback = onColorSelectCallback;

            this.setTitle(GuiTextures.COLOR_PICKER_MENU.apply(CosmeticsGUIConfig.getColorPickerGUIName()));
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

                    final int finalI = i;
                    this.setSlot(baseColorDisplaySlots[i], GuiElementBuilder.from(displayColorStack)
                            .setCallback((clickIndex, clickType, actionType) -> {
                                selectedBaseColorSlotIndex.setValue(finalI);
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

            ItemStack gradientItem;
            if(usePaintBrushView.getValue()){
                gradientItem = new ItemStack(Items.LEATHER_HORSE_ARMOR);
                gradientItem.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(CosmeticsGUIConfig.getPaintItemPolymerModelData().value()));
            } else {
                gradientItem = hatItemStack.copy();
            }

            float[] hsv = Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), null);
            int[] gradientDisplaySlots = CosmeticsGUIConfig.getColorGradientSlots();

            for (int j = 0; j < gradientDisplaySlots.length; j++) {
                float brightnessFactor = (1.0f / (gradientDisplaySlots.length + 1)) * (j + 1.0f);
                brightnessFactor = Math.min(Math.max(brightnessFactor, 0.1f), 1.0f);

                Color gradientStepColor = (baseColor.getRed() == baseColor.getGreen() && baseColor.getRed() == baseColor.getBlue())
                        ? new Color(Color.HSBtoRGB(hsv[0], 0, brightnessFactor))
                        : new Color(Color.HSBtoRGB(hsv[0], saturation.getValue() / 100F, brightnessFactor));

                int stepColorRgb = gradientStepColor.getRGB();
                gradientItem.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(stepColorRgb, true));

                this.setSlot(gradientDisplaySlots[j], GuiElementBuilder.from(gradientItem)
                        .setCallback(() -> {
                            ItemStack finalColoredHat = hatItemStack.copy();
                            finalColoredHat.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(stepColorRgb, true));

                            this.setSlot(CosmeticsGUIConfig.getColorOutputSlot(), GuiElementBuilder.from(finalColoredHat.copy())
                                    .setName(Text.literal("Click to Confirm"))
                                    .setCallback(() -> {
                                        this.close();
                                        onColorSelectCallback.accept(finalColoredHat);
                                    })
                            );
                        })
                );
            }
        }

        public void setupBrightnessButtons() {
            GUIUtils.setUpButton(this, COSMETICS_GUI_CONFIG.getButtonConfig("decreaseBrightness"), () -> {
                saturation.subtract(CosmeticsGUIConfig.getSaturationAdjustmentValue());
                if (saturation.getValue() < 15F) saturation.setValue(15F);
                drawGradientSlots();
            });

            GUIUtils.setUpButton(this, COSMETICS_GUI_CONFIG.getButtonConfig("increaseBrightness"), () -> {
                saturation.add(CosmeticsGUIConfig.getSaturationAdjustmentValue());
                if (saturation.getValue() > 100F) saturation.setValue(100F);
                drawGradientSlots();
            });
        }

        public void setupViewToggleButtons() {
            GUIUtils.setUpButton(this, COSMETICS_GUI_CONFIG.getButtonConfig("toggleColorView"), () -> {
                usePaintBrushView.setValue(!usePaintBrushView.getValue());
                drawBaseColorSlots();
            });
        }

        public void setupColorInputButton() {
            GUIUtils.setUpButton(this, COSMETICS_GUI_CONFIG.getButtonConfig("enterColor"),
                    () -> new ColorInputSign(player, hatItemStack, onColorSelectCallback).open()
            );
        }
    }

    private static class ColorInputSign extends SignGui {
        private final ItemStack itemToColor;
        private final Consumer<ItemStack> onColorSelectCallback;

        public ColorInputSign(ServerPlayerEntity player, ItemStack itemToColor, Consumer<ItemStack> onColorSelectCallback) {
            super(player);
            this.itemToColor = itemToColor;
            this.onColorSelectCallback = onColorSelectCallback;

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

            if (colorString.isEmpty()) {
                return;
            }

            try {
                // Allow formats like "FFFFFF" and "#FFFFFF"
                if (colorString.length() == 6 && !colorString.startsWith("#")) {
                    colorString = "#" + colorString;
                }
                Color color = Color.decode(colorString);

                ItemStack coloredStack = itemToColor.copy();
                coloredStack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(color.getRGB(), true));

                this.player.sendMessage(CosmeticsGUIConfig.getSuccessColorChangeMessage(), false);

                onColorSelectCallback.accept(coloredStack);

            } catch (NumberFormatException e) {
                this.player.sendMessage(CosmeticsGUIConfig.getErrorColorChangeMessage(), false);
            }
        }
    }
}