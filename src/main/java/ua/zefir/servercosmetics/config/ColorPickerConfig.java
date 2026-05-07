package ua.zefir.servercosmetics.config;

import static ua.zefir.servercosmetics.ModInit.id;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
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

    String paintItemModelPath = file.getString("paintItemModelPath");
    if (paintItemModelPath != null && !paintItemModelPath.isEmpty()) {
      paintItemStack = createPaintItemStack(paintItemModelPath);
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
      ItemStack stack = Items.LEATHER_HORSE_ARMOR.getDefaultStack();
      stack.set(DataComponentTypes.ITEM_MODEL, id(modelPath));
      RuntimeModelManager.requestItemModel(modelPath, true);
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

  public Text getColorPickerGUIName() {
    return Utils.formatDisplayName(colorPickerGUINameString);
  }

  public float getSaturationAdjustmentValue() {
    return saturationAdjustmentValue;
  }

  public String getSignType() {
    return signType;
  }

  public ItemStack getPaintItemStack() {
    return paintItemStack;
  }

  public DyeColor getSignColor() {
    return signColor;
  }

  public List<String> getTextLines() {
    return new ArrayList<>(textLines);
  }

  public Text getSuccessColorChangeMessage() {
    return Utils.formatDisplayName(successMessageString);
  }

  public Text getErrorColorChangeMessage() {
    return Utils.formatDisplayName(errorMessageString);
  }
}
