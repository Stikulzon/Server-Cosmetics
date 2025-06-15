package com.zefir.servercosmetics.gui.filters;

import com.zefir.servercosmetics.config.entries.CustomItemEntry;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.Predicate;

public class PermissionFilter implements Predicate<CustomItemEntry> {
    private final ServerPlayerEntity player;

    public PermissionFilter(ServerPlayerEntity player) {
        this.player = player;
    }

    @Override
    public boolean test(CustomItemEntry entry) {
        return Permissions.check(player, entry.permission());
    }
}