package com.zefir.servercosmetics.gui.actions;

import com.zefir.servercosmetics.data.CustomItemEntry;
import com.zefir.servercosmetics.data.ItemType;
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
        ((ICosmetics) player).getCosmeticFor(type).equip(cosmeticStack);
    }
}