package ua.zefir.servercosmetics.config;

import static ua.zefir.servercosmetics.ModInit.id;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.simpleyaml.configuration.file.YamlFile;
import ua.zefir.servercosmetics.ModInit;
import ua.zefir.servercosmetics.datagen.RuntimeModelManager;
import ua.zefir.servercosmetics.util.Utils;

public class ColorPickerConfig {

  private int[] colorSlots;
  private int[] colorGradientSlots;
  private int colorInputSlot;
  private int colorOutputSlot;
  private String[] colorHexValues;
  private String colorPickerGUINameString;
  private float saturationAdjustmentValue;
  private String signType;
  private String paintItemModelPath;
  private ItemStack paintItemStack;
  private DyeColor signColor;
  private List<String> textLines;
  private String successMessageString;
  private String errorMessageString;

  public void addDefaults(YamlFile file) {
    file.addDefault("slots.colorInput", 28);
    file.addDefault("slots.colorOutput", 34);
    file.addDefault("paintItemModelPath", "paint_button");
    file.addDefault("slots.color", new int[] {21, 22, 23, 30, 31, 32, 39, 40, 41});
    file.addDefault("slots.colorGradient", new int[] {1, 2, 3, 4, 5, 6, 7});
    file.addDefault(
        "colorPicker.hexValues",
        new String[] {
          "ff0000", "ff7700", "ffff00", "ff0099", "ffffff", "09ff00", "8800ff", "0000ff", "00ffff"
        });
    file.addDefault("colorPicker.name", "Color Picker");
    file.addDefault("colorPicker.saturationAdjustmentValue", 20.0F);
    file.addDefault("colorInput.signType", "minecraft:acacia_wall_sign");
    file.addDefault("colorInput.signColor", "WHITE");
    file.addDefault(
        "colorInput.textLines", List.of("Enter the color in", "HEX format", "Example: #FFFFFF"));
    file.addDefault("colorInput.messages.success", "&aColor successfully changed!");
    file.addDefault("colorInput.messages.error", "&cIncorrect color format!");
  }

  public void load(YamlFile file) {
    colorSlots = file.getIntegerList("slots.color").stream().mapToInt(Integer::intValue).toArray();
    colorGradientSlots =
        file.getIntegerList("slots.colorGradient").stream().mapToInt(Integer::intValue).toArray();
    colorInputSlot = file.getInt("slots.colorInput");
    colorOutputSlot = file.getInt("slots.colorOutput");
    colorHexValues = file.getStringList("colorPicker.hexValues").toArray(new String[0]);
    colorPickerGUINameString = file.getString("colorPicker.name");
    saturationAdjustmentValue =
        (float) file.getDouble("colorPicker.saturationAdjustmentValue", 20.0);

    paintItemModelPath = file.getString("paintItemModelPath");
    if (paintItemModelPath != null && !paintItemModelPath.isEmpty()) {
      RuntimeModelManager.requestItemModel(paintItemModelPath, true);
      paintItemStack = null;
    } else {
      paintItemStack = null;
    }

    signType = file.getString("colorInput.signType");
    signColor = parseSignColor(file.getString("colorInput.signColor", "WHITE"));
    textLines = file.getStringList("colorInput.textLines");
    successMessageString = file.getString("colorInput.messages.success");
    errorMessageString = file.getString("colorInput.messages.error");
  }

  private static ItemStack createPaintItemStack(String modelPath) {
    try {
      ItemStack stack = Items.LEATHER_HORSE_ARMOR.getDefaultInstance();
      stack.set(DataComponents.ITEM_MODEL, id(modelPath));
      return stack;
    } catch (Exception e) {
      ModInit.LOGGER.error(
          "Failed to load paintItemModelData for path '{}': {}", modelPath, e.getMessage());
      return null;
    }
  }

  private static DyeColor parseSignColor(String colorName) {
    try {
      return DyeColor.valueOf(colorName.toUpperCase());
    } catch (IllegalArgumentException e) {
      ModInit.LOGGER.warn("Invalid signColor '{}', defaulting to WHITE.", colorName);
      return DyeColor.WHITE;
    }
  }

  public int[] getColorSlots() {
    return colorSlots;
  }

  public int[] getColorGradientSlots() {
    return colorGradientSlots;
  }

  public int getColorInputSlot() {
    return colorInputSlot;
  }

  public int getColorOutputSlot() {
    return colorOutputSlot;
  }

  public String[] getColorHexValues() {
    return colorHexValues;
  }

  public Component getColorPickerGUIName() {
    return Utils.formatDisplayName(colorPickerGUINameString);
  }

  public float getSaturationAdjustmentValue() {
    return saturationAdjustmentValue;
  }

  public String getSignType() {
    return signType;
  }

  public synchronized ItemStack getPaintItemStack() {
    if (paintItemStack == null && paintItemModelPath != null && !paintItemModelPath.isEmpty()) {
      paintItemStack = createPaintItemStack(paintItemModelPath);
    }
    return paintItemStack;
  }

  public DyeColor getSignColor() {
    return signColor;
  }

  public List<String> getTextLines() {
    return new ArrayList<>(textLines);
  }

  public Component getSuccessColorChangeMessage() {
    return Utils.formatDisplayName(successMessageString);
  }

  public Component getErrorColorChangeMessage() {
    return Utils.formatDisplayName(errorMessageString);
  }
}
