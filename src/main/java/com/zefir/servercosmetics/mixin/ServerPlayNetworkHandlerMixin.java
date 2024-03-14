package com.zefir.servercosmetics.mixin;

import com.zefir.servercosmetics.ext.CosmeticSlotExt;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    @Inject(
            method = "onPickFromInventory",
            at = @At(
                    value = "TAIL",
                    target = "Lnet/minecraft/advancement/criterion/Criteria;INVENTORY_CHANGED:Lnet/minecraft/advancement/criterion/InventoryChangedCriterion;"
            )
    )
    void modifyHeadSlotItem2 (PickFromInventoryC2SPacket packet, CallbackInfo ci) {
        ScreenHandler handler = this.player.currentScreenHandler;
        if(((CosmeticSlotExt) handler).getHeadCosmetics() != ItemStack.EMPTY) {
            ItemStack itemStack = ((CosmeticSlotExt) handler).getHeadCosmetics();
            this.player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), 5, itemStack));
        }
    }

    @Inject(
            method = "onClickSlot",
            at = @At(
                    value = "TAIL",
                    target = "Lnet/minecraft/advancement/criterion/Criteria;INVENTORY_CHANGED:Lnet/minecraft/advancement/criterion/InventoryChangedCriterion;"
            )
    )
    void modifyHeadSlotItem3 (ClickSlotC2SPacket packet, CallbackInfo ci) {
        ScreenHandler handler = this.player.currentScreenHandler;
        if(handler instanceof PlayerScreenHandler) {
            if(((CosmeticSlotExt) handler).getHeadCosmetics() != ItemStack.EMPTY && packet.getSlot() == 5) {
                ItemStack itemStack = ((CosmeticSlotExt) handler).getHeadCosmetics();
                this.player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), 5, itemStack));
            }
        }
    }
}