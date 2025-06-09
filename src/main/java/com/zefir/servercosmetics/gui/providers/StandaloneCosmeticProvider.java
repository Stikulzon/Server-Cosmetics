package com.zefir.servercosmetics.gui.providers;

import com.zefir.servercosmetics.config.entries.CustomItemEntry;
import com.zefir.servercosmetics.config.entries.CustomItemRegistry;
import com.zefir.servercosmetics.gui.core.ICosmeticProvider;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.ArrayList;
import java.util.List;

public class StandaloneCosmeticProvider implements ICosmeticProvider {
    @Override
    public List<CustomItemEntry> getItems(ServerPlayerEntity player) {
        return new ArrayList<>(CustomItemRegistry.getAllStandaloneCosmetics());
    }
}
