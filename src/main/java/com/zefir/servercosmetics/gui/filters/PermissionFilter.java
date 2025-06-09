package com.zefir.servercosmetics.gui.filters;

import com.zefir.servercosmetics.config.entries.CustomItemEntry;
import com.zefir.servercosmetics.gui.core.IFilter;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.network.ServerPlayerEntity;

public class PermissionFilter implements IFilter {
    @Override
    public boolean test(ServerPlayerEntity player, CustomItemEntry entry) {
        return Permissions.check(player, entry.permission());
    }
}