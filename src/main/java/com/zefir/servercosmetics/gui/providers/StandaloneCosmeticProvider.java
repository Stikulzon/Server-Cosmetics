package com.zefir.servercosmetics.gui.providers;

import com.zefir.servercosmetics.data.CustomItemEntry;
import com.zefir.servercosmetics.data.CustomItemRegistry;
import com.zefir.servercosmetics.gui.core.ICosmeticProvider;

import java.util.List;

public class StandaloneCosmeticProvider implements ICosmeticProvider {
    @Override
    public List<CustomItemEntry> getItems() {
        return CustomItemRegistry.getCosmeticsList();
    }
}
