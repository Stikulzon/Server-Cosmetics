package ua.zefir.servercosmetics.data;

import ua.zefir.servercosmetics.ModInit;
import ua.zefir.servercosmetics.config.ConfigManager;
import ua.zefir.servercosmetics.datagen.RuntimeModelManager;
import ua.zefir.servercosmetics.util.Utils;
import lombok.Setter;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static ua.zefir.servercosmetics.ModInit.id;
import static ua.zefir.servercosmetics.datafixer.NbtDatafixer.NEW_NBT_KEY_CUSTOM_ITEM_ID;

public class CustomItemRegistry {

    private static final List<CustomItemEntry> cosmeticsList = new CopyOnWriteArrayList<>();

    @Setter
    private static boolean legacyMode = false;

    public static void initialize() {
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

    /**
     * Loads all .yml item definition files from a given directory.
     *
     * @param directory              The directory to scan for .yml files.
     * @param itemPropertiesRootNode The root YAML node for legacy cosmetic properties. Can be null.
     */
    private static void loadItemsFromDirectory(Path directory, String itemPropertiesRootNode) {
        if (!setupDirectory(directory)) return;

        List<Path> files = Utils.listFiles(directory);
        for (Path filePath : files) {
            if (!filePath.toString().toLowerCase().endsWith(".yml")) continue;

            try {
                processItemFile(filePath, itemPropertiesRootNode);
            } catch (Exception e) {
                ModInit.LOGGER.error("Failed to load custom item from file: {}", filePath, e);
            }
        }
    }

    /**
     * Sets up the directory, creating it if it doesn't exist.
     *
     * @param directory The path to the directory.
     * @return True if the directory exists and is valid, false otherwise.
     */
    private static boolean setupDirectory(Path directory) {
        try {
            if (Files.notExists(directory)) {
                Files.createDirectories(directory);
                ModInit.LOGGER.info("Created directory: {}", directory.toAbsolutePath());
                return false;
            }
            if (!Files.isDirectory(directory)) {
                ModInit.LOGGER.error("Path is not a directory: {}", directory.toAbsolutePath());
                return false;
            }
            return true;
        } catch (IOException e) {
            ModInit.LOGGER.error("Failed to create or access directory: {}", directory, e);
            return false;
        }
    }

    /**
     * Processes a single YAML item definition file.
     *
     * @param filePath               The path to the .yml file.
     * @param itemPropertiesRootNode The legacy root node for cosmetic properties.
     * @throws IOException If the file cannot be loaded.
     */
    private static void processItemFile(Path filePath, String itemPropertiesRootNode) throws IOException {
        YamlFile yamlFile = new YamlFile(filePath.toAbsolutePath().toString());
        yamlFile.load();

        String fileName = filePath.getFileName().toString();
        String itemId = fileName.substring(0, fileName.lastIndexOf('.'));
        itemId = itemId.replace("_helmet", "_head").replace("_chestplate", "_chest").replace("_leggings", "_legs").replace("_boots", "_feet");

        String permission = yamlFile.getString("permission");
        if (permission == null) {
            ModInit.LOGGER.error("Error loading {}: 'permission' not defined.", fileName);
            return;
        }

        String typeStr = yamlFile.getString("type");
        boolean isItemSkin = typeStr == null || ItemType.valueOf(typeStr.toUpperCase()) == ItemType.ITEM_SKIN;

        if (isItemSkin) {
            loadItemSkin(yamlFile, itemId, permission, fileName);
        } else {
            loadCosmetic(yamlFile, itemId, permission, fileName, itemPropertiesRootNode, typeStr);
        }
    }

    /**
     * Loads and registers an Item Skin from its parsed YAML configuration.
     */
    private static void loadItemSkin(YamlFile yaml, String itemId, String permission, String fileName) {
        Text displayName = parseDisplayName(yaml, ItemType.ITEM_SKIN, fileName, null);
        List<Text> lore = parseLore(yaml, ItemType.ITEM_SKIN, null);
        boolean dyable = yaml.getBoolean("dyable");
        int sortingPriority = yaml.getInt("sortingPriority", 0);

        List<String> targetMaterials = yaml.getStringList("material");
        if (targetMaterials.isEmpty()) {
            Optional.ofNullable(yaml.getString("material")).ifPresent(targetMaterials::add);
        }

        if (targetMaterials.isEmpty()) {
            ModInit.LOGGER.error("Error loading item skin {}: 'material' (list or string) not defined.", fileName);
            return;
        }

        for (String materialKey : targetMaterials) {
            String formattedMaterial = formatMaterialId(materialKey);
            ItemStack itemStack = createItemStack(formattedMaterial, displayName, itemId, lore, dyable);
            CustomItemEntry entry = new CustomItemEntry(itemId + "_" + formattedMaterial, permission, displayName, lore, itemStack, ItemType.ITEM_SKIN, formattedMaterial, sortingPriority, dyable, new ArrayList<>(), null);
            cosmeticsList.add(entry);
        }
    }

    /**
     * Loads and registers a Cosmetic Item from its parsed YAML configuration.
     */
    private static void loadCosmetic(YamlFile yaml, String itemId, String permission, String fileName, String itemPropertiesRootNode, String typeStr) {
        ItemType type = ItemType.valueOf(typeStr.toUpperCase());
        Text displayName = parseDisplayName(yaml, type, fileName, itemPropertiesRootNode);
        List<Text> lore = parseLore(yaml, type, itemPropertiesRootNode);
        boolean dyable = yaml.getBoolean("dyable");
        int sortingPriority = yaml.getInt("sortingPriority", 0);

        String baseItemMaterial = parseBaseMaterial(yaml, type, itemPropertiesRootNode);
        if (baseItemMaterial == null) {
            ModInit.LOGGER.error("Error loading cosmetic {}: 'material' not defined, using paper as fallback", fileName);
            baseItemMaterial = "minecraft:paper";
        }

        String formattedMaterial = formatMaterialId(baseItemMaterial);
        ItemStack itemStack = createItemStack(formattedMaterial, displayName, itemId, lore, dyable);
        CustomItemEntry entry = createCosmeticEntry(yaml, itemId, permission, displayName, lore, itemStack, type, formattedMaterial, sortingPriority, dyable);
        cosmeticsList.add(entry);
    }

    /**
     * Factory method to create a CustomItemEntry for a cosmetic item.
     *
     * @return The constructed CustomItemEntry.
     */
    private static CustomItemEntry createCosmeticEntry(YamlFile yaml, String itemId, String permission, Text displayName, List<Text> lore, ItemStack itemStack, ItemType type, String baseItemMaterial, int sortingPriority, boolean dyable) {
        BodyCosmeticsData bodyData;
        return switch (type) {
            case BODY_COSMETIC -> {
                bodyData = createBodyCosmeticsData(yaml, itemId);
                yield new CustomItemEntry(itemId, permission, displayName, lore, itemStack, type, baseItemMaterial, sortingPriority, dyable, List.of(Tags.ENTITY, Tags.BODY_COSMETIC), bodyData);
            }
            case CHESTPLATE_BODY_COSMETIC, HAT_BODY_COSMETIC, BOOTS_BODY_COSMETIC -> {
                bodyData = createBodyCosmeticsData(yaml, itemId);
                yield new CustomItemEntry(itemId, permission, displayName, lore, itemStack, type, baseItemMaterial, sortingPriority, dyable, List.of(Tags.ENTITY, Tags.ARMOR, Tags.BODY_COSMETIC), bodyData);
            }
            case HELMET, CHESTPLATE, LEGGINGS, BOOTS ->
                    new CustomItemEntry(itemId, permission, displayName, lore, itemStack, type, baseItemMaterial, sortingPriority, dyable, List.of(Tags.ARMOR, Tags.ITEM), new ArmorCosmeticsData());
            default -> // HAT and other types
                    new CustomItemEntry(itemId, permission, displayName, lore, itemStack, type, baseItemMaterial, sortingPriority, dyable, List.of(Tags.ITEM), null);
        };
    }

    /**
     * Creates a BodyCosmeticsData object from YAML properties.
     */
    private static BodyCosmeticsData createBodyCosmeticsData(YamlFile yaml, String itemId) {
        return new BodyCosmeticsData(
                id("item/" + itemId + "_sneaking"),
                yaml.getBoolean("isMirrored", false),
                yaml.getBoolean("autoAlignment", false),
                yaml.getBoolean("offsetWhenSneaking", false),
                yaml.getBoolean("autoscale", false)
        );
    }

    /**
     * Parses the display name from YAML, checking primary and legacy paths.
     */
    private static Text parseDisplayName(YamlFile yaml, ItemType type, String fileName, String itemPropertiesRootNode) {
        String name;
        if (type == ItemType.ITEM_SKIN) {
            name = yaml.getString("display-name");
            if (legacyMode && name == null) {
                name = yaml.getString("available-item.display-name");
            }
        } else {
            name = Optional.ofNullable(yaml.getString("display-name"))
                    .orElse(itemPropertiesRootNode != null ? yaml.getString(itemPropertiesRootNode + ".display-name") : null);
        }

        if (name == null) {
            ModInit.LOGGER.warn("Cosmetic {}: 'display-name' not defined. Using empty display name.", fileName);
            return Utils.formatDisplayName("");
        }
        return Utils.formatDisplayName(name);
    }

    /**
     * Parses the lore from YAML, checking primary and legacy paths.
     */
    private static List<Text> parseLore(YamlFile yaml, ItemType type, String itemPropertiesRootNode) {
        List<String> loreStrings;
        if (type == ItemType.ITEM_SKIN) {
            loreStrings = yaml.getStringList("lore");
            if (legacyMode && loreStrings.isEmpty()) {
                loreStrings = yaml.getStringList("available-item.lore");
            }
        } else {
            loreStrings = yaml.getStringList("lore");
            if (loreStrings.isEmpty() && itemPropertiesRootNode != null && yaml.isList(itemPropertiesRootNode + ".lore")) {
                loreStrings = yaml.getStringList(itemPropertiesRootNode + ".lore");
            }
        }
        return loreStrings.stream().map(Utils::formatDisplayName).toList();
    }

    /**
     * Parses the base material from YAML, handling special cases for armor types.
     */
    private static String parseBaseMaterial(YamlFile yaml, ItemType type, String itemPropertiesRootNode) {
        return switch (type) {
            case HELMET, CHESTPLATE, LEGGINGS, BOOTS -> "leather_" + type.name().toLowerCase();
            default -> Optional.ofNullable(yaml.getString("material"))
                    .orElse(itemPropertiesRootNode != null ? yaml.getString(itemPropertiesRootNode + ".material") : null);
        };
    }

    /**
     * Formats a material string to ensure it has a namespace.
     */
    private static String formatMaterialId(String materialId) {
        if (materialId != null && !materialId.contains(":")) {
            return "minecraft:" + materialId.toLowerCase();
        }
        return materialId;
    }

    public static ItemStack createItemStack(String baseMaterialId, Text displayName, String cosmeticOrSkinId, List<Text> loreTexts, boolean dyable) {
        Item baseItem = Registries.ITEM.get(Identifier.of(baseMaterialId));
        if (baseItem == Registries.ITEM.get(Registries.ITEM.getDefaultId()) && !baseMaterialId.equals(Registries.ITEM.getDefaultId().toString())) {
            ModInit.LOGGER.warn("Invalid baseMaterialId '{}' for item '{}'. Defaulting to minecraft:paper.", baseMaterialId, cosmeticOrSkinId);
            baseItem = Registries.ITEM.get(Identifier.of("minecraft:paper")); // Fallback
        }

        ItemStack newStack = new ItemStack(baseItem);
        ComponentMap baseItemComponents = baseItem.getComponents();
        try {
            if (baseItemComponents.get(DataComponentTypes.EQUIPPABLE) instanceof EquippableComponent equippableComponent && equippableComponent.slot() != EquipmentSlot.BODY) {
                // --- Armor Logic ---
                String armorId = cosmeticOrSkinId.replace("_" + equippableComponent.slot().getName().toLowerCase(), "");

                RuntimeModelManager.requestArmorModel(armorId, equippableComponent.slot());

                newStack = getItemFor(equippableComponent.slot()).getDefaultStack();

                Identifier itemModelId = id(cosmeticOrSkinId);
                newStack.set(DataComponentTypes.ITEM_MODEL, itemModelId);

                Identifier armorModelId = id(armorId);
                RegistryKey<EquipmentAsset> layers = RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, armorModelId);

                EquippableComponent oldEquippableComponent = newStack.get(DataComponentTypes.EQUIPPABLE);
                EquippableComponent newEquippableComponent = new EquippableComponent(
                        oldEquippableComponent.slot(),
                        oldEquippableComponent.equipSound(),
                        Optional.of(layers),
                        oldEquippableComponent.cameraOverlay(),
                        oldEquippableComponent.allowedEntities(),
                        oldEquippableComponent.dispensable(),
                        oldEquippableComponent.swappable(),
                        oldEquippableComponent.damageOnHurt(),
                        oldEquippableComponent.equipOnInteract(),
                        oldEquippableComponent.canBeSheared(),
                        oldEquippableComponent.shearingSound()
                );
                newStack.set(DataComponentTypes.EQUIPPABLE, newEquippableComponent);

            } else {
                // --- Standard Item Logic ---
                RuntimeModelManager.requestItemModel(cosmeticOrSkinId, dyable);
                if (dyable) {
                    newStack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(16777215));
                }

                Identifier modelId = id(cosmeticOrSkinId);
                newStack.set(DataComponentTypes.ITEM_MODEL, modelId);
            }
        } catch (Exception e) {
            ModInit.LOGGER.error("Failed to request model for item id '{}' with base item '{}': {}", cosmeticOrSkinId, baseMaterialId, e.getMessage());
            ItemStack errorStack = new ItemStack(baseItem);
            errorStack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Error: " + cosmeticOrSkinId));
            return errorStack;
        }

        if (loreTexts != null && !loreTexts.isEmpty()) {
            newStack.set(DataComponentTypes.LORE, new LoreComponent(loreTexts));
        } else {
            newStack.set(DataComponentTypes.LORE, new LoreComponent(Collections.emptyList()));
        }

        newStack.set(DataComponentTypes.CUSTOM_NAME, displayName);


        newStack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, comp ->
                comp.apply(nbt -> nbt.putString(NEW_NBT_KEY_CUSTOM_ITEM_ID, cosmeticOrSkinId))
        );

        return newStack;
    }

    private static Item getItemFor(EquipmentSlot type) {
        return switch (type) {
            case EquipmentSlot.HEAD -> Items.LEATHER_HELMET;
            case EquipmentSlot.CHEST -> Items.LEATHER_CHESTPLATE;
            case EquipmentSlot.LEGS -> Items.LEATHER_LEGGINGS;
            case EquipmentSlot.FEET -> Items.LEATHER_BOOTS;
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

    public static List<ItemStack> getAllCosmeticItemStacks() {
        List<ItemStack> filteredList = new ArrayList<>();
        cosmeticsList.forEach(entry -> filteredList.add(entry.itemStack()));
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