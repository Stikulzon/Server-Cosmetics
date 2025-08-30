package com.zefir.servercosmetics.config;

import com.mojang.brigadier.context.CommandContext;
import com.zefir.servercosmetics.ServerCosmetics;
import com.zefir.servercosmetics.data.CustomItemEntry;
import com.zefir.servercosmetics.data.CustomItemRegistry;
import com.zefir.servercosmetics.data.ItemType;
import com.zefir.servercosmetics.datagen.RuntimeModelManager;
import com.zefir.servercosmetics.util.Utils;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import lombok.Getter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
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

    public record NavigationButton(Text name, Item baseItem, PolymerModelData polymerModelData, int slotIndex, List<String> lore) {}

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

            if (Files.isDirectory(resourcePackSourceDir)) {
                ServerCosmetics.LOGGER.info("Scanning for .png and .json files in: {}", resourcePackSourceDir.toAbsolutePath());

                try (Stream<Path> pathStream = Files.walk(resourcePackSourceDir)) {
                    pathStream
                            .filter(Files::isRegularFile)
                            .forEach(filePath -> {
                                Path fileNamePath = filePath.getFileName();
                                if (fileNamePath == null) {
                                    return;
                                }

                                String fileNameString = fileNamePath.toString();
                                String filenameLower = fileNameString.toLowerCase(Locale.ROOT);
                                String targetBaseDir = null;
                                byte[] data;

                                try {
                                    data = Files.readAllBytes(filePath);

                                    if (filenameLower.endsWith(".png")) {
                                        if(filenameLower.endsWith("_helmet.png") || filenameLower.endsWith("_chestplate.png") || filenameLower.endsWith("_leggings.png") || filenameLower.endsWith("_boots.png")){
                                            targetBaseDir = TARGET_TEXTURE_PATH + "item/armor/";
                                        } else if (filenameLower.endsWith("_layer_1.png") || filenameLower.endsWith("_layer_2.png")) {
                                            targetBaseDir = TARGET_TEXTURE_PATH + "models/armor/";
                                        } else {
                                            targetBaseDir = TARGET_TEXTURE_PATH + "item/";
                                        }
                                    } else if (filenameLower.endsWith(".json")) {
                                        targetBaseDir = TARGET_MODEL_PATH;
                                            String content = new String(data, StandardCharsets.UTF_8);
                                            JSONObject jsonObject = new JSONObject(content);

                                            CustomItemEntry cosmeticEntry = CustomItemRegistry.getCosmetic(filenameLower.substring(0, filenameLower.lastIndexOf('.')));
                                            if(cosmeticEntry != null){
                                                if (cosmeticEntry.type() == ItemType.BODY_COSMETIC || cosmeticEntry.type() == ItemType.CHESTPLATE_BODY_COSMETIC) {
                                                    JSONObject displayObject = jsonObject.optJSONObject("display");
                                                    if (displayObject == null) {
                                                        displayObject = new JSONObject();
                                                        jsonObject.put("display", displayObject);
                                                    }

                                                    // ---------- REGULAR VARIANT ----------
                                                    JSONObject headNormal = new JSONObject();
                                                    headNormal.put("rotation", new JSONArray(Arrays.asList(0, -180, 0)));
                                                    headNormal.put("translation", new JSONArray(Arrays.asList(0, -56.5, 2.15)));
                                                    headNormal.put("scale", new JSONArray(Arrays.asList(1.45, 1.45, 1.45)));

                                                    displayObject.put("head", headNormal);

                                                    byte[] normalData = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
                                                    String finalTargetPathNormal = targetBaseDir + fileNameString;

                                                    addData(builder, finalTargetPathNormal, fileNameString, normalData);

                                                    // ---------- SNEAKING VARIANT ----------
                                                    JSONObject jsonSneaking = new JSONObject(jsonObject.toString());
                                                    JSONObject displaySneaking = jsonSneaking.getJSONObject("display");

                                                    JSONObject headSneaking = new JSONObject();
                                                    headSneaking.put("rotation", new JSONArray(Arrays.asList(-28, -180, 0)));
                                                    headSneaking.put("translation", new JSONArray(Arrays.asList(0, -56.5, 4.15)));
                                                    headSneaking.put("scale", new JSONArray(Arrays.asList(1.45, 1.45, 1.45)));

                                                    displaySneaking.put("head", headSneaking);

                                                    byte[] sneakingData = jsonSneaking.toString().getBytes(StandardCharsets.UTF_8);

                                                    String sneakingFileName = fileNameString.replace(".json", "_sneaking.json");
                                                    String finalTargetPathSneaking = targetBaseDir + sneakingFileName;

                                                    addData(builder, finalTargetPathSneaking, sneakingFileName, sneakingData);

                                                    return;
                                                } else if (cosmeticEntry.type() == ItemType.LEGGINGS_BODY_COSMETIC) {
                                                    JSONObject displayObject = jsonObject.optJSONObject("display");
                                                    if (displayObject == null) {
                                                        displayObject = new JSONObject();
                                                        jsonObject.put("display", displayObject);
                                                    }

                                                    // ---------- REGULAR VARIANT ----------
                                                    JSONObject headNormal = new JSONObject();
                                                    headNormal.put("rotation", new JSONArray(Arrays.asList(0, -180, 0)));
                                                    headNormal.put("translation", new JSONArray(Arrays.asList(0, -69.25, 2.15)));
                                                    headNormal.put("scale", new JSONArray(Arrays.asList(1.45, 1.45, 1.45)));

                                                    displayObject.put("head", headNormal);

                                                    byte[] normalData = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
                                                    String finalTargetPathNormal = targetBaseDir + fileNameString;

                                                    addData(builder, finalTargetPathNormal, fileNameString, normalData);

                                                    // ---------- SNEAKING VARIANT ----------
                                                    JSONObject jsonSneaking = new JSONObject(jsonObject.toString());
                                                    JSONObject displaySneaking = jsonSneaking.getJSONObject("display");

                                                    JSONObject headSneaking = new JSONObject();
                                                    headSneaking.put("rotation", new JSONArray(Arrays.asList(0, -180, 0)));
                                                    headSneaking.put("translation", new JSONArray(Arrays.asList(0, -65, 8.15)));
                                                    headSneaking.put("scale", new JSONArray(Arrays.asList(1.45, 1.45, 1.45)));

                                                    displaySneaking.put("head", headSneaking);

                                                    byte[] sneakingData = jsonSneaking.toString().getBytes(StandardCharsets.UTF_8);

                                                    String sneakingFileName = fileNameString.replace(".json", "_sneaking.json");
                                                    String finalTargetPathSneaking = targetBaseDir + sneakingFileName;

                                                    addData(builder, finalTargetPathSneaking, sneakingFileName, sneakingData);

                                                    return;
                                                } else if (cosmeticEntry.type() == ItemType.BOOTS_BODY_COSMETIC) {
                                                    JSONObject displayObject = jsonObject.optJSONObject("display");
                                                    if (displayObject == null) {
                                                        displayObject = new JSONObject();
                                                        jsonObject.put("display", displayObject);
                                                    }

                                                    // ---------- REGULAR VARIANT ----------
                                                    JSONObject headNormal = new JSONObject();
                                                    headNormal.put("rotation", new JSONArray(Arrays.asList(0, -180, 0)));
                                                    headNormal.put("translation", new JSONArray(Arrays.asList(0, -79.25, 2.15)));
                                                    headNormal.put("scale", new JSONArray(Arrays.asList(1.45, 1.45, 1.45)));

                                                    displayObject.put("head", headNormal);

                                                    byte[] normalData = jsonObject.toString().getBytes(StandardCharsets.UTF_8);
                                                    String finalTargetPathNormal = targetBaseDir + fileNameString;

                                                    addData(builder, finalTargetPathNormal, fileNameString, normalData);

                                                    // ---------- SNEAKING VARIANT ----------
                                                    JSONObject jsonSneaking = new JSONObject(jsonObject.toString());
                                                    JSONObject displaySneaking = jsonSneaking.getJSONObject("display");

                                                    JSONObject headSneaking = new JSONObject();
                                                    headSneaking.put("rotation", new JSONArray(Arrays.asList(0, -180, 0)));
                                                    headSneaking.put("translation", new JSONArray(Arrays.asList(0, -75, 8.15)));
                                                    headSneaking.put("scale", new JSONArray(Arrays.asList(1.45, 1.45, 1.45)));

                                                    displaySneaking.put("head", headSneaking);

                                                    byte[] sneakingData = jsonSneaking.toString().getBytes(StandardCharsets.UTF_8);

                                                    String sneakingFileName = fileNameString.replace(".json", "_sneaking.json");
                                                    String finalTargetPathSneaking = targetBaseDir + sneakingFileName;

                                                    addData(builder, finalTargetPathSneaking, sneakingFileName, sneakingData);

                                                    return;
                                                }
                                            }
                                    } else if (filenameLower.endsWith(".mcmeta")) {
                                        targetBaseDir = TARGET_TEXTURE_PATH + "item/";
                                        ServerCosmetics.LOGGER.debug("JSON file {} has 'animation' key, targeting TEXTURE_PATH.", fileNameString);
                                    }

                                    if (targetBaseDir == null) {
                                        return;
                                    }

                                    String finalTargetPath = targetBaseDir + fileNameString;

                                    addData(builder, finalTargetPath, fileNameString, data);
                                } catch (IOException e) {
                                    ServerCosmetics.LOGGER.error("Failed to read file {} for resource pack", filePath, e);
                                }
                            });
//                    ServerCosmetics.LOGGER.info("Finished adding custom .png and .json resources from {}", resourcePackSourceDir.toAbsolutePath());
                } catch (IOException e) {
                    ServerCosmetics.LOGGER.error("Error walking directory {} for resource pack generation", resourcePackSourceDir.toAbsolutePath(), e);
                }
            } else {
                ServerCosmetics.LOGGER.info("Custom resource source directory not found or is not a directory: {}. Skipping custom asset loading.", resourcePackSourceDir.toAbsolutePath());
                try {
                    Files.createDirectories(resourcePackSourceDir);
                    ServerCosmetics.LOGGER.info("Created assets directory at: {}", resourcePackSourceDir.toAbsolutePath());
                } catch (IOException e) {
                    ServerCosmetics.LOGGER.error("Failed to create assets directory: {}", resourcePackSourceDir.toAbsolutePath(), e);
                }
            }
        });
    }

    private static void addData(ResourcePackBuilder builder, String path, String fileName, byte[] data){
        if (builder.addData(path, data)) {
            ServerCosmetics.LOGGER.debug("Added {} -> {}", fileName, path);
        } else {
            ServerCosmetics.LOGGER.warn("Could not add {} as {} to resource pack (maybe already exists?)", fileName, path);
        }
    }

    public static void loadDemoConfigs() {
        if(SERVER_COSMETICS_DIR.toFile().exists() && SERVER_COSMETICS_DIR.resolve("config.yml").toFile().exists()) {
            return;
        }
        try {
            Files.createDirectories(SERVER_COSMETICS_DIR);
        } catch (IOException e) {
            ServerCosmetics.LOGGER.error("Failed to create base ServerCosmetics directory.", e);
        }


        Path demoConfigsPathSource = FabricLoader.getInstance().getModContainer("servercosmetics")
                .flatMap(modContainer -> modContainer.findPath("assets/servercosmetics/demo-configs/"))
                .orElse(null);

        if (demoConfigsPathSource == null) {
            ServerCosmetics.LOGGER.warn("Could not find demo-configs path in mod assets.");
            return;
        }

        ServerCosmetics.LOGGER.info("Loading demo configurations from {} to {}", demoConfigsPathSource, SERVER_COSMETICS_DIR);

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
            ServerCosmetics.LOGGER.error("Failed to load demo configs fully.", e);
        }
    }

    public static int reloadAllConfigsCommand(CommandContext<ServerCommandSource> context) {
        try {
            createAndLoadMainConfig();
            CustomItemRegistry.setLegacyMode(legacyMode);

            ITEM_SKINS_GUI_CONFIG.init();
            COSMETICS_GUI_CONFIG.init();
            CustomItemRegistry.reloadAll();

            context.getSource().sendFeedback(() -> successConfigReloadMessage, false);
        } catch (Exception e){
            context.getSource().sendFeedback(() -> errorConfigReloadMessage, false);
            ServerCosmetics.LOGGER.error("An error occurred during ALL configs reload!", e);
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
        } catch (Exception e){
            context.getSource().sendFeedback(() -> errorConfigReloadMessage, false);
            ServerCosmetics.LOGGER.error("An error occurred during ItemSkins configs reload!", e);
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
        } catch (Exception e){
            context.getSource().sendFeedback(() -> errorConfigReloadMessage, false);
            ServerCosmetics.LOGGER.error("An error occurred during Cosmetics configs reload!", e);
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