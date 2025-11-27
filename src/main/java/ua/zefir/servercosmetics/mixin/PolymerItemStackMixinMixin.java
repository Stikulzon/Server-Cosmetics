package ua.zefir.servercosmetics.mixin;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import ua.zefir.servercosmetics.util.Utils;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(value = ItemStack.class, priority = 1500)
public class PolymerItemStackMixinMixin {
    @TargetHandler(
            mixin = "eu.pb4.polymer.core.mixin.item.ItemStackMixin",
            name = "lambda$patchCodec$1"
    )
    @WrapOperation(
            method = "@MixinSquared:Handler",
            at = @At(
                    value = "INVOKE",
                    target = "Leu/pb4/polymer/core/api/item/PolymerItemUtils;getPolymerItemStack(Lnet/minecraft/item/ItemStack;Lxyz/nucleoid/packettweaker/PacketContext;)Lnet/minecraft/item/ItemStack;"
            )
    )
    private static ItemStack reduceLogLevel2(ItemStack itemStack, PacketContext context, Operation<ItemStack> original) {
        return original.call(Utils.filterItemStack(itemStack), context);
    }

    @TargetHandler(
            mixin = "eu.pb4.polymer.core.mixin.item.ItemStackMixin",
            name = "lambda$patchCodec2$4"
    )
    @WrapOperation(
            method = "@MixinSquared:Handler",
            at = @At(
                    value = "INVOKE",
                    target = "Leu/pb4/polymer/core/api/item/PolymerItemUtils;getPolymerItemStack(Lnet/minecraft/item/ItemStack;Lxyz/nucleoid/packettweaker/PacketContext;)Lnet/minecraft/item/ItemStack;"
            )
    )
    private static ItemStack reduceLogLevel1(ItemStack itemStack, PacketContext context, Operation<ItemStack> original) {
        return original.call(Utils.filterItemStack(itemStack), context);
    }
}
