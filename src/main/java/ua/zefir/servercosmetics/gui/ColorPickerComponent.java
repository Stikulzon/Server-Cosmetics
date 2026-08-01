package ua.zefir.servercosmetics.gui;

import static ua.zefir.servercosmetics.config.ConfigManager.COSMETICS_GUI_CONFIG;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SignGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import java.awt.Color;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.DyedItemColor;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableInt;
import ua.zefir.servercosmetics.ModInit;
import ua.zefir.servercosmetics.config.ColorPickerConfig;
import ua.zefir.servercosmetics.config.CosmeticsGuiConfig;
import ua.zefir.servercosmetics.data.ItemType;
import ua.zefir.servercosmetics.datagen.ui.GuiTextures;
import ua.zefir.servercosmetics.util.GuiUtils;

// TODO: remove mutable variables, refactor
public class ColorPickerComponent {

  private final ServerPlayer player;
  private final ItemStack itemToColor;
  private final ItemType targetType;
  private final Consumer<ItemStack> onColorSelectCallback;
  private final Runnable onCloseCallback;

  public ColorPickerComponent(
      ServerPlayer player,
      ItemStack itemToColor,
      ItemType targetType,
      Consumer<ItemStack> onColorSelectCallback,
      Runnable onCloseCallback) {
    this.player = player;
    this.itemToColor = itemToColor.copy();
    this.targetType = targetType;
    this.onColorSelectCallback = onColorSelectCallback;
    this.onCloseCallback = onCloseCallback;
  }

  public void open() {
    try {
      new ColorPickerScreen(player, itemToColor, onColorSelectCallback, onCloseCallback).open();
    } catch (Exception e) {
      player.sendSystemMessage(Component.literal("Error opening color picker."));
      ModInit.LOGGER.error("Failed to open ColorPickerComponent", e);
    }
  }

  private static class ColorPickerScreen extends SimpleGui {
    private final ServerPlayer player;
    private final ItemStack hatItemStack;
    private final Consumer<ItemStack> onColorSelectCallback;
    private final Runnable onCloseCallback;
    private final ColorPickerConfig colorPickerConfig;

    private final MutableFloat saturation = new MutableFloat(100F);
    private final MutableInt selectedBaseColorSlotIndex = new MutableInt(0);
    private final MutableBoolean usePaintBrushView = new MutableBoolean(true);
    private boolean initialGradientDrawn = false;

    public ColorPickerScreen(
        ServerPlayer player,
        ItemStack hatItemStack,
        Consumer<ItemStack> onColorSelectCallback,
        Runnable onCloseCallback) {
      super(MenuType.GENERIC_9x5, player, true);
      this.player = player;
      this.hatItemStack = hatItemStack.copy();
      this.onColorSelectCallback = onColorSelectCallback;
      this.onCloseCallback = onCloseCallback;
      this.colorPickerConfig = ((CosmeticsGuiConfig) COSMETICS_GUI_CONFIG).getColorPickerConfig();

      this.setTitle(GuiTextures.COLOR_PICKER_MENU.apply(colorPickerConfig.getColorPickerGUIName()));
      populateGui();
    }

    @Override
    public void onManualClose() {
      super.onManualClose();
      if (onCloseCallback != null) {
        onCloseCallback.run();
      }
    }

    private void populateGui() {
      this.setSlot(colorPickerConfig.getColorInputSlot(), GuiElementBuilder.from(hatItemStack));
      drawBaseColorSlots();
      if (!initialGradientDrawn && colorPickerConfig.getColorSlots().length > 0) {
        selectedBaseColorSlotIndex.setValue(colorPickerConfig.getColorSlots()[0]);
        drawGradientSlots();
        initialGradientDrawn = true;
      }
      setupBrightnessButtons();
      setupViewToggleButtons();
      setupColorInputButton();
    }

