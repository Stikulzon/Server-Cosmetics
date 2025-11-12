package com.zefir.servercosmetics.data;

import com.zefir.servercosmetics.ServerCosmetics;
import com.zefir.servercosmetics.config.ConfigManager;
import com.zefir.servercosmetics.datagen.RuntimeModelManager;
import com.zefir.servercosmetics.util.Utils;
import eu.pb4.polymer.resourcepack.api.PolymerArmorModel;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import lombok.Setter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.zefir.servercosmetics.ServerCosmetics.id;
import static com.zefir.servercosmetics.datafixer.NbtDatafixer.NEW_NBT_KEY_CUSTOM_ITEM_ID;

public class CustomItemRegistry {

    private static final List<CustomItemEntry> cosmeticsList = new CopyOnWriteArrayList<>();

    @Setter
    private static boolean legacyMode = false;

    public static void initialize() {
        RuntimeModelManager.clearRequestedModels();
        loadAllCosmetics();
        loadAllItemSkins();
    }

    public static void reloadAll() {
        cosmeticsList.clear();
        initialize();
    }

    public static void reloadCosmetics() {
        cosmeticsList.removeIf(entry -> entry.type() != ItemType.ITEM_SKIN);
        loadAllCosmetics();
    }

    public static void reloadItemSkins() {
        cosmeticsList.removeIf(entry -> entry.type() == ItemType.ITEM_SKIN);
        loadAllItemSkins();
    }

    private static void loadAllCosmetics() {
        Path cosmeticsDir = ConfigManager.SERVER_COSMETICS_DIR.resolve("Cosmetics");
        loadItemsFromDirectory(cosmeticsDir, "cosmetic-item");
    }

    private static void loadAllItemSkins() {
        Path itemSkinsDir = ConfigManager.SERVER_COSMETICS_DIR.resolve("ItemSkins");
        loadItemsFromDirectory(itemSkinsDir, null);
    }

    // TODO: Refactor
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

        List<Path> files = Utils.listFiles(directory);
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
                int sortingPriority = yamlFile.getInt("sortingPriority", 0);

