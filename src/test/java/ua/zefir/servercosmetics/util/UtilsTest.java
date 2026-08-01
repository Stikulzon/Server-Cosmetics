package ua.zefir.servercosmetics.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import ua.zefir.servercosmetics.data.ItemType;

class UtilsTest {

  @Test
  void sortsZeroPriorityAfterExplicitPriorities() {
    assertEquals(Integer.MAX_VALUE, Utils.getSortablePriority(0));
    assertEquals(-1, Utils.getSortablePriority(-1));
    assertEquals(42, Utils.getSortablePriority(42));
  }

  @Test
  void mapsCosmeticTypesToCurrentInventorySlots() {
    assertEquals(5, Utils.getSlotForType(ItemType.HAT));
    assertEquals(6, Utils.getSlotForType(ItemType.CHESTPLATE));
    assertEquals(6, Utils.getSlotForType(ItemType.CHESTPLATE_BODY_COSMETIC));
    assertEquals(7, Utils.getSlotForType(ItemType.LEGGINGS));
    assertEquals(7, Utils.getSlotForType(ItemType.LEGGINGS_BODY_COSMETIC));
    assertEquals(8, Utils.getSlotForType(ItemType.BOOTS));
    assertEquals(8, Utils.getSlotForType(ItemType.BOOTS_BODY_COSMETIC));
    assertThrows(IllegalStateException.class, () -> Utils.getSlotForType(ItemType.ITEM_SKIN));
  }

  @Test
  void mapsInventorySlotsToCurrentCosmeticTypes() {
    assertEquals(ItemType.HAT, Utils.getItemTypeForSlot(5));
    assertEquals(ItemType.CHESTPLATE, Utils.getItemTypeForSlot(6));
    assertEquals(ItemType.LEGGINGS, Utils.getItemTypeForSlot(7));
    assertEquals(ItemType.BOOTS, Utils.getItemTypeForSlot(8));
    assertNull(Utils.getItemTypeForSlot(4));
  }

  @Test
  void mapsVariantsToTheirEquippedSlotType() {
    assertEquals(ItemType.HAT, Utils.getRealEquipedItemType(ItemType.HELMET));
    assertEquals(ItemType.HAT, Utils.getRealEquipedItemType(ItemType.HAT_BODY_COSMETIC));
    assertEquals(
        ItemType.CHESTPLATE, Utils.getRealEquipedItemType(ItemType.CHESTPLATE_BODY_COSMETIC));
    assertEquals(ItemType.LEGGINGS, Utils.getRealEquipedItemType(ItemType.LEGGINGS_BODY_COSMETIC));
    assertEquals(ItemType.BOOTS, Utils.getRealEquipedItemType(ItemType.BOOTS_BODY_COSMETIC));
    assertEquals(ItemType.ITEM_SKIN, Utils.getRealEquipedItemType(ItemType.ITEM_SKIN));
  }
}
