package ua.zefir.servercosmetics.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.context.CommandContext;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.simpleyaml.configuration.comments.format.YamlCommentFormat;
import org.simpleyaml.configuration.file.YamlFile;
import ua.zefir.servercosmetics.ModInit;
import ua.zefir.servercosmetics.data.BodyCosmeticsData;
import ua.zefir.servercosmetics.data.CustomItemEntry;
import ua.zefir.servercosmetics.data.CustomItemRegistry;
import ua.zefir.servercosmetics.data.ItemType;
import ua.zefir.servercosmetics.datagen.RuntimeModelManager;
import ua.zefir.servercosmetics.util.Utils;

public class ConfigManager {
  public static final Path SERVER_COSMETICS_DIR =
      FabricLoader.getInstance().getConfigDir().resolve("ServerCosmetics");

  private static final String TARGET_TEXTURE_PATH = "assets/servercosmetics/textures/";
  private static final String TARGET_MODEL_PATH = "assets/servercosmetics/models/item/";
  private static final String TARGET_ARMOR_TEXTURE_PATH =
      "assets/servercosmetics/textures/entity/equipment/";

  public record NavigationButton(
      Text name, Item baseItem, Identifier modelPath, int slotIndex, List<String> lore) {}

  private static String configReloadPermission;
  private static String itemSkinsReloadPermission;
  private static String cosmeticsReloadPermission;
  private static Text successConfigReloadMessage;
  private static Text errorConfigReloadMessage;
  private static boolean legacyMode;
  private static boolean enableExperimentalFeatures;

  public static final AbstractGuiConfig ITEM_SKINS_GUI_CONFIG = new ItemSkinsGuiConfig();
  public static final AbstractGuiConfig COSMETICS_GUI_CONFIG = new CosmeticsGuiConfig();

  public static void registerConfigs() {
    RuntimeModelManager.clearRequestedModels();
    createAndLoadMainConfig();
    CustomItemRegistry.setLegacyMode(legacyMode);

    ITEM_SKINS_GUI_CONFIG.init();
    COSMETICS_GUI_CONFIG.init();
    CustomItemRegistry.initialize();

    registerResourcePackListener();
  }

  public static void registerResourcePackListener() {
    PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register(
        (builder) -> {
          RuntimeModelManager.generateAndProvideModels(builder::addData);

          Path resourcePackSourceDir = SERVER_COSMETICS_DIR.resolve("Assets");
          if (!setupDirectory(resourcePackSourceDir)) return;

          ModInit.LOGGER.info(
              "Scanning for resources in: {}", resourcePackSourceDir.toAbsolutePath());
          try (Stream<Path> pathStream = Files.walk(resourcePackSourceDir)) {
            pathStream
                .filter(Files::isRegularFile)
                .forEach(filePath -> processResourcePackFile(filePath, builder));
          } catch (IOException e) {
            ModInit.LOGGER.error(
                "Error walking directory {} for resource pack generation",
                resourcePackSourceDir.toAbsolutePath(),
                e);
          }
        });
  }

  public static boolean setupDirectory(Path path) {
    if (!Files.isDirectory(path)) {
      if (Files.notExists(path)) {
        try {
          Files.createDirectories(path);
          ModInit.LOGGER.info("Created directory: {}", path.toAbsolutePath());
        } catch (IOException e) {
          ModInit.LOGGER.error("Failed to create assets directory: {}", path.toAbsolutePath(), e);
          return false;
        }
      } else {
        ModInit.LOGGER.warn("Path exists but is not a directory: {}", path);
        return false;
      }
    }
    return true;
  }

  private static void processResourcePackFile(Path filePath, ResourcePackBuilder builder) {
    String fileName = filePath.getFileName().toString();
    String fileNameLower = fileName.toLowerCase(Locale.ROOT);

    try {
      byte[] data = Files.readAllBytes(filePath);

      if (fileNameLower.endsWith(".png")) {
        processPngFile(builder, fileName, fileNameLower, data);
      } else if (fileNameLower.endsWith(".json")) {
        processJsonFile(builder, fileName, fileNameLower, data);
      } else if (fileNameLower.endsWith(".mcmeta")) {
        addData(builder, TARGET_TEXTURE_PATH + "item/" + fileName, fileName, data);
      }
    } catch (IOException e) {
      ModInit.LOGGER.error("Failed to read file {} for resource pack", filePath, e);
    }
  }

