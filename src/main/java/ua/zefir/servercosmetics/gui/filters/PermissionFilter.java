package ua.zefir.servercosmetics.gui.filters;

import ua.zefir.servercosmetics.data.CustomItemEntry;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.Predicate;

public record PermissionFilter(ServerPlayerEntity player) implements Predicate<CustomItemEntry> {
    @Override
    public boolean test(CustomItemEntry entry) {
        return Permissions.check(player, entry.permission(), 4);
    }
}