package ua.zefir.servercosmetics.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ua.zefir.servercosmetics.cosmetic.ArmorCosmetic;
import ua.zefir.servercosmetics.cosmetic.BodyCosmetic;
import ua.zefir.servercosmetics.cosmetic.Cosmetic;
import ua.zefir.servercosmetics.cosmetic.CosmeticHolder;
import ua.zefir.servercosmetics.data.ItemType;

@Mixin(ClientboundSetPassengersPacket.class)
public class EntityPassengersSetS2CPacketMixin {
  @WrapOperation(
      method = "<init>(Lnet/minecraft/world/entity/Entity;)V",
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/world/entity/Entity;getPassengers()Ljava/util/List;"))
  private List<Entity> modifyPassengers(Entity instance, Operation<List<Entity>> original) {
    List<Entity> modifiedList = new ArrayList<>(original.call(instance));

    if (instance instanceof ServerPlayer player) {
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
