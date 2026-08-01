package ua.zefir.servercosmetics.mixin;

import static ua.zefir.servercosmetics.util.Utils.getItemTypeForSlot;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.zefir.servercosmetics.cosmetic.CosmeticHolder;
import ua.zefir.servercosmetics.data.ItemType;

@Mixin(targets = "net.minecraft.server.level.ServerPlayer$2")
public class ServerPlayerEntity$2Mixin {
  @Final @Shadow ServerPlayer this$0;

  @Inject(
      method = "slotChanged",
      at =
          @At(
              value = "TAIL",
              target =
                  "Lnet/minecraft/advancements/CriteriaTriggers;INVENTORY_CHANGED:Lnet/minecraft/advancements/critereon/InventoryChangeTrigger;"))
  void modifyArmorItemStack(
      AbstractContainerMenu handler, int slot, ItemStack _stack, CallbackInfo ci) {
    if (handler instanceof InventoryMenu) {
      CosmeticHolder cosmetics = (CosmeticHolder) this$0;
      ItemType itemType = getItemTypeForSlot(slot);
      if (itemType != null) {
        cosmetics.getCosmeticFor(itemType).tick();
      }
    }
  }
}