    public void drawBaseColorSlots() {
      ItemStack templateStack;
      if (usePaintBrushView.getValue() && colorPickerConfig.getPaintItemStack() != null) {
        templateStack = new ItemStack(Items.LEATHER_HORSE_ARMOR);
        templateStack.set(
            DataComponents.ITEM_MODEL,
            colorPickerConfig.getPaintItemStack().get(DataComponents.ITEM_MODEL));
      } else {
        templateStack = hatItemStack.copy();
        templateStack.remove(DataComponents.DYED_COLOR);
      }

      int[] baseColorDisplaySlots = colorPickerConfig.getColorSlots();
      String[] colorHexValues = colorPickerConfig.getColorHexValues();

      for (int i = 0; i < baseColorDisplaySlots.length && i < colorHexValues.length; i++) {
        ItemStack displayColorStack = templateStack.copy();
        try {
          int decimalColor = Integer.parseInt(colorHexValues[i], 16);
          displayColorStack.set(DataComponents.DYED_COLOR, new DyedItemColor(decimalColor));

          CompoundTag nbt = new CompoundTag();
          nbt.putInt("baseColorHexIndex", i);
          displayColorStack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));

          final int finalI = i;
          this.setSlot(
              baseColorDisplaySlots[i],
              GuiElementBuilder.from(displayColorStack)
                  .setCallback(
                      (clickIndex, clickType, actionType, gui) -> {
                        selectedBaseColorSlotIndex.setValue(finalI);
                        drawGradientSlots();
                      }));
        } catch (NumberFormatException e) {
          ModInit.LOGGER.warn("Invalid hex color in config: {}", colorHexValues[i]);
        }
      }
      if (!initialGradientDrawn && baseColorDisplaySlots.length > 0) {
        selectedBaseColorSlotIndex.setValue(0);
        drawGradientSlots();
        initialGradientDrawn = true;
      }
    }

    private void drawGradientSlots() {
      if (selectedBaseColorSlotIndex.getValue() < 0
          || selectedBaseColorSlotIndex.getValue()
              >= colorPickerConfig.getColorHexValues().length) {
        return;
      }
      String baseHex = colorPickerConfig.getColorHexValues()[selectedBaseColorSlotIndex.getValue()];
      Color baseColor;
      try {
        baseColor = new Color(Integer.parseInt(baseHex, 16));
      } catch (NumberFormatException e) {
        ModInit.LOGGER.warn("Invalid base hex for gradient: {}", baseHex);
        return;
      }

      ItemStack gradientItem;
      if (usePaintBrushView.getValue()) {
        gradientItem = new ItemStack(Items.LEATHER_HORSE_ARMOR);
        gradientItem.set(
            DataComponents.ITEM_MODEL,
            colorPickerConfig.getPaintItemStack().get(DataComponents.ITEM_MODEL));
      } else {
        gradientItem = hatItemStack.copy();
      }

      float[] hsv =
          Color.RGBtoHSB(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), null);
      int[] gradientDisplaySlots = colorPickerConfig.getColorGradientSlots();

      for (int j = 0; j < gradientDisplaySlots.length; j++) {
        float brightnessFactor = (1.0f / (gradientDisplaySlots.length + 1)) * (j + 1.0f);
        brightnessFactor = Math.min(Math.max(brightnessFactor, 0.1f), 1.0f);

        Color gradientStepColor =
            (baseColor.getRed() == baseColor.getGreen()
                    && baseColor.getRed() == baseColor.getBlue())
                ? new Color(Color.HSBtoRGB(hsv[0], 0, brightnessFactor))
                : new Color(Color.HSBtoRGB(hsv[0], saturation.getValue() / 100F, brightnessFactor));

        int stepColorRgb = gradientStepColor.getRGB();
        gradientItem.set(DataComponents.DYED_COLOR, new DyedItemColor(stepColorRgb));

        this.setSlot(
            gradientDisplaySlots[j],
            GuiElementBuilder.from(gradientItem)
                .setCallback(
                    () -> {
                      ItemStack finalColoredHat = hatItemStack.copy();
                      finalColoredHat.set(
                          DataComponents.DYED_COLOR, new DyedItemColor(stepColorRgb));

                      this.setSlot(
                          colorPickerConfig.getColorOutputSlot(),
                          GuiElementBuilder.from(finalColoredHat.copy())
                              .setName(Component.literal("Click to Confirm"))
                              .setCallback(
                                  () -> {
                                    this.close();
                                    onColorSelectCallback.accept(finalColoredHat);
                                  }));
                    }));
      }
    }

    public void setupBrightnessButtons() {
      GuiUtils.setUpButton(
          this,
          COSMETICS_GUI_CONFIG.getButtonConfig("decreaseBrightness"),
          () -> {
            saturation.subtract(colorPickerConfig.getSaturationAdjustmentValue());
            if (saturation.getValue() < 15F) saturation.setValue(15F);
            drawGradientSlots();
          });

      GuiUtils.setUpButton(
          this,
          COSMETICS_GUI_CONFIG.getButtonConfig("increaseBrightness"),
          () -> {
            saturation.add(colorPickerConfig.getSaturationAdjustmentValue());
            if (saturation.getValue() > 100F) saturation.setValue(100F);
            drawGradientSlots();
          });
    }

    public void setupViewToggleButtons() {
      GuiUtils.setUpButton(
          this,
          COSMETICS_GUI_CONFIG.getButtonConfig("toggleColorView"),
          () -> {
            usePaintBrushView.setValue(!usePaintBrushView.getValue());
            drawBaseColorSlots();
          });
    }

    public void setupColorInputButton() {
      GuiUtils.setUpButton(
          this,
          COSMETICS_GUI_CONFIG.getButtonConfig("enterColor"),
          () -> new ColorInputSign(player, hatItemStack, onColorSelectCallback).open());
    }
  }

  private static class ColorInputSign extends SignGui {
    private final ItemStack itemToColor;
    private final Consumer<ItemStack> onColorSelectCallback;
    private final ColorPickerConfig colorPickerConfig;

    public ColorInputSign(
        ServerPlayer player, ItemStack itemToColor, Consumer<ItemStack> onColorSelectCallback) {
      super(player);
      this.itemToColor = itemToColor;
      this.onColorSelectCallback = onColorSelectCallback;
      this.colorPickerConfig = ((CosmeticsGuiConfig) COSMETICS_GUI_CONFIG).getColorPickerConfig();

      this.setSignType(
          BuiltInRegistries.BLOCK.getValue(Identifier.parse(colorPickerConfig.getSignType())));
      this.setColor(colorPickerConfig.getSignColor());
      List<String> lines = colorPickerConfig.getTextLines();
      for (int i = 0; i < lines.size() && i < 4; i++) {
        this.setLine(i, Component.literal(lines.get(i)));
      }
    }

    @Override
    public void onManualClose() {
      String colorString = this.getLine(0).getString().trim();

      if (colorString.isEmpty()) {
        return;
      }

      try {
        if (colorString.length() == 6 && !colorString.startsWith("#")) {
          colorString = "#" + colorString;
        }
        Color color = Color.decode(colorString);

        ItemStack coloredStack = itemToColor.copy();
        coloredStack.set(DataComponents.DYED_COLOR, new DyedItemColor(color.getRGB()));

        this.player.sendSystemMessage(colorPickerConfig.getSuccessColorChangeMessage());

        onColorSelectCallback.accept(coloredStack);

      } catch (NumberFormatException e) {
        this.player.sendSystemMessage(colorPickerConfig.getErrorColorChangeMessage());
      }
    }
  }
}
