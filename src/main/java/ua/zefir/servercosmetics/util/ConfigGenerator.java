package ua.zefir.servercosmetics.util;

import static ua.zefir.servercosmetics.config.ConfigManager.SERVER_COSMETICS_DIR;

import com.mojang.brigadier.context.CommandContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.simpleyaml.configuration.file.YamlFile;
import ua.zefir.servercosmetics.ModInit;
import ua.zefir.servercosmetics.data.CustomItemRegistry;
import ua.zefir.servercosmetics.data.ItemType;

public class ConfigGenerator {

  /** The main command logic that scans for unused models and generates definitions. */
  public static int generateCosmeticDefinitions(CommandContext<ServerCommandSource> context) {
    Path assetsDir = SERVER_COSMETICS_DIR.resolve("Assets");
    if (!Files.isDirectory(assetsDir)) {
      context
          .getSource()
          .sendError(Text.literal("Assets directory not found at " + assetsDir.toAbsolutePath()));
      return 0;
    }

    long generatedCount;
    try (Stream<Path> pathStream = Files.walk(assetsDir)) {
      generatedCount =
          pathStream
              .filter(
                  path ->
                      Files.isRegularFile(path)
                          && path.getFileName().toString().toLowerCase().endsWith(".json"))
              .filter(ConfigGenerator::processSingleModelFile)
              .count();
    } catch (IOException e) {
      ModInit.LOGGER.error("Error walking assets directory for config generation", e);
      context
          .getSource()
          .sendError(
              Text.literal("An error occurred while scanning models. Check console for details."));
      return 0;
    }

    if (generatedCount > 0) {
      context
          .getSource()
          .sendFeedback(
              () ->
                  Text.literal(
                      "Successfully generated "
                          + generatedCount
                          + " new cosmetic definitions in 'Cosmetics/generated'. Use '/sc reload' to load them."),
              true);
    } else {
      context
          .getSource()
          .sendFeedback(
              () -> Text.literal("No new models found to generate definitions for."), false);
    }

    return (int) generatedCount;
  }

  /**
   * Processes a single model .json file. Returns true if a new config was generated, false
   * otherwise.
   */
  private static boolean processSingleModelFile(Path jsonFilePath) {
    String fileName = jsonFilePath.getFileName().toString();
    String cosmeticId = fileName.substring(0, fileName.lastIndexOf('.')).toLowerCase(Locale.ROOT);

    //        if (GUI_BUTTON_MODELS.contains(cosmeticId)) {
    //            return false;
    //        }

    if (CustomItemRegistry.getCosmetic(cosmeticId) != null) {
      return false;
    }

    Path outputDir = SERVER_COSMETICS_DIR.resolve("Cosmetics/generated");
    try {
      Files.createDirectories(outputDir);
    } catch (IOException e) {
      ModInit.LOGGER.error("Failed to create generated cosmetics directory", e);
      return false;
    }

    Path configFile = outputDir.resolve(cosmeticId + ".yml");
    if (Files.exists(configFile)) {
      return false;
    }

    ItemType inferredType = inferTypeFromFileName(cosmeticId);
    String displayName = prettifyName(cosmeticId);
    String permission = String.format("%s.cosmetics.%s", ModInit.MOD_ID, cosmeticId);

    YamlFile yamlFile = new YamlFile(configFile.toAbsolutePath().toString());
    try {
      yamlFile.createNewFile(false);
      yamlFile.addDefault("type", inferredType.toString());
      yamlFile.addDefault("display-name", displayName);
      yamlFile.addDefault("lore", List.of(""));
      yamlFile.addDefault("permission", permission);
      yamlFile.save();
      ModInit.LOGGER.info("Generated new cosmetic definition: " + configFile.getFileName());
      return true;
    } catch (IOException e) {
      ModInit.LOGGER.error("Failed to create or save cosmetic definition for " + cosmeticId, e);
      return false;
    }
  }

  /** Infers the cosmetic ItemType based on common naming conventions in the filename. */
  private static ItemType inferTypeFromFileName(String cosmeticId) {
    if (cosmeticId.contains("helmet")) return ItemType.HELMET;
    if (cosmeticId.contains("chestplate")) return ItemType.CHESTPLATE;
    if (cosmeticId.contains("leggings")) return ItemType.LEGGINGS;
    if (cosmeticId.contains("boots")) return ItemType.BOOTS;
    if (cosmeticId.contains("hat")) return ItemType.HAT;
    if (cosmeticId.contains("body")) return ItemType.BODY_COSMETIC;
    return ItemType.HAT;
  }

  /**
   * Converts a filename-style string (like "cool_hat_model") to a display-style string ("Cool Hat
   * Model").
   */
  private static String prettifyName(String rawName) {
    rawName = rawName.replace("_", " ").replace("-", " ");
    String[] words = rawName.split(" ");
    StringBuilder pretty = new StringBuilder();
    for (String word : words) {
      if (!word.isEmpty()) {
        pretty.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
      }
    }
    return pretty.toString().trim();
  }
}
