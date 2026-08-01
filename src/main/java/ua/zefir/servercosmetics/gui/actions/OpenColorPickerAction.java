package ua.zefir.servercosmetics.gui.actions;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;
import ua.zefir.servercosmetics.data.CustomItemEntry;
import ua.zefir.servercosmetics.data.ItemType;
import ua.zefir.servercosmetics.gui.ColorPickerComponent;
import ua.zefir.servercosmetics.gui.core.ItemAction;

public class OpenColorPickerAction implements ItemAction {
  private final ItemType targetType;

  public OpenColorPickerAction(ItemType targetType) {
    this.targetType = targetType;
  }

  @Override
  public void execute(ServerPlayer player, CustomItemEntry entry, SimpleGui gui) {
    new ColorPickerComponent(
            player,
            entry.itemStack(),
            targetType,
            (coloredStack) -> new EquipCosmeticAction().execute(player, coloredStack, targetType),
            null)
        .open();
  }
}
