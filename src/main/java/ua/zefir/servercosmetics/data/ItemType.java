package ua.zefir.servercosmetics.data;

import java.util.List;

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
}
