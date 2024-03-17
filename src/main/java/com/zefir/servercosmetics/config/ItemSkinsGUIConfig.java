package com.zefir.servercosmetics.config;

import com.zefir.servercosmetics.util.Utils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.comments.format.YamlCommentFormat;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ItemSkinsGUIConfig {
    private static final Map<String, Map<Integer, AbstractMap.SimpleEntry<String, AbstractMap.SimpleEntry<String, ItemStack>>>> itemSkinsMap = new HashMap<>();
    private static final Map<String, ConfigManager.NavigationButton> navigationButtons = new HashMap<>();
    private static String itemSkinsGuiName;
    private static int[] cosmeticSlots;
    private static String permissionOpenGui;
    private static String messageUnlocked;
    private static String messageLocked;
    private static boolean isPageIndicatorEnabled;
    private static int itemSlot;
    public static void itemSkinsInit(){
        loadConfig();
        loadItemSkins();
        if(ConfigManager.isHMCCosmeticsSupport()){
            loadHMCSkins();
        }
    }

    public static void loadConfig() {
        Path configFile = ConfigManager.SERVER_COSMETICS_DIR.resolve("ItemSkinsGUI.yml");
        YamlFile yamlFile = new YamlFile(configFile.toAbsolutePath().toString());

        try {
            yamlFile.createOrLoadWithComments();
            setupDefaultConfig(yamlFile);
            yamlFile.loadWithComments();

            ConfigManager.loadButtonConfigs(yamlFile, "next", navigationButtons);
            ConfigManager.loadButtonConfigs(yamlFile, "previous", navigationButtons);
            ConfigManager.loadButtonConfigs(yamlFile, "removeItem", navigationButtons);
            ConfigManager.loadButtonConfigs(yamlFile, "skinFilter.show-all-skins", navigationButtons);
            ConfigManager.loadButtonConfigs(yamlFile, "skinFilter.show-owned-skins", navigationButtons);
            ConfigManager.loadButtonConfigs(yamlFile, "pageIndicator", navigationButtons);

            itemSkinsGuiName = yamlFile.getString("guiName");
            cosmeticSlots = yamlFile.getIntegerList("cosmeticSlots").stream().mapToInt(Integer::intValue).toArray();
            permissionOpenGui = yamlFile.getString("permissions.openGui");
            messageUnlocked = yamlFile.getString("messages.unlocked");
            messageLocked = yamlFile.getString("messages.locked");
            isPageIndicatorEnabled = yamlFile.getBoolean("pageIndicatorEnabled");
            itemSlot = yamlFile.getInt("slots.itemSlot");
        } catch (IOException e) {
            throw new RuntimeException("Failed to create or load ItemSkinsGUI.yml file", e);
        }
    }

    private static void setupDefaultConfig(YamlFile yamlFile) {
        yamlFile.setCommentFormat(YamlCommentFormat.PRETTY);

        yamlFile.options().headerFormatter()
                .prefixFirst("###############################")
                .commentPrefix("## ")
                .commentSuffix(" ##")
                .suffixLast("###############################");

        yamlFile.setHeader("ItemSkins GUI Config File");

        yamlFile.addDefault("guiName", "&f\uF811\uF811\uF811\uF811\uF811\uF811\uF811\uF811솱\uF811\uF811\uF811\uF811\uF811\uF811\uF811\uF811\uF811\uF811\uF811\uF811䍒䍒䍒䍒䍒䍒䍒䍒䍒䍒");
        yamlFile.addDefault("cosmeticSlots", new int[]{
            19,20,21,22,23,24,25,
            28,29,30,31,32,33,34,
            37,38,39,40,41,42,43
        });

        yamlFile.addDefault("permissions.openGui", "servercosmetics.gui.itemskins");

        yamlFile.addDefault("pageIndicatorEnabled", false);

        yamlFile.addDefault("messages.unlocked", "§a(Unlocked)");
        yamlFile.addDefault("messages.locked", "§c(Locked)");

        yamlFile.addDefault("slots.itemSlot", 4);

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

        buttonDefaults.put("skinFilter.show-all-skins", Map.of(
                "name", "&bCosmetic Filter",
                "item", "minecraft:diamond_chestplate",
                "slotIndex", 10,
                "lore", List.of(
                        "&aAll skins &7(Selected)",
                        "&7Available skins",
                        "",
                        "&aClick to change mode!")));

        buttonDefaults.put("skinFilter.show-owned-skins", Map.of(
                "name", "&bCosmetic Filter",
                "item", "minecraft:golden_chestplate",
                "slotIndex", 10,
                "lore", List.of(
                        "&7All skins",
                        "&aAvailable skins &7(Selected)",
                        "",
                        "&aClick to change mode!")));

        buttonDefaults.put("pageIndicator", Map.of(
                "name", "Page",
                "item", "minecraft:paper",
                "customModelData", 0,
                "slotIndex", 53));

        return buttonDefaults;
    }

    public static ConfigManager.NavigationButton getButtonConfig(String buttonKey) {
        return navigationButtons.get(buttonKey);
    }
    public static Text getItemSkinsGuiName() {
        return Utils.formatDisplayName(itemSkinsGuiName);
    }
    public static int[] getCosmeticSlots() {
        return cosmeticSlots;
    }
    public static String getPermissionOpenGui() {
        return permissionOpenGui;
    }
    public static Text getMessageUnlocked() {
        return Utils.formatDisplayName(messageUnlocked);
    }
    public static Text getMessageLocked() {
        return Utils.formatDisplayName(messageLocked);
    }
    public static boolean getIsPageIndicatorEnabled() { return isPageIndicatorEnabled; }
    public static int getItemSlot() { return itemSlot; }


    public static void loadItemSkins() {
        Path itemSkinsDir = ConfigManager.SERVER_COSMETICS_DIR.resolve("ItemSkins");
        try {
            Files.createDirectories(itemSkinsDir);
        } catch (IOException e){
            throw new RuntimeException("Failed to create ItemSkins folder", e);
        }
        List<Path> files = ConfigManager.listFiles(itemSkinsDir);
        itemSkinsMap.clear();

        for (Path file : files) {
            loadItemSkin(file);
        }
    }
    public static void loadHMCSkins() {
        Path itemSkinsDir = ConfigManager.SERVER_COSMETICS_DIR.resolve("HMCSkins");
        try {
            Files.createDirectories(itemSkinsDir);
        } catch (IOException e){
            throw new RuntimeException("Failed to create HMCSkins folder", e);
        }
        List<Path> files = ConfigManager.listFiles(itemSkinsDir);

        for (Path file : files) {
            loadHMCSkin(file);
        }
    }
    private static void loadHMCSkin(Path file) {
        YamlFile yamlFile = new YamlFile(file.toAbsolutePath().toString());
        try {
            yamlFile.load();
            ConfigurationSection items = yamlFile.getConfigurationSection("items");

            Set<String> keys = items.getKeys(false);
            System.out.println("keys: " + keys);

            for(String mat : keys) {
                ConfigurationSection skins = items.getConfigurationSection(mat + ".wraps");
                Set<String> skinsKeys = skins.getKeys(false);
                System.out.println("skinsKeys: " + skinsKeys);

                for(String skin : skinsKeys) {
                    ConfigurationSection skinSection = skins.getConfigurationSection(skin);
                    System.out.println("section: " + skinSection);

                    List<String> materials = new ArrayList<>();
                    if (mat != null) {
                        materials.add(mat);
                    } else {
                        System.out.println("[ERROR] Error loading " + file.getFileName().toString() + ": you do not defined \"material\"");
                        return;
                    }

                    int customModelData = skinSection.getInt("id");
                    String uuid = skinSection.getString("uuid");

                    String permission = skinSection.getString("permission");
                    if (permission == null) {
                        System.out.println("[ERROR] Error loading " + file.getFileName().toString() + ": you do not defined \"permission\"");
                        return;
                    }

                    String tempName = skinSection.getString("name");
                    Text displayName;
                    if (tempName != null) {
                        displayName = Utils.miniMessageFormatter(Objects.requireNonNull(tempName));
                    } else {
                        System.out.println("[WARN] You do not defined \"display-name\" in " + file.getFileName().toString());
                        displayName = Utils.miniMessageFormatter("");
                    }

                    List<Text> lore = skinSection.getStringList("lore").stream().map(Utils::miniMessageFormatter).toList();

                    for (int i = 0; i < materials.size(); i++) {
                        String materialKey = materials.get(i);
                        if (!materialKey.contains(":")) {
                            materialKey = "minecraft:" + materialKey.toLowerCase();
                            materials.set(i, materialKey);
                        }

                        ItemStack itemStack = ConfigManager.createItemStack(materialKey, customModelData, displayName, file.getFileName().toString().replace(".yml", ""), lore);
                        addItemSkin(materialKey, itemStack, permission, uuid);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load item skin from file: " + file, e);
        }
    }

    private static void loadItemSkin(Path file) {
        YamlFile yamlFile = new YamlFile(file.toAbsolutePath().toString());
        try {
            yamlFile.load();
            List<String> materials = yamlFile.getStringList("material");
            if (materials.isEmpty()) {
                String material = yamlFile.getString("material");
                if(material != null){
                    materials.add(material);
                } else {
                    System.out.println("[ERROR] Error loading " + file.getFileName().toString() + ": you do not defined \"material\"");
                    return;
                }
            }

            int customModelData = yamlFile.getInt("custom-model-data");

            String permission = yamlFile.getString("permission");
            if (permission == null) {
                System.out.println("[ERROR] Error loading " + file.getFileName().toString() + ": you do not defined \"permission\"");
                return;
            }

            String tempName = yamlFile.getString("display-name");
            Text displayName;
            if(ConfigManager.isLegacyMode() && tempName == null && yamlFile.getString("available-item.display-name") != null) {
                displayName = Utils.formatDisplayName(yamlFile.getString("available-item.display-name"));
            } else if (tempName != null) {
                displayName = Utils.formatDisplayName(Objects.requireNonNull(tempName));
            } else {
                System.out.println("[WARN] You do not defined \"display-name\" in " + file.getFileName().toString());
                displayName = Utils.formatDisplayName("");
            }

            List<Text> lore = yamlFile.getStringList("lore").stream().map(Utils::formatDisplayName).toList();

            if(ConfigManager.isLegacyMode() && lore.isEmpty()) {
                lore = yamlFile.getStringList("available-item.lore").stream().map(Utils::formatDisplayName).toList();
            }

            for (int i = 0; i < materials.size(); i++) {
                String materialKey = materials.get(i);
                if (!materialKey.contains(":")) {
                    materialKey = "minecraft:" + materialKey.toLowerCase();
                    materials.set(i, materialKey);
                }

                ItemStack itemStack = ConfigManager.createItemStack(materialKey, customModelData, displayName, file.getFileName().toString().replace(".yml", ""), lore);
                addItemSkin(materialKey, itemStack, permission, file.getFileName().toString());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load item skin from file: " + file, e);
        }
    }

    public static Map<Integer, AbstractMap.SimpleEntry<String, AbstractMap.SimpleEntry<String, ItemStack>>> getItemSkinsItems(Item item) {
        return itemSkinsMap.get("minecraft:" + item.toString());
    }

    private static void addItemSkin(String material, ItemStack itemStack, String permission, String fileName) {
        Map<Integer, AbstractMap.SimpleEntry<String, AbstractMap.SimpleEntry<String, ItemStack>>> materialMap = itemSkinsMap.computeIfAbsent(material, k -> new HashMap<>());
        int index = materialMap.size();
        AbstractMap.SimpleEntry<String, ItemStack> permissionEntry = new AbstractMap.SimpleEntry<>(permission, itemStack);
        AbstractMap.SimpleEntry<String, AbstractMap.SimpleEntry<String, ItemStack>> itemEntry = new AbstractMap.SimpleEntry<>(fileName, permissionEntry);
        materialMap.put(index, itemEntry);
    }
}
