package ua.zefir.servercosmetics.cosmetic;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import ua.zefir.servercosmetics.data.ItemType;
import ua.zefir.servercosmetics.database.DatabaseManager;
import ua.zefir.servercosmetics.util.Utils;

public class ArmorCosmetic implements Cosmetic {

  private final ServerPlayerEntity player;
  private final ItemType slotType;
  private ItemType itemType;
  private ItemStack cosmeticItemStack = ItemStack.EMPTY;
  private final BodyCosmetic bodyCosmeticDelegate;

  public ArmorCosmetic(ServerPlayerEntity player, ItemType itemType) {
    this.player = player;
    this.slotType = itemType;
    this.itemType = itemType;
    this.bodyCosmeticDelegate = new BodyCosmetic(player, getBodyCosmeticType(itemType));
  }

  @Override
  public void equip(ItemStack newCosmeticStack, ItemType newType) {
    if (bodyCosmeticDelegate.getCosmeticItemStack() != ItemStack.EMPTY) {
      this.bodyCosmeticDelegate.unequip();
    }

    this.itemType = Utils.getRealEquipedItemType(newType);
    this.cosmeticItemStack = newCosmeticStack.copy();

    if (itemType == ItemType.HAT_BODY_COSMETIC
        || itemType == ItemType.CHESTPLATE_BODY_COSMETIC
        || itemType == ItemType.LEGGINGS_BODY_COSMETIC
        || itemType == ItemType.BOOTS_BODY_COSMETIC) {
      this.bodyCosmeticDelegate.equip(this.cosmeticItemStack, itemType);
      DatabaseManager.setCosmetic(player, slotType, ItemStack.EMPTY);
    } else {
      DatabaseManager.setCosmetic(this.player, itemType, this.cosmeticItemStack);
      DatabaseManager.setCosmetic(player, getBodyCosmeticType(itemType), ItemStack.EMPTY);
    }

    updateArmorView();
  }

  @Override
  public void init() {
    ItemStack stackFromDb = DatabaseManager.getCosmeticItemStack(player, itemType);
    if (stackFromDb.isEmpty()) {
      stackFromDb = DatabaseManager.getCosmeticItemStack(player, getBodyCosmeticType(itemType));
      equip(stackFromDb, getBodyCosmeticType(itemType));
    } else {
      equip(stackFromDb, this.itemType);
    }
  }

  @Override
  public void tick() {
    if (cosmeticItemStack.isEmpty() && bodyCosmeticDelegate.getCosmeticItemStack().isEmpty()) {
      return;
    }

    if (bodyCosmeticDelegate.getCosmeticItemStack() != ItemStack.EMPTY) {
      bodyCosmeticDelegate.tick();
    } else {
      updateArmorView();
    }
  }

  @Override
  public ItemType getItemType() {
    return slotType;
  }

  @Override
  public void onUnload() {
    if (bodyCosmeticDelegate.getCosmeticItemStack() != ItemStack.EMPTY) {
      this.bodyCosmeticDelegate.unequip();
    }
    updateArmorView();
  }

  @Nullable
  public Entity getBodyCosmeticModel() {
    if (bodyCosmeticDelegate.getCosmeticItemStack() != ItemStack.EMPTY) {
      return bodyCosmeticDelegate.getBodyCosmeticsModel();
    }
    return null;
  }

  private void updateArmorView() {
    if (player.isRemoved()) {
      return;
    }

    EquipmentSlot slot = getEquipmentSlotFor(this.slotType);
    ItemStack cosmeticStack = this.cosmeticItemStack;

    ItemStack stackForDisplay =
        cosmeticStack.isEmpty() ? player.getEquippedStack(slot) : cosmeticStack;

    sendInventorySlotPacket(player, getSlotFor(this.slotType), stackForDisplay);

    List<Pair<EquipmentSlot, ItemStack>> equipmentList =
        Lists.newArrayList(Pair.of(slot, stackForDisplay.copy()));
    player
        .getEntityWorld()
        .getChunkManager()
        .sendToNearbyPlayers(
            player, new EntityEquipmentUpdateS2CPacket(player.getId(), equipmentList));
  }

  public static int getSlotFor(ItemType type) {
    return switch (type) {
      case HAT -> 5;
      case CHESTPLATE -> 6;
      case LEGGINGS -> 7;
      case BOOTS -> 8;
      default -> throw new IllegalArgumentException("Invalid ItemType for ArmorCosmetic: " + type);
    };
  }

  public static ItemType getItemTypeForSlot(EquipmentSlot slot) {
    return switch (slot) {
      case HEAD -> ItemType.HAT;
      case CHEST -> ItemType.CHESTPLATE;
      case LEGS -> ItemType.LEGGINGS;
      case FEET -> ItemType.BOOTS;
      default ->
          throw new IllegalArgumentException("Invalid EquipmentSlot for ArmorCosmetic: " + slot);
    };
  }

  public static EquipmentSlot getEquipmentSlotFor(ItemType type) {
    return switch (type) {
      case HAT -> EquipmentSlot.HEAD;
      case CHESTPLATE -> EquipmentSlot.CHEST;
      case LEGGINGS -> EquipmentSlot.LEGS;
      case BOOTS -> EquipmentSlot.FEET;
      default -> throw new IllegalArgumentException("Invalid ItemType for ArmorCosmetic: " + type);
    };
  }

  private static ItemType getBodyCosmeticType(ItemType type) {
    return switch (type) {
      case HAT -> ItemType.HAT_BODY_COSMETIC;
      case CHESTPLATE -> ItemType.CHESTPLATE_BODY_COSMETIC;
      case LEGGINGS -> ItemType.LEGGINGS_BODY_COSMETIC;
      case BOOTS -> ItemType.BOOTS_BODY_COSMETIC;
      default -> type;
    };
  }

  public static void sendInventorySlotPacket(
      ServerPlayerEntity player, int slot, ItemStack targetItemStack) {
    player.networkHandler.sendPacket(
        new ScreenHandlerSlotUpdateS2CPacket(
            player.playerScreenHandler.syncId,
            player.playerScreenHandler.nextRevision(),
            slot,
            targetItemStack));
  }

  @Override
  public ItemStack getCosmeticItemStack() {
    return cosmeticItemStack;
  }
}
