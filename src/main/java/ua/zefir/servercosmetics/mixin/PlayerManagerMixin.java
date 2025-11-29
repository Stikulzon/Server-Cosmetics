package ua.zefir.servercosmetics.mixin;

import ua.zefir.servercosmetics.ext.ICosmetics;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(
            method = "onPlayerConnect",
            at = @At( value = "TAIL" )
    )
    void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        ((ICosmetics) player).initCosmetics();
    }
    @Inject(
            method = "remove",
            at = @At( value = "HEAD" )
    )
    void remove(ServerPlayerEntity player, CallbackInfo ci) {
        ((ICosmetics) player).removeCosmetics();
    }
}
