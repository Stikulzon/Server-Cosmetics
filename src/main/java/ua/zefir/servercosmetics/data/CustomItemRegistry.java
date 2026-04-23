package ua.zefir.servercosmetics.data;

import static ua.zefir.servercosmetics.ModInit.id;
import static ua.zefir.servercosmetics.datafixer.NbtDataFixer.NEW_NBT_KEY_CUSTOM_ITEM_ID;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.component.type.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import org.simpleyaml.configuration.file.YamlFile;
import ua.zefir.servercosmetics.ModInit;
import ua.zefir.servercosmetics.config.ConfigManager;
import ua.zefir.servercosmetics.util.ItemBuilder;
import ua.zefir.servercosmetics.util.Utils;

public class CustomItemRegistry {

  private static final List<CustomItemEntry> cosmeticsList = new CopyOnWriteArrayList<>();

  private static boolean legacyMode = false;

  public static void setLegacyMode(boolean legacyMode) {
    CustomItemRegistry.legacyMode = legacyMode;
  }

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

  private static void processItemFile(Path filePath, String itemPropertiesRootNode)
      throws IOException {
    YamlFile yamlFile = new YamlFile(filePath.toAbsolutePath().toString());
    yamlFile.load();

    String fileName = filePath.getFileName().toString();
    String itemId = fileName.substring(0, fileName.lastIndexOf('.'));
    itemId =
        itemId
            .replace("_helmet", "_head")
            .replace("_chestplate", "_chest")
            .replace("_leggings", "_legs")
            .replace("_boots", "_feet");

    String typeStr = yamlFile.getString("type");
    boolean isItemSkin =
        typeStr == null || ItemType.valueOf(typeStr.toUpperCase()) == ItemType.ITEM_SKIN;

    String permission = yamlFile.getString("permission");
    if (permission == null || permission.isEmpty()) {
      //            ModInit.LOGGER.error("Error loading {}: 'permission' not defined.", fileName);
      if (isItemSkin) {
        permission = ModInit.MOD_ID + ".item_skin." + itemId;
      } else {
        permission = ModInit.MOD_ID + "." + typeStr.toLowerCase() + "." + itemId;
      }
    }

    if (isItemSkin) {
      loadItemSkin(yamlFile, itemId, permission, fileName);
    } else {
      loadCosmetic(yamlFile, itemId, permission, fileName, itemPropertiesRootNode, typeStr);
    }
  }

  private static void loadItemSkin(
      YamlFile yaml, String itemId, String permission, String fileName) {
    Text displayName = parseDisplayName(yaml, ItemType.ITEM_SKIN, fileName, null);
    List<Text> lore = parseLore(yaml, ItemType.ITEM_SKIN, null);
    boolean dyeable = yaml.getBoolean("dyeable", yaml.getBoolean("dyable", false));
    int sortingPriority = yaml.getInt("sortingPriority", 0);

    List<String> targetMaterials = yaml.getStringList("material");
    if (targetMaterials.isEmpty()) {
      Optional.ofNullable(yaml.getString("material")).ifPresent(targetMaterials::add);
    }

    if (targetMaterials.isEmpty()) {
      ModInit.LOGGER.error(
          "Error loading item skin {}: 'material' (list or string) not defined.", fileName);
      return;
    }

    for (String materialKey : targetMaterials) {
      String formattedMaterial = formatMaterialId(materialKey);

      ItemStack itemStack =
          ItemBuilder.fromId(formattedMaterial)
              .applyCosmeticLogic(itemId, dyeable)
              .name(displayName)
              .lore(lore)
              .customData(NEW_NBT_KEY_CUSTOM_ITEM_ID, itemId + "_" + formattedMaterial)
              .build();

      CustomItemEntry entry =
          new CustomItemEntry(
              itemId + "_" + formattedMaterial,
              permission,
              displayName,
              lore,
              itemStack,
              ItemType.ITEM_SKIN,
              formattedMaterial,
              sortingPriority,
              dyeable,
              new ArrayList<>(),
              null);
      cosmeticsList.add(entry);
    }
  }

  private static void loadCosmetic(
      YamlFile yaml,
      String itemId,
      String permission,
      String fileName,
      String itemPropertiesRootNode,
      String typeStr) {
    ItemType type = ItemType.valueOf(typeStr.toUpperCase());
    Text displayName = parseDisplayName(yaml, type, fileName, itemPropertiesRootNode);
    List<Text> lore = parseLore(yaml, type, itemPropertiesRootNode);
    boolean dyeable = yaml.getBoolean("dyeable", yaml.getBoolean("dyable", false));
    int sortingPriority = yaml.getInt("sortingPriority", 0);

    String baseItemMaterial = parseBaseMaterial(yaml, type, itemPropertiesRootNode);
    if (baseItemMaterial == null) {
      ModInit.LOGGER.debug(
          "'material' not defined in cosmetic {}, using paper as fallback", fileName);
      baseItemMaterial = "minecraft:paper";
    }

    String formattedMaterial = formatMaterialId(baseItemMaterial);

    ItemStack itemStack =
        ItemBuilder.fromId(formattedMaterial)
            .applyCosmeticLogic(itemId, dyeable)
            .name(displayName)
            .lore(lore)
            .customData(NEW_NBT_KEY_CUSTOM_ITEM_ID, itemId)
            .build();

    CustomItemEntry entry =
        createCosmeticEntry(
            yaml,
            itemId,
            permission,
            displayName,
            lore,
            itemStack,
            type,
            formattedMaterial,
            sortingPriority,
            dyeable);
    cosmeticsList.add(entry);
  }

