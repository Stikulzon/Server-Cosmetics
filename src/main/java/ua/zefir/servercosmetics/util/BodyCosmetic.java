package ua.zefir.servercosmetics.util;

import static ua.zefir.servercosmetics.database.DatabaseManager.setCosmetic;
import static ua.zefir.servercosmetics.util.Utils.getTiltedItemStack;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.MathHelper;
import ua.zefir.servercosmetics.data.BodyCosmeticsData;
import ua.zefir.servercosmetics.data.ItemType;
import ua.zefir.servercosmetics.database.DatabaseManager;
import ua.zefir.servercosmetics.ext.ICosmetic;

public class BodyCosmetic implements ICosmetic {
  private final ServerPlayerEntity player;
  @Getter private final ItemType itemType;
  @Getter private final Entity bodyCosmeticsModel;
  @Getter private ItemStack cosmeticItemStack = ItemStack.EMPTY;
  private ItemStack cosmeticItemStackWhenSneaking = ItemStack.EMPTY;
  private BodyCosmeticsData cosmeticData;
  private boolean isHidden = false;
  private boolean isTilted = false;

  public BodyCosmetic(ServerPlayerEntity player, ItemType itemType) {
    this.bodyCosmeticsModel = new ArmorStandEntity(EntityType.ARMOR_STAND, player.getWorld());
    this.player = player;
    this.itemType = itemType;
  }

  @Override
  public void equip(ItemStack is, ItemType _type) {
    setCosmetic(player, itemType, is);
    if (is.isEmpty()) {
      this.unequip();
      return;
    }
    init();
  }

  @Override
  public void init() {
    cosmeticItemStack = DatabaseManager.getCosmeticItemStack(player, itemType);

    if (cosmeticItemStack.isEmpty()) {
      unequip();
      return;
    }

    if (DatabaseManager.getCosmeticEntry(player, itemType) != null) {
      cosmeticData =
          (BodyCosmeticsData)
              Objects.requireNonNull(DatabaseManager.getCosmeticEntry(player, itemType))
                  .cosmeticData();
      cosmeticItemStackWhenSneaking =
          getTiltedItemStack(cosmeticItemStack, cosmeticData.modelWhenSneaking());
    } else {
      cosmeticItemStackWhenSneaking = cosmeticItemStack;
    }

    // Configure the ArmorStand
    bodyCosmeticsModel.setPosition(player.getX(), player.getY(), player.getZ());
    bodyCosmeticsModel.setInvulnerable(true);
    bodyCosmeticsModel.setNoGravity(true);
    bodyCosmeticsModel.setInvisible(true);
    ((ArmorStandEntity) bodyCosmeticsModel).setHeadRotation(new EulerAngle(0.0F, 0f, 0f));

    // Send packets to spawn the new entity for all nearby players
    player
        .getWorld()
        .getChunkManager()
        .sendToNearbyPlayers(
            player,
            new EntitySpawnS2CPacket(bodyCosmeticsModel, 1, bodyCosmeticsModel.getBlockPos()));
    setItem(cosmeticItemStack);
    player
        .getWorld()
        .getChunkManager()
        .sendToNearbyPlayers(
            player,
            new EntityTrackerUpdateS2CPacket(
                bodyCosmeticsModel.getId(),
                bodyCosmeticsModel.getDataTracker().getChangedEntries()));

    // Set the cosmetic to ride the player
    bodyCosmeticsModel.startRiding(player, true);
    player
        .getWorld()
        .getChunkManager()
        .sendToNearbyPlayers(player, new EntityPassengersSetS2CPacket(player));
  }

  @Override
  public void onUnload() {
    if (!cosmeticItemStack.isEmpty()) {
      player
          .getWorld()
          .getChunkManager()
          .sendToNearbyPlayers(player, new EntitiesDestroyS2CPacket(bodyCosmeticsModel.getId()));
    }
  }

  @Override
  public void tick() {
    if (cosmeticItemStack.isEmpty()) {
      return;
    }

    (bodyCosmeticsModel).setYaw(player.bodyYaw);
    player
        .getWorld()
        .getChunkManager()
        .sendToNearbyPlayers(
            player,
            new EntitySetHeadYawS2CPacket(
                bodyCosmeticsModel,
                (byte) MathHelper.floor(bodyCosmeticsModel.getYaw() * 256.0F / 360.0F)));

    if (cosmeticData != null && cosmeticData.offsetWhenSneaking()) {
      if (player.isSneaking() && !isTilted) {
        setItem(cosmeticItemStackWhenSneaking);
        isTilted = true;
      } else if (!player.isSneaking() && isTilted) {
        setItem(cosmeticItemStack);
        isTilted = false;
      }
    }

    boolean shouldBeHidden =
        player.isSwimming()
            || player.isCrawling()
            || player.isSpectator()
            || player.isInvisible()
            || player.isSleeping();

    if (shouldBeHidden && !isHidden) {
      setItem(ItemStack.EMPTY);
      isHidden = true;
    } else if (!shouldBeHidden && isHidden) {
      setItem(isTilted ? cosmeticItemStackWhenSneaking : cosmeticItemStack);
      isHidden = false;
    }
  }

  private void setItem(ItemStack itemStack) {
    List<Pair<EquipmentSlot, ItemStack>> equipmentList =
        ImmutableList.of(new Pair<>(EquipmentSlot.HEAD, itemStack));
    player
        .getWorld()
        .getChunkManager()
        .sendToNearbyPlayers(
            player, new EntityEquipmentUpdateS2CPacket(bodyCosmeticsModel.getId(), equipmentList));
  }

  public void unequip() {
    if (!this.cosmeticItemStack.isEmpty()) {
      bodyCosmeticsModel.stopRiding();
      player
          .getWorld()
          .getChunkManager()
          .sendToNearbyPlayers(player, new EntitiesDestroyS2CPacket(bodyCosmeticsModel.getId()));
      player
          .getWorld()
          .getChunkManager()
          .sendToNearbyPlayers(player, new EntityPassengersSetS2CPacket(player));
    }
    this.cosmeticItemStack = ItemStack.EMPTY;
    this.cosmeticItemStackWhenSneaking = ItemStack.EMPTY;
    this.isHidden = false;
    this.isTilted = false;
  }
}
