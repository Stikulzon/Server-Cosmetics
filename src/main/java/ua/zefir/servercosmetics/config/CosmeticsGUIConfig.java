package ua.zefir.servercosmetics.config;

import ua.zefir.servercosmetics.ServerCosmetics;
import ua.zefir.servercosmetics.gui.resources.GuiTextures;
import ua.zefir.servercosmetics.util.Utils;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import lombok.Getter;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

import java.util.*;

public class CosmeticsGUIConfig extends AbstractGuiConfig {
    @Getter
    private static int[] colorSlots;
    @Getter
    private static int[] colorGradientSlots;
    @Getter
    private static int colorInputSlot;
    @Getter
    private static int colorOutputSlot;
    @Getter
    private static String[] colorHexValues;
    @Getter
    private static String colorPickerGUINameString;
    @Getter
    private static float saturationAdjustmentValue;
    @Getter
    private static String signType;
    @Getter
    private static PolymerModelData paintItemPolymerModelData;
    @Getter
    private static DyeColor signColor;
    private static List<String> textLines;
    @Getter
    private static String successMessageString;
    @Getter
    private static String errorMessageString;
    @Getter
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
        file.addDefault("slots.color", new int[]{21, 22, 23, 30, 31, 32, 39, 40, 41});
        file.addDefault("slots.colorGradient", new int[]{1, 2, 3, 4, 5, 6, 7});
        file.addDefault("colorPicker.hexValues", new String[]{"ff0000", "ff7700", "ffff00", "ff0099", "ffffff", "09ff00", "8800ff", "0000ff", "00ffff"});
        file.addDefault("colorPicker.name", "Color Picker");
        file.addDefault("colorPicker.saturationAdjustmentValue", 20.0F);
        file.addDefault("permissions.openGui", "servercosmetics.gui.cosmetics");
        file.addDefault("colorInput.signType", "minecraft:acacia_wall_sign");
        file.addDefault("colorInput.signColor", "WHITE");
        file.addDefault("colorInput.textLines", List.of("Enter the color in", "HEX format", "Example: #FFFFFF"));
        file.addDefault("colorInput.messages.success", "&aColor successfully changed!");
        file.addDefault("colorInput.messages.error", "&cIncorrect color format!");
        file.addDefault("bodyCosmeticsAutoAlignment", true);

