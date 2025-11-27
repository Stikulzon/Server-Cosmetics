package com.zefir.servercosmetics.util;

import com.zefir.servercosmetics.data.ItemType;
import com.zefir.servercosmetics.database.DatabaseManager;
import com.zefir.servercosmetics.ext.ICosmetic;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import static com.zefir.servercosmetics.util.PacketUtil.sendInventorySlotPacket;

public class ArmorCosmetic implements ICosmetic {

    private final ServerPlayerEntity player;
    private final ItemType slotType;
    private ItemType itemType;
    @Getter
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

        if (itemType == ItemType.HAT_BODY_COSMETIC || itemType == ItemType.CHESTPLATE_BODY_COSMETIC || itemType == ItemType.LEGGINGS_BODY_COSMETIC || itemType == ItemType.BOOTS_BODY_COSMETIC) {
            this.bodyCosmeticDelegate.equip(this.cosmeticItemStack, itemType);
            DatabaseManager.setCosmetic(player, slotType, ItemStack.EMPTY);
        } else {
            DatabaseManager.setCosmetic(this.player, itemType, this.cosmeticItemStack);
            DatabaseManager.setCosmetic(player, getBodyCosmeticType(itemType), ItemStack.EMPTY);
        }

        updatePlayerArmorView();
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
        if (cosmeticItemStack.isEmpty()) {
            return;
        }

        if (bodyCosmeticDelegate.getCosmeticItemStack() != ItemStack.EMPTY) {
            bodyCosmeticDelegate.tick();
        }

        updatePlayerArmorView();
    }

    @Override
    public ItemType getItemType(){
        return slotType;
    }

    @Override
    public void onUnload() {
        if (bodyCosmeticDelegate.getCosmeticItemStack() != ItemStack.EMPTY) {
            this.bodyCosmeticDelegate.unequip();
        }
    }

    @Nullable
    public Entity getBodyCosmeticModel() {
        if (bodyCosmeticDelegate.getCosmeticItemStack() != ItemStack.EMPTY) {
            return bodyCosmeticDelegate.getBodyCosmeticsModel();
        }
        return null;
    }

    /**
     * Sends the packet to make the player see the cosmetic in their armor slot.
     * If the cosmetic is empty, it makes them see their real armor.
     */
    private void updatePlayerArmorView() {
        if(player.isRemoved()) {
            return;
        }
        ItemStack itemStackToSend;
        if (!cosmeticItemStack.isEmpty()) {
            itemStackToSend = cosmeticItemStack;
        } else {
            // If no cosmetic, show the real armor piece
            itemStackToSend = player.getEquippedStack(getEquipmentSlotFor(this.slotType));
        }
        sendInventorySlotPacket(player, getSlotFor(this.slotType), itemStackToSend);
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

    public static EquipmentSlot getEquipmentSlotFor(ItemType type) {
        return switch (type) {
            case HAT -> EquipmentSlot.HEAD;
            case CHESTPLATE -> EquipmentSlot.CHEST;
            case LEGGINGS -> EquipmentSlot.LEGS;
            case BOOTS -> EquipmentSlot.FEET;
            default -> throw new IllegalArgumentException("Invalid ItemType for ArmorCosmetic: " + type);
        };
    }

    /**
     * Maps a base armor type to its corresponding body cosmetic type.
     */
    private static ItemType getBodyCosmeticType(ItemType type) {
        return switch (type) {
            case HAT -> ItemType.HAT_BODY_COSMETIC;
            case CHESTPLATE -> ItemType.CHESTPLATE_BODY_COSMETIC;
            case LEGGINGS -> ItemType.LEGGINGS_BODY_COSMETIC;
            case BOOTS -> ItemType.BOOTS_BODY_COSMETIC;
            default -> type;
        };
    }
}