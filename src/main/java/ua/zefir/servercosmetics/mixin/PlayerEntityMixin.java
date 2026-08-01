package ua.zefir.servercosmetics.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.zefir.servercosmetics.cosmetic.CosmeticHolder;

@Mixin(Player.class)
public class PlayerEntityMixin {
  @Inject(method = "remove", at = @At("HEAD"))
  private void onRemove(Entity.RemovalReason reason, CallbackInfo ci) {
    Player player = (Player) (Object) this;
    if (player instanceof ServerPlayer serverPlayerEntity) {
      ((CosmeticHolder) serverPlayerEntity).removeCosmetics();
    }
  }
}
