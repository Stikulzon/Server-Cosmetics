package com.zefir.servercosmetics.mixin;

import com.mojang.datafixers.util.Pair;
import com.zefir.servercosmetics.data.ItemType;
import com.zefir.servercosmetics.database.DatabaseManager;
import com.zefir.servercosmetics.util.Utils;
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
    ItemStack modifyArmorItemStack (ItemStack instance, List<Pair<EquipmentSlot, ItemStack>> list, EquipmentSlot slot, ItemStack stack) {
        if ((LivingEntity) (Object) this instanceof ServerPlayerEntity player){
            ItemType itemType = getItemTypeForSlot(8 - slot.getEntitySlotId());
            if (itemType == null) {
                throw new IllegalStateException("Invalid slot for cosmetic: " + (slot.getEntitySlotId() - 8));
            }
            ItemStack cosmeticsIS = DatabaseManager.getCosmeticItemStack(player, itemType);
            if (cosmeticsIS != ItemStack.EMPTY) {
                return Utils.filterItemStack(cosmeticsIS, player);
            }
        }
        return Utils.filterItemStack(instance);
    }
}
