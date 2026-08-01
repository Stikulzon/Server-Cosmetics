package ua.zefir.servercosmetics.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import ua.zefir.servercosmetics.ModInit;
import ua.zefir.servercosmetics.data.BodyCosmeticsData;
import ua.zefir.servercosmetics.data.CustomItemEntry;
import ua.zefir.servercosmetics.data.CustomItemRegistry;
import ua.zefir.servercosmetics.data.ItemType;
import ua.zefir.servercosmetics.datagen.RuntimeModelManager;

public class ResourcePackAssetProcessor {

  private static final String TARGET_TEXTURE_PATH = "assets/servercosmetics/textures/";
  private static final String TARGET_MODEL_PATH = "assets/servercosmetics/models/item/";
  private static final String TARGET_ARMOR_TEXTURE_PATH =
      "assets/servercosmetics/textures/entity/equipment/";

  private record PathResolveResult(String fullPath, String correctedFileName) {}

  public static void registerListener() {
    PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(
        builder -> {
          CustomItemRegistry.materializeAll();
          RuntimeModelManager.generateAndProvideModels(builder::addData);
          processResourcePackAssets(builder);
        });
  }

  private static void processResourcePackAssets(ResourcePackBuilder builder) {
    Path resourcePackSourceDir = MainConfig.SERVER_COSMETICS_DIR.resolve("Assets");
    if (!ensureDirectoryExists(resourcePackSourceDir)) {
      return;
    }

    ModInit.LOGGER.info("Scanning for resources in: {}", resourcePackSourceDir.toAbsolutePath());
    try (Stream<Path> pathStream = Files.walk(resourcePackSourceDir)) {
      pathStream.filter(Files::isRegularFile).forEach(path -> processAssetFile(path, builder));
    } catch (IOException e) {
      ModInit.LOGGER.error(
          "Error walking directory {} for resource pack generation",
          resourcePackSourceDir.toAbsolutePath(),
          e);
    }
  }

  private static boolean ensureDirectoryExists(Path path) {
    if (Files.isDirectory(path)) {
      return true;
    }
    if (Files.notExists(path)) {
      try {
        Files.createDirectories(path);
        ModInit.LOGGER.info("Created directory: {}", path.toAbsolutePath());
        return true;
      } catch (IOException e) {
        ModInit.LOGGER.error("Failed to create assets directory: {}", path.toAbsolutePath(), e);
        return false;
      }
    }
    ModInit.LOGGER.warn("Path exists but is not a directory: {}", path);
    return false;
  }

  private static void processAssetFile(Path filePath, ResourcePackBuilder builder) {
    String fileName = filePath.getFileName().toString().toLowerCase(Locale.ROOT);

    try {
      byte[] data = Files.readAllBytes(filePath);
      String baseName = filePath.getFileName().toString();

      if (fileName.endsWith(".png")) {
        processPngAsset(builder, baseName, fileName, data);
      } else if (fileName.endsWith(".json")) {
        processJsonAsset(builder, baseName, fileName, data);
      } else if (fileName.endsWith(".mcmeta")) {
        addData(builder, TARGET_TEXTURE_PATH + "item/" + baseName, baseName, data);
      }
    } catch (IOException e) {
      ModInit.LOGGER.error("Failed to read file {} for resource pack", filePath, e);
    }
  }

  private static void processPngAsset(
      ResourcePackBuilder builder, String fileName, String fileNameLower, byte[] data) {
    PathResolveResult result = resolveTexturePath(fileName, fileNameLower);
    addData(builder, result.fullPath(), result.correctedFileName(), data);
  }

  private static PathResolveResult resolveTexturePath(String fileName, String fileNameLower) {
    String targetBaseDir;
    String finalName = fileName;

    if (fileNameLower.endsWith("_helmet.png")
        || fileNameLower.endsWith("_chestplate.png")
        || fileNameLower.endsWith("_leggings.png")
        || fileNameLower.endsWith("_boots.png")) {

      targetBaseDir = TARGET_TEXTURE_PATH + "item/";
      finalName =
          fileName
              .replace("_helmet.png", "_head.png")
              .replace("_chestplate.png", "_chest.png")
              .replace("_leggings.png", "_legs.png")
              .replace("_boots.png", "_feet.png");

    } else if (fileNameLower.endsWith("_head.png")
        || fileNameLower.endsWith("_chest.png")
        || fileNameLower.endsWith("_legs.png")
        || fileNameLower.endsWith("_feet.png")) {
      targetBaseDir = TARGET_TEXTURE_PATH + "item/";
    } else if (fileNameLower.endsWith("_layer_1.png") || fileNameLower.endsWith("_humanoid.png")) {
      targetBaseDir = TARGET_ARMOR_TEXTURE_PATH + "humanoid/";
      finalName =
          fileName.replace("_armor_layer_1", "").replace("_layer_1", "").replace("_humanoid", "");
    } else if (fileNameLower.endsWith("_layer_2.png")
        || fileNameLower.endsWith("_humanoid_leggings.png")) {
      targetBaseDir = TARGET_ARMOR_TEXTURE_PATH + "humanoid_leggings/";
      finalName =
          fileName
              .replace("_armor_layer_2", "")
              .replace("_layer_2", "")
              .replace("_humanoid_leggings", "");
    } else {
      targetBaseDir = TARGET_TEXTURE_PATH + "item/";
    }

    return new PathResolveResult(targetBaseDir + finalName, finalName);
  }

