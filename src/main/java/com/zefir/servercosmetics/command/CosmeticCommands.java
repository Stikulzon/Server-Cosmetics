package com.zefir.servercosmetics.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.zefir.servercosmetics.config.ConfigManager;
import com.zefir.servercosmetics.gui.CosmeticsGUI;
import com.zefir.servercosmetics.gui.ItemSkinsGUI;
import com.zefir.servercosmetics.util.Utils;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Objects;

import static com.zefir.servercosmetics.config.ConfigManager.*;
import static net.minecraft.server.command.CommandManager.literal;

public class CosmeticCommands {
    public static void registerCommands(){
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("sc")
                    .then(literal("reload")
                            .requires(Permissions.require(Objects.requireNonNullElse(ConfigManager.getConfigReloadPermission(), "servercosmetics.reload"), 4))
                            .executes(ConfigManager::reloadAllConfigsCommand))
            );
            dispatcher.register(
                    literal("cm").executes(CosmeticsGUI::openGui)
                            .requires(Permissions.require(COSMETICS_GUI_CONFIG.getPermissionOpenGui(), 0))
                            .then(literal("reload")
                                    .requires(Permissions.require(Objects.requireNonNullElse(ConfigManager.getCosmeticsReloadPermission(), "servercosmetics.reload.cosmetics"), 4))
                                    .executes(ConfigManager::reloadCosmeticsConfigsCommand))
            );
            dispatcher.register(
                    literal("cosmetics").executes(CosmeticsGUI::openGui)
                            .requires(Permissions.require(COSMETICS_GUI_CONFIG.getPermissionOpenGui(), 0))
                            .then(literal("reload")
                                    .requires(Permissions.require(Objects.requireNonNullElse(ConfigManager.getCosmeticsReloadPermission(), "servercosmetics.reload.cosmetics"), 4))
                                    .executes(ConfigManager::reloadCosmeticsConfigsCommand))
            );

            dispatcher.register(literal("test")
                    .requires(Permissions.require("servercosmetics.wearcosmetic", 4))
                    .executes(CosmeticCommands::debugCommand)
            );
            dispatcher.register(literal("wearcosmetic")
                    .requires(Permissions.require("servercosmetics.wearcosmetic", 4))
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                            .then(CommandManager.argument("cosmeticId", StringArgumentType.string())
                                    .executes(Utils::wearCosmeticById)))
            );
            dispatcher.register(
                    literal("is").executes(ItemSkinsGUI::openItemSkinsGui)
                            .requires(Permissions.require(ITEM_SKINS_GUI_CONFIG.getPermissionOpenGui(), 0))
                            .then(literal("reload")
                                    .requires(Permissions.require(Objects.requireNonNullElse(ConfigManager.getItemSkinsReloadPermission(), "servercosmetics.reload.itemskins"), 4))
                                    .executes(ConfigManager::reloadItemSkinsConfigsCommand))
            );
            dispatcher.register(
                    literal("itemskins").executes(ItemSkinsGUI::openItemSkinsGui)
                            .requires(Permissions.require(ITEM_SKINS_GUI_CONFIG.getPermissionOpenGui(), 0))
                            .then(literal("reload")
                                    .requires(Permissions.require(Objects.requireNonNullElse(ConfigManager.getItemSkinsReloadPermission(), "servercosmetics.reload.itemskins"), 4))
                                    .executes(ConfigManager::reloadItemSkinsConfigsCommand))
            );
        });
    }

    public static boolean test = false;

    private static int debugCommand(CommandContext<ServerCommandSource> serverCommandSourceCommandContext) {
        test = !test;
        return 1;
    }
}