  private static CustomItemEntry createCosmeticEntry(
      YamlFile yaml,
      String itemId,
      String permission,
      Text displayName,
      List<Text> lore,
      ItemStack itemStack,
      ItemType type,
      String baseItemMaterial,
      int sortingPriority,
      boolean dyeable) {
    BodyCosmeticsData bodyData;
    return switch (type) {
      case BODY_COSMETIC -> {
        bodyData = createBodyCosmeticsData(yaml, itemId);
        yield new CustomItemEntry(
            itemId,
            permission,
            displayName,
            lore,
            itemStack,
            type,
            baseItemMaterial,
            sortingPriority,
            dyeable,
            List.of("entity", "body_cosmetic"),
            bodyData);
      }
      case CHESTPLATE_BODY_COSMETIC, HAT_BODY_COSMETIC, BOOTS_BODY_COSMETIC -> {
        bodyData = createBodyCosmeticsData(yaml, itemId);
        yield new CustomItemEntry(
            itemId,
            permission,
            displayName,
            lore,
            itemStack,
            type,
            baseItemMaterial,
            sortingPriority,
            dyeable,
            List.of("entity", "armor", "body_cosmetic"),
            bodyData);
      }
      case HELMET, CHESTPLATE, LEGGINGS, BOOTS ->
          new CustomItemEntry(
              itemId,
              permission,
              displayName,
              lore,
              itemStack,
              type,
              baseItemMaterial,
              sortingPriority,
              dyeable,
              List.of("armor", "item"),
              new ArmorCosmeticsData());
      default -> // HAT and other types
          new CustomItemEntry(
              itemId,
              permission,
              displayName,
              lore,
              itemStack,
              type,
              baseItemMaterial,
              sortingPriority,
              dyeable,
              List.of("item"),
              null);
    };
  }

  /** Creates a BodyCosmeticsData object from YAML properties. */
  private static BodyCosmeticsData createBodyCosmeticsData(YamlFile yaml, String itemId) {
    return new BodyCosmeticsData(
        id("item/" + itemId + "_sneaking"),
        yaml.getBoolean("isMirrored", false),
        yaml.getBoolean("autoAlignment", false),
        yaml.getBoolean("offsetWhenSneaking", false),
        yaml.getBoolean("autoscale", false));
  }

  private static Text parseDisplayName(
      YamlFile yaml, ItemType type, String fileName, String itemPropertiesRootNode) {
    String name;
    if (type == ItemType.ITEM_SKIN) {
      name = yaml.getString("display-name");
      if (legacyMode && name == null) {
        name = yaml.getString("available-item.display-name");
      }
    } else {
      name =
          Optional.ofNullable(yaml.getString("display-name"))
              .orElse(
                  itemPropertiesRootNode != null
                      ? yaml.getString(itemPropertiesRootNode + ".display-name")
                      : null);
    }

    if (name == null) {
      ModInit.LOGGER.warn(
          "Cosmetic {}: 'display-name' not defined. Using empty display name.", fileName);
      return Utils.formatDisplayName("");
    }
    return Utils.formatDisplayName(name);
  }

  private static List<Text> parseLore(YamlFile yaml, ItemType type, String itemPropertiesRootNode) {
    List<String> loreStrings;
    if (type == ItemType.ITEM_SKIN) {
      loreStrings = yaml.getStringList("lore");
      if (legacyMode && loreStrings.isEmpty()) {
        loreStrings = yaml.getStringList("available-item.lore");
      }
    } else {
      loreStrings = yaml.getStringList("lore");
      if (loreStrings.isEmpty()
          && itemPropertiesRootNode != null
          && yaml.isList(itemPropertiesRootNode + ".lore")) {
        loreStrings = yaml.getStringList(itemPropertiesRootNode + ".lore");
      }
    }
    return loreStrings.stream().map(Utils::formatDisplayName).toList();
  }

  private static String parseBaseMaterial(
      YamlFile yaml, ItemType type, String itemPropertiesRootNode) {
    return switch (type) {
      case HELMET, CHESTPLATE, LEGGINGS, BOOTS -> "leather_" + type.name().toLowerCase();
      default ->
          Optional.ofNullable(yaml.getString("material"))
              .orElse(
                  itemPropertiesRootNode != null
                      ? yaml.getString(itemPropertiesRootNode + ".material")
                      : null);
    };
  }

  private static String formatMaterialId(String materialId) {
    if (materialId != null && !materialId.contains(":")) {
      return "minecraft:" + materialId.toLowerCase();
    }
    return materialId;
  }

  // --- Accessor methods ---

  public static CustomItemEntry getCosmetic(String id) {
    for (CustomItemEntry entry : cosmeticsList) {
      if (entry.id().equals(id)) return entry;
    }
    return null;
  }

  public static List<CustomItemEntry> getCosmeticsList() {
    return Collections.unmodifiableList(cosmeticsList);
  }

  public static List<CustomItemEntry> getAllCosmeticsForMaterial(
      ItemType type, String targetMaterialId) {
    List<CustomItemEntry> filteredList = new ArrayList<>();
    for (CustomItemEntry entry : cosmeticsList) {
      if (entry.type() == type && entry.baseItemForModel().equals(targetMaterialId)) {
        filteredList.add(entry);
      }
    }
    return filteredList;
  }

  public static List<ItemStack> getAllCosmeticItemStacks() {
    return cosmeticsList.stream().map(CustomItemEntry::itemStack).toList();
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
