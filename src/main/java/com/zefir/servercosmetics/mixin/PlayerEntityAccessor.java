package com.zefir.servercosmetics.mixin;

import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerEntity.class)
public interface PlayerEntityAccessor {
//    @Accessor("LEFT_SHOULDER_ENTITY")
//    static TrackedData<NbtCompound> getLeftShoulderEntityTrackedData(){
//        throw new AssertionError();
//    };
}
