package com.zefir.servercosmetics.gui.actions;


import com.zefir.servercosmetics.config.entries.CustomItemEntry;
import com.zefir.servercosmetics.database.DatabaseManager;
import com.zefir.servercosmetics.ext.CosmeticSlotExt;
import com.zefir.servercosmetics.gui.ColorPickerComponent;
import com.zefir.servercosmetics.gui.core.IItemAction;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;

public class OpenColorPickerAction implements IItemAction {
    @Override
    public void execute(ServerPlayerEntity player, CustomItemEntry entry, SimpleGui gui) {
        new ColorPickerComponent(player, entry.itemStack(), (coloredStack) -> {
            DatabaseManager.setHeadCosmetics(player.getUuid(), coloredStack);
            ((CosmeticSlotExt) player.playerScreenHandler).setHeadCosmetics(coloredStack);
            player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(
                    player.playerScreenHandler.syncId,
                    player.playerScreenHandler.nextRevision(),
                    5, // Head Slot
                    coloredStack
            ));
        }).open();
    }
}
