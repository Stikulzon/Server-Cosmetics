package ua.zefir.servercosmetics.datagen;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import net.minecraft.item.ArmorItem;
import ua.zefir.servercosmetics.ModInit;

public class RuntimeModelManager {
  private static final Map<String, Set<ArmorItem.Type>> requestedModels = new ConcurrentHashMap<>();

  /**
   * Submits a request to generate an armor model at runtime. This should be called when an armor
   * cosmetic is loaded from the config.
   *
   * @param cosmeticId The unique ID of the cosmetic set (e.g., "magma_armor").
   * @param type The type of armor piece (e.g., HELMET).
   */
  public static void requestArmorModel(String cosmeticId, ArmorItem.Type type) {
    requestedModels.computeIfAbsent(cosmeticId, k -> ConcurrentHashMap.newKeySet()).add(type);
    ModInit.LOGGER.debug(
        "Requested runtime model generation for cosmetic '{}' of type {}",
        cosmeticId,
        type.getName());
  }

  /**
   * Generates all requested models and provides them to the given consumer. This is called during
   * the Polymer resource pack creation event.
   *
   * @param provider A consumer that accepts a resource path and the corresponding file data.
   */
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

    //        requestedModels.clear();
  }

  public static void clearRequestedModels() {
    requestedModels.clear();
  }
}
