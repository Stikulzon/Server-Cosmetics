package ua.zefir.servercosmetics;

import ua.zefir.servercosmetics.command.CosmeticCommands;
import ua.zefir.servercosmetics.config.ConfigManager;
import ua.zefir.servercosmetics.database.DatabaseManager;
import ua.zefir.servercosmetics.datagen.ui.GuiTextures;
import ua.zefir.servercosmetics.datagen.ui.UiResourceCreator;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModInit implements ModInitializer {
	public static final String MOD_ID = "servercosmetics";
    public static final String VERSION = FabricLoader.getInstance().getModContainer(MOD_ID).get().getMetadata().getVersion().getFriendlyString();
    public static final boolean DEV_ENV = FabricLoader.getInstance().isDevelopmentEnvironment();
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static MinecraftServer SERVER;

	@Override
	public void onInitialize() {
        if (VERSION.contains("-dev.")) {
            LOGGER.warn("=====================================================");
            LOGGER.warn("You are using development version of ServerCosmetics!");
            LOGGER.warn("Support is limited, as features might be unfinished!");
            LOGGER.warn("You are on your own!");
            LOGGER.warn("=====================================================");
        }

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