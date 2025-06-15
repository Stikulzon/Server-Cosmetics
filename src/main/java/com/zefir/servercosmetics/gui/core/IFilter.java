package com.zefir.servercosmetics.gui.core;

import com.zefir.servercosmetics.config.entries.CustomItemEntry;
import com.zefir.servercosmetics.config.entries.ItemType;
import net.minecraft.server.network.ServerPlayerEntity;

public interface IFilter {
    boolean test(ServerPlayerEntity player, CustomItemEntry entry);
}
