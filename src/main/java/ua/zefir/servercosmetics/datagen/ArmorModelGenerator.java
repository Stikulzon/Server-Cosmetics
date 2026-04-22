package ua.zefir.servercosmetics.datagen;

import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.item.*;
import ua.zefir.servercosmetics.ModInit;

public class ArmorModelGenerator {
  private static final String EMPTY_TEXTURE_PATH = String.valueOf(ModInit.id("item/empty"));

  public static Map<String, byte[]> generateModels(String cosmeticId, ArmorItem.Type armorType) {
    Map<String, byte[]> generatedModels = new HashMap<>();

    ArmorItem dummyArmorItem = getDummyArmorItem(armorType);
    String modelName = cosmeticId + "_" + armorType.getName().toLowerCase();
    String baseModelPath = "assets/servercosmetics/models/item/armor/" + modelName + ".json";

    JsonObject baseModel = createArmorJsonWithOverrides(dummyArmorItem, cosmeticId);
    generatedModels.put(baseModelPath, baseModel.toString().getBytes(StandardCharsets.UTF_8));

    return generatedModels;
  }

  private static ArmorItem getDummyArmorItem(ArmorItem.Type armorType) {
    return switch (armorType) {
      case HELMET -> (ArmorItem) Items.LEATHER_HELMET;
      case CHESTPLATE -> (ArmorItem) Items.LEATHER_CHESTPLATE;
      case LEGGINGS -> (ArmorItem) Items.LEATHER_LEGGINGS;
      case BOOTS -> (ArmorItem) Items.LEATHER_BOOTS;
      default ->
          throw new IllegalStateException(
              "Unsupported armor type for model generation: " + armorType);
    };
  }

  private static JsonObject createArmorJsonWithOverrides(ArmorItem armor, String cosmeticId) {
    JsonObject root = new JsonObject();
    String modelName = cosmeticId + "_" + armor.getType().getName().toLowerCase();

    root.addProperty("parent", "minecraft:item/generated");
    JsonObject textures = new JsonObject();
    textures.addProperty("layer0", EMPTY_TEXTURE_PATH);
    textures.addProperty("layer1", ModInit.MOD_ID + ":item/armor/" + modelName);
    root.add("textures", textures);

    return root;
  }
}
