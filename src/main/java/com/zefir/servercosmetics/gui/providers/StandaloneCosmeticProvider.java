package com.zefir.servercosmetics.gui.providers;

import com.zefir.servercosmetics.config.entries.CustomItemEntry;
import com.zefir.servercosmetics.config.entries.CustomItemRegistry;
import com.zefir.servercosmetics.config.entries.ItemType;
import com.zefir.servercosmetics.gui.core.ICosmeticProvider;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class StandaloneCosmeticProvider implements ICosmeticProvider {

//    public final ItemType type;
//    public StandaloneCosmeticProvider(ItemType type) {
//        this.type = type;
//    }
//
    @Override
    public List<CustomItemEntry> getItems() {
        return Stream.concat(CustomItemRegistry.getAllCosmetics(ItemType.HAT).stream(), CustomItemRegistry.getAllCosmetics(ItemType.BODY_COSMETIC).stream()).toList();
    }
}
