package ua.zefir.servercosmetics.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import ua.zefir.servercosmetics.ModInit;

public class CustomItemModelGenerator {

  public static Map<String, byte[]> generateSimpleItemModel(String itemId) {
    Map<String, byte[]> models = new HashMap<>();
    String texturePath = ModInit.MOD_ID + ":item/" + itemId;

    JsonObject textureModel = createBaseModelJson("minecraft:item/generated", texturePath);
    models.put(
        "assets/servercosmetics/models/item/" + itemId + ".json",
        textureModel.toString().getBytes(StandardCharsets.UTF_8));

    JsonObject itemDefinition = createSimpleModelDefinition(itemId);
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


    JsonObject itemDefinition = createSimpleModelDefinition(modelName);
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
      String modelLocation = ModInit.MOD_ID + ":item/" + modelId;
    JsonObject root = new JsonObject();
    JsonObject model = new JsonObject();
    model.addProperty("type", "minecraft:model");
    model.addProperty("model", modelLocation);
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

  private static JsonObject createBaseModelJson(String parent, String layer0) {
    JsonObject root = new JsonObject();
    root.addProperty("parent", parent);
    JsonObject textures = new JsonObject();
    textures.addProperty("layer0", layer0);
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

}
