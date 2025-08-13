package com.zefir.servercosmetics.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zefir.servercosmetics.ext.ICosmetics;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;

@Mixin(EntityPassengersSetS2CPacket.class)
public class EntityPassengersSetS2CPacketMixin {
    @WrapOperation(
            method = "<init>(Lnet/minecraft/entity/Entity;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getPassengerList()Ljava/util/List;")
    )
    private List<Entity> modifyPassengers(Entity instance, Operation<List<Entity>> original) {
        List<Entity> modifiedList = new ArrayList<>(original.call(instance));
        if(instance instanceof ServerPlayerEntity player) {
            if( ((ICosmetics) player).getBodyCosmetics().getCosmeticItemStack() != ItemStack.EMPTY) {
                modifiedList.add(((ICosmetics) player).getBodyCosmetics().getBodyCosmeticsModel());
            }
            if( ((ICosmetics) player).getChestCosmetic().getCosmeticItemStack() != ItemStack.EMPTY) {
                modifiedList.add(((ICosmetics) player).getChestCosmetic().getBodyCosmeticsModel());
            }
            if( ((ICosmetics) player).getLeggingsCosmetic().getCosmeticItemStack() != ItemStack.EMPTY) {
            modifiedList.add(((ICosmetics) player).getLeggingsCosmetic().getBodyCosmeticsModel());
            }
            if( ((ICosmetics) player).getBootsCosmetic().getCosmeticItemStack() != ItemStack.EMPTY) {
                modifiedList.add(((ICosmetics) player).getBootsCosmetic().getBodyCosmeticsModel());
            }
        }
        return modifiedList;
    }
}
