package ua.zefir.servercosmetics.datagen;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import net.minecraft.entity.EquipmentSlot;
import ua.zefir.servercosmetics.ModInit;

public class RuntimeModelManager {
  private static final Map<String, Set<EquipmentSlot>> requestedArmorModels =
      new ConcurrentHashMap<>();
  private static final Set<String> requestedItemModels = ConcurrentHashMap.newKeySet();
  private static final Set<String> requestedDyeableItemModels = ConcurrentHashMap.newKeySet();

  public static void requestArmorModel(String cosmeticId, EquipmentSlot slot) {
    requestedArmorModels.computeIfAbsent(cosmeticId, k -> ConcurrentHashMap.newKeySet()).add(slot);
  }

  public static void requestItemModel(String modelId, boolean dyeable) {
    if (dyeable) {
      requestedDyeableItemModels.add(modelId);
    } else {
      requestedItemModels.add(modelId);
    }
  }

  public static void clearRequestedModels() {
    requestedArmorModels.clear();
    requestedItemModels.clear();
    requestedDyeableItemModels.clear();
  }

  public static void generateAndProvideModels(BiConsumer<String, byte[]> provider) {
    if (requestedArmorModels.isEmpty() && requestedItemModels.isEmpty()) {
      return;
    }

    ModInit.LOGGER.info(
        "Generating runtime models: {} armor sets, {} simple items.",
        requestedArmorModels.size(),
        requestedItemModels.size());

    for (String modelId : requestedItemModels) {
      Map<String, byte[]> models = CustomItemModelGenerator.generateSimpleItemModel(modelId);
      provideModels(models, provider);
    }

    for (String modelId : requestedDyeableItemModels) {
      Map<String, byte[]> models = CustomItemModelGenerator.generateDyeableItemModel(modelId);
      provideModels(models, provider);
    }

    requestedArmorModels.forEach(
        (modelId, equipmentSlots) -> {
          for (EquipmentSlot slot : equipmentSlots) {
            Map<String, byte[]> models =
                CustomItemModelGenerator.generateArmorModels(modelId, slot);
            provideModels(models, provider);
          }
        });
  }

  private static void provideModels(
      Map<String, byte[]> models, BiConsumer<String, byte[]> provider) {
    if (provider == null) return;
    models.forEach(provider);
  }
}
