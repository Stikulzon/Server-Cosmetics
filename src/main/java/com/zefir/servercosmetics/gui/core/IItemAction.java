package com.zefir.servercosmetics.gui.core;

import com.zefir.servercosmetics.config.entries.CustomItemEntry;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.network.ServerPlayerEntity;

@FunctionalInterface
public interface IItemAction {
    void execute(ServerPlayerEntity player, CustomItemEntry entry, SimpleGui gui);
}
