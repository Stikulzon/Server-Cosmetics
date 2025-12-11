package ua.zefir.servercosmetics.gui.filters;

import java.util.function.Predicate;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.network.ServerPlayerEntity;
import ua.zefir.servercosmetics.data.CustomItemEntry;

public record PermissionFilter(ServerPlayerEntity player) implements Predicate<CustomItemEntry> {
  @Override
  public boolean test(CustomItemEntry entry) {
    return Permissions.check(player, entry.permission(), 4);
  }
}
