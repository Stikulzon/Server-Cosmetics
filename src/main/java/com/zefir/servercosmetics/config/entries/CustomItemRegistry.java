package com.zefir.servercosmetics.config.entries;

import com.zefir.servercosmetics.ServerCosmetics;
import com.zefir.servercosmetics.config.ConfigManager;
import com.zefir.servercosmetics.util.Utils;
import lombok.Setter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class CustomItemRegistry {

    private static final List<CustomItemEntry> cosmeticsList = new CopyOnWriteArrayList<>();

    @Setter
    private static boolean legacyMode = false;

    public static void initialize() {
        loadAllCosmetics();
        loadAllItemSkins();
    }

    public static void reloadAll() {
        clearAll();
        initialize();
    }

    public static void reloadCosmetics() {
        cosmeticsList.clear();
        loadAllCosmetics();
    }

    public static void reloadItemSkins() {
        cosmeticsList.clear();
        loadAllItemSkins();
    }

    private static void clearAll() {
        cosmeticsList.clear();
    }

    private static void loadAllCosmetics() {
        Path cosmeticsDir = ConfigManager.SERVER_COSMETICS_DIR.resolve("Cosmetics");
        loadItemsFromDirectory(cosmeticsDir, "cosmetic-item");
    }

    private static void loadAllItemSkins() {
        Path itemSkinsDir = ConfigManager.SERVER_COSMETICS_DIR.resolve("ItemSkins");
        loadItemsFromDirectory(itemSkinsDir, null);
    }

    private static void loadItemsFromDirectory(Path directory, String itemPropertiesRootNode) {
        try {
            if (Files.notExists(directory)) {
                Files.createDirectories(directory);
                ServerCosmetics.LOGGER.info("Created directory: {}", directory.toAbsolutePath());
                return;
            }
            if (!Files.isDirectory(directory)) {
                ServerCosmetics.LOGGER.error("Path is not a directory: {}", directory.toAbsolutePath());
                return;
            }
        } catch (IOException e) {
            ServerCosmetics.LOGGER.error("Failed to create or access directory: {}", directory, e);
            return;
        }

        List<Path> files = ConfigManager.listFiles(directory);
        for (Path filePath : files) {
            if (!filePath.toString().toLowerCase().endsWith(".yml")) continue;

            String fileName = filePath.getFileName().toString();
            String itemId = fileName.substring(0, fileName.lastIndexOf('.'));
            YamlFile yamlFile = new YamlFile(filePath.toAbsolutePath().toString());

            try {
                yamlFile.load();

                String permission = yamlFile.getString("permission");
                if (permission == null) {
                    ServerCosmetics.LOGGER.error("Error loading {}: 'permission' not defined.", fileName);
                    continue;
                }

                Text displayName;
                List<Text> lore;

                String type = yamlFile.getString("type");

                if (type != null) {
                    String namePath = itemPropertiesRootNode + ".display-name";
                    String lorePath = "lore";
                    String legacyLorePath = itemPropertiesRootNode + ".lore";

                    String tempName = yamlFile.getString(namePath);
                    if (tempName == null) {
                        ServerCosmetics.LOGGER.warn("Cosmetic {}: '{}' not defined. Using empty display name.", fileName, namePath);
                        displayName = Utils.formatDisplayName("");
                    } else {
                        displayName = Utils.formatDisplayName(tempName);
                    }

                    lore = yamlFile.getStringList(lorePath).stream().map(Utils::formatDisplayName).toList();
                    if (lore.isEmpty() && yamlFile.isList(legacyLorePath)) {
                        lore = yamlFile.getStringList(legacyLorePath).stream().map(Utils::formatDisplayName).toList();
                    }


                } else { // ITEM_SKIN
                    String tempName = yamlFile.getString("display-name");
                    if (legacyMode && tempName == null && yamlFile.getString("available-item.display-name") != null) {
                        displayName = Utils.formatDisplayName(yamlFile.getString("available-item.display-name"));
                    } else if (tempName != null) {
                        displayName = Utils.formatDisplayName(tempName);
                    } else {
                        ServerCosmetics.LOGGER.warn("Item Skin {}: 'display-name' not defined. Using empty display name.", fileName);
                        displayName = Utils.formatDisplayName("");
                    }

                    lore = yamlFile.getStringList("lore").stream().map(Utils::formatDisplayName).toList();
                    if (legacyMode && lore.isEmpty()) {
                        lore = yamlFile.getStringList("available-item.lore").stream().map(Utils::formatDisplayName).toList();
                    }
                }

                if (type != null) {
                    String materialPath = itemPropertiesRootNode + ".material";
                    String baseItemMaterial = yamlFile.getString(materialPath);
                    if (baseItemMaterial == null) {
                        ServerCosmetics.LOGGER.error("Error loading cosmetic {}: '{}' not defined.", fileName, materialPath);
                        continue;
                    }
                    if (!baseItemMaterial.contains(":")) {
                        baseItemMaterial = "minecraft:" + baseItemMaterial.toLowerCase();
                    }

                    ItemStack itemStack = ConfigManager.createItemStack(baseItemMaterial, displayName, itemId, lore);
                    CustomItemEntry entry = new CustomItemEntry(itemId, permission, displayName, lore, itemStack, ItemType.valueOf(type.toUpperCase()), baseItemMaterial);
                    cosmeticsList.add(entry);

                } else { // ITEM_SKIN
                    List<String> targetMaterials = yamlFile.getStringList("material");
                    if (targetMaterials.isEmpty()) {
                        String singleMaterial = yamlFile.getString("material");
                        if (singleMaterial != null) {
                            targetMaterials.add(singleMaterial);
                        } else {
                            ServerCosmetics.LOGGER.error("Error loading item skin {}: 'material' (list or string) not defined.", fileName);
                            continue;
                        }
                    }

                    for (String materialKey : targetMaterials) {
                        if (!materialKey.contains(":")) {
                            materialKey = "minecraft:" + materialKey.toLowerCase();
                        }

                        ItemStack itemStack = ConfigManager.createItemStack(materialKey, displayName, itemId, lore);
                        CustomItemEntry entry = new CustomItemEntry(itemId, permission, displayName, lore, itemStack, ItemType.ITEM_SKIN, materialKey);

                        cosmeticsList.add(entry);
                    }
                }
            } catch (Exception e) {
                ServerCosmetics.LOGGER.error("Failed to load custom item from file: {}", filePath, e);
            }
        }
    }

    // --- Accessor methods ---

    public static CustomItemEntry getCosmetic(String id) {
        CustomItemEntry cosmetic = null;
        for (CustomItemEntry entry : cosmeticsList) {
            if(entry.id().equals(id)){
                cosmetic = entry;
            }
        }
        return cosmetic;
    }

    public static List<CustomItemEntry> getAllCosmetics(ItemType type) {
        List<CustomItemEntry> filteredList = new ArrayList<>();
        for (CustomItemEntry entry : cosmeticsList) {
            if(entry.type() == type){
                filteredList.add(entry);
            }
        }
        return filteredList;
    }

    public static List<CustomItemEntry> getCosmeticsList() {
        return Collections.unmodifiableList(cosmeticsList);
    }

    public static List<CustomItemEntry> getAllCosmeticsForMaterial(ItemType type, String targetMaterialId) {
        List<CustomItemEntry> filteredList = new ArrayList<>();
        for (CustomItemEntry entry : cosmeticsList) {
            if(entry.type() == type && entry.baseItemForModel().equals(targetMaterialId)){
                filteredList.add(entry);
            }
        }
        return filteredList;
    }

    public static List<CustomItemEntry> getAllCosmeticsForMaterial(ItemType type, Item item) {
        String targetMaterialId = Registries.ITEM.getId(item).toString();
        return CustomItemRegistry.getAllCosmeticsForMaterial(type, targetMaterialId);
    }

}