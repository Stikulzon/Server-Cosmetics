package ua.zefir.servercosmetics.gui.core;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.network.ServerPlayerEntity;
import ua.zefir.servercosmetics.data.CustomItemEntry;

@FunctionalInterface
public interface ItemAction {
  void execute(ServerPlayerEntity player, CustomItemEntry entry, SimpleGui gui);
}
