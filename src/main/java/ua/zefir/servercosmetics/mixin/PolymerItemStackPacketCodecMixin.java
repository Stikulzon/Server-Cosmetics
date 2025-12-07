package ua.zefir.servercosmetics.mixin;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ua.zefir.servercosmetics.util.Utils;
import xyz.nucleoid.packettweaker.PacketContext;

@Mixin(targets = "net.minecraft.item.ItemStack$1", priority = 1500)
public class PolymerItemStackPacketCodecMixin {
  @TargetHandler(
      mixin = "eu.pb4.polymer.core.mixin.item.packet.ItemStackPacketCodecMixin",
      name = "polymer$replaceWithVanillaItem")
  @WrapOperation(
      method = "@MixinSquared:Handler",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Leu/pb4/polymer/core/api/item/PolymerItemUtils;getPolymerItemStack(Lnet/minecraft/item/ItemStack;Lxyz/nucleoid/packettweaker/PacketContext;)Lnet/minecraft/item/ItemStack;"))
  private static ItemStack wrapItemStack(
      ItemStack itemStack, PacketContext context, Operation<ItemStack> original) {
    return original.call(Utils.filterItemStack(itemStack), context);
  }
}
