package ua.zefir.servercosmetics.command;

import static net.minecraft.server.command.CommandManager.literal;
import static ua.zefir.servercosmetics.config.ConfigManager.*;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.util.Objects;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import ua.zefir.servercosmetics.config.ConfigManager;
import ua.zefir.servercosmetics.config.MainConfig;
import ua.zefir.servercosmetics.gui.CosmeticsGui;
import ua.zefir.servercosmetics.gui.ItemSkinsGui;
import ua.zefir.servercosmetics.util.ConfigGenerator;
import ua.zefir.servercosmetics.util.Utils;

public class CosmeticCommands {
  public static void registerCommands() {
    CommandRegistrationCallback.EVENT.register(
        (dispatcher, registryAccess, environment) -> {
          MainConfig mainConfig = ConfigManager.getMainConfig();
          dispatcher.register(
              literal("sc")
                  .then(
                      literal("reload")
                          .requires(
                              Permissions.require(
                                  Objects.requireNonNullElse(
                                      mainConfig.getConfigReloadPermission(),
                                      "servercosmetics.reload"),
                                  4))
                          .executes(ConfigManager::reloadAllConfigsCommand))
                  .then(
                      literal("generate")
                          .requires(Permissions.require("servercosmetics.generate", 4))
                          .executes(ConfigGenerator::generateCosmeticDefinitions)));
          dispatcher.register(
              literal("cm")
                  .executes(CosmeticsGui::openGui)
                  .requires(Permissions.require(COSMETICS_GUI_CONFIG.getPermissionOpenGui(), 0))
                  .then(
                      literal("reload")
                          .requires(
                              Permissions.require(
                                  Objects.requireNonNullElse(
                                      mainConfig.getCosmeticsReloadPermission(),
                                      "servercosmetics.reload.cosmetics"),
                                  4))
                          .executes(ConfigManager::reloadCosmeticsConfigsCommand)));
          dispatcher.register(
              literal("cosmetics")
                  .executes(CosmeticsGui::openGui)
                  .requires(Permissions.require(COSMETICS_GUI_CONFIG.getPermissionOpenGui(), 0))
                  .then(
                      literal("reload")
                          .requires(
                              Permissions.require(
                                  Objects.requireNonNullElse(
                                      mainConfig.getCosmeticsReloadPermission(),
                                      "servercosmetics.reload.cosmetics"),
                                  4))
                          .executes(ConfigManager::reloadCosmeticsConfigsCommand)));

          dispatcher.register(
              literal("test")
                  .requires(Permissions.require("servercosmetics.wearcosmetic", 4))
                  .executes(CosmeticCommands::debugCommand));
          dispatcher.register(
              literal("wearcosmetic")
                  .requires(Permissions.require("servercosmetics.wearcosmetic", 4))
                  .then(
                      CommandManager.argument("player", EntityArgumentType.player())
                          .then(
                              CommandManager.argument("cosmeticId", StringArgumentType.string())
                                  .executes(Utils::wearCosmeticById))));
          dispatcher.register(
              literal("is")
                  .executes(ItemSkinsGui::openItemSkinsGui)
                  .requires(Permissions.require(ITEM_SKINS_GUI_CONFIG.getPermissionOpenGui(), 0))
                  .then(
                      literal("reload")
                          .requires(
                              Permissions.require(
                                  Objects.requireNonNullElse(
                                      mainConfig.getItemSkinsReloadPermission(),
                                      "servercosmetics.reload.itemskins"),
                                  4))
                          .executes(ConfigManager::reloadItemSkinsConfigsCommand)));
          dispatcher.register(
              literal("itemskins")
                  .executes(ItemSkinsGui::openItemSkinsGui)
                  .requires(Permissions.require(ITEM_SKINS_GUI_CONFIG.getPermissionOpenGui(), 0))
                  .then(
                      literal("reload")
                          .requires(
                              Permissions.require(
                                  Objects.requireNonNullElse(
                                      mainConfig.getItemSkinsReloadPermission(),
                                      "servercosmetics.reload.itemskins"),
                                  4))
                          .executes(ConfigManager::reloadItemSkinsConfigsCommand)));
        });
  }

  public static boolean test = false;

  private static int debugCommand(
      CommandContext<ServerCommandSource> serverCommandSourceCommandContext) {
    test = !test;
    return 1;
  }
}
