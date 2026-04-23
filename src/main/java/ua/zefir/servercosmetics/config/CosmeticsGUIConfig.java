package ua.zefir.servercosmetics.config;

import static ua.zefir.servercosmetics.ModInit.id;

import java.util.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;
import ua.zefir.servercosmetics.ModInit;
import ua.zefir.servercosmetics.datagen.RuntimeModelManager;
import ua.zefir.servercosmetics.datagen.ui.GuiTextures;
import ua.zefir.servercosmetics.util.Utils;

public class CosmeticsGUIConfig extends AbstractGuiConfig {
  private static int[] colorSlots;
  private static int[] colorGradientSlots;
  private static int colorInputSlot;
  private static int colorOutputSlot;
  private static String[] colorHexValues;
  private static String colorPickerGUINameString;
  private static float saturationAdjustmentValue;
  private static String signType;
  private static ItemStack paintItemStack;
  private static DyeColor signColor;
  private static List<String> textLines;
  private static String successMessageString;
  private static String errorMessageString;
  private static boolean bodyCosmeticsAutoAlignment;

  public CosmeticsGUIConfig() {
    super("CosmeticsGUI.yml");
  }

  @Override
  protected String getGuiConfigHeader() {
    return "Cosmetics GUI Config File";
  }

  @Override
  protected void addSpecificDefaults(YamlFile file) {
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
    file.addDefault("permissions.openGui", "servercosmetics.gui.cosmetics");
    file.addDefault("colorInput.signType", "minecraft:acacia_wall_sign");
    file.addDefault("colorInput.signColor", "WHITE");
    file.addDefault(
        "colorInput.textLines", List.of("Enter the color in", "HEX format", "Example: #FFFFFF"));
    file.addDefault("colorInput.messages.success", "&aColor successfully changed!");
    file.addDefault("colorInput.messages.error", "&cIncorrect color format!");
    file.addDefault("bodyCosmeticsAutoAlignment", true);

    if (!file.contains("displaySlots")) {
      file.set(
          "displaySlots",
          List.of(
              19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43));
    }
  }

  @Override
  protected void loadSpecificConfig(YamlFile file) {
    colorSlots = file.getIntegerList("slots.color").stream().mapToInt(Integer::intValue).toArray();
    colorGradientSlots =
        file.getIntegerList("slots.colorGradient").stream().mapToInt(Integer::intValue).toArray();
    colorInputSlot = file.getInt("slots.colorInput");
    colorOutputSlot = file.getInt("slots.colorOutput");
    colorHexValues = file.getStringList("colorPicker.hexValues").toArray(new String[0]);
    colorPickerGUINameString = file.getString("colorPicker.name");
    saturationAdjustmentValue =
        (float) file.getDouble("colorPicker.saturationAdjustmentValue", 20.0);
    bodyCosmeticsAutoAlignment = file.getBoolean("bodyCosmeticsAutoAlignment", true);

    String paintItemModelPath = file.getString("paintItemModelPath");
    if (paintItemModelPath != null && !paintItemModelPath.isEmpty()) {
      try {
        paintItemStack = Items.LEATHER_HORSE_ARMOR.getDefaultStack();
        paintItemStack.set(DataComponentTypes.ITEM_MODEL, id(paintItemModelPath));
        RuntimeModelManager.requestItemModel(paintItemModelPath, true);
      } catch (Exception e) {
        ModInit.LOGGER.error(
            "Failed to load paintItemModelData for path '{}': {}",
            paintItemModelPath,
            e.getMessage());
        paintItemStack = null;
      }
    } else {
      paintItemStack = null;
    }

    signType = file.getString("colorInput.signType");
    try {
      signColor = DyeColor.valueOf(file.getString("colorInput.signColor", "WHITE").toUpperCase());
    } catch (IllegalArgumentException e) {
      ModInit.LOGGER.warn(
          "Invalid signColor '{}' in CosmeticsGUI.yml, defaulting to WHITE.",
          file.getString("colorInput.signColor"));
      signColor = DyeColor.WHITE;
    }
    textLines = file.getStringList("colorInput.textLines");
    successMessageString = file.getString("colorInput.messages.success");
    errorMessageString = file.getString("colorInput.messages.error");
  }

