package com.zefir.servercosmetics.gui.actions;

import com.zefir.servercosmetics.data.CustomItemEntry;
import com.zefir.servercosmetics.data.ItemType;
import com.zefir.servercosmetics.ext.ICosmetics;
import com.zefir.servercosmetics.gui.core.IItemAction;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.zefir.servercosmetics.datafixer.NbtDatafixer.NEW_NBT_KEY_CUSTOM_ITEM_ID;

public class EquipCosmeticAction implements IItemAction {
    @Override
    public void execute(ServerPlayerEntity player, CustomItemEntry entry, SimpleGui gui) {
        execute(player, entry.itemStack(), entry.type());
    }

    public void execute(ServerPlayerEntity player, ItemStack cosmeticStack, ItemType type) {
        ((ICosmetics) player).getCosmeticFor(type).equip(cosmeticStack, type);
    }
}