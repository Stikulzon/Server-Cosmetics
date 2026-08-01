package ua.zefir.servercosmetics.data;

import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;

public enum ItemType {
  HAT,
  HELMET,
  CHESTPLATE,
  LEGGINGS,
  BOOTS,
  BODY_COSMETIC,
  ITEM_SKIN,
  HAT_BODY_COSMETIC,
  CHESTPLATE_BODY_COSMETIC,
  LEGGINGS_BODY_COSMETIC,
  BOOTS_BODY_COSMETIC;

  public List<ItemType> getMatchingTypes() {
    return switch (this) {
      case HAT -> List.of(HAT, HAT_BODY_COSMETIC, HELMET);
      case HELMET -> List.of(HELMET);
      case CHESTPLATE -> List.of(CHESTPLATE, CHESTPLATE_BODY_COSMETIC);
      case LEGGINGS -> List.of(LEGGINGS, LEGGINGS_BODY_COSMETIC);
      case BOOTS -> List.of(BOOTS, BOOTS_BODY_COSMETIC);
      case BODY_COSMETIC -> List.of(BODY_COSMETIC);
      case ITEM_SKIN -> List.of(ITEM_SKIN);
      case HAT_BODY_COSMETIC -> List.of(HAT, HAT_BODY_COSMETIC);
      case CHESTPLATE_BODY_COSMETIC -> List.of(CHESTPLATE, CHESTPLATE_BODY_COSMETIC);
      case LEGGINGS_BODY_COSMETIC -> List.of(LEGGINGS, LEGGINGS_BODY_COSMETIC);
      case BOOTS_BODY_COSMETIC -> List.of(BOOTS, BOOTS_BODY_COSMETIC);
    };
  }

  public ItemType equippedSlotType() {
    return switch (this) {
      case HELMET, HAT_BODY_COSMETIC -> HAT;
      case CHESTPLATE_BODY_COSMETIC -> CHESTPLATE;
      case LEGGINGS_BODY_COSMETIC -> LEGGINGS;
      case BOOTS_BODY_COSMETIC -> BOOTS;
      default -> this;
    };
  }

  public int inventoryMenuSlot() {
    return switch (this) {
      case HAT -> 5;
      case CHESTPLATE, CHESTPLATE_BODY_COSMETIC -> 6;
      case LEGGINGS, LEGGINGS_BODY_COSMETIC -> 7;
      case BOOTS, BOOTS_BODY_COSMETIC -> 8;
      default ->
          throw new IllegalStateException("Unsupported item type for slot calculation: " + this);
    };
  }

  public int armorInventoryMenuSlot() {
    return switch (this) {
      case HAT -> 5;
      case CHESTPLATE -> 6;
      case LEGGINGS -> 7;
      case BOOTS -> 8;
      default -> throw new IllegalArgumentException("Invalid ItemType for ArmorCosmetic: " + this);
    };
  }

  public EquipmentSlot equipmentSlot() {
    return switch (this) {
      case HAT -> EquipmentSlot.HEAD;
      case CHESTPLATE -> EquipmentSlot.CHEST;
      case LEGGINGS -> EquipmentSlot.LEGS;
      case BOOTS -> EquipmentSlot.FEET;
      default -> throw new IllegalArgumentException("Invalid ItemType for ArmorCosmetic: " + this);
    };
  }

  public ItemType bodyCosmeticType() {
    return switch (this) {
      case HAT -> HAT_BODY_COSMETIC;
      case CHESTPLATE -> CHESTPLATE_BODY_COSMETIC;
      case LEGGINGS -> LEGGINGS_BODY_COSMETIC;
      case BOOTS -> BOOTS_BODY_COSMETIC;
      default -> this;
    };
  }

  public static ItemType fromInventoryMenuSlot(int slot) {
    return switch (slot) {
      case 5 -> HAT;
      case 6 -> CHESTPLATE;
      case 7 -> LEGGINGS;
      case 8 -> BOOTS;
      default -> null;
    };
  }

  public static ItemType fromEquipmentSlot(EquipmentSlot slot) {
    return switch (slot) {
      case HEAD -> HAT;
      case CHEST -> CHESTPLATE;
      case LEGS -> LEGGINGS;
      case FEET -> BOOTS;
      default ->
          throw new IllegalArgumentException("Invalid EquipmentSlot for ArmorCosmetic: " + slot);
    };
  }
}
