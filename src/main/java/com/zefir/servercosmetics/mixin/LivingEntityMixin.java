package com.zefir.servercosmetics.mixin;

import com.zefir.servercosmetics.data.ItemType;
import com.zefir.servercosmetics.database.DatabaseManager;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

import static com.zefir.servercosmetics.util.Utils.getItemTypeForSlot;

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
        if ((LivingEntity) (Object) this instanceof ServerPlayerEntity player){
            ItemType itemType = getItemTypeForSlot(8 - slot.getEntitySlotId());
            if (itemType == null) {
                throw new IllegalStateException("Invalid slot for cosmetic: " + (slot.getEntitySlotId() - 8));
            }
            ItemStack cosmeticsIS = DatabaseManager.getCosmeticItemStack(player, itemType);
            if (cosmeticsIS != ItemStack.EMPTY) {
                return cosmeticsIS;
            }
        }
        return instance.copy();
    }
}
