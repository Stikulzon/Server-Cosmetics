package ua.zefir.servercosmetics.config;

import java.io.IOException;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import org.simpleyaml.configuration.comments.format.YamlCommentFormat;
import org.simpleyaml.configuration.file.YamlFile;
import ua.zefir.servercosmetics.util.Utils;

public class MainConfig {

  public static final Path SERVER_COSMETICS_DIR =
      FabricLoader.getInstance().getConfigDir().resolve("ServerCosmetics");

  private String configReloadPermission;
  private String itemSkinsReloadPermission;
  private String cosmeticsReloadPermission;
  private Component successConfigReloadMessage;
  private Component errorConfigReloadMessage;
  private boolean legacyMode;

  public void load() {
    ConfigManager.loadDemoConfigs();

    Path configFile = SERVER_COSMETICS_DIR.resolve("config.yml");
    YamlFile yamlFile = new YamlFile(configFile.toAbsolutePath().toString());

    try {
      yamlFile.createOrLoadWithComments();
      initializeDefaults(yamlFile);
      yamlFile.loadWithComments();

      configReloadPermission = yamlFile.getString("permissions.reloadAllConfigs");
      itemSkinsReloadPermission = yamlFile.getString("permissions.reloadItemSkins");
      cosmeticsReloadPermission = yamlFile.getString("permissions.reloadCosmetics");
      successConfigReloadMessage =
          Utils.formatDisplayName(yamlFile.getString("configReload.message.success"));
      errorConfigReloadMessage =
          Utils.formatDisplayName(yamlFile.getString("configReload.message.error"));
      legacyMode = yamlFile.getBoolean("legacyMode");

    } catch (IOException e) {
      throw new RuntimeException("Failed to load main configuration file (config.yml)", e);
    }
  }

  private void initializeDefaults(YamlFile yamlFile) {
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
    yamlFile.path("legacyMode").addDefault(false).commentSide("Recommended: false for new setups.");

    try {
      yamlFile.save();
    } catch (IOException e) {
      throw new RuntimeException("Failed to save default main yml configuration", e);
    }
  }

  public String getConfigReloadPermission() {
    return configReloadPermission;
  }

  public String getItemSkinsReloadPermission() {
    return itemSkinsReloadPermission;
  }

  public String getCosmeticsReloadPermission() {
    return cosmeticsReloadPermission;
  }

  public Component getSuccessConfigReloadMessage() {
    return successConfigReloadMessage;
  }

  public Component getErrorConfigReloadMessage() {
    return errorConfigReloadMessage;
  }

  public boolean isLegacyMode() {
    return legacyMode;
  }
}
