package ua.zefir.servercosmetics;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.registry.RegistryBuilder;
import ua.zefir.servercosmetics.datagen.CustomAssetsProvider;

public class ModDataGenerator implements DataGeneratorEntrypoint {
  @Override
  public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
    FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

    pack.addProvider(CustomAssetsProvider::new);
  }

  @Override
  public void buildRegistry(RegistryBuilder registryBuilder) {}
}
