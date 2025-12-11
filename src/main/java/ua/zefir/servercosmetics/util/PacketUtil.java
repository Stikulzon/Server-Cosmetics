package ua.zefir.servercosmetics.util;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;

public class PacketUtil {
  public static void sendInventorySlotPacket(
      ServerPlayerEntity player, int slot, ItemStack targetItemStack) {
    player.networkHandler.sendPacket(
        new ScreenHandlerSlotUpdateS2CPacket(
            player.playerScreenHandler.syncId,
            player.playerScreenHandler.nextRevision(),
            slot,
            targetItemStack));
  }
}
