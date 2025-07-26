package com.zefir.servercosmetics.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zefir.servercosmetics.ServerCosmetics;
import lombok.Getter;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Model;
import net.minecraft.data.client.Models;
import net.minecraft.data.client.TextureMap;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
            Map<RegistryEntry<ArmorMaterial>, String> overrideArmorMaterials,
            TrimMaterialSource source
    ) {
        public String getAppliedName(RegistryEntry<ArmorMaterial> armorMaterial) {
            return overrideArmorMaterials.getOrDefault(armorMaterial, name);
        }
    }

    private static final List<TrimMaterial> ALL_TRIM_MATERIALS = createTrimMaterials();

    private static List<TrimMaterial> createTrimMaterials() {
        return List.of(
                new TrimMaterial("quartz", 0.1F, Map.of(), TrimMaterialSource.VANILLA),
                new TrimMaterial("iron", 0.2F, Map.of(ArmorMaterials.IRON, "iron_darker"), TrimMaterialSource.VANILLA),
                new TrimMaterial("netherite", 0.3F, Map.of(ArmorMaterials.NETHERITE, "netherite_darker"), TrimMaterialSource.VANILLA),
                new TrimMaterial("redstone", 0.4F, Map.of(), TrimMaterialSource.VANILLA),
                new TrimMaterial("copper", 0.5F, Map.of(), TrimMaterialSource.VANILLA),
                new TrimMaterial("gold", 0.6F, Map.of(ArmorMaterials.GOLD, "gold_darker"), TrimMaterialSource.VANILLA),
                new TrimMaterial("emerald", 0.7F, Map.of(), TrimMaterialSource.VANILLA),
                new TrimMaterial("diamond", 0.8F, Map.of(ArmorMaterials.DIAMOND, "diamond_darker"), TrimMaterialSource.VANILLA),
                new TrimMaterial("lapis", 0.9F, Map.of(), TrimMaterialSource.VANILLA),
                new TrimMaterial("amethyst", 1.0F, Map.of(), TrimMaterialSource.VANILLA)
        );
    }


    /**
     * Generates all necessary item model JSONs for a single piece of custom armor.
     *
     * @param cosmeticId The ID of the cosmetic, e.g., "magma_armor".
     * @param armorType  The type of armor piece.
     * @return A map where the key is the resource pack path and the value is the JSON file content.
     */
    public static Map<String, byte[]> generateModels(String cosmeticId, ArmorItem.Type armorType) {
        Map<String, byte[]> generatedModels = new HashMap<>();

        ArmorItem dummyArmorItem = getDummyArmorItem(armorType);
        String modelName = cosmeticId + "_" + armorType.getName().toLowerCase();
        String baseModelPath = "assets/servercosmetics/models/item/armor/" + modelName + ".json";
        String baseTexturePath = ServerCosmetics.MOD_ID + ":item/armor/" + modelName;

        // 1. Generate the base model with overrides for each trim
        JsonObject baseModel = createArmorJsonWithOverrides(dummyArmorItem, cosmeticId);
        generatedModels.put(baseModelPath, baseModel.toString().getBytes(StandardCharsets.UTF_8));

        // 2. Generate a separate model for each trim variant
        for (TrimMaterial trimMaterial : ALL_TRIM_MATERIALS) {
            String appliedTrimName = trimMaterial.getAppliedName(dummyArmorItem.getMaterial());
            String trimModelName = modelName + "_" + appliedTrimName + "_trim";
            String trimModelPath = "assets/servercosmetics/models/item/armor/" + trimModelName + ".json";
            String trimTexturePath = "minecraft:trims/items/" + armorType.getName() + "_trim_" + appliedTrimName;

            JsonObject trimModelJson = createTrimmedArmorJson(baseTexturePath, trimTexturePath);
            generatedModels.put(trimModelPath, trimModelJson.toString().getBytes(StandardCharsets.UTF_8));
        }

        return generatedModels;
    }

    private static ArmorItem getDummyArmorItem(ArmorItem.Type armorType) {
        return switch (armorType) {
            case HELMET -> (ArmorItem) Items.LEATHER_HELMET;
            case CHESTPLATE -> (ArmorItem) Items.LEATHER_CHESTPLATE;
            case LEGGINGS -> (ArmorItem) Items.LEATHER_LEGGINGS;
            case BOOTS -> (ArmorItem) Items.LEATHER_BOOTS;
            default -> throw new IllegalStateException("Unsupported armor type for model generation: " + armorType);
        };
    }

    private static JsonObject createArmorJsonWithOverrides(ArmorItem armor, String cosmeticId) {
        JsonObject root = new JsonObject();
        String modelName = cosmeticId + "_" + armor.getType().getName().toLowerCase();

        root.addProperty("parent", "minecraft:item/generated");
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", ServerCosmetics.MOD_ID + ":item/armor/" + modelName);
        textures.addProperty("layer1", ServerCosmetics.MOD_ID + ":item/armor/" + modelName + "_overlay");
        root.add("textures", textures);

        JsonArray overrides = new JsonArray();
        for (TrimMaterial trimMaterial : ALL_TRIM_MATERIALS) {
            JsonObject override = new JsonObject();
            JsonObject predicate = new JsonObject();
            predicate.addProperty("trim_type", trimMaterial.itemModelIndex());
            override.add("predicate", predicate);

            String appliedTrimName = trimMaterial.getAppliedName(armor.getMaterial());
            String trimModelId = ServerCosmetics.MOD_ID + ":item/armor/" + modelName + "_" + appliedTrimName + "_trim";
            override.addProperty("model", trimModelId);
            overrides.add(override);
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
