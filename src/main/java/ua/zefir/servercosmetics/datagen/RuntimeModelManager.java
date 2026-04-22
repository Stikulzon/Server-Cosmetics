package ua.zefir.servercosmetics.datagen;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import net.minecraft.item.ArmorItem;
import ua.zefir.servercosmetics.ModInit;

public class RuntimeModelManager {
  private static final Map<String, Set<ArmorItem.Type>> requestedModels = new ConcurrentHashMap<>();

  public static void requestArmorModel(String cosmeticId, ArmorItem.Type type) {
    requestedModels.computeIfAbsent(cosmeticId, k -> ConcurrentHashMap.newKeySet()).add(type);
    ModInit.LOGGER.debug(
        "Requested runtime model generation for cosmetic '{}' of type {}",
        cosmeticId,
        type.getName());
  }

  public static void generateAndProvideModels(BiConsumer<String, byte[]> provider) {

    if (requestedModels.isEmpty()) {
      return;
    }

    ModInit.LOGGER.info(
        "Starting runtime generation of {} cosmetic armor model set(s).", requestedModels.size());

    requestedModels.forEach(
        (cosmeticId, types) -> {
          for (ArmorItem.Type type : types) {
            Map<String, byte[]> models = ArmorModelGenerator.generateModels(cosmeticId, type);
            models.forEach(
                (path, data) -> {
                  if (provider != null) {
                    provider.accept(path, data);
                    ModInit.LOGGER.debug("Provided runtime model: {}", path);
                  }
                });
          }
        });
  }

  public static void clearRequestedModels() {
    requestedModels.clear();
  }
}
