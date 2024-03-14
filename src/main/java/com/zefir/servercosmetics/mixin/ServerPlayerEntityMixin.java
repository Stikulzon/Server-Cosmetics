package com.zefir.servercosmetics.mixin;

import com.zefir.servercosmetics.ext.CosmeticSlotExt;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.network.ServerPlayerEntity$2")
public class ServerPlayerEntityMixin {
    @Final
    @Shadow
    ServerPlayerEntity field_29183;
    @Inject(
            method = "onSlotUpdate",
            at = @At(
                    value = "TAIL",
                    target = "Lnet/minecraft/advancement/criterion/Criteria;INVENTORY_CHANGED:Lnet/minecraft/advancement/criterion/InventoryChangedCriterion;"
            )
    )
    void modifyHeadSlotItem (ScreenHandler handler, int slot, ItemStack _stack, CallbackInfo ci) {
        if(handler instanceof PlayerScreenHandler) {
            if (((CosmeticSlotExt) handler).getHeadCosmetics() != ItemStack.EMPTY && slot == 5) {
                ItemStack itemStack = ((CosmeticSlotExt) handler).getHeadCosmetics();

                this.field_29183.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), 5, itemStack));
            }
        }
    }
}
