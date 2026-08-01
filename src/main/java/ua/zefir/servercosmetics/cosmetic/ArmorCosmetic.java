package ua.zefir.servercosmetics.cosmetic;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import ua.zefir.servercosmetics.data.ItemType;
import ua.zefir.servercosmetics.database.DatabaseManager;
import ua.zefir.servercosmetics.util.Utils;

public class ArmorCosmetic implements Cosmetic {

  private final ServerPlayer player;
  private final ItemType slotType;
  private ItemType itemType;
  private ItemStack cosmeticItemStack = ItemStack.EMPTY;
  private final BodyCosmetic bodyCosmeticDelegate;

  public ArmorCosmetic(ServerPlayer player, ItemType itemType) {
    this.player = player;
    this.slotType = itemType;
    this.itemType = itemType;
    this.bodyCosmeticDelegate = new BodyCosmetic(player, itemType.bodyCosmeticType());
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
      DatabaseManager.setCosmetic(player, itemType.bodyCosmeticType(), ItemStack.EMPTY);
    }

    updateArmorView();
  }

  @Override
  public void init() {
    ItemStack stackFromDb = DatabaseManager.getCosmeticItemStack(player, itemType);
    if (stackFromDb.isEmpty()) {
      stackFromDb = DatabaseManager.getCosmeticItemStack(player, itemType.bodyCosmeticType());
      equip(stackFromDb, itemType.bodyCosmeticType());
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
    ItemStack realStack = player.getItemBySlot(slot);

    ItemStack stackForDisplay =
        cosmeticStack.isEmpty()
            ? realStack
            : Utils.reflectRealDurability(cosmeticStack.copy(), realStack);

    sendInventorySlotPacket(player, getSlotFor(this.slotType), stackForDisplay);

    List<Pair<EquipmentSlot, ItemStack>> equipmentList =
        Lists.newArrayList(Pair.of(slot, stackForDisplay.copy()));
    player
        .level()
        .getChunkSource()
        .sendToTrackingPlayersAndSelf(
            player, new ClientboundSetEquipmentPacket(player.getId(), equipmentList));
  }

  public static int getSlotFor(ItemType type) {
    return type.armorInventoryMenuSlot();
  }

  public static ItemType getItemTypeForSlot(EquipmentSlot slot) {
    return ItemType.fromEquipmentSlot(slot);
  }

  public static EquipmentSlot getEquipmentSlotFor(ItemType type) {
    return type.equipmentSlot();
  }

  public static void sendInventorySlotPacket(
      ServerPlayer player, int slot, ItemStack targetItemStack) {
    player.connection.send(
        new ClientboundContainerSetSlotPacket(
            player.inventoryMenu.containerId,
            player.inventoryMenu.incrementStateId(),
            slot,
            targetItemStack));
  }

  @Override
  public ItemStack getCosmeticItemStack() {
    return cosmeticItemStack;
  }
}
