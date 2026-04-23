package ua.zefir.servercosmetics.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.ArmorMaterials;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.registry.RegistryKey;
import ua.zefir.servercosmetics.ModInit;

public class CustomItemModelGenerator {

  // --- Simple Item Logic ---

  public static Map<String, byte[]> generateSimpleItemModel(String itemId) {
    Map<String, byte[]> models = new HashMap<>();
    String texturePath = ModInit.MOD_ID + ":item/" + itemId;
    String modelLocation = ModInit.MOD_ID + ":item/" + itemId;

    JsonObject textureModel = createBaseModelJson("minecraft:item/generated", texturePath);
    models.put(
        "assets/servercosmetics/models/item/" + itemId + ".json",
        textureModel.toString().getBytes(StandardCharsets.UTF_8));

    JsonObject itemDefinition = createSimpleModelDefinition(modelLocation);
    models.put(
        "assets/servercosmetics/items/" + itemId + ".json",
        itemDefinition.toString().getBytes(StandardCharsets.UTF_8));

    return models;
  }

  public static Map<String, byte[]> generateDyeableItemModel(String itemId) {
    Map<String, byte[]> models = new HashMap<>();
    String texturePath = ModInit.MOD_ID + ":item/" + itemId;
    String modelLocation = ModInit.MOD_ID + ":item/" + itemId;

    JsonObject textureModel = createBaseModelJson("minecraft:item/generated", texturePath);
    models.put(
        "assets/servercosmetics/models/item/" + itemId + ".json",
        textureModel.toString().getBytes(StandardCharsets.UTF_8));

    JsonObject itemDefinition = createDyeableModelDefinition(modelLocation);
    models.put(
        "assets/servercosmetics/items/" + itemId + ".json",
        itemDefinition.toString().getBytes(StandardCharsets.UTF_8));

    return models;
  }

  public static Map<String, byte[]> generateArmorModels(String cosmeticId, EquipmentSlot slot) {
    Map<String, byte[]> generatedModels = new HashMap<>();
    Item dummyArmorItem = getDummyArmorItem(slot);
    EquippableComponent equippable =
        dummyArmorItem.getComponents().get(DataComponentTypes.EQUIPPABLE);

    String slotName = slot.getName().toLowerCase();
    String modelName = cosmeticId + "_" + slotName;

    if (slot == EquipmentSlot.CHEST) {
      JsonObject equipmentJson = generateEquipmentDefinition(cosmeticId);
      generatedModels.put(
          "assets/servercosmetics/equipment/" + cosmeticId + ".json",
          equipmentJson.toString().getBytes(StandardCharsets.UTF_8));
    }

    String baseTexturePath = ModInit.MOD_ID + ":item/" + modelName;

    JsonObject baseModelJson = createBaseModelJson("minecraft:item/generated", baseTexturePath);
    generatedModels.put(
        "assets/servercosmetics/models/item/" + modelName + ".json",
        baseModelJson.toString().getBytes(StandardCharsets.UTF_8));

    for (TrimMaterial trim : ALL_TRIM_MATERIALS) {
      if (equippable == null || equippable.assetId().isEmpty()) continue;

      String appliedTrimName = trim.getAppliedName(equippable.assetId().get());
      String trimModelName = modelName + "_" + appliedTrimName + "_trim";
      String trimTexturePath = "minecraft:trims/items/" + slotName + "_trim_" + appliedTrimName;

      JsonObject trimModelJson = createTrimmedModelJson(baseTexturePath, trimTexturePath);
      generatedModels.put(
          "assets/servercosmetics/models/item/" + trimModelName + ".json",
          trimModelJson.toString().getBytes(StandardCharsets.UTF_8));
    }

    JsonObject itemDefinition = createArmorItemDefinition(equippable, modelName);
    generatedModels.put(
        "assets/servercosmetics/items/" + modelName + ".json",
        itemDefinition.toString().getBytes(StandardCharsets.UTF_8));

    return generatedModels;
  }

  // --- JSON Construction Helpers ---

  private static JsonObject generateEquipmentDefinition(String cosmeticId) {
    JsonObject root = new JsonObject();
    JsonObject layers = new JsonObject();

    String textureId = ModInit.MOD_ID + ":" + cosmeticId;

    layers.add("humanoid", createLayerList(textureId));

    layers.add("humanoid_leggings", createLayerList(textureId));

    root.add("layers", layers);
    return root;
  }

  private static JsonArray createLayerList(String textureId) {
    JsonArray list = new JsonArray();
    JsonObject layerEntry = new JsonObject();

    layerEntry.addProperty("texture", textureId);

    list.add(layerEntry);
    return list;
  }

  private static JsonObject createSimpleModelDefinition(String modelId) {
    JsonObject root = new JsonObject();
    JsonObject model = new JsonObject();
    model.addProperty("type", "minecraft:model");
    model.addProperty("model", modelId);
    root.add("model", model);
    return root;
  }

