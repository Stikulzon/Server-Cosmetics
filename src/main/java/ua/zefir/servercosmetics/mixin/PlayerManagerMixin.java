package ua.zefir.servercosmetics.mixin;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.zefir.servercosmetics.cosmetic.CosmeticHolder;

@Mixin(PlayerList.class)
public class PlayerManagerMixin {
  @Inject(method = "placeNewPlayer", at = @At(value = "TAIL"))
  void onPlayerConnect(
      Connection connection,
      ServerPlayer player,
      CommonListenerCookie clientData,
      CallbackInfo ci) {
    ((CosmeticHolder) player).initCosmetics();
  }

  @Inject(method = "remove", at = @At(value = "HEAD"))
  void remove(ServerPlayer player, CallbackInfo ci) {
    ((CosmeticHolder) player).removeCosmetics();
  }
}
