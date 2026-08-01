package ua.zefir.servercosmetics.cosmetic;

import static ua.zefir.servercosmetics.database.DatabaseManager.setCosmetic;
import static ua.zefir.servercosmetics.util.Utils.getTiltedItemStack;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.Rotations;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import ua.zefir.servercosmetics.data.BodyCosmeticsData;
import ua.zefir.servercosmetics.data.ItemType;
import ua.zefir.servercosmetics.database.DatabaseManager;

public class BodyCosmetic implements Cosmetic {
  private final ServerPlayer player;
  private final ItemType itemType;
  private final Entity bodyCosmeticsModel;
  private ItemStack cosmeticItemStack = ItemStack.EMPTY;
  private ItemStack cosmeticItemStackWhenSneaking = ItemStack.EMPTY;
  private BodyCosmeticsData cosmeticData;
  private boolean isHidden = false;
  private boolean isTilted = false;

  public BodyCosmetic(ServerPlayer player, ItemType itemType) {
    this.bodyCosmeticsModel = new ArmorStand(EntityTypes.ARMOR_STAND, player.level());
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
    bodyCosmeticsModel.setPos(player.getX(), player.getY(), player.getZ());
    bodyCosmeticsModel.setInvulnerable(true);
    bodyCosmeticsModel.setNoGravity(true);
    bodyCosmeticsModel.setInvisible(true);
    ((ArmorStand) bodyCosmeticsModel).setHeadPose(new Rotations(0.0F, 0f, 0f));

    // Send packets to spawn the new entity for all nearby players
    player
        .level()
        .getChunkSource()
        .sendToTrackingPlayersAndSelf(
            player,
            new ClientboundAddEntityPacket(
                bodyCosmeticsModel, 1, bodyCosmeticsModel.blockPosition()));
    setItem(cosmeticItemStack);
    player
        .level()
        .getChunkSource()
        .sendToTrackingPlayersAndSelf(
            player,
            new ClientboundSetEntityDataPacket(
                bodyCosmeticsModel.getId(), bodyCosmeticsModel.getEntityData().packDirty()));

    // Set the cosmetic to ride the player
    bodyCosmeticsModel.startRiding(player, true, false);
    player
        .level()
        .getChunkSource()
        .sendToTrackingPlayersAndSelf(player, new ClientboundSetPassengersPacket(player));
  }

  @Override
  public void onUnload() {
    if (!cosmeticItemStack.isEmpty()) {
      player
          .level()
          .getChunkSource()
          .sendToTrackingPlayersAndSelf(
              player, new ClientboundRemoveEntitiesPacket(bodyCosmeticsModel.getId()));
    }
  }

  @Override
  public void tick() {
    if (cosmeticItemStack.isEmpty()) {
      return;
    }
    tickYaw();
    tickIsHidden();
    tickSneaking();
  }

  private void tickYaw() {
    (bodyCosmeticsModel).setYRot(player.yBodyRot);
    player
        .level()
        .getChunkSource()
        .sendToTrackingPlayersAndSelf(
            player,
            new ClientboundRotateHeadPacket(
                bodyCosmeticsModel,
                (byte) Mth.floor(bodyCosmeticsModel.getYRot() * 256.0F / 360.0F)));
  }

  private void tickIsHidden() {
    boolean shouldBeHidden =
        player.isSwimming()
            || player.isVisuallyCrawling()
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

  private void tickSneaking() {
    if (cosmeticData != null && cosmeticData.offsetWhenSneaking() && !isHidden) {
      if (player.isShiftKeyDown() && !isTilted) {
        setItem(cosmeticItemStackWhenSneaking);
        isTilted = true;
      } else if (!player.isShiftKeyDown() && isTilted) {
        setItem(cosmeticItemStack);
        isTilted = false;
      }
    }
  }

  private void setItem(ItemStack itemStack) {
    List<Pair<EquipmentSlot, ItemStack>> equipmentList =
        ImmutableList.of(new Pair<>(EquipmentSlot.HEAD, itemStack));
    player
        .level()
        .getChunkSource()
        .sendToTrackingPlayersAndSelf(
            player, new ClientboundSetEquipmentPacket(bodyCosmeticsModel.getId(), equipmentList));
  }

  public void unequip() {
    if (!this.cosmeticItemStack.isEmpty()) {
      bodyCosmeticsModel.stopRiding();
      player
          .level()
          .getChunkSource()
          .sendToTrackingPlayersAndSelf(
              player, new ClientboundRemoveEntitiesPacket(bodyCosmeticsModel.getId()));
      player
          .level()
          .getChunkSource()
          .sendToTrackingPlayersAndSelf(player, new ClientboundSetPassengersPacket(player));
    }
    this.cosmeticItemStack = ItemStack.EMPTY;
    this.cosmeticItemStackWhenSneaking = ItemStack.EMPTY;
    this.isHidden = false;
    this.isTilted = false;
  }

  @Override
  public ItemType getItemType() {
    return itemType;
  }

  public Entity getBodyCosmeticsModel() {
    return bodyCosmeticsModel;
  }

  @Override
  public ItemStack getCosmeticItemStack() {
    return cosmeticItemStack;
  }
}
