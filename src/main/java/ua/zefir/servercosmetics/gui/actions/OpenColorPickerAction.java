package ua.zefir.servercosmetics.gui.actions;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.network.ServerPlayerEntity;
import ua.zefir.servercosmetics.cosmetic.CosmeticHolder;
import ua.zefir.servercosmetics.data.CustomItemEntry;
import ua.zefir.servercosmetics.data.ItemType;
import ua.zefir.servercosmetics.gui.ColorPickerComponent;
import ua.zefir.servercosmetics.gui.core.ItemAction;

public class OpenColorPickerAction implements ItemAction {
  @Override
  public void execute(ServerPlayerEntity player, CustomItemEntry entry, SimpleGui gui) {
    new ColorPickerComponent(
            player,
            entry.itemStack(),
            (coloredStack) ->
                ((CosmeticHolder) player)
                    .getCosmeticFor(ItemType.HAT)
                    .equip(coloredStack, ItemType.HAT))
        .open();
  }
}
