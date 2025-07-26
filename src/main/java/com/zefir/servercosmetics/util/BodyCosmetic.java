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
        // We still position the cosmetic at the player's location.
        // The Y-offset might need adjustment based on the pose (e.g., sneaking).
        double yOffset = player.isSneaking() ? 1.55 : 1.8;
        bodyCosmetics.setPosition(player.getX(), player.getY() + yOffset, player.getZ());

        // --- YAW PREDICTION ---
        // This is the core logic for replicating the client's rendered body yaw.

        float yawToUse;
        // Get horizontal velocity squared. Using squared values avoids a square root calculation.
        double velX = player.getVelocity().getX();
        double velZ = player.getVelocity().getZ();
        double horizontalVelocitySq = velX * velX + velZ * velZ;

        // Check if the player is moving horizontally.
        // The threshold (1.0E-6) is small to detect any meaningful movement but ignore tiny jitters.
        if (horizontalVelocitySq > 1.0E-6) {
            // If moving, the client renders the body facing the same direction as the head.
            // So, we use the player's head yaw.
            yawToUse = player.getYaw();
        } else {
            // If not moving, the client uses the standard body yaw logic (lagging behind the head).
            // The server's getBodyYaw() is perfect for this.
            yawToUse = player.getBodyYaw();
        }


        // --- PITCH PREDICTION ---
        // We need to handle several player states for accurate vertical rotation.

        float pitchToUse;
        if (player.isSwimming() || player.isCrawling()) {
            // When swimming or crawling, the player's body is horizontal.
            pitchToUse = 90.0F;
        } else if (player.isSneaking()) {
            // Your original logic for sneaking pitch is good.
            pitchToUse = 28.0F;
        } else {
            // When standing/walking, the body doesn't pitch with the head.
            // A pitch of 0.0F keeps the cosmetic upright.
            // If you want a subtle tilt when the player looks up/down, you can use:
            // pitchToUse = player.getPitch() * 0.4f; // Sclae it down to avoid extreme tilting
            pitchToUse = 0.0F;
        }


        // --- SENDING THE PACKET ---
        // Now we send the update packet with our predicted yaw and pitch.
        player.getServerWorld().getChunkManager().sendToNearbyPlayers(player,
                new EntityS2CPacket.Rotate(
                        bodyCosmetics.getId(),
                        (byte) MathHelper.floor(yawToUse * 256.0F / 360.0F),
                        (byte) MathHelper.floor(pitchToUse * 256.0F / 360.0F),
                        false // onGround status doesn't matter much for a DisplayEntity
                )
        );
    }

    private void sendPassengersPacket(ServerPlayerEntity player, DisplayEntity bodyCosmetics){
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeVarInt(player.getId()); // Entity ID
        buf.writeIntArray(new int[]{bodyCosmetics.getId()}); // Passenger IDs

        player.getServerWorld().getChunkManager().sendToNearbyPlayers(player,
                EntityPassengersSetS2CPacketAccessor.invokeInit(buf));
    }
}