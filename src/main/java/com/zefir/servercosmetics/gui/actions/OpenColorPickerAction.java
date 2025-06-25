package com.zefir.servercosmetics.gui.actions;


import com.zefir.servercosmetics.config.entries.CustomItemEntry;
import com.zefir.servercosmetics.ext.ICosmetics;
import com.zefir.servercosmetics.gui.ColorPickerComponent;
import com.zefir.servercosmetics.gui.core.IItemAction;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.network.ServerPlayerEntity;

public class OpenColorPickerAction implements IItemAction {
    @Override
    public void execute(ServerPlayerEntity player, CustomItemEntry entry, SimpleGui gui) {
        new ColorPickerComponent(player, entry.itemStack(), (coloredStack) -> {
            ((ICosmetics) player).getHatCosmetic().equip(coloredStack);
        }).open();
    }
}
