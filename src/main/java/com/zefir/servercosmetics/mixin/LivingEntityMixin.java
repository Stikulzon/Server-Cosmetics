package com.zefir.servercosmetics.mixin;

import com.zefir.servercosmetics.CosmeticsData;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Redirect(
            method = "method_30120",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;",
                    ordinal = 0
            )
    )
    ItemStack modifyHeadSlotItem (ItemStack instance, List list, EquipmentSlot slot, ItemStack stack) {
        if ((LivingEntity) (Object) this instanceof PlayerEntity){
            if(slot.getEntitySlotId() == 3) {
                LivingEntity livingEntity = (LivingEntity) (Object) this;
                PlayerEntity playerEntity = (PlayerEntity) livingEntity;
                ItemStack cosmeticsIS = CosmeticsData.getHeadCosmetics(playerEntity);
                if (cosmeticsIS != ItemStack.EMPTY) {
                    return cosmeticsIS;
                }
            }
        }
        return instance.copy();
    }
}
