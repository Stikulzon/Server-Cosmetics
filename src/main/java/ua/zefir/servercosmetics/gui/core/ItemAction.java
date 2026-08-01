package ua.zefir.servercosmetics.gui.core;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;
import ua.zefir.servercosmetics.data.CustomItemEntry;

@FunctionalInterface
public interface ItemAction {
  void execute(ServerPlayer player, CustomItemEntry entry, SimpleGui gui);
}
