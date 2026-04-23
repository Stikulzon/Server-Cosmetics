package ua.zefir.servercosmetics.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ua.zefir.servercosmetics.cosmetic.ArmorCosmetic;
import ua.zefir.servercosmetics.cosmetic.BodyCosmetic;
import ua.zefir.servercosmetics.cosmetic.Cosmetic;
import ua.zefir.servercosmetics.cosmetic.CosmeticHolder;
import ua.zefir.servercosmetics.data.ItemType;

@Mixin(EntityPassengersSetS2CPacket.class)
public class EntityPassengersSetS2CPacketMixin {
  @WrapOperation(
      method = "<init>(Lnet/minecraft/entity/Entity;)V",
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/entity/Entity;getPassengerList()Ljava/util/List;"))
  private List<Entity> modifyPassengers(Entity instance, Operation<List<Entity>> original) {
    List<Entity> modifiedList = new ArrayList<>(original.call(instance));

    if (instance instanceof ServerPlayerEntity player) {
      CosmeticHolder cosmetics = (CosmeticHolder) player;

      Cosmetic body = cosmetics.getCosmeticFor(ItemType.BODY_COSMETIC);
      if (body instanceof BodyCosmetic bodyCosmetic
          && !bodyCosmetic.getCosmeticItemStack().isEmpty()) {
        modifiedList.add(bodyCosmetic.getBodyCosmeticsModel());
      }

      List<ItemType> armorTypes =
          List.of(ItemType.HAT, ItemType.CHESTPLATE, ItemType.LEGGINGS, ItemType.BOOTS);

      for (ItemType type : armorTypes) {
        Cosmetic cosmetic = cosmetics.getCosmeticFor(type);
        if (cosmetic instanceof ArmorCosmetic armorCosmetic) {
          Entity model = armorCosmetic.getBodyCosmeticModel();
          if (model != null) {
            modifiedList.add(model);
          }
        }
      }
    }
    return modifiedList;
  }
}