                if (type != null) {
                    String namePath = "display-name";
                    String legacyNamePath = itemPropertiesRootNode + ".display-name";
                    String lorePath = "lore";
                    String legacyLorePath = itemPropertiesRootNode + ".lore";

                    String tempName = yamlFile.getString(namePath) == null ? yamlFile.getString(legacyNamePath) : yamlFile.getString(namePath);
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
                    String baseItemMaterial;
                    String materialPath = "material";
                    String legacyMaterialPath = itemPropertiesRootNode + ".material";

                    if (ItemType.valueOf(type.toUpperCase()) == ItemType.HELMET || ItemType.valueOf(type.toUpperCase()) == ItemType.CHESTPLATE || ItemType.valueOf(type.toUpperCase()) == ItemType.LEGGINGS || ItemType.valueOf(type.toUpperCase()) == ItemType.BOOTS) {
                        baseItemMaterial = "leather_" + type.toLowerCase();
                    }
                    else {
                        baseItemMaterial = yamlFile.getString(materialPath) == null ? yamlFile.getString(legacyMaterialPath) : yamlFile.getString(materialPath);
                        if (baseItemMaterial == null) {
                            ServerCosmetics.LOGGER.error("Error loading cosmetic {}: '{}' not defined, using paper as fallback", fileName, materialPath);
                            baseItemMaterial = "minecraft:paper";
                        }
                    }
                    if (!baseItemMaterial.contains(":")) {
                        baseItemMaterial = "minecraft:" + baseItemMaterial.toLowerCase();
                    }


                    ItemStack itemStack = createItemStack(baseItemMaterial, displayName, itemId, lore);
                    CustomItemEntry entry;
                    if (ItemType.valueOf(type.toUpperCase()) == ItemType.BODY_COSMETIC) {
                        PolymerModelData polymerModelData = PolymerResourcePackUtils.requestModel(Registries.ITEM.get(Identifier.of(baseItemMaterial)), Identifier.of(ServerCosmetics.MOD_ID, "item/" + itemId + "_sneaking"));
                        boolean isMirrored = yamlFile.getBoolean("isMirrored", false);
                        boolean autoAlignment = yamlFile.getBoolean("autoAlignment", false);
                        boolean offsetWhenSneaking = yamlFile.getBoolean("offsetWhenSneaking", false);
                        boolean autoscale = yamlFile.getBoolean("autoscale", false);

                        entry = new CustomItemEntry(itemId, permission, displayName, lore, itemStack, ItemType.valueOf(type.toUpperCase()), baseItemMaterial, sortingPriority, List.of(Tags.ENTITY, Tags.BODY_COSMETIC), new BodyCosmeticsData(polymerModelData, isMirrored, autoAlignment, offsetWhenSneaking, autoscale));
                    } else if (ItemType.valueOf(type.toUpperCase()) == ItemType.CHESTPLATE_BODY_COSMETIC || ItemType.valueOf(type.toUpperCase()) == ItemType.HAT_BODY_COSMETIC || ItemType.valueOf(type.toUpperCase()) == ItemType.BOOTS_BODY_COSMETIC) {
                        PolymerModelData polymerModelData = PolymerResourcePackUtils.requestModel(Registries.ITEM.get(Identifier.of(baseItemMaterial)), Identifier.of(ServerCosmetics.MOD_ID, "item/" + itemId + "_sneaking"));
                        boolean isMirrored = yamlFile.getBoolean("isMirrored", false);
                        boolean autoAlignment = yamlFile.getBoolean("autoAlignment", false);
                        boolean offsetWhenSneaking = yamlFile.getBoolean("offsetWhenSneaking", false);
                        boolean autoscale = yamlFile.getBoolean("autoscale", false);

                        entry = new CustomItemEntry(itemId, permission, displayName, lore, itemStack, ItemType.valueOf(type.toUpperCase()), baseItemMaterial, sortingPriority, List.of(Tags.ENTITY, Tags.ARMOR, Tags.BODY_COSMETIC), new BodyCosmeticsData(polymerModelData, isMirrored, autoAlignment, offsetWhenSneaking, autoscale));
                    } else if (ItemType.valueOf(type.toUpperCase()) == ItemType.HELMET || ItemType.valueOf(type.toUpperCase()) == ItemType.CHESTPLATE || ItemType.valueOf(type.toUpperCase()) == ItemType.LEGGINGS || ItemType.valueOf(type.toUpperCase()) == ItemType.BOOTS) {
                        entry = new CustomItemEntry(itemId, permission, displayName, lore, itemStack,
                                ItemType.valueOf(type.toUpperCase()), baseItemMaterial, sortingPriority, List.of(Tags.ARMOR, Tags.ITEM), new ArmorCosmeticsData());

                    } else {
                        // HAT
                        entry = new CustomItemEntry(itemId, permission, displayName, lore, itemStack, ItemType.valueOf(type.toUpperCase()), baseItemMaterial, sortingPriority, List.of(Tags.ITEM), null);
                    }
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
                            ItemStack itemStack = createItemStack(materialKey, displayName, itemId, lore);

                            CustomItemEntry entry = new CustomItemEntry(itemId + "_" + materialKey, permission, displayName, lore, itemStack, ItemType.ITEM_SKIN, materialKey, sortingPriority, new ArrayList<>(), null);
                            cosmeticsList.add(entry);
                    }
                }
            } catch (Exception e) {
                ServerCosmetics.LOGGER.error("Failed to load custom item from file: {}", filePath, e);
            }
        }
    }

    public static ItemStack createItemStack(String baseMaterialId, Text displayName, String cosmeticOrSkinId, List<Text> loreTexts) {
        Item baseItem = Registries.ITEM.get(Identifier.of(baseMaterialId));
        if (baseItem == Registries.ITEM.get(Registries.ITEM.getDefaultId()) && !baseMaterialId.equals(Registries.ITEM.getDefaultId().toString())) {
            ServerCosmetics.LOGGER.warn("Invalid baseMaterialId '{}' for item '{}'. Defaulting to minecraft:paper.", baseMaterialId, cosmeticOrSkinId);
            baseItem = Registries.ITEM.get(Identifier.of("minecraft:paper")); // Fallback
        }

        PolymerModelData polymerModel;
        Integer blockingModelData = null;
        try {
            if (baseItem instanceof ArmorItem armorItem && armorItem.getType() != ArmorItem.Type.BODY) {

                String armorId = cosmeticOrSkinId.replace("_" + armorItem.getType().getName().toLowerCase(), "");

                RuntimeModelManager.requestArmorModel(armorId, armorItem.getType());


                String modelIdPath = "item/armor/" + cosmeticOrSkinId;
                polymerModel = PolymerResourcePackUtils.requestModel(getItemFor(armorItem.getType()), id(modelIdPath));

            } else {
                polymerModel = PolymerResourcePackUtils.requestModel(baseItem, Identifier.of(ServerCosmetics.MOD_ID, "item/" + cosmeticOrSkinId));
                if (baseItem instanceof net.minecraft.item.ShieldItem) {
                    try {
                        PolymerModelData blockingModel = PolymerResourcePackUtils.requestModel(baseItem, Identifier.of(ServerCosmetics.MOD_ID, "item/" + cosmeticOrSkinId + "_blocking"));
                        blockingModelData = blockingModel.value();
                    } catch (Exception ignored) {
                        if (ServerCosmetics.LOGGER.isDebugEnabled()) {
                            ServerCosmetics.LOGGER.debug("No blocking model defined for shield cosmetic '{}'.", cosmeticOrSkinId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            ServerCosmetics.LOGGER.error("Failed to request model for item id '{}' with base item '{}': {}", cosmeticOrSkinId, baseMaterialId, e.getMessage());
            ItemStack errorStack = new ItemStack(baseItem);
            errorStack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Error: " + cosmeticOrSkinId));
            return errorStack;
        }


        ItemStack itemStack = new ItemStack(polymerModel.item());

        itemStack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT,
                comp -> comp.apply(
                        nbt -> nbt.putString(NEW_NBT_KEY_CUSTOM_ITEM_ID, cosmeticOrSkinId))
        );

        Utils.initializeModelData(itemStack, polymerModel.value(), blockingModelData);

        if (baseItem instanceof ArmorItem armorItem && armorItem.getType() != ArmorItem.Type.BODY) {
            String armorId = cosmeticOrSkinId.replace("_" + armorItem.getType().getName().toLowerCase(), "");
            PolymerArmorModel armorModel = PolymerResourcePackUtils.requestArmor(id(armorId));
            itemStack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(armorModel.color(), true));
        }

        if (loreTexts != null && !loreTexts.isEmpty()) {
            itemStack.set(DataComponentTypes.LORE, new LoreComponent(loreTexts));
        } else {
            itemStack.set(DataComponentTypes.LORE, new LoreComponent(Collections.emptyList()));
        }

        itemStack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(polymerModel.value()));
        itemStack.set(DataComponentTypes.CUSTOM_NAME, displayName);

        return itemStack;
    }

    private static Item getItemFor(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> Items.LEATHER_HELMET;
            case CHESTPLATE -> Items.LEATHER_CHESTPLATE;
            case LEGGINGS -> Items.LEATHER_LEGGINGS;
            case BOOTS -> Items.LEATHER_BOOTS;
            default -> Items.STONE;
        };
    }
    // --- Accessor methods ---

    public static CustomItemEntry getCosmetic(String id) {
        CustomItemEntry cosmetic = null;
        for (CustomItemEntry entry : cosmeticsList) {
            if (entry.id().equals(id)) {
                cosmetic = entry;
            }
        }
        return cosmetic;
    }

    public static List<CustomItemEntry> getCosmeticsList() {
        return Collections.unmodifiableList(cosmeticsList);
    }

    public static List<CustomItemEntry> getAllCosmeticsForMaterial(ItemType type, String targetMaterialId) {
        List<CustomItemEntry> filteredList = new ArrayList<>();
        for (CustomItemEntry entry : cosmeticsList) {
            if (entry.type() == type && entry.baseItemForModel().equals(targetMaterialId)) {
                filteredList.add(entry);
            }
        }
        return filteredList;
    }

    public static List<CustomItemEntry> getAllCosmeticsForMaterial(ItemType type, Item item) {
        String targetMaterialId = Registries.ITEM.getId(item).toString();
        return CustomItemRegistry.getAllCosmeticsForMaterial(type, targetMaterialId);
    }

    public static List<CustomItemEntry> getAllCosmeticsForType(ItemType type) {
        List<CustomItemEntry> filteredList = new ArrayList<>();
        for (CustomItemEntry entry : cosmeticsList) {
            if (entry.type() == type) {
                filteredList.add(entry);
            }
        }
        return filteredList;
    }

}