package com.zefir.servercosmetics.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.zefir.servercosmetics.config.ConfigManager;
import com.zefir.servercosmetics.config.CosmeticsGUIConfig;
import com.zefir.servercosmetics.config.ItemSkinsGUIConfig;
import com.zefir.servercosmetics.gui.CosmeticsGUI;
import com.zefir.servercosmetics.gui.ItemSkinsGUI;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;

import java.util.Objects;

import static com.zefir.servercosmetics.config.ConfigManager.*;
import static net.minecraft.server.command.CommandManager.literal;

public class CosmeticCommands {
    public static void registerCommands(){
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("sc")
                    .then(literal("reload")
                            .requires(Permissions.require(Objects.requireNonNullElse(configReloadPermission, "servercosmetics.reload"), 4))
                            .executes(ConfigManager::reloadAllConfigsCommand))
            );
            dispatcher.register(
                    literal("cm").executes(CosmeticsGUI::openGui)
                            .requires(Permissions.require(CosmeticsGUIConfig.get().getPermissionOpenGui(), 0))
                            .then(literal("reload")
                                    .requires(Permissions.require(Objects.requireNonNullElse(cosmeticsReloadPermission, "servercosmetics.reload.cosmetics"), 4))
                                    .executes(ConfigManager::reloadCosmeticsConfigsCommand))
            );
            dispatcher.register(literal("wearcosmetic")
                    .requires(Permissions.require("servercosmetics.wearcosmetic", 4))
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                            .then(CommandManager.argument("cosmeticId", StringArgumentType.string())
                                    .executes(CosmeticsGUI::wearCosmeticById)))
            );
            dispatcher.register(
                    literal("is").executes(ItemSkinsGUI::openIsGui)
                            .requires(Permissions.require(ItemSkinsGUIConfig.get().getPermissionOpenGui(), 0))
                            .then(literal("reload")
                                    .requires(Permissions.require(Objects.requireNonNullElse(itemSkinsReloadPermission, "servercosmetics.reload.itemskins"), 4))
                                    .executes(ConfigManager::reloadItemSkinsConfigsCommand))
            );
        });
    }
}
