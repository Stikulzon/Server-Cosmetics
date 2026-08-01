package ua.zefir.servercosmetics.mixin;

import static ua.zefir.servercosmetics.util.Utils.getItemTypeForSlot;

import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.zefir.servercosmetics.cosmetic.CosmeticHolder;
import ua.zefir.servercosmetics.data.ItemType;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerPlayNetworkHandlerMixin {
  @Shadow public ServerPlayer player;

  @Inject(method = "tryPickItem", at = @At(value = "TAIL"))
  void modifyItemStack(ItemStack stack, CallbackInfo ci) {
    CosmeticHolder cosmetics = (CosmeticHolder) player;
    cosmetics.tickArmor();
  }

  @Inject(method = "handleContainerClick", at = @At(value = "TAIL"))
  void modifyItemStack(ServerboundContainerClickPacket packet, CallbackInfo ci) {
    AbstractContainerMenu handler = this.player.containerMenu;
    if (handler instanceof InventoryMenu) {
      CosmeticHolder cosmetics = (CosmeticHolder) player;
      ItemType itemType = getItemTypeForSlot(packet.slotNum());
      if (itemType != null) {
        cosmetics.getCosmeticFor(itemType).tick();
      }
    }
  }
}
