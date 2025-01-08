package com.zefir.servercosmetics;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.zefir.servercosmetics.config.ConfigManager;
import com.zefir.servercosmetics.gui.CosmeticsGUI;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;

public class ServerCosmetics implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("server-cosmetics");
	public static MinecraftServer SERVER;

	@Override
	public void onInitialize() {
		ConfigManager.registerConfigs();
		CosmeticsData.init();

		ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
		ConfigManager.registerCommands();
	}

	private void onServerStarting(MinecraftServer server) {
		SERVER = server;
		LOGGER.info("initialized");
	}
}