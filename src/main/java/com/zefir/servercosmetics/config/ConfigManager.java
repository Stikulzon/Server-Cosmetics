package com.zefir.servercosmetics.config;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.zefir.servercosmetics.ServerCosmetics;
import com.zefir.servercosmetics.gui.CosmeticsGUI;
import com.zefir.servercosmetics.gui.ItemSkinsGUI;
import com.zefir.servercosmetics.util.Utils;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.json.JSONException;
import org.json.JSONObject;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.comments.format.YamlCommentFormat;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigManager {
    public static final Path SERVER_COSMETICS_DIR = FabricLoader.getInstance().getConfigDir().resolve("ServerCosmetics");

    private static final String TARGET_TEXTURE_PATH = "assets/servercosmetics/textures/item/";
    private static final String TARGET_MODEL_PATH = "assets/servercosmetics/models/item/";

    public record NavigationButton(Text name, Item baseItem, PolymerModelData polymerModelData, int slotIndex, List<String> lore) {}
    public static String configReloadPermission;
    public static String itemSkinsPermission;
    public static String cosmeticsReloadPermission;
    private static Text successConfigReloadMessage;
    private static Text errorConfigReloadMessage;
    private static Boolean legacyMode;

    public static void registerConfigs() {
        createAndLoadConfig();
        ItemSkinsGUIConfig.itemSkinsInit();
        CosmeticsGUIConfig.serverCosmeticsInit();
        registerResourcePackListener();
    }

    public static void registerResourcePackListener() {
        PolymerResourcePackUtils.RESOURCE_PACK_CREATION_EVENT.register((builder) -> {
            Path resourcePackSourceDir = Path.of("config", "ServerCosmetics", "Assets");

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
                                        targetBaseDir = TARGET_TEXTURE_PATH;
                                    } else if (filenameLower.endsWith(".json")) {
                                        targetBaseDir = TARGET_MODEL_PATH;
                                        try {
                                            String content = new String(data, StandardCharsets.UTF_8);
                                            JSONObject jsonObject = new JSONObject(content);

                                            if (jsonObject.has("animation")) {
                                                targetBaseDir = TARGET_TEXTURE_PATH;
                                                ServerCosmetics.LOGGER.debug("JSON file {} has 'animation' key, targeting TEXTURE_PATH.", fileNameString);
                                            }
                                        } catch (JSONException e) {
                                            ServerCosmetics.LOGGER.warn("Could not parse JSON file {} to check for 'animation' key. Assuming it's a model. Error: {}", fileNameString, e.getMessage());
                                        }
                                    }

                                    if (targetBaseDir == null) {
                                        // Neither .png nor .json
                                        return;
                                    }

                                    String finalTargetPath = targetBaseDir + fileNameString;


                                    if (builder.addData(finalTargetPath, data)) {
                                        ServerCosmetics.LOGGER.debug("Added {} -> {}", filePath.getFileName(), finalTargetPath);
                                    } else {
                                        ServerCosmetics.LOGGER.warn("Could not add {} as {} to resource pack (maybe already exists?)", filePath.getFileName(), finalTargetPath);
                                    }
                                } catch (IOException e) {
                                    ServerCosmetics.LOGGER.error("Failed to read file {} for resource pack", filePath, e);
                                }
                            });

                    ServerCosmetics.LOGGER.info("Finished adding custom .png and .json resources from {}", resourcePackSourceDir.toAbsolutePath());

                } catch (IOException e) {
                    ServerCosmetics.LOGGER.error("Error walking directory {} for resource pack generation", resourcePackSourceDir.toAbsolutePath(), e);
                }
            } else {
                ServerCosmetics.LOGGER.warn("Custom resource source directory not found or is not a directory: {}", resourcePackSourceDir.toAbsolutePath());
            }
        });
    }

    public static void loadDemoConfigs() {
        if(SERVER_COSMETICS_DIR.toFile().exists()) {
            return;
        }

        Path demoConfigsPath = FabricLoader.getInstance().getModContainer("servercosmetics").flatMap(servercosmetics -> servercosmetics.findPath("assets/servercosmetics/demo-configs/")).get();

        try {
        Files.walk(demoConfigsPath).forEach(path -> {
            Path destPath = SERVER_COSMETICS_DIR.resolve(demoConfigsPath.relativize(path).toString());

            try {
                if (Files.isDirectory(path)) {
                    if (Files.notExists(destPath)) {
                        Files.createDirectories(destPath);
                    }
                } else {
                    Files.copy(path, destPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int reloadAllConfigs(CommandContext<ServerCommandSource> context) {
        try {
            createAndLoadConfig();
            ItemSkinsGUIConfig.itemSkinsInit();
            CosmeticsGUIConfig.serverCosmeticsInit();
            context.getSource().sendFeedback(() -> successConfigReloadMessage, false);
        } catch (Exception e){
            context.getSource().sendFeedback(() -> errorConfigReloadMessage, false);
            throw new RuntimeException("An error occurred during configs reload!", e);
        }
        return 0;
    }
    public static int reloadItemSkinsConfigs(CommandContext<ServerCommandSource> context) {
        try {
            ItemSkinsGUIConfig.itemSkinsInit();
            context.getSource().sendFeedback(() -> successConfigReloadMessage, false);
        } catch (Exception e){
            context.getSource().sendFeedback(() -> errorConfigReloadMessage, false);
            throw new RuntimeException("An error occurred during configs reload!", e);
        }
        return 0;
    }

    public static int reloadCosmeticsConfigs(CommandContext<ServerCommandSource> context) {
        try {
            CosmeticsGUIConfig.serverCosmeticsInit();
            context.getSource().sendFeedback(() -> successConfigReloadMessage, false);
        } catch (Exception e){
            context.getSource().sendFeedback(() -> errorConfigReloadMessage, false);
            throw new RuntimeException("An error occurred during configs reload!", e);
        }
        return 0;
    }

    private static void createAndLoadConfig() {
        loadDemoConfigs();

        Path configFile = SERVER_COSMETICS_DIR.resolve("config.yml");
        YamlFile yamlFile = new YamlFile(configFile.toAbsolutePath().toString());

        try {
            yamlFile.createOrLoadWithComments();
            initializeConfigDefaults(yamlFile);
            yamlFile.loadWithComments();

            configReloadPermission = yamlFile.getString("permissions.reloadAllConfigs");
            itemSkinsPermission = yamlFile.getString("permissions.reloadItemSkins");
            cosmeticsReloadPermission = yamlFile.getString("permissions.reloadCosmetics");
            successConfigReloadMessage = Utils.formatDisplayName(yamlFile.getString("configReload.message.success"));
            errorConfigReloadMessage = Utils.formatDisplayName(yamlFile.getString("configReload.message.error"));
            legacyMode = yamlFile.getBoolean("legacyMode");

        } catch (IOException e) {
            throw new RuntimeException("Failed to create or load configuration file", e);
        }
    }
    public static boolean isLegacyMode() {
        return legacyMode;
    }

    private static void initializeConfigDefaults(YamlFile yamlFile) {
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
        yamlFile.path("legacyMode").addDefault(false).commentSide("If you don't know what it is, you want it to be false");

        try {
            yamlFile.save();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save default yml configuration", e);
        }
    }


    public static void addButtonDefault(ConfigurationSection parentSection, String buttonName, Map<String, Object> properties) {
        ConfigurationSection buttonSection = parentSection.getConfigurationSection(buttonName);
        if (buttonSection == null) {
            buttonSection = parentSection.createSection(buttonName);
        }

        properties.forEach(buttonSection::addDefault);
    }

    public static void loadButtonConfigs(YamlFile yamlFile, String buttonKey, Map<String, NavigationButton> navigationButtons) {
        String basePath = "buttons." + buttonKey;
        String baseItemString = yamlFile.getString(basePath + ".item");
        String complitedItemString = baseItemString.contains(":") ? baseItemString : "minecraft:" + baseItemString.toLowerCase();
        PolymerModelData polymerModelData = yamlFile.isSet(basePath + ".textureName") ? PolymerResourcePackUtils.requestModel(Registries.ITEM.get(Identifier.of(complitedItemString)), Identifier.of(ServerCosmetics.MOD_ID, "item/" + yamlFile.getString(basePath + ".textureName"))) : null;
        navigationButtons.put(buttonKey, new NavigationButton(
                Utils.formatDisplayName(yamlFile.getString(basePath + ".name")),
                Registries.ITEM.get(Identifier.of(complitedItemString)),
                polymerModelData,
                yamlFile.getInt(basePath + ".slotIndex"),
                yamlFile.getStringList(basePath + ".lore")
        ));
    }

    public static ItemStack createItemStack(String material, Text displayName, String itemSkinId, List<Text> lore) {
        PolymerModelData polymerModel = PolymerResourcePackUtils.requestModel(Registries.ITEM.get(Identifier.of(material)), Identifier.of(ServerCosmetics.MOD_ID, "item/" + itemSkinId));
        ItemStack itemStack = new ItemStack(polymerModel.item());

        itemStack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, comp -> comp.apply(nbt -> {
            if (itemSkinId != null) {
                nbt.putString("itemSkinsID", itemSkinId);
            }
        }));
        if (!lore.isEmpty()) {
            for (Text l : lore) {
                itemStack.apply(DataComponentTypes.LORE, LoreComponent.DEFAULT, l, LoreComponent::with);
            }
        }
        itemStack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(polymerModel.value()));
        itemStack.set(DataComponentTypes.CUSTOM_NAME, displayName);

        return itemStack;
    }

    public static List<Path> listFiles(Path dir) {
        try (Stream<Path> walk = Files.walk(dir)) {
            return walk.filter(Files::isRegularFile).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to list files in directory: " + dir, e);
        }
    }
}