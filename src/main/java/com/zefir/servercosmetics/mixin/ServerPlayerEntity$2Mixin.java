package com.zefir.servercosmetics.mixin;

import com.zefir.servercosmetics.data.ItemType;
import com.zefir.servercosmetics.ext.ICosmetics;
import com.zefir.servercosmetics.util.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.zefir.servercosmetics.util.Utils.getItemTypeForSlot;

@Mixin(targets = "net.minecraft.server.network.ServerPlayerEntity$2")
public class ServerPlayerEntity$2Mixin {
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
    void modifyArmorItemStack (ScreenHandler handler, int slot, ItemStack _stack, CallbackInfo ci) {
        if(handler instanceof PlayerScreenHandler) {
            ICosmetics cosmetics = (ICosmetics) field_29183;
            ItemType itemType = getItemTypeForSlot(slot);
            if(itemType != null) {
                cosmetics.getCosmeticFor(itemType).tick();
            }
        }
    }
}
