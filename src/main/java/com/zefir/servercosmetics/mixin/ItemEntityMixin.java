package com.zefir.servercosmetics.mixin;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemEntity.class)
public interface ItemEntityMixin {
    @Accessor("STACK")
    static TrackedData<ItemStack> getStackConstant() {
        throw new AssertionError();
    }
}
