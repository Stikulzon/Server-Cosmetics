package ua.zefir.servercosmetics.gui.actions;

import ua.zefir.servercosmetics.data.CustomItemEntry;
import ua.zefir.servercosmetics.data.ItemType;
import ua.zefir.servercosmetics.ext.ICosmetics;
import ua.zefir.servercosmetics.gui.core.IItemAction;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public class EquipCosmeticAction implements IItemAction {
    @Override
    public void execute(ServerPlayerEntity player, CustomItemEntry entry, SimpleGui gui) {
//        gui.close();
        execute(player, entry.itemStack(), entry.type());
    }

    public void execute(ServerPlayerEntity player, ItemStack cosmeticStack, ItemType type) {
        ((ICosmetics) player).getCosmeticFor(type).equip(cosmeticStack, type);
    }
}