package ua.zefir.servercosmetics.util;

import com.mojang.brigadier.context.CommandContext;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.simpleyaml.configuration.file.YamlFile;
import ua.zefir.servercosmetics.ModInit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static ua.zefir.servercosmetics.config.ConfigManager.*;

public class ConfigGenerator {

    public static int generateConfigsBasedOnModels(CommandContext<ServerCommandSource> context){

        Path resourcePackSourceDir = SERVER_COSMETICS_DIR.resolve("Assets");
        if (!setupDirectory(resourcePackSourceDir)) return 0;

        ModInit.LOGGER.info("Scanning for resources in: {}", resourcePackSourceDir.toAbsolutePath());
        try (Stream<Path> pathStream = Files.walk(resourcePackSourceDir)) {
            pathStream.filter(Files::isRegularFile).forEach(ConfigGenerator::processResourcePackFile);
        } catch (IOException e) {
            ModInit.LOGGER.error("Error walking directory {} for resource pack generation", resourcePackSourceDir.toAbsolutePath(), e);
        }
        context.getSource().sendFeedback(() -> Text.of("Configs generated"), true);
        return 1;
    }

    private static void processResourcePackFile(Path filePath) {
        String fileName = filePath.getFileName().toString();
        String fileNameLower = fileName.toLowerCase(Locale.ROOT);

        if (fileNameLower.endsWith(".json")) {


            Path configFile = SERVER_COSMETICS_DIR.resolve("Cosmetics").resolve(fileNameLower.replace(".json", ".yml"));
            YamlFile yamlFile = new YamlFile(configFile.toAbsolutePath().toString());
            try {

                yamlFile.createOrLoadWithComments();

                yamlFile.addDefault("type", "HAT");
                yamlFile.addDefault("display-name", fileNameLower.replace(".json", ""));
                yamlFile.addDefault("lore", List.of());
                yamlFile.addDefault("permission", ModInit.MOD_ID + ".hat." +  fileNameLower.replace(".json", ""));

                yamlFile.loadWithComments();
            } catch (IOException e) {
                throw new RuntimeException("Failed to create config file", e);
            }
        }
    }
}
