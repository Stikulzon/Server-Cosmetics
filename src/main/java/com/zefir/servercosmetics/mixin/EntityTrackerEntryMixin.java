package com.zefir.servercosmetics.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zefir.servercosmetics.ext.ICosmetics;
import com.zefir.servercosmetics.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.registry.Registries;
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

//    @WrapOperation(
//            method = "sendPackets",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V",
//                    ordinal = 1
//            )
//    )
//    private <T> void onSendPackets(Consumer<T> instance, T t, Operation<Void> original) {
//        if(entity instanceof ItemEntity itemEntity) {
//            List<DataTracker.SerializedEntry<?>> entries = new java.util.ArrayList<>(List.copyOf(changedEntries));
//            if(entries.removeIf(entry -> entry.id() == ItemEntityMixin.getStackConstant().id())){
//                entries.add(DataTracker.SerializedEntry.of(ItemEntityMixin.getStackConstant(), Utils.filterItemStack(itemEntity.getStack())));
//            }
//
//            original.call(instance, new EntityTrackerUpdateS2CPacket(this.entity.getId(), entries));
//        }
//    }
//
//    @WrapOperation(
//            method = "syncEntityData",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Lnet/minecraft/server/network/EntityTrackerEntry;sendSyncPacket(Lnet/minecraft/network/packet/Packet;)V",
//                    ordinal = 0
//            )
//    )
//    private <T> void onSendPackets(EntityTrackerEntry instance, Packet<?> packet, Operation<Void> original, @Local List<DataTracker.SerializedEntry<?>> list) {
//        if(entity instanceof ItemEntity itemEntity) {
//            List<DataTracker.SerializedEntry<?>> entries = new java.util.ArrayList<>(List.copyOf(list));
//            if(entries.removeIf(entry -> entry.id() == ItemEntityMixin.getStackConstant().id())){
//                entries.add(DataTracker.SerializedEntry.of(ItemEntityMixin.getStackConstant(), Utils.filterItemStack(itemEntity.getStack())));
//            }
//
////            System.out.println(entries + " " + Utils.filterItemStack(itemEntity.getStack()).getComponents());
//            original.call(instance, new EntityTrackerUpdateS2CPacket(this.entity.getId(), list));
//        }
//    }
//    @Inject(
//            method = "sendPackets",
//            at = @At(
//                    value = "INVOKE",
//                    target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V",
//                    ordinal = 0
//            )
//    )
//    private void onSendPackets1(ServerPlayerEntity player, Consumer<Packet<ClientPlayPacketListener>> sender, CallbackInfo ci) {
//        if(entity instanceof ItemEntity itemEntity) {
//            List<DataTracker.SerializedEntry<?>> entries = new java.util.ArrayList<>();
//            entries.add(DataTracker.SerializedEntry.of(ItemEntityMixin.getStackConstant(), Utils.filterItemStack(itemEntity.getStack())));
//
//            sender.accept(new EntityTrackerUpdateS2CPacket(this.entity.getId(), entries));
//        }
//    }
}
