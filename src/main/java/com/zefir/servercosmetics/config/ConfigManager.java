package com.zefir.servercosmetics.config;

import com.mojang.brigadier.context.CommandContext;
import com.zefir.servercosmetics.gui.CosmeticsGUI;
import com.zefir.servercosmetics.gui.ItemSkinsGUI;
import com.zefir.servercosmetics.util.Utils;
import eu.pb4.sgui.api.GuiHelpers;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.comments.format.YamlCommentFormat;
import org.simpleyaml.configuration.file.YamlFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.minecraft.server.command.CommandManager.literal;

public class ConfigManager {
    public static final Path SERVER_COSMETICS_DIR = FabricLoader.getInstance().getConfigDir().resolve("ServerCosmetics");
    public record NavigationButton(Text name, String item, int customModelData, int slotIndex, List<String> lore) {}
    private static String configReloadPermission;
    private static String itemSkinsPermission;
    private static String cosmeticsReloadPermission;
    private static Text successConfigReloadMessage;
    private static Text errorConfigReloadMessage;
    private static Boolean legacyMode;
    private static Boolean HMCCosmeticsSupport;

    public static void registerConfigs() {
        createAndLoadConfig();
        ItemSkinsGUIConfig.itemSkinsInit();
        CosmeticsGUIConfig.serverCosmeticsInit();
    }

    public static void registerCommands(){
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("sc")
                    .then(literal("reload")
                            .requires(Permissions.require(Objects.requireNonNullElse(cosmeticsReloadPermission, "servercosmetics.reload")))
                            .executes(ConfigManager::reloadAllConfigs))
            );
            dispatcher.register(
                    literal("cm").executes(CosmeticsGUI::openGui)
                            .requires(Permissions.require(ItemSkinsGUIConfig.getPermissionOpenGui()))
                            .then(literal("reload")
                                    .requires(Permissions.require(Objects.requireNonNullElse(itemSkinsPermission, "servercosmetics.reload.cosmetics")))
                                    .executes(ConfigManager::reloadCosmeticsConfigs))
            );
            dispatcher.register(
                    literal("is").executes(ItemSkinsGUI::openIsGui)
                            .requires(Permissions.require(CosmeticsGUIConfig.getPermissionOpenGui()))
                            .then(literal("reload")
                                    .requires(Permissions.require(Objects.requireNonNullElse(configReloadPermission, "servercosmetics.reload.itemskins")))
                                    .executes(ConfigManager::reloadItemSkinsConfigs))
            );
        });
    }
    public static void loadDemoConfigs() {
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
        if(!SERVER_COSMETICS_DIR.toFile().exists()){
            loadDemoConfigs();
        }

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
            HMCCosmeticsSupport = yamlFile.getBoolean("HMCCosmeticsSupport");

        } catch (IOException e) {
            throw new RuntimeException("Failed to create or load configuration file", e);
        }
    }
    public static boolean isLegacyMode() {
        return legacyMode;
    }
    public static boolean isHMCCosmeticsSupport() {return HMCCosmeticsSupport;}

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
        yamlFile.path("HMCCosmeticsSupport").addDefault(false).commentSide("Adds HMCCosmetics and HMCSkins folder");

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
        int customModelData = yamlFile.isSet(basePath + ".customModelData") ? yamlFile.getInt(basePath + ".customModelData") : -1;
        navigationButtons.put(buttonKey, new NavigationButton(
                Utils.formatDisplayName(yamlFile.getString(basePath + ".name")),
                yamlFile.getString(basePath + ".item"),
                customModelData,
                yamlFile.getInt(basePath + ".slotIndex"),
                yamlFile.getStringList(basePath + ".lore")
        ));
    }

    public static ItemStack createItemStack(String material, int customModelData, Text displayName, String itemSkinId, List<Text> lore) {
        NbtCompound nbtData = new NbtCompound();
        nbtData.putInt("CustomModelData", customModelData);
        if (itemSkinId != null) {
            nbtData.putString("itemSkinsID", itemSkinId);
        }

        if (!lore.isEmpty()) {
            NbtCompound display = nbtData.getCompound("display");
            NbtList loreItems = new NbtList();
            for (Text l : lore) {
                l = l.copy().styled(GuiHelpers.STYLE_CLEARER);
                loreItems.add(NbtString.of(Text.Serializer.toJson(l)));
            }
            display.put("Lore", loreItems);
            nbtData.put("display", display);
        }

        ItemStack itemStack = new ItemStack(Registries.ITEM.get(new Identifier(material)));
        itemStack.setNbt(nbtData);
        itemStack.setCustomName(displayName);
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