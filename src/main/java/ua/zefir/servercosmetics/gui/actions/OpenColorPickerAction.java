package ua.zefir.servercosmetics.gui.actions;

import ua.zefir.servercosmetics.data.CustomItemEntry;
import ua.zefir.servercosmetics.data.ItemType;
import ua.zefir.servercosmetics.ext.ICosmetics;
import ua.zefir.servercosmetics.gui.ColorPickerComponent;
import ua.zefir.servercosmetics.gui.core.IItemAction;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.network.ServerPlayerEntity;

public class OpenColorPickerAction implements IItemAction {
    @Override
    public void execute(ServerPlayerEntity player, CustomItemEntry entry, SimpleGui gui) {
        new ColorPickerComponent(player, entry.itemStack(), (coloredStack) -> (
                (ICosmetics) player).getCosmeticFor(ItemType.HAT).equip(coloredStack, ItemType.HAT)
        ).open();
    }
}
