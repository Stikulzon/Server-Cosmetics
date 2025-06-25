package com.zefir.servercosmetics.gui.actions;

import com.zefir.servercosmetics.config.entries.CustomItemEntry;
import com.zefir.servercosmetics.config.entries.ItemType;
import com.zefir.servercosmetics.ext.ICosmetics;
import com.zefir.servercosmetics.gui.core.IItemAction;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public class EquipCosmeticAction implements IItemAction {
    @Override
    public void execute(ServerPlayerEntity player, CustomItemEntry entry, SimpleGui gui) {
        gui.close();
        execute(player, entry.itemStack(), entry.type());
    }

    public void execute(ServerPlayerEntity player, ItemStack cosmeticStack, ItemType type) {
        if(type == ItemType.HAT) {
            ((ICosmetics) player).getHatCosmetic().equip(cosmeticStack);
        } else if(type == ItemType.BODY_COSMETIC) {
            ((ICosmetics) player).getBodyCosmetics().equip(cosmeticStack);
        }
    }
}