  private static void processJsonAsset(
      ResourcePackBuilder builder, String fileName, String fileNameLower, byte[] data) {
    String cosmeticId = fileNameLower.substring(0, fileNameLower.lastIndexOf('.'));
    CustomItemEntry cosmeticEntry = CustomItemRegistry.getCosmetic(cosmeticId);

    if (cosmeticEntry != null) {
      ItemType type = cosmeticEntry.type();
      if (EnumSet.of(
              ItemType.BODY_COSMETIC,
              ItemType.CHESTPLATE_BODY_COSMETIC,
              ItemType.LEGGINGS_BODY_COSMETIC,
              ItemType.BOOTS_BODY_COSMETIC)
          .contains(type)) {
        String content = new String(data, StandardCharsets.UTF_8);
        handleBodyCosmeticJson(
            builder, JsonParser.parseString(content).getAsJsonObject(), fileName, cosmeticEntry);
        return;
      }
    }

    addData(builder, TARGET_MODEL_PATH + fileName, fileName, data);
  }

  private static void handleBodyCosmeticJson(
      ResourcePackBuilder builder,
      JsonObject originalJson,
      String fileName,
      CustomItemEntry entry) {
    BodyCosmeticsData data = (BodyCosmeticsData) entry.cosmeticData();

    List<Number> normalRotation = data.mirrored() ? Arrays.asList(0, -180, 0) : null;
    List<Number> sneakingRotation =
        data.mirrored() ? Arrays.asList(-28, -180, 0) : Arrays.asList(-28, 0, 0);
    List<Number> scale = data.autoscale() ? Arrays.asList(1.45, 1.45, 1.45) : null;
    List<Number> normalTranslation = null;
    List<Number> sneakingTranslation = null;

    if (data.autoAlignment()) {
      switch (entry.type()) {
        case BODY_COSMETIC, CHESTPLATE_BODY_COSMETIC -> {
          normalTranslation = Arrays.asList(0, -56.5, 2.15);
          sneakingTranslation = Arrays.asList(0, -56.5, 4.15);
        }
        case LEGGINGS_BODY_COSMETIC -> {
          normalTranslation = Arrays.asList(0, -69.25, 2.15);
          sneakingTranslation = Arrays.asList(0, -65, 8.15);
        }
        case BOOTS_BODY_COSMETIC -> {
          normalTranslation = Arrays.asList(0, -79.25, 2.15);
          sneakingTranslation = Arrays.asList(0, -75, 8.15);
        }
      }
    }

    addHeadDisplay(originalJson, normalRotation, normalTranslation, scale);
    addData(
        builder,
        TARGET_MODEL_PATH + fileName,
        fileName,
        originalJson.toString().getBytes(StandardCharsets.UTF_8));

    JsonObject jsonSneaking = JsonParser.parseString(originalJson.toString()).getAsJsonObject();
    addHeadDisplay(jsonSneaking, sneakingRotation, sneakingTranslation, scale);

    String sneakingFileName = fileName.replace(".json", "_sneaking.json");
    addData(
        builder,
        TARGET_MODEL_PATH + sneakingFileName,
        sneakingFileName,
        jsonSneaking.toString().getBytes(StandardCharsets.UTF_8));
  }

  private static void addHeadDisplay(
      JsonObject parentJson, List<Number> rotation, List<Number> translation, List<Number> scale) {
    JsonObject displayObj =
        parentJson.has("display") ? parentJson.getAsJsonObject("display") : null;
    if (displayObj == null) {
      displayObj = new JsonObject();
      parentJson.add("display", displayObj);
    }

    JsonObject headObj = displayObj.has("head") ? displayObj.getAsJsonObject("head") : null;
    if (headObj == null) {
      headObj = new JsonObject();
      displayObj.add("head", headObj);
    }

    if (rotation != null) headObj.add("rotation", toJsonArray(rotation));
    if (translation != null) headObj.add("translation", toJsonArray(translation));
    if (scale != null) headObj.add("scale", toJsonArray(scale));
  }

  private static JsonArray toJsonArray(List<Number> values) {
    JsonArray array = new JsonArray(values.size());
    for (Number value : values) {
      array.add(value);
    }
    return array;
  }

  private static void addData(
      ResourcePackBuilder builder, String path, String fileName, byte[] data) {
    if (builder.addData(path, data)) {
      ModInit.LOGGER.debug("Added {} -> {}", fileName, path);
    } else {
      ModInit.LOGGER.warn("Could not add {} as {} (maybe duplicate?)", fileName, path);
    }
  }
}
