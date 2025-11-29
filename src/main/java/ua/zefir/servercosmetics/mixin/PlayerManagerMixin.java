package ua.zefir.servercosmetics.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
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
            at = @At( value = "TAIL" )
    )
    void remove(ServerPlayerEntity player, CallbackInfo ci) {
        ((ICosmetics) player).removeCosmetics();
    }

    @Inject(
            method = "sendToDimension",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", shift = At.Shift.AFTER)
    )
    private void onDimensionChange(Packet<?> packet, RegistryKey<World> dimension, CallbackInfo ci, @Local ServerPlayerEntity serverPlayerEntity) {
        ((ICosmetics) serverPlayerEntity).initCosmetics();
    }
}
