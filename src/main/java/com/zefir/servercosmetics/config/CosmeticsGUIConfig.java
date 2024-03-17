package com.zefir.servercosmetics.config;

import com.zefir.servercosmetics.util.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.comments.format.YamlCommentFormat;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CosmeticsGUIConfig {
    private static final Map<Integer, AbstractMap.SimpleEntry<String, ItemStack>> cosmeticsItemsMap = new HashMap<>();
    private static final Map<String, ConfigManager.NavigationButton> navigationButtons = new HashMap<>();
    private static String cosmeticsGUIName;
    private static int[] cosmeticSlots;
    private static int[] colorSlots;
    private static int[] colorGradientSlots;
    private static int colorInputSlot;
    private static int colorOutputSlot;
    private static String[] colorHexValues;
    private static String colorPickerGUIName;
    private static String guiAccessPermission;
    private static String textUnlocked;
    private static String textLocked;
    private static float saturationAdjustmentValue;
    private static boolean isPageIndicatorEnabled;
    private static boolean replaceInventory;
    private static String signType;
    private static int paintItemCMD;
    private static DyeColor signColor;
    private static List<String> textLines;
    private static String successMessage;
    private static String errorMessage;


    public static void serverCosmeticsInit(){
        loadConfig();
        loadCosmeticItems();
        if(ConfigManager.isHMCCosmeticsSupport()){
            loadHMCCosmetics();
        }
    }

    public static void loadConfig() {
        Path configFile = ConfigManager.SERVER_COSMETICS_DIR.resolve("cosmeticsGUI.yml");
        YamlFile yamlFile = new YamlFile(configFile.toAbsolutePath().toString());

        try {
            yamlFile.createOrLoadWithComments();
            setupDefaultConfig(yamlFile);
            yamlFile.loadWithComments();

            ConfigManager.loadButtonConfigs(yamlFile, "next", navigationButtons);
            ConfigManager.loadButtonConfigs(yamlFile, "previous", navigationButtons);
            ConfigManager.loadButtonConfigs(yamlFile, "removeItem", navigationButtons);
            ConfigManager.loadButtonConfigs(yamlFile, "toggleColorView", navigationButtons);
            ConfigManager.loadButtonConfigs(yamlFile, "enterColor", navigationButtons);
            ConfigManager.loadButtonConfigs(yamlFile, "decreaseBrightness", navigationButtons);
            ConfigManager.loadButtonConfigs(yamlFile, "increaseBrightness", navigationButtons);
            ConfigManager.loadButtonConfigs(yamlFile, "pageIndicator", navigationButtons);
            ConfigManager.loadButtonConfigs(yamlFile, "cosmeticFilter.show-all-skins", navigationButtons);
            ConfigManager.loadButtonConfigs(yamlFile, "cosmeticFilter.show-owned-skins", navigationButtons);

            cosmeticsGUIName = yamlFile.getString("guiName");
            cosmeticSlots = yamlFile.getIntegerList("slots.cosmetic").stream().mapToInt(Integer::intValue).toArray();
            colorSlots = yamlFile.getIntegerList("slots.color").stream().mapToInt(Integer::intValue).toArray();
            colorGradientSlots = yamlFile.getIntegerList("slots.colorGradient").stream().mapToInt(Integer::intValue).toArray();
            colorInputSlot = yamlFile.getInt("slots.colorInput");
            colorOutputSlot = yamlFile.getInt("slots.colorOutput");
            colorHexValues = yamlFile.getStringList("colorPicker.hexValues").toArray(new String[0]);
            colorPickerGUIName = yamlFile.getString("colorPicker.name");
            saturationAdjustmentValue = yamlFile.getLong("colorPicker.saturationAdjustmentValue");
            paintItemCMD = yamlFile.getInt("paintItemCustomModelData");
            guiAccessPermission = yamlFile.getString("permissions.openGui");
            textUnlocked = yamlFile.getString("texts.unlocked");
            textLocked = yamlFile.getString("texts.locked");
            isPageIndicatorEnabled = yamlFile.getBoolean("pageIndicatorEnabled");
            replaceInventory = yamlFile.getBoolean("replaceInventory");
            signType = yamlFile.getString("colorInput.signType");
            signColor = DyeColor.valueOf(yamlFile.getString("colorInput.signColor").toUpperCase());
            textLines = yamlFile.getStringList("colorInput.textLines");
            successMessage = yamlFile.getString("colorInput.messages.success");
            errorMessage = yamlFile.getString("colorInput.messages.error");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create or load cosmeticsGUI.yml file", e);
        }
    }

    private static void setupDefaultConfig(YamlFile yamlFile) {
        yamlFile.setCommentFormat(YamlCommentFormat.PRETTY);

        // Header
        yamlFile.options().headerFormatter()
                .prefixFirst("###############################")
                .commentPrefix("## ")
                .commentSuffix(" ##")
                .suffixLast("###############################");

        yamlFile.setHeader("Cosmetics GUI Config File");

        // Config body

        yamlFile.path("guiName")
                .addDefault("&f\uF811\uF811\uF811\uF811\uF811\uF811\uF811\uF811솯\uF811\uF811\uF811\uF811\uF811\uF811\uF811\uF811\uF811\uF811\uF811\uF811䍒䍒䍒䍒䍒䍒䍒䍒䍒䍒")
                .commentSide("GUI title.");

        yamlFile.path("replaceInventory")
                .addDefault(true)
                .commentSide("Use player inventory as GUI slots. With it you can use slots from 53 til 89");

        // Slot configurations
        yamlFile.path("slots.cosmetic")
                .addDefault(new int[]{19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43}).commentSide("Cosmetic slots.");

        yamlFile.path("slots.colorInput")
                .addDefault(28);

        yamlFile.path("slots.colorOutput")
                .addDefault(34);
        yamlFile.path("paintItemCustomModelData")
                .addDefault(1);

        yamlFile.path("slots.color")
                .addDefault(new int[]{21, 22, 23, 30, 31, 32, 39, 40, 41})
                .commentSide("Color picker slots.");

        yamlFile.path("slots.colorGradient")
                .addDefault(new int[]{1, 2, 3, 4, 5, 6, 7})
                .commentSide("Gradient slots.");

        // Color picker settings
        yamlFile.path("colorPicker.hexValues")
                .addDefault(new String[]{"ff0000", "ff7700", "ffff00", "ff0099", "ffffff", "09ff00", "8800ff", "0000ff", "00ffff"})
                .commentSide("Color HEX values, must have same variables count as color picker slots");

        yamlFile.path("colorPicker.name")
                .addDefault("§f\uF811\uF811\uF811\uF811\uF811\uF811\uF811\uF811섈")
                .commentSide("Color picker title.");

        yamlFile.path("colorPicker.saturationAdjustmentValue")
                .addDefault(20F)
                .commentSide("Saturation adjustment.");

        // Permissions
        yamlFile.path("permissions.openGui")
                .addDefault("servercosmetics.gui.cosmetics")
                .commentSide("Permission that needed to open gui.");

        // Texts
        yamlFile.path("texts.unlocked")
                .addDefault("§a(Unlocked)")
                .commentSide("Text displayed for unlocked items.");

        yamlFile.path("texts.locked")
                .addDefault("§c(Locked)")
                .commentSide("Text displayed for locked items.");

        yamlFile.path("pageIndicatorEnabled")
                .addDefault(false)
                .commentSide("Is page indicator enabled.");

        yamlFile.path("colorInput.signType")
                .addDefault("minecraft:acacia_wall_sign")
                .commentSide("Sign type.");

        yamlFile.path("colorInput.signColor")
                .addDefault("WHITE")
                .commentSide("The color of the sign text.");

        yamlFile.path("colorInput.textLines")
                .addDefault(List.of("Enter the color in", "HEX format", "Example: #FFFFFF"))
                .commentSide("Sign text.");

        yamlFile.path("colorInput.messages.success")
                .addDefault("§aColor successfully changed!")
                .commentSide("The message displayed to the player upon successful color change.");

        yamlFile.path("colorInput.messages.error")
                .addDefault("§cIncorrect color format!")
                .commentSide("The error message displayed when an incorrect color format is entered.");

        // Buttons
        ConfigurationSection buttons = yamlFile.getConfigurationSection("buttons") == null ? yamlFile.createSection("buttons") : yamlFile.getConfigurationSection("buttons");

        Map<String, Map<String, Object>> buttonDefaults = setupButtonDefaults();
        buttonDefaults.forEach((buttonName, properties) -> ConfigManager.addButtonDefault(buttons, buttonName, properties));

        try {
            yamlFile.save();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save default yml configuration", e);
        }
    }

    public static Map<String, Map<String, Object>> setupButtonDefaults() {
        Map<String, Map<String, Object>> buttonDefaults = new HashMap<>();

        buttonDefaults.put("next", Map.of(
                "name", "Next",
                "item", "minecraft:paper",
                "customModelData", 10,
                "slotIndex", 51));

        buttonDefaults.put("previous", Map.of(
                "name", "Back",
                "item", "minecraft:paper",
                "customModelData", 11,
                "slotIndex", 47));

        buttonDefaults.put("removeItem", Map.of(
                "name", "Remove item",
                "item", "minecraft:paper",
                "customModelData", 12,
                "slotIndex", 49));

        buttonDefaults.put("toggleColorView", Map.of(
                "name", "Toggle view",
                "item", "minecraft:diamond_chestplate",
                "slotIndex", 10));

        buttonDefaults.put("enterColor", Map.of(
                "name", "Enter custom color",
                "item", "minecraft:oak_sign",
                "slotIndex", 9,
                "lore", List.of(
                        "§eEnter the color in HEX format",
                        "§ein the first line of the sign")));

        buttonDefaults.put("decreaseBrightness", Map.of(
                "name", "Decrease brightness",
                "item", "minecraft:paper",
                "customModelData", 11,
                "slotIndex", 15));

        buttonDefaults.put("increaseBrightness", Map.of(
                "name", "Increase brightness",
                "item", "minecraft:paper",
                "customModelData", 10,
                "slotIndex", 16));

        buttonDefaults.put("cosmeticFilter.show-all-skins", Map.of(
                "name", "&bCosmetic Filter",
                "item", "minecraft:diamond_chestplate",
                "slotIndex", 4,
                "lore", List.of(
                        "&aAll cosmetics &7(Selected)",
                        "&7Available cosmetics",
                        "",
                        "&aClick to change mode!")));

        buttonDefaults.put("cosmeticFilter.show-owned-skins", Map.of(
                "name", "&bCosmetic Filter",
                "item", "minecraft:golden_chestplate",
                "slotIndex", 4,
                "lore", List.of(
                        "&7All cosmetics",
                        "&aAvailable cosmetics &7(Selected)",
                        "",
                        "&aClick to change mode!")));

        buttonDefaults.put("pageIndicator", Map.of(
                "name", "Page",
                "item", "minecraft:paper",
                "customModelData", 0,
                "slotIndex", 53));

        return buttonDefaults;
    }
    public static String getSignType() {
        return signType;
    }

    public static DyeColor getSignColor() {
        return signColor;
    }

    public static List<String> getTextLines() {
        return new ArrayList<>(textLines);
    }

    public static Text getSuccessColorChangeMessage() {
        return Utils.formatDisplayName(successMessage);
    }
    public static int getPaintItemCMD(){
        return paintItemCMD;
    }
    public static Text getErrorColorChangeMessage() {
        return Utils.formatDisplayName(errorMessage);
    }
    public static float getSaturationAdjustmentValue() { return saturationAdjustmentValue; }
    public static boolean getIsPageIndicatorEnabled() { return isPageIndicatorEnabled; }
    public static Text getCosmeticsGUIName() {
        return Utils.formatDisplayName(cosmeticsGUIName);
    }
    public static int[] getCosmeticSlots() {
        return cosmeticSlots;
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
    public static boolean getReplaceInventory(){
        return replaceInventory;
    }

    public static int getColorOutputSlot() {
        return colorOutputSlot;
    }

    public static String[] getColorHexValues() {
        return colorHexValues;
    }

    public static Text getColorPickerGUIName() {
        return Utils.formatDisplayName(colorPickerGUIName);
    }

    public static String getPermissionOpenGui() {
        return guiAccessPermission;
    }

    public static Text getTextUnlocked() {
        return Utils.formatDisplayName(textUnlocked);
    }

    public static Text getTextLocked() {
        return Utils.formatDisplayName(textLocked);
    }

    public static ConfigManager.NavigationButton getButtonConfig(String buttonKey) {
        return navigationButtons.get(buttonKey);
    }

    public static void loadCosmeticItems() {
        Path cosmeticsDir = ConfigManager.SERVER_COSMETICS_DIR.resolve("cosmetics");
        try {
            Files.createDirectories(cosmeticsDir);
        } catch (IOException e){
            throw new RuntimeException("Failed to create cosmetics folder", e);
        }
        List<Path> files = ConfigManager.listFiles(cosmeticsDir);
        cosmeticsItemsMap.clear();

        for (Path file : files) {
            loadCosmeticItem(file);
        }
    }

    public static void loadHMCCosmetics() {
        Path cosmeticsDir = ConfigManager.SERVER_COSMETICS_DIR.resolve("HMCCosmetics");
        try {
            Files.createDirectories(cosmeticsDir);
        } catch (IOException e){
            throw new RuntimeException("Failed to create cosmetics folder", e);
        }
        List<Path> files = ConfigManager.listFiles(cosmeticsDir);

        for (Path file : files) {
            loadHMCCosmeticItem(file);
        }
    }
    private static void loadHMCCosmeticItem(Path file) {
        YamlFile yamlFile = new YamlFile(file.toAbsolutePath().toString());
        try {
            yamlFile.load();

            Set<String> keys =  yamlFile.getKeys(false);
            System.out.println("keys: " + keys);

            for(String str : keys){
                ConfigurationSection section = yamlFile.getConfigurationSection(str);
                System.out.println("section: " + str);

                if(Objects.equals(section.getString("slot"), "HELMET")) {
                    String material = section.getString("item.material");
                    if (material == null) {
                        System.out.println("[ERROR] Error loading " + file.getFileName().toString() + ": you do not defined \"material\"");
                        return;
                    }
                    if (!material.contains(":")) {
                        material = "minecraft:" + material.toLowerCase();
                    }

                    String permission = section.getString("permission");
                    if (permission == null) {
                        System.out.println("[ERROR] Error loading " + file.getFileName().toString() + ": you do not defined \"permission\"");
                        return;
                    }

                    int customModelData = section.getInt("item.model-data");

                    List<Text> lore = section.getStringList("item.lore").stream().map(Utils::miniMessageFormatter).toList();

                    String tempName = section.getString("item.name");
                    Text displayName;
                    if (tempName != null) {
                        displayName = Utils.miniMessageFormatter(tempName);
                    } else {
                        System.out.println("[WARN] You do not defined \"display-name\" in " + file.getFileName().toString());
                        displayName = Utils.miniMessageFormatter("");
                    }

                    ItemStack itemStack = ConfigManager.createItemStack(material, customModelData, displayName, null, lore);

                    addCosmeticItem(itemStack, permission);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load cosmetic item from file: " + file, e);
        }
    }


    private static void loadCosmeticItem(Path file) {
        YamlFile yamlFile = new YamlFile(file.toAbsolutePath().toString());
        try {
            yamlFile.load();

            String material = yamlFile.getString("cosmetic-item.material");
            if (material == null) {
                System.out.println("[ERROR] Error loading " + file.getFileName().toString() + ": you do not defined \"material\"");
                return;
            }
            if (!material.contains(":")){
                material = "minecraft:" + material.toLowerCase();
            }

            String permission = yamlFile.getString("permission");
            if (permission == null) {
                System.out.println("[ERROR] Error loading " + file.getFileName().toString() + ": you do not defined \"permission\"");
                return;
            }

            int customModelData = yamlFile.getInt("cosmetic-item.customModelData");

            List<Text> lore = yamlFile.getStringList("lore").stream().map(Utils::formatDisplayName).toList();

            if(ConfigManager.isLegacyMode() && lore.isEmpty()) {
                lore = yamlFile.getStringList("cosmetic-item.lore").stream().map(Utils::formatDisplayName).toList();
            }

            String tempName = yamlFile.getString("cosmetic-item.display-name");
            Text displayName;
            if (tempName != null) {
                displayName = Utils.formatDisplayName(tempName);
            } else {
                System.out.println("[WARN] You do not defined \"display-name\" in " + file.getFileName().toString());
                displayName = Utils.formatDisplayName("");
            }

            ItemStack itemStack = ConfigManager.createItemStack(material, customModelData, displayName, null, lore);

            addCosmeticItem(itemStack, permission);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load cosmetic item from file: " + file, e);
        }
    }
    private static void addCosmeticItem(ItemStack itemStack, String permission) {
        int index = cosmeticsItemsMap.size();
        AbstractMap.SimpleEntry<String, ItemStack> entry = new AbstractMap.SimpleEntry<>(permission, itemStack);
        cosmeticsItemsMap.put(index, entry);
    }
    public static Map<Integer, AbstractMap.SimpleEntry<String, ItemStack>> getAllCosmeticItems() {
        return cosmeticsItemsMap;
    }
}
