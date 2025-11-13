package com.zefir.servercosmetics.mixin;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zefir.servercosmetics.util.Utils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.item.ItemStack$1", priority = 1500)
public class PolymerItemStackPacketCodecMixin {
    @TargetHandler(
            mixin = "eu.pb4.polymer.core.mixin.item.packet.ItemStackPacketCodecMixin",
            name = "polymer$replaceWithVanillaItem"
    )
    @WrapOperation(
            method = "@MixinSquared:Handler",
            at = @At(
                    value = "INVOKE",
                    target = "Leu/pb4/polymer/core/api/item/PolymerItemUtils;getPolymerItemStack(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/tooltip/TooltipType;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;Lnet/minecraft/server/network/ServerPlayerEntity;)Lnet/minecraft/item/ItemStack;"
            )
    )
    private static ItemStack reduceLogLevel1(ItemStack itemStack, TooltipType tooltipType, RegistryWrapper.WrapperLookup tooltipContext, ServerPlayerEntity lookup, Operation<ItemStack> original) {
        return original.call(Utils.filterItemStack(itemStack), tooltipType, tooltipContext, lookup);
    }
}
