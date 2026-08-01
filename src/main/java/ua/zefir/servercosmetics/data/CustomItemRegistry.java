package ua.zefir.servercosmetics.data;

import static ua.zefir.servercosmetics.ModInit.id;
import static ua.zefir.servercosmetics.datafixer.NbtDataFixer.NEW_NBT_KEY_CUSTOM_ITEM_ID;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.simpleyaml.configuration.file.YamlFile;
import ua.zefir.servercosmetics.ModInit;
import ua.zefir.servercosmetics.config.MainConfig;
import ua.zefir.servercosmetics.util.ItemBuilder;
import ua.zefir.servercosmetics.util.Utils;

public class CustomItemRegistry {

  private static final CustomItemCatalog catalog = new CustomItemCatalog();

  private static boolean legacyMode = false;

  public static void setLegacyMode(boolean legacyMode) {
    CustomItemRegistry.legacyMode = legacyMode;
  }

  public static void initialize() {
    loadAllCosmetics();
    loadAllItemSkins();
  }

  public static void reloadAll() {
    catalog.clear();
    initialize();
    materializeAll();
  }

  public static void reloadCosmetics() {
    catalog.removeIf(entry -> entry.type() != ItemType.ITEM_SKIN);
    loadAllCosmetics();
    materializeAll();
  }

  public static void reloadItemSkins() {
    catalog.removeIf(entry -> entry.type() == ItemType.ITEM_SKIN);
    loadAllItemSkins();
    materializeAll();
  }

  private static void loadAllCosmetics() {
    Path cosmeticsDir = MainConfig.SERVER_COSMETICS_DIR.resolve("Cosmetics");
    loadItemsFromDirectory(cosmeticsDir, "cosmetic-item");
  }

  private static void loadAllItemSkins() {
    Path itemSkinsDir = MainConfig.SERVER_COSMETICS_DIR.resolve("ItemSkins");
    loadItemsFromDirectory(itemSkinsDir, null);
  }

  private static void loadItemsFromDirectory(Path directory, String itemPropertiesRootNode) {
    CustomItemDefinitionLoader.load(
        directory, itemPropertiesRootNode, CustomItemRegistry::processItemFile);
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
    Component displayName = parseDisplayName(yaml, ItemType.ITEM_SKIN, fileName, null);
    List<Component> lore = parseLore(yaml, ItemType.ITEM_SKIN, null);
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

      Supplier<ItemStack> itemStack =
          () ->
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
      catalog.add(entry);
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
    Component displayName = parseDisplayName(yaml, type, fileName, itemPropertiesRootNode);
    List<Component> lore = parseLore(yaml, type, itemPropertiesRootNode);
    boolean dyeable = yaml.getBoolean("dyeable", yaml.getBoolean("dyable", false));
    int sortingPriority = yaml.getInt("sortingPriority", 0);

    String baseItemMaterial = parseBaseMaterial(yaml, type, itemPropertiesRootNode);
    if (baseItemMaterial == null) {
      ModInit.LOGGER.debug(
          "'material' not defined in cosmetic {}, using paper as fallback", fileName);
      baseItemMaterial = "minecraft:paper";
    }

    String formattedMaterial = formatMaterialId(baseItemMaterial);

    Supplier<ItemStack> itemStack =
        () ->
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
    catalog.add(entry);
  }

  private static CustomItemEntry createCosmeticEntry(
      YamlFile yaml,
      String itemId,
      String permission,
      Component displayName,
      List<Component> lore,
      Supplier<ItemStack> itemStack,
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

  private static Component parseDisplayName(
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

  private static List<Component> parseLore(
      YamlFile yaml, ItemType type, String itemPropertiesRootNode) {
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
    return catalog.get(id);
  }

  public static List<CustomItemEntry> getCosmeticsList() {
    return catalog.entries();
  }

  public static List<CustomItemEntry> getAllCosmeticsForMaterial(
      ItemType type, String targetMaterialId) {
    return catalog.forMaterial(type, targetMaterialId);
  }

  public static List<ItemStack> getAllCosmeticItemStacks() {
    return catalog.itemStacks();
  }

  public static void materializeAll() {
    catalog.materializeAll();
  }

  public static List<CustomItemEntry> getAllCosmeticsForMaterial(ItemType type, Item item) {
    String targetMaterialId = BuiltInRegistries.ITEM.getKey(item).toString();
    return CustomItemRegistry.getAllCosmeticsForMaterial(type, targetMaterialId);
  }

  public static List<CustomItemEntry> getAllCosmeticsForType(ItemType type) {
    return catalog.forType(type);
  }
}
