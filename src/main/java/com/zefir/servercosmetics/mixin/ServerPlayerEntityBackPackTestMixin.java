package com.zefir.servercosmetics.mixin;

import com.zefir.servercosmetics.ext.IBodyCosmetics;
import com.zefir.servercosmetics.util.BodyCosmetics;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityBackPackTestMixin implements IBodyCosmetics {
    @Unique
    private BodyCosmetics bodyCosmetics;

    @Inject(method = "playerTick", at = @At("TAIL"))
    private void sendBackpackCosmeticPacket(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if(bodyCosmetics == null) {
            bodyCosmetics = new BodyCosmetics(player);
        }
        bodyCosmetics.tick();
    }

    @Unique
    public BodyCosmetics getBodyCosmetics() {
        return bodyCosmetics;
    }

}
