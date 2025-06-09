package com.zefir.servercosmetics.mixin;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityPassengersSetS2CPacket.class)
public interface EntityPassengersSetS2CPacketAccessor {
    @Invoker("<init>")
    static EntityPassengersSetS2CPacket invokeInit(PacketByteBuf buf) {
        throw new AssertionError();
    }
}
