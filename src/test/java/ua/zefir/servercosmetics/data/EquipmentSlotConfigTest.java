package ua.zefir.servercosmetics.data;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EquipmentSlotConfigTest {

  @Test
  void convertsRowAndColumnToInventoryIndex() {
    EquipmentSlotConfig slot = new EquipmentSlotConfig("head", 2, 4, ItemType.HAT, true, "Head");

    assertEquals(22, slot.slotIndex());
  }
}
