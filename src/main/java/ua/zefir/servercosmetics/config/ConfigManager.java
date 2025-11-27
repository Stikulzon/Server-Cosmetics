package ua.zefir.servercosmetics.config;

import com.mojang.brigadier.context.CommandContext;
import ua.zefir.servercosmetics.ModInit;
import ua.zefir.servercosmetics.data.BodyCosmeticsData;
import ua.zefir.servercosmetics.data.CustomItemEntry;
import ua.zefir.servercosmetics.data.CustomItemRegistry;
import ua.zefir.servercosmetics.data.ItemType;
import ua.zefir.servercosmetics.datagen.RuntimeModelManager;
import ua.zefir.servercosmetics.util.Utils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import lombok.Getter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.json.JSONArray;
import org.json.JSONObject;
import org.simpleyaml.configuration.comments.format.YamlCommentFormat;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Stream;

public class ConfigManager {
    public static final Path SERVER_COSMETICS_DIR = FabricLoader.getInstance().getConfigDir().resolve("ServerCosmetics");

    private static final String TARGET_TEXTURE_PATH = "assets/servercosmetics/textures/";
    private static final String TARGET_MODEL_PATH = "assets/servercosmetics/models/item/";
    private static final String TARGET_ARMOR_TEXTURE_PATH = "assets/servercosmetics/textures/entity/equipment/";

    public record NavigationButton(Text name, Item baseItem, Identifier modelPath, int slotIndex,
                                   List<String> lore) {
    }

    @Getter
    private static String configReloadPermission;
    @Getter
    private static String itemSkinsReloadPermission;
    @Getter
    private static String cosmeticsReloadPermission;
    private static Text successConfigReloadMessage;
    private static Text errorConfigReloadMessage;
    private static boolean legacyMode;
    @Getter
    private static boolean enableExperimentalFeatures;

