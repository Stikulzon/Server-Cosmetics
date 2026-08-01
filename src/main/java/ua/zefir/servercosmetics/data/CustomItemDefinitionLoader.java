package ua.zefir.servercosmetics.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import ua.zefir.servercosmetics.ModInit;
import ua.zefir.servercosmetics.util.Utils;

final class CustomItemDefinitionLoader {

  private CustomItemDefinitionLoader() {}

  static void load(Path directory, String itemPropertiesRootNode, ItemProcessor processor) {
    if (!setupDirectory(directory)) {
      return;
    }

    for (Path filePath : Utils.listFiles(directory)) {
      if (!filePath.toString().toLowerCase().endsWith(".yml")) {
        continue;
      }

      try {
        processor.process(filePath, itemPropertiesRootNode);
      } catch (Exception exception) {
        ModInit.LOGGER.error("Failed to load custom item from file: {}", filePath, exception);
      }
    }
  }

  private static boolean setupDirectory(Path directory) {
    try {
      if (Files.notExists(directory)) {
        Files.createDirectories(directory);
        ModInit.LOGGER.info("Created directory: {}", directory.toAbsolutePath());
        return false;
      }
      if (!Files.isDirectory(directory)) {
        ModInit.LOGGER.error("Path is not a directory: {}", directory.toAbsolutePath());
        return false;
      }
      return true;
    } catch (IOException exception) {
      ModInit.LOGGER.error("Failed to create or access directory: {}", directory, exception);
      return false;
    }
  }

  @FunctionalInterface
  interface ItemProcessor {
    void process(Path filePath, String itemPropertiesRootNode) throws Exception;
  }
}
