package ua.zefir.servercosmetics.config;

import com.mojang.brigadier.context.CommandContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
import ua.zefir.servercosmetics.ModInit;
import ua.zefir.servercosmetics.data.CustomItemRegistry;
import ua.zefir.servercosmetics.datagen.RuntimeModelManager;

public class ConfigManager {

  public static final AbstractGuiConfig ITEM_SKINS_GUI_CONFIG = new ItemSkinsGuiConfig();
  public static final AbstractGuiConfig COSMETICS_GUI_CONFIG = new CosmeticsGuiConfig();

  private static final MainConfig mainConfig = new MainConfig();

  public static void registerConfigs() {
    RuntimeModelManager.clearRequestedModels();
    mainConfig.load();
    CustomItemRegistry.setLegacyMode(mainConfig.isLegacyMode());
    ITEM_SKINS_GUI_CONFIG.init();
    COSMETICS_GUI_CONFIG.init();
    CustomItemRegistry.initialize();
    ResourcePackAssetProcessor.registerListener();
  }

  public static int reloadAllConfigsCommand(CommandContext<ServerCommandSource> context) {
    return doReload(
        context,
        () -> {
          RuntimeModelManager.clearRequestedModels();
          mainConfig.load();
          CustomItemRegistry.setLegacyMode(mainConfig.isLegacyMode());
          ITEM_SKINS_GUI_CONFIG.init();
          COSMETICS_GUI_CONFIG.init();
          CustomItemRegistry.reloadAll();
        });
  }

  public static int reloadItemSkinsConfigsCommand(CommandContext<ServerCommandSource> context) {
    return doReload(
        context,
        () -> {
          mainConfig.load();
          CustomItemRegistry.setLegacyMode(mainConfig.isLegacyMode());
          ITEM_SKINS_GUI_CONFIG.init();
          CustomItemRegistry.reloadItemSkins();
        });
  }

  public static int reloadCosmeticsConfigsCommand(CommandContext<ServerCommandSource> context) {
    return doReload(
        context,
        () -> {
          mainConfig.load();
          CustomItemRegistry.setLegacyMode(mainConfig.isLegacyMode());
          COSMETICS_GUI_CONFIG.init();
          CustomItemRegistry.reloadCosmetics();
        });
  }

  private static int doReload(CommandContext<ServerCommandSource> context, Runnable reloadAction) {
    try {
      reloadAction.run();
      context.getSource().sendFeedback(() -> mainConfig.getSuccessConfigReloadMessage(), false);
    } catch (Exception e) {
      context.getSource().sendFeedback(() -> mainConfig.getErrorConfigReloadMessage(), false);
      ModInit.LOGGER.error("An error occurred during config reload!", e);
    }
    return 1;
  }

  public static void loadDemoConfigs() {
    Path serverCosmeticsDir = MainConfig.SERVER_COSMETICS_DIR;
    if (serverCosmeticsDir.resolve("config.yml").toFile().exists()) {
      return;
    }

    try {
      Files.createDirectories(serverCosmeticsDir);

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
                  serverCosmeticsDir.resolve(
                      demoConfigsPathSource.relativize(sourcePath).toString());
              copyWithDirectories(sourcePath, destPath);
            });
      }
    } catch (Exception e) {
      ModInit.LOGGER.error("Failed to load demo configs.", e);
    }
  }

  private static void copyWithDirectories(Path source, Path dest) {
    try {
      if (Files.isDirectory(source)) {
        Files.createDirectories(dest);
      } else {
        Files.copy(source, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to copy demo file " + source, e);
    }
  }

  public static MainConfig getMainConfig() {
    return mainConfig;
  }
}
