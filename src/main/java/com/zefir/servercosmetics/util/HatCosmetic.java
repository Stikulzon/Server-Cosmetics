package com.zefir.servercosmetics.util;

import com.zefir.servercosmetics.config.entries.ItemType;
import com.zefir.servercosmetics.database.DatabaseManager;
import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;

public class HatCosmetic {
    @Getter
    private ItemStack cosmeticItemStack = ItemStack.EMPTY;
    private final ServerPlayerEntity player;

    public HatCosmetic(ServerPlayerEntity player){
        this.player = player;
    }

    public void initItemStack() {
        this.cosmeticItemStack = DatabaseManager.getCosmetic(player, ItemType.HAT);
    }

    public void equip(ItemStack cosmeticStack){
        this.cosmeticItemStack = cosmeticStack;
        DatabaseManager.setCosmetic(this.player, ItemType.HAT, cosmeticStack);

        ItemStack targetItemStack;
        if(cosmeticItemStack.isEmpty() || cosmeticItemStack == ItemStack.EMPTY) {
            targetItemStack = player.getInventory().getArmorStack(3);
        } else {
            targetItemStack = cosmeticStack;
        }
        sendHeadCosmeticsPacket(targetItemStack);
    }

    public void tick(){
        if(cosmeticItemStack.isEmpty() || cosmeticItemStack == ItemStack.EMPTY) {
            return;
        }
        sendHeadCosmeticsPacket(cosmeticItemStack);
    }

    public void sendHeadCosmeticsPacket(ItemStack targetItemStack){
        this.player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(
                this.player.playerScreenHandler.syncId,
                this.player.playerScreenHandler.nextRevision(),
                5, // Head Slot
                targetItemStack
        ));
    }
}
