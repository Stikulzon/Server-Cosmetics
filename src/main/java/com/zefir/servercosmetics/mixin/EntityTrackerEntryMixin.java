package com.zefir.servercosmetics.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zefir.servercosmetics.ext.ICosmetics;
import com.zefir.servercosmetics.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Consumer;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {
    @Shadow
    @Final
    private Entity entity;
    @Shadow
    private List<DataTracker.SerializedEntry<?>> changedEntries;

    @Inject(method = "sendPackets", at = @At("TAIL"))
    private void onSendPackets(ServerPlayerEntity observer, Consumer<Packet<ClientPlayPacketListener>> sender, CallbackInfo ci) {
        // That is ugly but works fairly well
        if (this.entity instanceof ServerPlayerEntity cosmeticOwner) {
            ((ICosmetics) cosmeticOwner).initCosmetics();
        }
    }

    @WrapOperation(
            method = "sendPackets",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V",
                    ordinal = 1
            )
    )
    private <T> void onSendPackets(Consumer instance, T t, Operation<Void> original) {
        if(entity instanceof ItemEntity itemEntity) {
            DataTracker dataTracker = entity.getDataTracker();
            dataTracker.set(ItemEntityMixin.getStackConstant(), Utils.filterItemStack(itemEntity.getStack()));
            List<DataTracker.SerializedEntry<?>> entries = dataTracker.getChangedEntries();


            original.call(instance, new EntityTrackerUpdateS2CPacket(this.entity.getId(), entries));
        }
    }
}