  @Override
  protected void addDefaultButtons(ConfigurationSection buttonsSection) {
    super.addDefaultButtons(buttonsSection);

    buttonDefaults.put(
        "toggleColorView",
        Map.of("name", "Toggle view", "item", "minecraft:diamond_chestplate", "slotIndex", 10));
    buttonDefaults.put(
        "enterColor",
        Map.of(
            "name",
            "Enter custom color",
            "item",
            "minecraft:oak_sign",
            "slotIndex",
            9,
            "lore",
            List.of("§eEnter the color in HEX format", "§ein the first line of the sign")));
    buttonDefaults.put(
        "decreaseBrightness",
        Map.of(
            "name",
            "Decrease brightness",
            "item",
            "minecraft:paper",
            "textureName",
            "previous",
            "slotIndex",
            15));
    buttonDefaults.put(
        "increaseBrightness",
        Map.of(
            "name",
            "Increase brightness",
            "item",
            "minecraft:paper",
            "textureName",
            "next",
            "slotIndex",
            16));

    buttonDefaults.put(
        "filter.hats-disabled",
        Map.of(
            "name",
            "<blue>Hats",
            "item",
            "minecraft:leather_helmet",
            "slotIndex",
            12,
            "lore",
            List.of()));
    buttonDefaults.put(
        "filter.hats-enabled",
        Map.of(
            "name",
            "<blue>Hats",
            "item",
            "minecraft:diamond_helmet",
            "slotIndex",
            12,
            "lore",
            List.of()));

    buttonDefaults.put(
        "filter.body-cosmetics-disabled",
        Map.of(
            "name",
            "<blue>Body Cosmetics",
            "item",
            "minecraft:chainmail_chestplate",
            "slotIndex",
            13,
            "lore",
            List.of()));
    buttonDefaults.put(
        "filter.body-cosmetics-enabled",
        Map.of(
            "name",
            "<blue>Body Cosmetics",
            "item",
            "minecraft:diamond_chestplate",
            "slotIndex",
            13,
            "lore",
            List.of()));

    buttonDefaults.put(
        "filter.armor-cosmetics-disabled",
        Map.of(
            "name",
            "<blue>Chestplate Cosmetics",
            "item",
            "minecraft:leather_chestplate",
            "slotIndex",
            14,
            "lore",
            List.of()));
    buttonDefaults.put(
        "filter.armor-cosmetics-enabled",
        Map.of(
            "name",
            "<blue>Chestplate Cosmetics",
            "item",
            "minecraft:diamond_chestplate",
            "slotIndex",
            14,
            "lore",
            List.of()));

    buttonDefaults.forEach(
        (buttonName, properties) ->
            addDefaultButtonToSection(buttonsSection, buttonName, properties));
  }

  @Override
  protected void loadAllNavigationButtons(YamlFile file) {
    super.loadAllNavigationButtons(file);

    loadNavigationButton(file, "toggleColorView");
    loadNavigationButton(file, "enterColor");
    loadNavigationButton(file, "decreaseBrightness");
    loadNavigationButton(file, "increaseBrightness");

    loadNavigationButton(file, "filter.hats-disabled");
    loadNavigationButton(file, "filter.hats-enabled");

    loadNavigationButton(file, "filter.body-cosmetics-disabled");
    loadNavigationButton(file, "filter.body-cosmetics-enabled");

    loadNavigationButton(file, "filter.armor-cosmetics-disabled");
    loadNavigationButton(file, "filter.armor-cosmetics-enabled");
  }

  public static List<String> getTextLines() { // For sign
    return new ArrayList<>(textLines); // Return a copy
  }

  public static Text getSuccessColorChangeMessage() {
    return Utils.formatDisplayName(successMessageString);
  }

  public static Text getErrorColorChangeMessage() {
    return Utils.formatDisplayName(errorMessageString);
  }

  public static Text getColorPickerGUIName() {
    return Utils.formatDisplayName(colorPickerGUINameString);
  }

  public Text getGuiName() {
    return GuiTextures.COSMETICS_MENU.apply(Utils.formatDisplayName(this.guiNameString));
  }

  public static int[] getColorSlots() {
    return colorSlots;
  }

  public static int[] getColorGradientSlots() {
    return colorGradientSlots;
  }

  public static int getColorInputSlot() {
    return colorInputSlot;
  }

  public static int getColorOutputSlot() {
    return colorOutputSlot;
  }

  public static String[] getColorHexValues() {
    return colorHexValues;
  }

  public static String getColorPickerGUINameString() {
    return colorPickerGUINameString;
  }

  public static float getSaturationAdjustmentValue() {
    return saturationAdjustmentValue;
  }

  public static String getSignType() {
    return signType;
  }

  public static ItemStack getPaintItemStack() {
    return paintItemStack;
  }

  public static DyeColor getSignColor() {
    return signColor;
  }

  public static String getSuccessMessageString() {
    return successMessageString;
  }

  public static String getErrorMessageString() {
    return errorMessageString;
  }

  public static boolean getBodyCosmeticsAutoAlignment() {
    return bodyCosmeticsAutoAlignment;
  }
}
