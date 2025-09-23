package com.zefir.servercosmetics.mixin;

import com.zefir.servercosmetics.data.ItemType;
import com.zefir.servercosmetics.database.DatabaseManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {
//    @Inject(
//            method = "startTracking",
//            at = @At(
//                    value = "HEAD"
//            )
//    )
//    void sendBodyCosmeticsToNewPlayers (ServerPlayerEntity player, CallbackInfo ci) {
//        if ((LivingEntity) (Object) this instanceof ServerPlayerEntity player){
//            if(slot.getEntitySlotId() == 3) {
//                ItemStack cosmeticsIS = DatabaseManager.getCosmeticItemStack(player, ItemType.HAT);
//                if (cosmeticsIS != ItemStack.EMPTY) {
//                    return cosmeticsIS;
//                }
//            }
//        }
//    }
}
