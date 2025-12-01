package ua.zefir.servercosmetics.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ua.zefir.servercosmetics.ext.ICosmetics;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @Inject(method = "remove", at = @At("HEAD"))
    private void onRemove(Entity.RemovalReason reason, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (player instanceof ServerPlayerEntity serverPlayerEntity) {
            ((ICosmetics) serverPlayerEntity).removeCosmetics();
        }
    }
}