  private static JsonObject createDyeableModelDefinition(String modelId) {
    JsonObject root = new JsonObject();
    JsonObject model = new JsonObject();
    model.addProperty("type", "minecraft:model");
    model.addProperty("model", modelId);

    JsonArray tintsArray = new JsonArray();
    JsonObject tintObject = new JsonObject();
    tintObject.addProperty("type", "minecraft:dye");
    tintObject.addProperty("default", 16777215);
    tintsArray.add(tintObject);

    model.add("tints", tintsArray);
    root.add("model", model);
    return root;
  }

  private static JsonObject createArmorItemDefinition(
      EquippableComponent equippable, String baseModelName) {
    JsonObject root = new JsonObject();
    JsonObject selector = new JsonObject();

    selector.addProperty("type", "minecraft:select");
    selector.addProperty("property", "minecraft:trim_material");

    JsonObject fallback = new JsonObject();
    fallback.addProperty("type", "minecraft:model");
    fallback.addProperty("model", ModInit.MOD_ID + ":item/" + baseModelName);
    selector.add("fallback", fallback);

    JsonArray cases = new JsonArray();
    for (TrimMaterial trim : ALL_TRIM_MATERIALS) {
      if (equippable.assetId().isEmpty()) continue;

      JsonObject caseObj = new JsonObject();
      caseObj.addProperty("when", "minecraft:" + trim.name());

      String appliedTrimName = trim.getAppliedName(equippable.assetId().get());
      String trimModelId =
          ModInit.MOD_ID + ":item/" + baseModelName + "_" + appliedTrimName + "_trim";

      JsonObject modelObj = new JsonObject();
      modelObj.addProperty("type", "minecraft:model");
      modelObj.addProperty("model", trimModelId);

      caseObj.add("model", modelObj);
      cases.add(caseObj);
    }
    selector.add("cases", cases);

    root.add("model", selector);
    return root;
  }

  private static JsonObject createBaseModelJson(String parent, String layer0) {
    JsonObject root = new JsonObject();
    root.addProperty("parent", parent);
    JsonObject textures = new JsonObject();
    textures.addProperty("layer0", layer0);
    root.add("textures", textures);
    return root;
  }

  private static JsonObject createTrimmedModelJson(String layer0, String layer1) {
    JsonObject root = new JsonObject();
    root.addProperty("parent", "minecraft:item/generated");
    JsonObject textures = new JsonObject();
    textures.addProperty("layer0", layer0);
    textures.addProperty("layer1", layer1);
    root.add("textures", textures);
    return root;
  }

  private static Item getDummyArmorItem(EquipmentSlot slot) {
    return switch (slot) {
      case HEAD -> Items.LEATHER_HELMET;
      case CHEST -> Items.LEATHER_CHESTPLATE;
      case LEGS -> Items.LEATHER_LEGGINGS;
      case FEET -> Items.LEATHER_BOOTS;
      default -> throw new IllegalStateException("Unsupported armor type: " + slot);
    };
  }

  // --- Trim Data ---

  private enum TrimMaterialSource {
    VANILLA
  }

  private record TrimMaterial(
      String name,
      Map<RegistryKey<EquipmentAsset>, String> overrideArmorMaterials,
      TrimMaterialSource source) {
    public String getAppliedName(RegistryKey<EquipmentAsset> assetKey) {
      return overrideArmorMaterials.getOrDefault(assetKey, name);
    }
  }

  private static final List<TrimMaterial> ALL_TRIM_MATERIALS =
      List.of(
          new TrimMaterial("quartz", Map.of(), TrimMaterialSource.VANILLA),
          new TrimMaterial(
              "iron",
              Map.of(ArmorMaterials.IRON.assetId(), "iron_darker"),
              TrimMaterialSource.VANILLA),
          new TrimMaterial(
              "netherite",
              Map.of(ArmorMaterials.NETHERITE.assetId(), "netherite_darker"),
              TrimMaterialSource.VANILLA),
          new TrimMaterial("redstone", Map.of(), TrimMaterialSource.VANILLA),
          new TrimMaterial("copper", Map.of(), TrimMaterialSource.VANILLA),
          new TrimMaterial(
              "gold",
              Map.of(ArmorMaterials.GOLD.assetId(), "gold_darker"),
              TrimMaterialSource.VANILLA),
          new TrimMaterial("emerald", Map.of(), TrimMaterialSource.VANILLA),
          new TrimMaterial(
              "diamond",
              Map.of(ArmorMaterials.DIAMOND.assetId(), "diamond_darker"),
              TrimMaterialSource.VANILLA),
          new TrimMaterial("lapis", Map.of(), TrimMaterialSource.VANILLA),
          new TrimMaterial("amethyst", Map.of(), TrimMaterialSource.VANILLA),
          new TrimMaterial("resin", Map.of(), TrimMaterialSource.VANILLA));
}
