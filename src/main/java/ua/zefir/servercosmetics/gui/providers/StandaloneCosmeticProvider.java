package ua.zefir.servercosmetics.gui.providers;

import java.util.List;
import ua.zefir.servercosmetics.data.CustomItemEntry;
import ua.zefir.servercosmetics.data.CustomItemRegistry;
import ua.zefir.servercosmetics.gui.core.CosmeticProvider;

public class StandaloneCosmeticProvider implements CosmeticProvider {
  @Override
  public List<CustomItemEntry> getItems() {
    return CustomItemRegistry.getCosmeticsList();
  }
}
