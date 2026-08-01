package ua.zefir.servercosmetics;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import ua.zefir.servercosmetics.datagen.CustomAssetsProvider;

public class ModDataGenerator implements DataGeneratorEntrypoint {
  @Override
  public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
    FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

    pack.addProvider(
        (FabricDataGenerator.Pack.Factory<CustomAssetsProvider>) CustomAssetsProvider::new);
  }

  @Override
  public void buildRegistry(RegistrySetBuilder registryBuilder) {}
}