    public static final AbstractGuiConfig ITEM_SKINS_GUI_CONFIG = new ItemSkinsGUIConfig();
    public static final AbstractGuiConfig COSMETICS_GUI_CONFIG = new CosmeticsGUIConfig();

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
        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register((builder) -> {
            // Generate and add runtime armor models
            RuntimeModelManager.generateAndProvideModels(builder::addData);

            Path resourcePackSourceDir = SERVER_COSMETICS_DIR.resolve("Assets");
            if (!Files.isDirectory(resourcePackSourceDir)) {
                ModInit.LOGGER.info("Custom resource source directory not found: {}. Creating it.", resourcePackSourceDir.toAbsolutePath());
                try {
                    Files.createDirectories(resourcePackSourceDir);
                } catch (IOException e) {
                    ModInit.LOGGER.error("Failed to create assets directory: {}", resourcePackSourceDir.toAbsolutePath(), e);
                }
                return;
            }

            ModInit.LOGGER.info("Scanning for .png and .json files in: {}", resourcePackSourceDir.toAbsolutePath());
            try (Stream<Path> pathStream = Files.walk(resourcePackSourceDir)) {
                pathStream
                        .filter(Files::isRegularFile)
                        .forEach(filePath -> processResourcePackFile(filePath, builder));
            } catch (IOException e) {
                ModInit.LOGGER.error("Error walking directory {} for resource pack generation", resourcePackSourceDir.toAbsolutePath(), e);
            }
        });
    }

    /**
     * Processes a single file for the resource pack, dispatching to the correct handler based on file type.
     */
    private static void processResourcePackFile(Path filePath, ResourcePackBuilder builder) {
        Path fileNamePath = filePath.getFileName();
        if (fileNamePath == null) {
            return;
        }

        String fileName = fileNamePath.toString();
        String fileNameLower = fileName.toLowerCase(Locale.ROOT);

        try {
            byte[] data = Files.readAllBytes(filePath);

            if (fileNameLower.endsWith(".png")) {
                processPngFile(builder, fileName, fileNameLower, data);
            } else if (fileNameLower.endsWith(".json")) {
                processJsonFile(builder, fileName, fileNameLower, data);
            } else if (fileNameLower.endsWith(".mcmeta")) {
                processMcmetaFile(builder, fileName, data);
            }
        } catch (IOException e) {
            ModInit.LOGGER.error("Failed to read file {} for resource pack", filePath, e);
        }
    }

    /**
     * Determines the correct path for a .png file and adds it to the resource pack.
     */
    private static void processPngFile(ResourcePackBuilder builder, String fileName, String fileNameLower, byte[] data) {
        String targetBaseDir;

        if (fileNameLower.endsWith("_helmet.png") || fileNameLower.endsWith("_chestplate.png") || fileNameLower.endsWith("_leggings.png") || fileNameLower.endsWith("_boots.png")
        || fileNameLower.endsWith("_head.png") || fileNameLower.endsWith("_chest.png") || fileNameLower.endsWith("_legs.png") || fileNameLower.endsWith("_feet.png")) {
            targetBaseDir = TARGET_TEXTURE_PATH + "item/";
            // backwards compatibility with an old naming scheme
            fileName = fileName.replace("_helmet.png", "_head.png").replace("_chestplate.png", "_chest.png").replace("_leggings.png", "_legs.png").replace("_boots.png", "_feet.png");

        } else if (fileNameLower.endsWith("_layer_1.png") || fileNameLower.endsWith("_humanoid.png")) {
            targetBaseDir = TARGET_ARMOR_TEXTURE_PATH + "humanoid/";
            fileName = fileName.replace("_layer_1", "").replace("_humanoid", "");

        } else if (fileNameLower.endsWith("_layer_2.png") || fileNameLower.endsWith("_humanoid_leggings.png")) {
            targetBaseDir = TARGET_ARMOR_TEXTURE_PATH + "humanoid_leggings/";
            fileName = fileName.replace("_layer_2", "").replace("_humanoid_leggings", "");
        } else {
            targetBaseDir = TARGET_TEXTURE_PATH + "item/";
        }
        addData(builder, targetBaseDir + fileName, fileName, data);
    }

    /**
     * Adds a .mcmeta file to the resource pack.
     */
    private static void processMcmetaFile(ResourcePackBuilder builder, String fileName, byte[] data) {
        ModInit.LOGGER.debug("MCMETA file {} found, targeting TEXTURE_PATH.", fileName);
        String targetBaseDir = TARGET_TEXTURE_PATH + "item/";
        addData(builder, targetBaseDir + fileName, fileName, data);
    }

    /**
     * Processes a .json model file, handling special body cosmetics that require multiple model variants.
     */
    private static void processJsonFile(ResourcePackBuilder builder, String fileName, String fileNameLower, byte[] data) {
        String cosmeticId = fileNameLower.substring(0, fileNameLower.lastIndexOf('.'));
        CustomItemEntry cosmeticEntry = CustomItemRegistry.getCosmetic(cosmeticId);

        if (cosmeticEntry != null) {
            ItemType type = cosmeticEntry.type();
            if (type == ItemType.BODY_COSMETIC || type == ItemType.CHESTPLATE_BODY_COSMETIC ||
                    type == ItemType.LEGGINGS_BODY_COSMETIC || type == ItemType.BOOTS_BODY_COSMETIC) {

                String content = new String(data, StandardCharsets.UTF_8);
                JSONObject jsonObject = new JSONObject(content);
                handleBodyCosmeticJson(builder, jsonObject, fileName, cosmeticEntry);
                return;
            }
        }

        String targetPath = TARGET_MODEL_PATH + fileName;
        addData(builder, targetPath, fileName, data);
    }

    /**
     * Generates and adds normal and sneaking variants of a body cosmetic model.
     */
    private static void handleBodyCosmeticJson(ResourcePackBuilder builder, JSONObject originalJson, String fileName, CustomItemEntry entry) {
        ItemType type = entry.type();
        List<Number> normalTranslation, sneakingTranslation, normalRotation, sneakingRotation, scale;

        final String targetBaseDir = TARGET_MODEL_PATH;

        if (((BodyCosmeticsData) entry.cosmeticData()).mirrored()) {
            normalRotation = Arrays.asList(0, -180, 0);
            sneakingRotation = Arrays.asList(-28, -180, 0);
        } else {
            normalRotation = null;
            sneakingRotation = Arrays.asList(-28, 0, 0);
        }


        if (((BodyCosmeticsData) entry.cosmeticData()).autoscale()) {
            scale = Arrays.asList(1.45, 1.45, 1.45);
        } else {
            scale = null;
        }

        // Determine type-specific transformations
        if (((BodyCosmeticsData) entry.cosmeticData()).autoAlignment()) {
            switch (type) {
                case BODY_COSMETIC:
                case CHESTPLATE_BODY_COSMETIC:
                    normalTranslation = Arrays.asList(0, -56.5, 2.15);
                    sneakingTranslation = Arrays.asList(0, -56.5, 4.15);
                    break;
                case LEGGINGS_BODY_COSMETIC:
                    normalTranslation = Arrays.asList(0, -69.25, 2.15);
                    sneakingTranslation = Arrays.asList(0, -65, 8.15);
                    break;
                case BOOTS_BODY_COSMETIC:
                    normalTranslation = Arrays.asList(0, -79.25, 2.15);
                    sneakingTranslation = Arrays.asList(0, -75, 8.15);
                    break;
                default:
                    ModInit.LOGGER.warn("Unhandled cosmetic type {} in handleBodyCosmeticJson.", type);
                    return;
            }
        } else {
            normalTranslation = null;
            sneakingTranslation = null;
        }

        // Create a deep copy for the sneaking variant before modifying the original
        JSONObject jsonSneaking = new JSONObject(originalJson.toString());

        // --- REGULAR VARIANT ---
        // Modify the original JSON object for the normal variant
        addHeadDisplay(originalJson, normalRotation, normalTranslation, scale);
        byte[] normalData = originalJson.toString().getBytes(StandardCharsets.UTF_8);
        addData(builder, targetBaseDir + fileName, fileName, normalData);

        // --- SNEAKING VARIANT ---
        // Modify the copied JSON object for the sneaking variant
        String sneakingFileName = fileName.replace(".json", "_sneaking.json");
        addHeadDisplay(jsonSneaking, sneakingRotation, sneakingTranslation, scale);
        byte[] sneakingData = jsonSneaking.toString().getBytes(StandardCharsets.UTF_8);
        addData(builder, targetBaseDir + sneakingFileName, sneakingFileName, sneakingData);
    }

    /**
     * Creates a "head" display object and adds it to the parent JSON object.
     */
    private static void addHeadDisplay(JSONObject parentJson, List<Number> rotation, List<Number> translation, List<Number> scale) {
        JSONObject displayObject = parentJson.optJSONObject("display");
        if (displayObject == null) {
            displayObject = new JSONObject();
            parentJson.put("display", displayObject);
        }

        JSONObject headObject = displayObject.optJSONObject("head");
        if (headObject == null) {
            headObject = new JSONObject();
            displayObject.put("head", headObject);
        }

        if (rotation != null) {
            headObject.put("rotation", new JSONArray(rotation));
        }
        if (translation != null) {
            headObject.put("translation", new JSONArray(translation));
        }
        if (scale != null) {
            headObject.put("scale", new JSONArray(scale));
        }
        if (!headObject.isEmpty()) {
            displayObject.put("head", headObject);
        }
    }

    private static void addData(ResourcePackBuilder builder, String path, String fileName, byte[] data) {
        if (builder.addData(path, data)) {
            ModInit.LOGGER.debug("Added {} -> {}", fileName, path);
        } else {
            ModInit.LOGGER.warn("Could not add {} as {} to resource pack (maybe already exists?)", fileName, path);
        }
    }

    public static void loadDemoConfigs() {
        if (SERVER_COSMETICS_DIR.toFile().exists() && SERVER_COSMETICS_DIR.resolve("config.yml").toFile().exists()) {
            return;
        }
        try {
            Files.createDirectories(SERVER_COSMETICS_DIR);
        } catch (IOException e) {
            ModInit.LOGGER.error("Failed to create base ServerCosmetics directory.", e);
        }


        Path demoConfigsPathSource = FabricLoader.getInstance().getModContainer("servercosmetics")
                .flatMap(modContainer -> modContainer.findPath("assets/servercosmetics/demo-configs/"))
                .orElse(null);

        if (demoConfigsPathSource == null) {
            ModInit.LOGGER.warn("Could not find demo-configs path in mod assets.");
            return;
        }

        ModInit.LOGGER.info("Loading demo configurations from {} to {}", demoConfigsPathSource, SERVER_COSMETICS_DIR);

        try (Stream<Path> stream = Files.walk(demoConfigsPathSource)) {
            stream.forEach(sourcePath -> {
                Path destPath = SERVER_COSMETICS_DIR.resolve(demoConfigsPathSource.relativize(sourcePath).toString());
                try {
                    if (Files.isDirectory(sourcePath)) {
                        if (Files.notExists(destPath)) {
                            Files.createDirectories(destPath);
                        }
                    } else {
                        Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed to copy demo file " + sourcePath + " to " + destPath, e);
                }
            });
        } catch (IOException | RuntimeException e) {
            ModInit.LOGGER.error("Failed to load demo configs fully.", e);
        }
    }

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
            successConfigReloadMessage = Utils.formatDisplayName(yamlFile.getString("configReload.message.success"));
            errorConfigReloadMessage = Utils.formatDisplayName(yamlFile.getString("configReload.message.error"));
            legacyMode = yamlFile.getBoolean("legacyMode");

        } catch (IOException e) {
            throw new RuntimeException("Failed to create or load main configuration file (config.yml)", e);
        }
    }

    private static void initializeMainConfigDefaults(YamlFile yamlFile) {
        yamlFile.setCommentFormat(YamlCommentFormat.PRETTY);

        yamlFile.options().headerFormatter()
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
        yamlFile.path("permissions").comment("If the mod cannot get permissions from config, the default one will be used");
        yamlFile.addDefault("configReload.message.success", "&aConfig successfully reload!");
        yamlFile.addDefault("configReload.message.error", "&cAn error occurred during configs reload!");
        yamlFile.addDefault("enableExperimentalFeatures", false);
        yamlFile.path("legacyMode").addDefault(false).commentSide("If true, plugin will try to read some fields from older config structures for cosmetic/skin definitions. Recommended: false for new setups.");

        try {
            yamlFile.save();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save default main yml configuration", e);
        }
    }
}