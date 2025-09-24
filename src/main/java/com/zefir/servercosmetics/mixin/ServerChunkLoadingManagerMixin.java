package com.zefir.servercosmetics.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerChunkLoadingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerChunkLoadingManager.class)
public class ServerChunkLoadingManagerMixin {
//    @ModifyVariable(method = "loadEntity", at = @At("STORE"), ordinal = 0)
//    private int evd_replaceDistance(int distance, Entity entity) {
//        return distance == 0 ? distance : 2;
//    }
}
