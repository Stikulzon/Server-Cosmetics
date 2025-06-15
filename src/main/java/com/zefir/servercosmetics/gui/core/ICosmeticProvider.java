package com.zefir.servercosmetics.gui.core;

import com.zefir.servercosmetics.config.entries.CustomItemEntry;
import com.zefir.servercosmetics.config.entries.ItemType;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.List;

@FunctionalInterface
public interface ICosmeticProvider {
    List<CustomItemEntry> getItems();
}