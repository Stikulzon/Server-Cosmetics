package ua.zefir.servercosmetics.gui.providers;

import ua.zefir.servercosmetics.data.CustomItemEntry;
import ua.zefir.servercosmetics.data.CustomItemRegistry;
import ua.zefir.servercosmetics.gui.core.ICosmeticProvider;

import java.util.List;

public class StandaloneCosmeticProvider implements ICosmeticProvider {
    @Override
    public List<CustomItemEntry> getItems() {
        return CustomItemRegistry.getCosmeticsList();
    }
}
