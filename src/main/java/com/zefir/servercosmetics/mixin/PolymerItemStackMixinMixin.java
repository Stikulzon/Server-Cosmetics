package com.zefir.servercosmetics.mixin;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zefir.servercosmetics.util.Utils;
import eu.pb4.polymer.core.mixin.item.ItemStackMixin;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(value = ItemStack.class, priority = 1500)
public class PolymerItemStackMixinMixin {
//    @TargetHandler(
//            mixin = "eu.pb4.polymer.core.mixin.item.ItemStackMixin",
//            name = "lambda$patchCodec$0"
//    )
//    @WrapOperation(
//            method = "@MixinSquared:Handler",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Leu/pb4/polymer/core/api/item/PolymerItemUtils;getRealItemStack(Lnet/minecraft/item/ItemStack;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/item/ItemStack;"
//            )
//    )
//    private static ItemStack reduceLogLevel(ItemStack itemStack, RegistryWrapper.WrapperLookup lookup, Operation<ItemStack> original) {
//        return original.call(Utils.filterItemStack(itemStack), lookup);
//    }

    @TargetHandler(
            mixin = "eu.pb4.polymer.core.mixin.item.ItemStackMixin",
            name = "lambda$patchCodec$1"
    )
    @WrapOperation(
            method = "@MixinSquared:Handler",
            at = @At(
                    value = "INVOKE",
                    target = "Leu/pb4/polymer/core/api/item/PolymerItemUtils;getPolymerItemStack(Lnet/minecraft/item/ItemStack;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;Lnet/minecraft/server/network/ServerPlayerEntity;)Lnet/minecraft/item/ItemStack;"
            )
    )
    private static ItemStack reduceLogLevel1(ItemStack itemStack, RegistryWrapper.WrapperLookup lookup, @Nullable ServerPlayerEntity player, Operation<ItemStack> original) {
        return original.call(Utils.filterItemStack(itemStack), lookup, player);
    }
}
