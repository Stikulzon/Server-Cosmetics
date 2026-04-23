package ua.zefir.servercosmetics.mixin;

import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
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

@Mixin(targets = "net.minecraft.server.network.ServerPlayerEntity$1")
public class ServerPlayerEntity$1Mixin {
  @Final @Shadow ServerPlayerEntity field_58075;

  @ModifyVariable(method = "updateSlot", at = @At("HEAD"), argsOnly = true)
  private ItemStack modifyArmorItemStack(ItemStack stack, ScreenHandler handler, int slot) {
    CosmeticHolder cosmetics = (CosmeticHolder) field_58075;
    ItemType itemType = Utils.getItemTypeForSlot(slot);
    if (itemType != null) {
      cosmetics.getCosmeticFor(itemType).tick();
    }
    return stack;
  }

  @Inject(method = "updateState", at = @At(value = "TAIL"))
  void modifyArmorItemStack(
      ScreenHandler handler,
      List<ItemStack> stacks,
      ItemStack cursorStack,
      int[] properties,
      CallbackInfo ci) {
    if (handler instanceof PlayerScreenHandler) {
      CosmeticHolder cosmetics = (CosmeticHolder) field_58075;
      cosmetics.tickArmor();
    }
  }
}
