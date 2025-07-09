package com.zefir.servercosmetics.util;

import com.zefir.servercosmetics.config.entries.ItemType;
import com.zefir.servercosmetics.database.DatabaseManager;
import com.zefir.servercosmetics.mixin.EntityPassengersSetS2CPacketAccessor;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;

import static com.zefir.servercosmetics.database.DatabaseManager.setCosmetic;

public class BodyCosmetic {

    private final DisplayEntity.ItemDisplayEntity bodyCosmetics;
    private ItemStack cosmeticItemStack = ItemStack.EMPTY;
    private final ServerPlayerEntity player;

    public BodyCosmetic(ServerPlayerEntity player){
        this.bodyCosmetics = new DisplayEntity.ItemDisplayEntity(EntityType.ITEM_DISPLAY, player.getServerWorld());
        this.player = player;
    }

    public void equip(ItemStack is) {
        setCosmetic(player, ItemType.BODY_COSMETIC, is);
        this.cosmeticItemStack = is;
        initNewCosmetic();
    }

    public void initNewCosmetic() {
        this.cosmeticItemStack = DatabaseManager.getCosmetic(player, ItemType.BODY_COSMETIC);
        bodyCosmetics.setPosition(player.getX(), player.getY(), player.getZ());

        bodyCosmetics.setItemStack(cosmeticItemStack);
        bodyCosmetics.setInvulnerable(true);
        bodyCosmetics.setNoGravity(true);

        player.getServerWorld().getChunkManager().sendToNearbyPlayers(player,
                new EntitySpawnS2CPacket(bodyCosmetics, 1, bodyCosmetics.getBlockPos()));

        player.getServerWorld().getChunkManager().sendToNearbyPlayers(player,
                new EntityTrackerUpdateS2CPacket(bodyCosmetics.getId(),
                        bodyCosmetics.getDataTracker().getChangedEntries()));

        sendPassengersPacket(player, bodyCosmetics);
        bodyCosmetics.startRiding(player);
    }

    public void tick() {
        bodyCosmetics.setPosition(player.getX(), player.getY()+1.8, player.getZ());

        player.getServerWorld().getChunkManager().sendToNearbyPlayers(player,
                new EntityS2CPacket.Rotate(bodyCosmetics.getId(), (byte) MathHelper.floor(player.getBodyYaw() * 256.0F / 360.0F), (byte) MathHelper.floor(bodyCosmetics.getPitch() * 256.0F / 360.0F), false));
    }

    private void sendPassengersPacket(ServerPlayerEntity player, DisplayEntity bodyCosmetics){
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeVarInt(player.getId()); // Entity ID
        buf.writeIntArray(new int[]{bodyCosmetics.getId()}); // Passenger IDs

        player.getServerWorld().getChunkManager().sendToNearbyPlayers(player,
                EntityPassengersSetS2CPacketAccessor.invokeInit(buf));
    }

}
