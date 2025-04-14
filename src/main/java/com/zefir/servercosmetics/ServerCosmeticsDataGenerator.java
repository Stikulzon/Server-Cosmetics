package com.zefir.servercosmetics;

import com.zefir.servercosmetics.datagen.CustomAssetsProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.registry.RegistryBuilder;

public class ServerCosmeticsDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator){
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(CustomAssetsProvider::new);
	}

	@Override
	public void buildRegistry(RegistryBuilder registryBuilder) {
	}
}
