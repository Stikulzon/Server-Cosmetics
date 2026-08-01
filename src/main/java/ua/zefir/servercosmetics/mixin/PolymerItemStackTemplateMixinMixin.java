package ua.zefir.servercosmetics.mixin;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ua.zefir.servercosmetics.util.Utils;

@Mixin(value = ItemStackTemplate.class, priority = 1500)
public class PolymerItemStackTemplateMixinMixin {
  @TargetHandler(
      mixin = "eu.pb4.polymer.core.mixin.item.ItemStackTemplateMixin",
      name = "lambda$patchCodec$1")
  @WrapOperation(
      method = {"@MixinSquared:Handler"},
      at =
          @At(
              value = "INVOKE",
              target =
                  "Leu/pb4/polymer/core/api/item/PolymerItemUtils;getPolymerItemStack(Lnet/minecraft/world/item/ItemStack;Lnet/fabricmc/fabric/api/networking/v1/context/PacketContext;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;"))
  private static ItemStack filterSerializedMapStack(
      ItemStack itemStack,
      PacketContext context,
      HolderLookup.Provider lookup,
      Operation<ItemStack> original) {
    return original.call(
        Utils.filterItemStack(itemStack, PolymerCommonUtils.getPlayer(context)), context, lookup);
  }

  @TargetHandler(
      mixin = "eu.pb4.polymer.core.mixin.item.ItemStackTemplateMixin",
      name = "lambda$patchPacketCodec$0")
  @WrapOperation(
      method = {"@MixinSquared:Handler"},
      at =
          @At(
              value = "INVOKE",
              target =
                  "Leu/pb4/polymer/core/api/item/PolymerItemUtils;getPolymerItemStack(Lnet/minecraft/world/item/ItemStack;Lnet/fabricmc/fabric/api/networking/v1/context/PacketContext;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;"))
  private static ItemStack filterSerializedPacketStack(
      ItemStack itemStack,
      PacketContext context,
      HolderLookup.Provider lookup,
      Operation<ItemStack> original) {
    return original.call(
        Utils.filterItemStack(itemStack, PolymerCommonUtils.getPlayer(context)), context, lookup);
  }
}
