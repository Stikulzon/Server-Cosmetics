package com.zefir.servercosmetics.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.zefir.servercosmetics.ServerCosmetics;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.ArmorMaterials;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.registry.RegistryKey;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomItemModelGenerator {

    // --- Simple Item Logic ---

    public static Map<String, byte[]> generateSimpleItemModel(String cosmeticId) {
        Map<String, byte[]> models = new HashMap<>();
        String texturePath = ServerCosmetics.MOD_ID + ":item/" + cosmeticId;
        String modelLocation = ServerCosmetics.MOD_ID + ":item/" + cosmeticId;

        JsonObject textureModel = createBaseModelJson("minecraft:item/generated", texturePath);
        models.put("assets/servercosmetics/models/item/" + cosmeticId + ".json",
                textureModel.toString().getBytes(StandardCharsets.UTF_8));

        JsonObject itemDefinition = createSimpleModelDefinition(modelLocation);
        models.put("assets/servercosmetics/items/" + cosmeticId + ".json",
                itemDefinition.toString().getBytes(StandardCharsets.UTF_8));

        return models;
    }

    // --- Armor Logic (Updated for 1.21.2+) ---

    public static Map<String, byte[]> generateArmorModels(String cosmeticId, EquipmentSlot slot) {
        Map<String, byte[]> generatedModels = new HashMap<>();
        Item dummyArmorItem = getDummyArmorItem(slot);
        EquippableComponent equippable = dummyArmorItem.getComponents().get(DataComponentTypes.EQUIPPABLE);

        String slotName = slot.getName().toLowerCase();
        String modelName = cosmeticId + "_" + slotName;

        String baseModelLocation = ServerCosmetics.MOD_ID + ":item/" + modelName;
        String baseTexturePath = ServerCosmetics.MOD_ID + ":item/" + modelName;

        JsonObject baseModelJson = createBaseModelJson("minecraft:item/generated", baseTexturePath);
        generatedModels.put("assets/servercosmetics/models/item/" + modelName + ".json",
                baseModelJson.toString().getBytes(StandardCharsets.UTF_8));

        for (TrimMaterial trim : ALL_TRIM_MATERIALS) {
            if (equippable == null || equippable.assetId().isEmpty()) continue;

            String appliedTrimName = trim.getAppliedName(equippable.assetId().get());
            String trimModelName = modelName + "_" + appliedTrimName + "_trim";

            String trimTexturePath = "minecraft:trims/items/" + slotName + "_trim_" + appliedTrimName;

            JsonObject trimModelJson = createTrimmedModelJson(baseTexturePath, trimTexturePath);
            generatedModels.put("assets/servercosmetics/models/item/" + trimModelName + ".json",
                    trimModelJson.toString().getBytes(StandardCharsets.UTF_8));
        }

        JsonObject itemDefinition = createArmorItemDefinition(equippable, modelName);
        generatedModels.put("assets/servercosmetics/items/" + modelName + ".json",
                itemDefinition.toString().getBytes(StandardCharsets.UTF_8));

        return generatedModels;
    }

    // --- JSON Construction Helpers ---

    /**
     * Creates: { "model": { "type": "minecraft:model", "model": "..." } }
     */
    private static JsonObject createSimpleModelDefinition(String modelId) {
        JsonObject root = new JsonObject();
        JsonObject model = new JsonObject();
        model.addProperty("type", "minecraft:model");
        model.addProperty("model", modelId);
        root.add("model", model);
        return root;
    }

    /**
     * Creates the complex 1.21.2 definition using "minecraft:select" for trims.
     */
    private static JsonObject createArmorItemDefinition(EquippableComponent equippable, String baseModelName) {
        JsonObject root = new JsonObject();
        JsonObject selector = new JsonObject();

        selector.addProperty("type", "minecraft:select");
        selector.addProperty("property", "minecraft:trim_material");

        JsonObject fallback = new JsonObject();
        fallback.addProperty("type", "minecraft:model");
        fallback.addProperty("model", ServerCosmetics.MOD_ID + ":item/" + baseModelName);
        selector.add("fallback", fallback);

        JsonArray cases = new JsonArray();
        for (TrimMaterial trim : ALL_TRIM_MATERIALS) {
            if (equippable.assetId().isEmpty()) continue;

            JsonObject caseObj = new JsonObject();

            caseObj.addProperty("when", "minecraft:" + trim.name());

            String appliedTrimName = trim.getAppliedName(equippable.assetId().get());
            String trimModelId = ServerCosmetics.MOD_ID + ":item/" + baseModelName + "_" + appliedTrimName + "_trim";

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

    /**
     * Standard item model with 1 layer.
     */
    private static JsonObject createBaseModelJson(String parent, String layer0) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", parent);
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", layer0);
        root.add("textures", textures);
        return root;
    }

    /**
     * Standard item model with 2 layers (Cosmetic + Trim).
     */
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

    public static Map<String, byte[]> generateEquipmentDefinition(String armorSetId) {
        Map<String, byte[]> models = new HashMap<>();

        JsonObject root = new JsonObject();
        JsonObject layers = new JsonObject();
        JsonArray humanoid = new JsonArray();
        JsonObject layerEntry = new JsonObject();

        layerEntry.addProperty("texture", ServerCosmetics.MOD_ID + ":" + armorSetId);

        humanoid.add(layerEntry);
        layers.add("humanoid", humanoid);
        root.add("layers", layers);

        models.put("assets/servercosmetics/equipment/" + armorSetId + ".json",
                root.toString().getBytes(StandardCharsets.UTF_8));

        return models;
    }

    // --- Trim Data definitions ---

    private enum TrimMaterialSource {
        VANILLA
    }

    private record TrimMaterial(
            String name,
            Map<RegistryKey<EquipmentAsset>, String> overrideArmorMaterials,
            TrimMaterialSource source
    ) {
        public String getAppliedName(RegistryKey<EquipmentAsset> assetKey) {
            return overrideArmorMaterials.getOrDefault(assetKey, name);
        }
    }

    private static final List<TrimMaterial> ALL_TRIM_MATERIALS = List.of(
            new TrimMaterial("quartz", Map.of(), TrimMaterialSource.VANILLA),
            new TrimMaterial("iron", Map.of(ArmorMaterials.IRON.assetId(), "iron_darker"), TrimMaterialSource.VANILLA),
            new TrimMaterial("netherite", Map.of(ArmorMaterials.NETHERITE.assetId(), "netherite_darker"), TrimMaterialSource.VANILLA),
            new TrimMaterial("redstone", Map.of(), TrimMaterialSource.VANILLA),
            new TrimMaterial("copper", Map.of(), TrimMaterialSource.VANILLA),
            new TrimMaterial("gold", Map.of(ArmorMaterials.GOLD.assetId(), "gold_darker"), TrimMaterialSource.VANILLA),
            new TrimMaterial("emerald", Map.of(), TrimMaterialSource.VANILLA),
            new TrimMaterial("diamond", Map.of(ArmorMaterials.DIAMOND.assetId(), "diamond_darker"), TrimMaterialSource.VANILLA),
            new TrimMaterial("lapis", Map.of(), TrimMaterialSource.VANILLA),
            new TrimMaterial("amethyst", Map.of(), TrimMaterialSource.VANILLA),
            new TrimMaterial("resin", Map.of(), TrimMaterialSource.VANILLA)
    );
}