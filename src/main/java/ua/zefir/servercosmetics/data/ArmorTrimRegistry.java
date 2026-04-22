package ua.zefir.servercosmetics.data;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.minecraft.item.Item;
import net.minecraft.item.trim.ArmorTrimMaterial;
import net.minecraft.item.trim.ArmorTrimPattern;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import ua.zefir.servercosmetics.ModInit;

public class ArmorTrimRegistry {
  private ArmorTrimRegistry() {}

  private static final String MATERIAL_ASSET_NAME = "cosmetic";
  private static final float CUSTOM_ITEM_MODEL_INDEX = 0.05f;
  private static final Map<String, RegistryEntry<ArmorTrimPattern>> patternEntries =
      new HashMap<>();
  private static RegistryEntry<ArmorTrimMaterial> cosmeticMaterial;
  private static final Map<String, Text> pendingPatterns = new HashMap<>();
  private static boolean registered = false;

  public static void schedulePattern(String armorId, Text displayName) {
    if (!patternEntries.containsKey(armorId)) {
      pendingPatterns.put(armorId, displayName);
    }
  }

  public static void registerCallback() {
    DynamicRegistrySetupCallback.EVENT.register(
        view -> {
          var registryManager = view.asDynamicRegistryManager();
          registerPatterns(registryManager);
          registerMaterial(registryManager);
        });
  }

  private static void registerPatterns(
      net.minecraft.registry.DynamicRegistryManager registryManager) {
    var trimPatternRegistry = registryManager.getOptional(RegistryKeys.TRIM_PATTERN).orElse(null);
    if (trimPatternRegistry == null) {
      ModInit.LOGGER.warn("TRIM_PATTERN registry not available during setup");
      return;
    }

    RegistryEntry<Item> templateItem =
        Registries.ITEM.getEntry(Identifier.of("minecraft", "paper")).orElseThrow();

    for (Map.Entry<String, Text> entry : pendingPatterns.entrySet()) {
      String armorId = entry.getKey();
      Text displayName = entry.getValue();

      Identifier patternId = Identifier.of(ModInit.MOD_ID, armorId);
      if (trimPatternRegistry.getEntry(patternId).isPresent()) {
        patternEntries.put(armorId, trimPatternRegistry.getEntry(patternId).orElseThrow());
        continue;
      }

      ArmorTrimPattern pattern = new ArmorTrimPattern(patternId, templateItem, displayName, false);
      Registry.register(trimPatternRegistry, patternId, pattern);
      patternEntries.put(armorId, trimPatternRegistry.getEntry(patternId).orElseThrow());
      ModInit.LOGGER.debug("Registered trim pattern for armor set: {}", armorId);
    }
    pendingPatterns.clear();
    registered = true;
  }

  private static void registerMaterial(
      net.minecraft.registry.DynamicRegistryManager registryManager) {
    if (cosmeticMaterial != null) {
      return;
    }

    var trimMaterialRegistry = registryManager.getOptional(RegistryKeys.TRIM_MATERIAL).orElse(null);
    if (trimMaterialRegistry == null) {
      ModInit.LOGGER.warn("TRIM_MATERIAL registry not available during setup");
      return;
    }

    Identifier materialId = Identifier.of(ModInit.MOD_ID, MATERIAL_ASSET_NAME);
    if (trimMaterialRegistry.getEntry(materialId).isPresent()) {
      cosmeticMaterial = trimMaterialRegistry.getEntry(materialId).orElseThrow();
      return;
    }

    RegistryEntry<Item> ingredient =
        Registries.ITEM.getEntry(Identifier.of("minecraft", "paper")).orElseThrow();

    ArmorTrimMaterial material =
        new ArmorTrimMaterial(
            MATERIAL_ASSET_NAME,
            ingredient,
            CUSTOM_ITEM_MODEL_INDEX,
            Map.of(),
            Text.literal("Cosmetic"));
    Registry.register(trimMaterialRegistry, materialId, material);
    cosmeticMaterial = trimMaterialRegistry.getEntry(materialId).orElseThrow();
    ModInit.LOGGER.debug("Registered cosmetic trim material");
  }

  public static float getItemModelIndex() {
    return CUSTOM_ITEM_MODEL_INDEX;
  }

  public static RegistryEntry<ArmorTrimPattern> getPattern(String armorId) {
    return patternEntries.get(armorId);
  }

  public static RegistryEntry<ArmorTrimMaterial> getCosmeticMaterial() {
    return cosmeticMaterial;
  }

  public static boolean isRegistered() {
    return registered;
  }

  public static String getMaterialAssetName() {
    return MATERIAL_ASSET_NAME;
  }

  public static String getTrimTexturePath(String armorId, boolean leggings) {
    String suffix = leggings ? "_leggings_" : "_";
    return "trims/models/armor/" + armorId + suffix + MATERIAL_ASSET_NAME;
  }
}
