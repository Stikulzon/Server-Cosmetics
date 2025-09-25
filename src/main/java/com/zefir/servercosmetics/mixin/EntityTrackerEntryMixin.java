package com.zefir.servercosmetics.mixin;

import com.zefir.servercosmetics.ext.ICosmetics;
import net.minecraft.entity.Entity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {
    @Shadow
    @Final
    private Entity entity;

    @Inject(method = "sendPackets", at = @At("TAIL"))
    private void onSendPackets(ServerPlayerEntity observer, Consumer<Packet<ClientPlayPacketListener>> sender, CallbackInfo ci) {
        // That is ugly but works fairly well
        if (this.entity instanceof ServerPlayerEntity cosmeticOwner) {
//            ((LivingEntityAccessor) cosmeticOwner).invokeSendEquipmentChanges();
            ((ICosmetics) cosmeticOwner).initCosmetics();
        }
    }
}
