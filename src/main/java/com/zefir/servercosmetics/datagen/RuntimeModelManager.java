package com.zefir.servercosmetics.datagen;

import com.zefir.servercosmetics.ServerCosmetics;
import net.minecraft.entity.EquipmentSlot;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class RuntimeModelManager {
    private static final Map<String, Set<EquipmentSlot>> requestedArmorModels = new ConcurrentHashMap<>();
    private static final Set<String> requestedItemModels = ConcurrentHashMap.newKeySet();

    public static void requestArmorModel(String cosmeticId, EquipmentSlot slot) {
        requestedArmorModels.computeIfAbsent(cosmeticId, k -> ConcurrentHashMap.newKeySet()).add(slot);
    }

    public static void requestItemModel(String cosmeticId) {
        requestedItemModels.add(cosmeticId);
    }

    public static void clearRequestedModels() {
        requestedArmorModels.clear();
        requestedItemModels.clear();
    }

    public static void generateAndProvideModels(BiConsumer<String, byte[]> provider) {
        if (requestedArmorModels.isEmpty() && requestedItemModels.isEmpty()) {
            return;
        }

        ServerCosmetics.LOGGER.info("Generating runtime models: {} armor sets, {} simple items.",
                requestedArmorModels.size(), requestedItemModels.size());

        for (String itemId : requestedItemModels) {
            Map<String, byte[]> models = CustomItemModelGenerator.generateSimpleItemModel(itemId);
            provideModels(models, provider);
        }

        requestedArmorModels.forEach((cosmeticId, equipmentSlots) -> {
            for (EquipmentSlot slot : equipmentSlots) {
                Map<String, byte[]> models = CustomItemModelGenerator.generateArmorModels(cosmeticId, slot);
                provideModels(models, provider);
            }
        });
    }

    private static void provideModels(Map<String, byte[]> models, BiConsumer<String, byte[]> provider) {
        if (provider == null) return;
        models.forEach(provider::accept);
    }
}