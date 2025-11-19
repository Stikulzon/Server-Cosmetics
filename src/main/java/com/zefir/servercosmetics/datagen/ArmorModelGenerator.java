package com.zefir.servercosmetics.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.zefir.servercosmetics.ServerCosmetics;
import lombok.Getter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.item.equipment.ArmorMaterials;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.registry.RegistryKey;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class ArmorModelGenerator {
    @Getter
    public enum TrimMaterialSource {
        VANILLA("minecraft");
        private final String namespace;
        TrimMaterialSource(String namespace) { this.namespace = namespace; }
    }

    private record TrimMaterial(
            String name,
            float itemModelIndex,
            Map<RegistryKey<EquipmentAsset>, String> overrideArmorMaterials,
            TrimMaterialSource source
    ) {
        public String getAppliedName(RegistryKey<EquipmentAsset> assetKey) {
            return overrideArmorMaterials.getOrDefault(assetKey, name);
        }
    }

    private static final List<TrimMaterial> ALL_TRIM_MATERIALS = createTrimMaterials();

    private static List<TrimMaterial> createTrimMaterials() {
        return List.of(
                new TrimMaterial("quartz", 0.1F, Map.of(), TrimMaterialSource.VANILLA),
                new TrimMaterial("iron", 0.2F, Map.of(ArmorMaterials.IRON.assetId(), "iron_darker"), TrimMaterialSource.VANILLA),
                new TrimMaterial("netherite", 0.3F, Map.of(ArmorMaterials.NETHERITE.assetId(), "netherite_darker"), TrimMaterialSource.VANILLA),
                new TrimMaterial("redstone", 0.4F, Map.of(), TrimMaterialSource.VANILLA),
                new TrimMaterial("copper", 0.5F, Map.of(), TrimMaterialSource.VANILLA),
                new TrimMaterial("gold", 0.6F, Map.of(ArmorMaterials.GOLD.assetId(), "gold_darker"), TrimMaterialSource.VANILLA),
                new TrimMaterial("emerald", 0.7F, Map.of(), TrimMaterialSource.VANILLA),
                new TrimMaterial("diamond", 0.8F, Map.of(ArmorMaterials.DIAMOND.assetId(), "diamond_darker"), TrimMaterialSource.VANILLA),
                new TrimMaterial("lapis", 0.9F, Map.of(), TrimMaterialSource.VANILLA),
                new TrimMaterial("amethyst", 1.0F, Map.of(), TrimMaterialSource.VANILLA)
        );
    }

    public static Map<String, byte[]> generateModels(String cosmeticId, EquipmentSlot slot) {
        Map<String, byte[]> generatedModels = new HashMap<>();

        Item dummyArmorItem = getDummyArmorItem(slot);
        EquippableComponent equippable = dummyArmorItem.getComponents().get(DataComponentTypes.EQUIPPABLE);

        String modelName = cosmeticId + "_" + slot.getName().toLowerCase();
        String baseModelPath = "assets/servercosmetics/models/item/armor/" + modelName + ".json";
        String baseTexturePath = ServerCosmetics.MOD_ID + ":item/armor/" + modelName;

        // 1. Generate the base model with overrides for each trim
        JsonObject baseModel = createArmorJsonWithOverrides(equippable, cosmeticId, slot);
        generatedModels.put(baseModelPath, baseModel.toString().getBytes(StandardCharsets.UTF_8));

        // 2. Generate a separate model for each trim variant
        for (TrimMaterial trimMaterial : ALL_TRIM_MATERIALS) {
            if (equippable == null || equippable.assetId().isEmpty()) continue;

            String appliedTrimName = trimMaterial.getAppliedName(equippable.assetId().get());
            String trimModelName = modelName + "_" + appliedTrimName + "_trim";
            String trimModelPath = "assets/servercosmetics/models/item/armor/" + trimModelName + ".json";
            String trimTexturePath = "minecraft:trims/items/" + slot.getName() + "_trim_" + appliedTrimName;

            JsonObject trimModelJson = createTrimmedArmorJson(baseTexturePath, trimTexturePath);
            generatedModels.put(trimModelPath, trimModelJson.toString().getBytes(StandardCharsets.UTF_8));
        }

        return generatedModels;
    }

    private static Item getDummyArmorItem(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> Items.LEATHER_HELMET;
            case CHEST -> Items.LEATHER_CHESTPLATE;
            case LEGS -> Items.LEATHER_LEGGINGS;
            case FEET -> Items.LEATHER_BOOTS;
            default -> throw new IllegalStateException("Unsupported armor type for model generation: " + slot);
        };
    }

    private static JsonObject createArmorJsonWithOverrides(EquippableComponent equippableComponent, String cosmeticId, EquipmentSlot slot) {
        JsonObject root = new JsonObject();
        String modelName = cosmeticId + "_" + slot.getName().toLowerCase();

        root.addProperty("parent", "minecraft:item/generated");
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", ServerCosmetics.MOD_ID + ":item/armor/" + modelName);
//        textures.addProperty("layer1", ServerCosmetics.MOD_ID + ":item/armor/" + modelName + "_overlay");
        root.add("textures", textures);

        JsonArray overrides = new JsonArray();
        for (TrimMaterial trimMaterial : ALL_TRIM_MATERIALS) {
            JsonObject override = new JsonObject();
            JsonObject predicate = new JsonObject();
            predicate.addProperty("trim_type", trimMaterial.itemModelIndex());
            override.add("predicate", predicate);

            if (equippableComponent.assetId().isPresent()) {
                String appliedTrimName = trimMaterial.getAppliedName(equippableComponent.assetId().get());
                String trimModelId = ServerCosmetics.MOD_ID + ":item/armor/" + modelName + "_" + appliedTrimName + "_trim";
                override.addProperty("model", trimModelId);
                overrides.add(override);
            }
        }
        root.add("overrides", overrides);

        return root;
    }

    private static JsonObject createTrimmedArmorJson(String layer0, String layer1) {
        JsonObject root = new JsonObject();
        root.addProperty("parent", "minecraft:item/generated");
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", layer0);
        textures.addProperty("layer1", layer1);
        root.add("textures", textures);
        return root;
    }
}
