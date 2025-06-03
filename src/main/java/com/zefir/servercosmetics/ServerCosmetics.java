package com.zefir.servercosmetics;

import com.zefir.servercosmetics.command.CosmeticCommands;
import com.zefir.servercosmetics.config.ConfigManager;
import com.zefir.servercosmetics.database.DatabaseManager;
import com.zefir.servercosmetics.gui.resources.GuiTextures;
import com.zefir.servercosmetics.gui.resources.UiResourceCreator;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerCosmetics implements ModInitializer {
	public static final String MOD_ID = "servercosmetics";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static MinecraftServer SERVER;

	@Override
	public void onInitialize() {
		ConfigManager.registerConfigs();
		DatabaseManager.init();

		ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
		CosmeticCommands.registerCommands();
		UiResourceCreator.setup();
		GuiTextures.register();

		if (PolymerResourcePackUtils.addModAssets(MOD_ID)) {
			LOGGER.info("Successfully added mod assets for " + MOD_ID);
		} else {
			LOGGER.error("Failed to add mod assets for " + MOD_ID);
		}
		PolymerResourcePackUtils.markAsRequired();
	}


	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	private void onServerStarting(MinecraftServer server) {
		SERVER = server;
	}
}