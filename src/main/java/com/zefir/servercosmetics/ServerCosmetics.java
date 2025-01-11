package com.zefir.servercosmetics;

import com.zefir.servercosmetics.config.ConfigManager;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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