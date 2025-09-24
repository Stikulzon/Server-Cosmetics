package com.zefir.servercosmetics.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Entity.class)
public class EntityMixin {

//    @Inject(method = "shouldRender(DDD)Z", at = @At("TAIL"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
//    private void evd_shouldRender(double cameraX, double cameraY, double cameraZ, CallbackInfoReturnable<Boolean> cir, double deltaX, double deltaY, double deltaZ) {
////        var config = ConfigManager.getConfig();
////        if (config.mode.client) {
////            var value = ((EvdEntityType) this.getType()).evd_getTrackingDistance();
//
////            if (value != -1) {
//                cir.setReturnValue(Math.abs(deltaX) < 2 && Math.abs(deltaY) < 2 && Math.abs(deltaZ) < 2);
////            }
////        }
//    }
}
