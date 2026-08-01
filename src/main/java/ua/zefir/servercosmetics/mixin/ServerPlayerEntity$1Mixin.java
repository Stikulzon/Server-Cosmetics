package ua.zefir.servercosmetics.mixin;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.zefir.servercosmetics.cosmetic.CosmeticHolder;
import ua.zefir.servercosmetics.data.ItemType;
import ua.zefir.servercosmetics.util.Utils;

@Mixin(targets = "net.minecraft.server.level.ServerPlayer$1")
public class ServerPlayerEntity$1Mixin {
  @Final @Shadow ServerPlayer this$0;

  @ModifyVariable(method = "sendSlotChange", at = @At("HEAD"), argsOnly = true)
  private ItemStack modifyArmorItemStack(ItemStack stack, AbstractContainerMenu handler, int slot) {
    CosmeticHolder cosmetics = (CosmeticHolder) this$0;
    ItemType itemType = Utils.getItemTypeForSlot(slot);
    if (itemType != null) {
      cosmetics.getCosmeticFor(itemType).tick();
    }
    return stack;
  }

  @Inject(method = "sendInitialData", at = @At(value = "TAIL"))
  void modifyArmorItemStack(
      AbstractContainerMenu handler,
      List<ItemStack> stacks,
      ItemStack cursorStack,
      int[] properties,
      CallbackInfo ci) {
    if (handler instanceof InventoryMenu) {
      CosmeticHolder cosmetics = (CosmeticHolder) this$0;
      cosmetics.tickArmor();
    }
  }
}
