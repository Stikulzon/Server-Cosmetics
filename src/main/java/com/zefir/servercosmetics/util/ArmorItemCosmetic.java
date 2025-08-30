package com.zefir.servercosmetics.util;

import com.zefir.servercosmetics.data.ItemType;
import com.zefir.servercosmetics.database.DatabaseManager;
import com.zefir.servercosmetics.ext.ICosmetic;
import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.zefir.servercosmetics.util.PacketUtil.sendInventorySlotPacket;

public class ArmorItemCosmetic implements ICosmetic {
    @Getter
    private ItemStack cosmeticItemStack = ItemStack.EMPTY;
    private final ServerPlayerEntity player;
    @Getter
    private final ItemType itemType;

    public ArmorItemCosmetic(ServerPlayerEntity player, ItemType itemType) {
        this.player = player;
        this.itemType = itemType;
    }

    public void init() {
        this.cosmeticItemStack = DatabaseManager.getCosmeticItemStack(player, itemType);
        tick();
    }

    public void equip(ItemStack cosmeticStack){
        this.cosmeticItemStack = cosmeticStack;
        DatabaseManager.setCosmetic(this.player, itemType, cosmeticStack);

        ItemStack targetItemStack;
        if(cosmeticItemStack.isEmpty() || cosmeticItemStack == ItemStack.EMPTY) {
            targetItemStack = player.getInventory().getArmorStack(getSlotFor(itemType) - 5);
        } else {
            targetItemStack = cosmeticStack;
        }
        sendInventorySlotPacket(player, getSlotFor(itemType), targetItemStack);
    }

    public void tick(){
        if(cosmeticItemStack.isEmpty() || cosmeticItemStack == ItemStack.EMPTY) {
            return;
        }
        sendInventorySlotPacket(player, getSlotFor(itemType), cosmeticItemStack);
    }

    private static int getSlotFor(ItemType type) {
        return switch (type) {
            case ItemType.HAT -> 5;
            case ItemType.CHESTPLATE -> 6;
            case ItemType.LEGGINGS -> 7;
            case ItemType.BOOTS -> 8;
            default -> throw new IllegalArgumentException("Invalid ItemType");
        };
    }
}
