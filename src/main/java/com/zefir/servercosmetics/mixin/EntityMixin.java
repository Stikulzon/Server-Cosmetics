package com.zefir.servercosmetics.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "updatePassengerPosition(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/Entity$PositionUpdater;)V", at = @At("TAIL"), cancellable = true)
    private void modifyPassengerRotation(Entity passenger, Entity.PositionUpdater positionUpdater, CallbackInfo ci) {
        if (passenger instanceof PlayerEntity) {
            passenger.setYaw(passenger.getYaw() % 360.0f);
            passenger.setPitch(passenger.getPitch() % 360.0f);
            ci.cancel();
        }
    }
}
