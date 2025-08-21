package com.zefir.servercosmetics.util;

import com.google.common.collect.ImmutableList;
import com.zefir.servercosmetics.data.ItemType;
import com.zefir.servercosmetics.data.BodyCosmeticsData;
import com.zefir.servercosmetics.database.DatabaseManager;
import com.zefir.servercosmetics.ext.ICosmetic;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.network.ServerPlayerEntity;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Objects;

import static com.zefir.servercosmetics.database.DatabaseManager.setCosmetic;
import static com.zefir.servercosmetics.util.Utils.getTiltedItemStack;

// TODO: Refactor
public class BodyCosmetic implements ICosmetic {
    final ServerPlayerEntity player;
    final ItemType itemType;
    @Getter
    private final Entity bodyCosmeticsModel;
    @Getter
    ItemStack cosmeticItemStack = ItemStack.EMPTY;
    ItemStack cosmeticItemStackWhenSneaking = ItemStack.EMPTY;
    private final boolean useArmorStand = true;
    private boolean isHidden = false;
    private boolean isTilted = false;

    public BodyCosmetic(ServerPlayerEntity player, ItemType itemType){
        if(useArmorStand){
            this.bodyCosmeticsModel = new ArmorStandEntity(EntityType.ARMOR_STAND, player.getServerWorld());
        } else {
            this.bodyCosmeticsModel = new DisplayEntity.ItemDisplayEntity(EntityType.ITEM_DISPLAY, player.getServerWorld());
        }
        this.player = player;
        this.itemType = itemType;
    }

    public void equip(ItemStack is) {
        setCosmetic(player, itemType, is);
        this.cosmeticItemStack = is;
        init();
    }

    public void init() {
        cosmeticItemStack = DatabaseManager.getCosmeticItemStack(player, itemType);
        if(DatabaseManager.getCosmeticEntry(player, itemType) != null) {
            cosmeticItemStackWhenSneaking = getTiltedItemStack(cosmeticItemStack, ((BodyCosmeticsData) Objects.requireNonNull(DatabaseManager.getCosmeticEntry(player, itemType)).cosmeticData()).polymerModelWhenSneaking());
        } else {
            cosmeticItemStackWhenSneaking = cosmeticItemStack;
        }

        bodyCosmeticsModel.setPosition(player.getX(), player.getY(), player.getZ());
        bodyCosmeticsModel.setInvulnerable(true);
        bodyCosmeticsModel.setNoGravity(true);

        player.getServerWorld().getChunkManager().sendToNearbyPlayers(player,
                new EntitySpawnS2CPacket(bodyCosmeticsModel, 1, bodyCosmeticsModel.getBlockPos()));

        if(useArmorStand) {
            bodyCosmeticsModel.setInvisible(true);
            ((ArmorStandEntity) bodyCosmeticsModel).setHeadRotation(new EulerAngle(0.0F, 0f, 0f));
        } else {
            ((DisplayEntity.ItemDisplayEntity) bodyCosmeticsModel).setBillboardMode(DisplayEntity.BillboardMode.FIXED);
        }
        setItem(cosmeticItemStack);

        player.getServerWorld().getChunkManager().sendToNearbyPlayers(player,
                new EntityTrackerUpdateS2CPacket(bodyCosmeticsModel.getId(),
                        bodyCosmeticsModel.getDataTracker().getChangedEntries()));

        player.getServerWorld().getChunkManager().sendToNearbyPlayers(player,
                new EntityPassengersSetS2CPacket(player));
        bodyCosmeticsModel.startRiding(player);
    }

    public void tick() {
        (bodyCosmeticsModel).setYaw(player.bodyYaw);
        player.getServerWorld().getChunkManager().sendToNearbyPlayers(player,
                new EntitySetHeadYawS2CPacket(
                        bodyCosmeticsModel,
                        (byte) MathHelper.floor(bodyCosmeticsModel.getYaw() * 256.0F / 360.0F)
                )
        );

        if (player.isSneaking() && !isTilted) {
            setItem(cosmeticItemStackWhenSneaking);

            player.getServerWorld().getChunkManager().sendToNearbyPlayers(player,
                    new EntityTrackerUpdateS2CPacket(bodyCosmeticsModel.getId(),
                            bodyCosmeticsModel.getDataTracker().getChangedEntries()));
            isTilted = true;

        } else if (!player.isSneaking() && isTilted) {
            setItem(cosmeticItemStack);

            player.getServerWorld().getChunkManager().sendToNearbyPlayers(player,
                    new EntityTrackerUpdateS2CPacket(bodyCosmeticsModel.getId(),
                            bodyCosmeticsModel.getDataTracker().getChangedEntries()));
            isTilted = false;
        }

        if(player.isSwimming() || player.isCrawling() && !isHidden) {
            setItem(ItemStack.EMPTY);
            isHidden = true;
        } else if(!player.isSwimming() && !player.isCrawling() && isHidden) {
            setItem(cosmeticItemStack);
            isHidden = false;
        }
    }

    private void setItem(ItemStack itemStack) {
        if(useArmorStand) {
            List<Pair<EquipmentSlot, ItemStack>> equipmentList = ImmutableList.of(
                    new Pair<>(EquipmentSlot.HEAD, itemStack)
            );
            player.getServerWorld().getChunkManager().sendToNearbyPlayers(player,
                    new EntityEquipmentUpdateS2CPacket(bodyCosmeticsModel.getId(), equipmentList));
        } else {
            ((DisplayEntity.ItemDisplayEntity) bodyCosmeticsModel).setItemStack(itemStack);
        }
    }

}