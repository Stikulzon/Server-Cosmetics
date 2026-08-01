package ua.zefir.servercosmetics.mixin;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ua.zefir.servercosmetics.util.Utils;

@Mixin(value = ItemStack.class, priority = 1500)
public class PolymerItemStackMixinMixin {
  @TargetHandler(
      mixin = "eu.pb4.polymer.core.mixin.item.ItemStackMixin",
      name = "lambda$patchCodec$2")
  @WrapOperation(
      method = {"@MixinSquared:Handler"},
      at =
          @At(
              value = "INVOKE",
              target =
                  "Leu/pb4/polymer/core/api/item/PolymerItemUtils;getPolymerItemStack(Lnet/minecraft/world/item/ItemStack;Lnet/fabricmc/fabric/api/networking/v1/context/PacketContext;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;"))
  private static ItemStack filterSerializedStack(
      ItemStack itemStack,
      PacketContext context,
      HolderLookup.Provider lookup,
      Operation<ItemStack> original) {
    return original.call(
        Utils.filterItemStack(itemStack, PolymerCommonUtils.getPlayer(context)), context, lookup);
  }
}
