package com.zefir.servercosmetics.util;

import com.zefir.servercosmetics.data.ItemType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.zefir.servercosmetics.database.DatabaseManager.setCosmetic;
import static com.zefir.servercosmetics.util.PacketUtil.sendInventorySlotPacket;
import static com.zefir.servercosmetics.util.Utils.getSlotForType;

public class ArmorBodyCosmetic extends BodyCosmetic {
    public ArmorBodyCosmetic(ServerPlayerEntity player, ItemType itemType) {
        super(player, itemType);
    }

    @Override
    public void equip(ItemStack is) {
        setCosmetic(player, itemType, is);
        this.cosmeticItemStack = is;
        init();
    }

    @Override
    public void init() {
        super.init();
        tickItem();
    }

    public void tickItem(){
        if(cosmeticItemStack.isEmpty() || cosmeticItemStack == ItemStack.EMPTY) {
            return;
        }
        sendInventorySlotPacket(player, getSlotForType(itemType), cosmeticItemStack);
    }
}
