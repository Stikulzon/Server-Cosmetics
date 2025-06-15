package com.zefir.servercosmetics.gui.actions;

import com.zefir.servercosmetics.config.entries.CustomItemEntry;
import com.zefir.servercosmetics.config.entries.ItemType;
import com.zefir.servercosmetics.database.DatabaseManager;
import com.zefir.servercosmetics.ext.CosmeticSlotExt;
import com.zefir.servercosmetics.ext.IBodyCosmetics;
import com.zefir.servercosmetics.gui.core.IItemAction;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;


public class EquipCosmeticAction implements IItemAction {
    @Override
    public void execute(ServerPlayerEntity player, CustomItemEntry entry, SimpleGui gui) {
        gui.close();
        ItemStack cosmeticStack = entry.itemStack();
        if(entry.type() == ItemType.HAT) {
            DatabaseManager.setHeadCosmetics(player.getUuid(), cosmeticStack);
            ((CosmeticSlotExt) player.playerScreenHandler).setHeadCosmetics(cosmeticStack);
            player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(
                    player.playerScreenHandler.syncId,
                    player.playerScreenHandler.nextRevision(),
                    5, // Head Slot
                    cosmeticStack
            ));
        } else {
            ((IBodyCosmetics)player).getBodyCosmetics().equipCosmetics(cosmeticStack);
        }
    }
}