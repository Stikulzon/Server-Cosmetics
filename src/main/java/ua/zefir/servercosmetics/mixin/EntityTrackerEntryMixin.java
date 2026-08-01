package ua.zefir.servercosmetics.mixin;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ua.zefir.servercosmetics.cosmetic.ArmorCosmetic;
import ua.zefir.servercosmetics.cosmetic.Cosmetic;
import ua.zefir.servercosmetics.cosmetic.CosmeticHolder;
import ua.zefir.servercosmetics.data.ItemType;
import ua.zefir.servercosmetics.util.Utils;

@Mixin(ServerEntity.class)
public class EntityTrackerEntryMixin {
  @Shadow @Final private Entity entity;

  @ModifyVariable(
      method = "sendPairingData",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/network/protocol/game/ClientboundSetEquipmentPacket;<init>(ILjava/util/List;)V"),
      ordinal = 0)
  private List<Pair<EquipmentSlot, ItemStack>> modifyEquipmentOnSpawn(
      List<Pair<EquipmentSlot, ItemStack>> originalList) {
    if (!(this.entity instanceof ServerPlayer trackedPlayer)) {
      return originalList;
    }

    CosmeticHolder cosmetics = (CosmeticHolder) trackedPlayer;
    if (cosmetics.getCosmeticsList().isEmpty()) {
      return originalList;
    }

    List<Pair<EquipmentSlot, ItemStack>> modifiedList = new ArrayList<>();

    for (EquipmentSlot slot : EquipmentSlot.VALUES) {
      ItemStack realStack = trackedPlayer.getItemBySlot(slot);
      ItemStack stackToSend = realStack;

      if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
        try {
          ItemType itemType = ArmorCosmetic.getItemTypeForSlot(slot);
          Cosmetic cosmetic = cosmetics.getCosmeticFor(itemType);
          ItemStack cosmeticStack = cosmetic.getCosmeticItemStack();

          if (cosmeticStack != null && !cosmeticStack.isEmpty()) {
            stackToSend = Utils.reflectRealDurability(cosmeticStack.copy(), realStack);
          }
        } catch (Exception ignored) {
        }
      }

      if (stackToSend != null && !stackToSend.isEmpty()) {
        modifiedList.add(Pair.of(slot, stackToSend.copy()));
      }
    }
    return modifiedList;
  }
}
