package com.zefir.servercosmetics.mixin;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.zefir.servercosmetics.ext.ArmorStandAcess;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityBackPackMixin {

//    @Shadow public abstract ServerWorld getServerWorld();
//
//    @Shadow public abstract void playerTick();
//    @Unique
//    private ArmorStandEntity playersArmorStand;
//
//    @Inject(method = "playerTick", at = @At("TAIL"))
//    private void sendBackpackCosmeticPacket(CallbackInfo ci) {
//        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
//        String cosmetic = "111"; // dummy
//
//        if (playersArmorStand == null) {
//            ArmorStandEntity armorStand = new ArmorStandEntity(this.getServerWorld(),
//                    player.getX(), player.getY() - 0.5, player.getZ());
//
//            armorStand.setYaw(player.getYaw());
//            armorStand.setPitch(player.getPitch());
//            armorStand.setHeadYaw(player.getHeadYaw());
//
//            armorStand.setInvulnerable(true);
//            armorStand.setNoGravity(true);
//            armorStand.setInvisible(true);
//
//            ((ArmorStandAcess) armorStand).accessedSetMarker(true);
//
////            this.getServerWorld().getChunkManager().sendToNearbyPlayers(player,
////                    new EntitySpawnS2CPacket(armorStand));
//
//            this.getServerWorld().getChunkManager().sendToNearbyPlayers(player,
//                    new EntityTrackerUpdateS2CPacket(armorStand.getId(),
//                            armorStand.getDataTracker().getChangedEntries()));
//
//            sendPassengersPacket(player, armorStand);
//            playersArmorStand = armorStand;
//        }
//
//        if (cosmetic != null) {
//            ArmorStandEntity armorStand = playersArmorStand;
//
//            this.getServerWorld().getChunkManager().sendToNearbyPlayers(player,
//                    new EntityPositionS2CPacket(armorStand));
//
//                    ItemStack cosmeticItem = new ItemStack(Items.STICK);
//
//                    List<Pair<EquipmentSlot, ItemStack>> equipmentList = ImmutableList.of(
//                            new Pair<>(EquipmentSlot.HEAD, cosmeticItem)
//                    );
//                    this.getServerWorld().getChunkManager().sendToNearbyPlayers(player,
//                            new EntityEquipmentUpdateS2CPacket(armorStand.getId(), equipmentList));
//
//            this.getServerWorld().getChunkManager().sendToNearbyPlayers(player,
//                    new EntityTrackerUpdateS2CPacket(armorStand.getId(),
//                            armorStand.getDataTracker().getChangedEntries()));
//            playersArmorStand = armorStand;
//        }
//    }
//    @Unique
//    private void sendPassengersPacket(ServerPlayerEntity player, ArmorStandEntity armorStand){
//        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
//        buf.writeVarInt(player.getId()); // Entity ID
//        buf.writeIntArray(new int[]{armorStand.getId()}); // Passenger IDs
//
//        this.getServerWorld().getChunkManager().sendToNearbyPlayers(player,
//                new EntityPassengersSetS2CPacket(buf));
//    }
}
