package com.zefir.servercosmetics.mixin;

import com.zefir.servercosmetics.ext.ICosmetics;
import com.zefir.servercosmetics.util.BodyCosmetic;
import com.zefir.servercosmetics.util.HatCosmetic;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityBackPackTestMixin implements ICosmetics {
    @Unique
    private BodyCosmetic bodyCosmetic;
    @Unique
    private HatCosmetic hatCosmetic;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        bodyCosmetic = new BodyCosmetic(player);
        hatCosmetic = new HatCosmetic(player);
    }

    @Inject(method = "playerTick", at = @At("TAIL"))
    private void sendBackpackCosmeticPacket(CallbackInfo ci) {
        bodyCosmetic.tick();
    }

    @Unique
    public BodyCosmetic getBodyCosmetics() {
        return bodyCosmetic;
    }

    @Unique
    public HatCosmetic getHatCosmetic() {
        return hatCosmetic;
    }

}