        if (!file.contains("displaySlots")) {
            file.set("displaySlots", List.of(
                    19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43
            ));
        }
    }

    @Override
    protected void loadSpecificConfig(YamlFile file) {
        colorSlots = file.getIntegerList("slots.color").stream().mapToInt(Integer::intValue).toArray();
        colorGradientSlots = file.getIntegerList("slots.colorGradient").stream().mapToInt(Integer::intValue).toArray();
        colorInputSlot = file.getInt("slots.colorInput");
        colorOutputSlot = file.getInt("slots.colorOutput");
        colorHexValues = file.getStringList("colorPicker.hexValues").toArray(new String[0]);
        colorPickerGUINameString = file.getString("colorPicker.name");
        saturationAdjustmentValue = (float) file.getDouble("colorPicker.saturationAdjustmentValue", 20.0);
        bodyCosmeticsAutoAlignment = file.getBoolean("bodyCosmeticsAutoAlignment", true);


        String paintItemModelPath = file.getString("paintItemModelPath");
        if (paintItemModelPath != null && !paintItemModelPath.isEmpty()) {
            try {
                paintItemPolymerModelData = PolymerResourcePackUtils.requestModel(Items.LEATHER_HORSE_ARMOR, Identifier.of(ServerCosmetics.MOD_ID, "item/" + paintItemModelPath));
            } catch (Exception e) {
                ServerCosmetics.LOGGER.error("Failed to load paintItemModelData for path '{}': {}", paintItemModelPath, e.getMessage());
                paintItemPolymerModelData = null;
            }
        } else {
            paintItemPolymerModelData = null;
        }


        signType = file.getString("colorInput.signType");
        try {
            signColor = DyeColor.valueOf(file.getString("colorInput.signColor", "WHITE").toUpperCase());
        } catch (IllegalArgumentException e) {
            ServerCosmetics.LOGGER.warn("Invalid signColor '{}' in CosmeticsGUI.yml, defaulting to WHITE.", file.getString("colorInput.signColor"));
            signColor = DyeColor.WHITE;
        }
        textLines = file.getStringList("colorInput.textLines");
        successMessageString = file.getString("colorInput.messages.success");
        errorMessageString = file.getString("colorInput.messages.error");
    }

    @Override
    protected void addDefaultButtons(ConfigurationSection buttonsSection) {
        super.addDefaultButtons(buttonsSection);

        buttonDefaults.put("toggleColorView", Map.of(
                "name", "Toggle view", "item", "minecraft:diamond_chestplate", "slotIndex", 10));
        buttonDefaults.put("enterColor", Map.of(
                "name", "Enter custom color", "item", "minecraft:oak_sign", "slotIndex", 9,
                "lore", List.of("§eEnter the color in HEX format", "§ein the first line of the sign")));
        buttonDefaults.put("decreaseBrightness", Map.of(
                "name", "Decrease brightness", "item", "minecraft:paper", "textureName", "previous", "slotIndex", 15));
        buttonDefaults.put("increaseBrightness", Map.of(
                "name", "Increase brightness", "item", "minecraft:paper", "textureName", "next", "slotIndex", 16));
        
        buttonDefaults.put("filter.hats-disabled", Map.of(
                "name", "<blue>Hats", "item", "minecraft:leather_helmet", "slotIndex", 12,
                "lore", List.of()));
        buttonDefaults.put("filter.hats-enabled", Map.of(
                "name", "<blue>Hats", "item", "minecraft:diamond_helmet", "slotIndex", 12,
                "lore", List.of()));
        
        buttonDefaults.put("filter.body-cosmetics-disabled", Map.of(
                "name", "<blue>Body Cosmetics", "item", "minecraft:chainmail_chestplate", "slotIndex", 13,
                "lore", List.of()));
        buttonDefaults.put("filter.body-cosmetics-enabled", Map.of(
                "name", "<blue>Body Cosmetics", "item", "minecraft:diamond_chestplate", "slotIndex", 13,
                "lore", List.of()));

        buttonDefaults.put("filter.chestplate-cosmetics-disabled", Map.of(
                "name", "<blue>Chestplate Cosmetics", "item", "minecraft:leather_chestplate", "slotIndex", 14,
                "lore", List.of()));
        buttonDefaults.put("filter.chestplate-cosmetics-enabled", Map.of(
                "name", "<blue>Chestplate Cosmetics", "item", "minecraft:diamond_chestplate", "slotIndex", 14,
                "lore", List.of()));

        buttonDefaults.put("filter.leggings-cosmetics-disabled", Map.of(
                "name", "<blue>Leggings Cosmetics", "item", "minecraft:leather_leggings", "slotIndex", 15,
                "lore", List.of()));
        buttonDefaults.put("filter.leggings-cosmetics-enabled", Map.of(
                "name", "<blue>Leggings Cosmetics", "item", "minecraft:diamond_leggings", "slotIndex", 15,
                "lore", List.of()));

        buttonDefaults.put("filter.boots-cosmetics-disabled", Map.of(
                "name", "<blue>Boots Cosmetics", "item", "minecraft:leather_boots", "slotIndex", 16,
                "lore", List.of()));
        buttonDefaults.put("filter.boots-cosmetics-enabled", Map.of(
                "name", "<blue>Boots Cosmetics", "item", "minecraft:diamond_boots", "slotIndex", 16,
                "lore", List.of()));

        buttonDefaults.forEach((buttonName, properties) -> addDefaultButtonToSection(buttonsSection, buttonName, properties));
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

        loadNavigationButton(file, "filter.chestplate-cosmetics-disabled");
        loadNavigationButton(file, "filter.chestplate-cosmetics-enabled");

        loadNavigationButton(file, "filter.leggings-cosmetics-disabled");
        loadNavigationButton(file, "filter.leggings-cosmetics-enabled");

        loadNavigationButton(file, "filter.boots-cosmetics-disabled");
        loadNavigationButton(file, "filter.boots-cosmetics-enabled");
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

}
