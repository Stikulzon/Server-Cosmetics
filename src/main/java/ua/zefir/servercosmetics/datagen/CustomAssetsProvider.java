package ua.zefir.servercosmetics.datagen;

import com.google.common.hash.HashCode;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.util.Util;
import ua.zefir.servercosmetics.ModInit;
import ua.zefir.servercosmetics.gui.resources.UiResourceCreator;

public class CustomAssetsProvider implements DataProvider {
  private final DataOutput output;

  public CustomAssetsProvider(FabricDataOutput output) {
    this.output = output;
  }

  @Override
  public CompletableFuture<?> run(DataWriter writer) {
    BiConsumer<String, byte[]> assetWriter =
        (path, data) -> {
          try {
            writer.write(this.output.getPath().resolve(path), data, HashCode.fromBytes(data));
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
        Util.getMainWorkerExecutor());
  }

  @Override
  public String getName() {
    return ModInit.MOD_ID + ":assets";
  }
}