  private static void processPngFile(
      ResourcePackBuilder builder, String fileName, String fileNameLower, byte[] data) {
    PathResolveResult result = resolveTexturePath(fileName, fileNameLower);
    addData(builder, result.fullPath, result.correctedFileName, data);
  }

  private record PathResolveResult(String fullPath, String correctedFileName) {}

  private static PathResolveResult resolveTexturePath(String fileName, String fileNameLower) {
    String targetBaseDir;
    String finalName = fileName;

    if (fileNameLower.endsWith("_helmet.png")
        || fileNameLower.endsWith("_chestplate.png")
        || fileNameLower.endsWith("_leggings.png")
        || fileNameLower.endsWith("_boots.png")) {

      targetBaseDir = TARGET_TEXTURE_PATH + "item/";
      // Backwards compatibility naming
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

  private static void processJsonFile(
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

    // Apply normal
    addHeadDisplay(originalJson, normalRotation, normalTranslation, scale);
    addData(
        builder,
        TARGET_MODEL_PATH + fileName,
        fileName,
        originalJson.toString().getBytes(StandardCharsets.UTF_8));

    // Apply sneaking (on a copy)
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
        parentJson.has("display") && parentJson.get("display").isJsonObject()
            ? parentJson.getAsJsonObject("display")
            : null;
    if (displayObj == null) {
      displayObj = new JsonObject();
      parentJson.add("display", displayObj);
    }

    JsonObject headObj =
        displayObj.has("head") && displayObj.get("head").isJsonObject()
            ? displayObj.getAsJsonObject("head")
            : null;
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

  public static void loadDemoConfigs() {
    if (SERVER_COSMETICS_DIR.resolve("config.yml").toFile().exists()) return;

    try {
      Files.createDirectories(SERVER_COSMETICS_DIR);

      Path demoConfigsPathSource =
          FabricLoader.getInstance()
              .getModContainer("servercosmetics")
              .flatMap(mod -> mod.findPath("assets/servercosmetics/demo-configs/"))
              .orElse(null);

      if (demoConfigsPathSource == null) {
        ModInit.LOGGER.warn("Could not find demo-configs path in mod assets.");
        return;
      }

      ModInit.LOGGER.info("Loading demo configurations...");
      try (Stream<Path> stream = Files.walk(demoConfigsPathSource)) {
        stream.forEach(
            sourcePath -> {
              Path destPath =
                  SERVER_COSMETICS_DIR.resolve(
                      demoConfigsPathSource.relativize(sourcePath).toString());
              try {
                if (Files.isDirectory(sourcePath)) {
                  Files.createDirectories(destPath);
                } else {
                  Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
                }
              } catch (IOException e) {
                throw new RuntimeException("Failed to copy demo file " + sourcePath, e);
              }
            });
      }
    } catch (Exception e) {
      ModInit.LOGGER.error("Failed to load demo configs.", e);
    }
  }

  //    private static int reload(CommandContext<ServerCommandSource> context, Runnable
  // reloadAction) {
  //        try {
  //            RuntimeModelManager.clearRequestedModels();
  //            createAndLoadMainConfig();
  //            CustomItemRegistry.setLegacyMode(legacyMode);
  //
  //            if (reloadAction != null) {
  //                reloadAction.run();
  //            }
  //
  //            context.getSource().sendFeedback(() -> successConfigReloadMessage, false);
  //            return 1;
  //        } catch (Exception e) {
  //            context.getSource().sendFeedback(() -> errorConfigReloadMessage, false);
  //            ModInit.LOGGER.error("An error occurred during config reload!", e);
  //            return 0;
  //        }
  //    }

  public static int reloadAllConfigsCommand(CommandContext<ServerCommandSource> context) {
    try {
      RuntimeModelManager.clearRequestedModels();
      createAndLoadMainConfig();
      CustomItemRegistry.setLegacyMode(legacyMode);

      ITEM_SKINS_GUI_CONFIG.init();
      COSMETICS_GUI_CONFIG.init();
      CustomItemRegistry.reloadAll();

      context.getSource().sendFeedback(() -> successConfigReloadMessage, false);
    } catch (Exception e) {
      context.getSource().sendFeedback(() -> errorConfigReloadMessage, false);
      ModInit.LOGGER.error("An error occurred during ALL configs reload!", e);
    }
    return 1;
  }

  public static int reloadItemSkinsConfigsCommand(CommandContext<ServerCommandSource> context) {
    try {
      createAndLoadMainConfig();
      CustomItemRegistry.setLegacyMode(legacyMode);

      ITEM_SKINS_GUI_CONFIG.init();
      CustomItemRegistry.reloadItemSkins();

      context.getSource().sendFeedback(() -> successConfigReloadMessage, false);
    } catch (Exception e) {
      context.getSource().sendFeedback(() -> errorConfigReloadMessage, false);
      ModInit.LOGGER.error("An error occurred during ItemSkins configs reload!", e);
    }
    return 1;
  }

  public static int reloadCosmeticsConfigsCommand(CommandContext<ServerCommandSource> context) {
    try {
      createAndLoadMainConfig();
      CustomItemRegistry.setLegacyMode(legacyMode);

      COSMETICS_GUI_CONFIG.init();
      CustomItemRegistry.reloadCosmetics();

      context.getSource().sendFeedback(() -> successConfigReloadMessage, false);
    } catch (Exception e) {
      context.getSource().sendFeedback(() -> errorConfigReloadMessage, false);
      ModInit.LOGGER.error("An error occurred during Cosmetics configs reload!", e);
    }
    return 1;
  }

  private static void createAndLoadMainConfig() {
    loadDemoConfigs();

    Path configFile = SERVER_COSMETICS_DIR.resolve("config.yml");
    YamlFile yamlFile = new YamlFile(configFile.toAbsolutePath().toString());

    try {
      yamlFile.createOrLoadWithComments();
      initializeMainConfigDefaults(yamlFile);
      yamlFile.loadWithComments();

      configReloadPermission = yamlFile.getString("permissions.reloadAllConfigs");
      itemSkinsReloadPermission = yamlFile.getString("permissions.reloadItemSkins");
      cosmeticsReloadPermission = yamlFile.getString("permissions.reloadCosmetics");
      enableExperimentalFeatures = yamlFile.getBoolean("enableExperimentalFeatures", false);
      successConfigReloadMessage =
          Utils.formatDisplayName(yamlFile.getString("configReload.message.success"));
      errorConfigReloadMessage =
          Utils.formatDisplayName(yamlFile.getString("configReload.message.error"));
      legacyMode = yamlFile.getBoolean("legacyMode");

    } catch (IOException e) {
      throw new RuntimeException("Failed to load main configuration file (config.yml)", e);
    }
  }

  private static void initializeMainConfigDefaults(YamlFile yamlFile) {
    yamlFile.setCommentFormat(YamlCommentFormat.PRETTY);
    yamlFile
        .options()
        .headerFormatter()
        .prefixFirst("######################")
        .commentPrefix("## ")
        .commentSuffix(" ##")
        .suffixLast("######################");
    yamlFile.setHeader("Main Config File");

    yamlFile.addDefault("configVersion", 1);
    yamlFile.addDefault("debug", false);
    yamlFile.addDefault("permissions.reloadAllConfigs", "servercosmetics.reload");
    yamlFile.addDefault("permissions.reloadItemSkins", "servercosmetics.reload.itemskins");
    yamlFile.addDefault("permissions.reloadCosmetics", "servercosmetics.reload.cosmetics");
    yamlFile.addDefault("configReload.message.success", "&aConfig successfully reload!");
    yamlFile.addDefault("configReload.message.error", "&cAn error occurred during configs reload!");
    yamlFile.addDefault("enableExperimentalFeatures", false);
    yamlFile.path("legacyMode").addDefault(false).commentSide("Recommended: false for new setups.");

    try {
      yamlFile.save();
    } catch (IOException e) {
      throw new RuntimeException("Failed to save default main yml configuration", e);
    }
  }

  public static String getConfigReloadPermission() {
    return configReloadPermission;
  }

  public static String getItemSkinsReloadPermission() {
    return itemSkinsReloadPermission;
  }

  public static String getCosmeticsReloadPermission() {
    return cosmeticsReloadPermission;
  }

  public static boolean isEnableExperimentalFeatures() {
    return enableExperimentalFeatures;
  }
}
