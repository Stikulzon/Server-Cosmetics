package ua.zefir.servercosmetics.datagen;

import com.google.common.hash.HashCode;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.util.Util;
import ua.zefir.servercosmetics.ModInit;
import ua.zefir.servercosmetics.datagen.ui.UiResourceCreator;

public class CustomAssetsProvider implements DataProvider {
  private final PackOutput output;

  public CustomAssetsProvider(FabricPackOutput output) {
    this.output = output;
  }

  @Override
  public CompletableFuture<?> run(CachedOutput writer) {
    BiConsumer<String, byte[]> assetWriter =
        (path, data) -> {
          try {
            writer.writeIfNeeded(
                this.output.getOutputFolder().resolve(path), data, HashCode.fromBytes(data));
          } catch (IOException e) {
            e.printStackTrace();
          }
        };
    return CompletableFuture.runAsync(
        () -> {
          try {
            UiResourceCreator.generateAssets(assetWriter);
          } catch (Throwable e) {
            throw new RuntimeException(e);
          }
        },
        Util.backgroundExecutor());
  }

  @Override
  public String getName() {
    return ModInit.MOD_ID + ":assets";
  }
}
