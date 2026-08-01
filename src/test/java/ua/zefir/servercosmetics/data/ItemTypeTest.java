package ua.zefir.servercosmetics.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;
import org.junit.jupiter.api.Test;

class ItemTypeTest {

  @Test
  void returnsMatchingTypesInCurrentOrder() {
    assertEquals(
        List.of(ItemType.HAT, ItemType.HAT_BODY_COSMETIC, ItemType.HELMET),
        ItemType.HAT.getMatchingTypes());
    assertEquals(List.of(ItemType.HELMET), ItemType.HELMET.getMatchingTypes());
    assertEquals(
        List.of(ItemType.CHESTPLATE, ItemType.CHESTPLATE_BODY_COSMETIC),
        ItemType.CHESTPLATE.getMatchingTypes());
    assertEquals(
        List.of(ItemType.LEGGINGS, ItemType.LEGGINGS_BODY_COSMETIC),
        ItemType.LEGGINGS.getMatchingTypes());
    assertEquals(
        List.of(ItemType.BOOTS, ItemType.BOOTS_BODY_COSMETIC), ItemType.BOOTS.getMatchingTypes());
    assertEquals(List.of(ItemType.BODY_COSMETIC), ItemType.BODY_COSMETIC.getMatchingTypes());
    assertEquals(List.of(ItemType.ITEM_SKIN), ItemType.ITEM_SKIN.getMatchingTypes());
    assertEquals(
        List.of(ItemType.HAT, ItemType.HAT_BODY_COSMETIC),
        ItemType.HAT_BODY_COSMETIC.getMatchingTypes());
    assertEquals(
        List.of(ItemType.CHESTPLATE, ItemType.CHESTPLATE_BODY_COSMETIC),
        ItemType.CHESTPLATE_BODY_COSMETIC.getMatchingTypes());
    assertEquals(
        List.of(ItemType.LEGGINGS, ItemType.LEGGINGS_BODY_COSMETIC),
        ItemType.LEGGINGS_BODY_COSMETIC.getMatchingTypes());
    assertEquals(
        List.of(ItemType.BOOTS, ItemType.BOOTS_BODY_COSMETIC),
        ItemType.BOOTS_BODY_COSMETIC.getMatchingTypes());
  }

  @Test
  void normalizesVariantsToEquippedSlotTypes() {
    assertEquals(ItemType.HAT, ItemType.HELMET.equippedSlotType());
    assertEquals(ItemType.HAT, ItemType.HAT_BODY_COSMETIC.equippedSlotType());
    assertEquals(ItemType.CHESTPLATE, ItemType.CHESTPLATE_BODY_COSMETIC.equippedSlotType());
    assertEquals(ItemType.LEGGINGS, ItemType.LEGGINGS_BODY_COSMETIC.equippedSlotType());
    assertEquals(ItemType.BOOTS, ItemType.BOOTS_BODY_COSMETIC.equippedSlotType());
    assertEquals(ItemType.ITEM_SKIN, ItemType.ITEM_SKIN.equippedSlotType());
  }

  @Test
  void mapsInventoryMenuSlotsWithoutBroadeningAcceptedTypes() {
    assertEquals(5, ItemType.HAT.inventoryMenuSlot());
    assertEquals(6, ItemType.CHESTPLATE_BODY_COSMETIC.inventoryMenuSlot());
    assertEquals(7, ItemType.LEGGINGS_BODY_COSMETIC.inventoryMenuSlot());
    assertEquals(8, ItemType.BOOTS_BODY_COSMETIC.inventoryMenuSlot());
    assertThrows(IllegalStateException.class, ItemType.HAT_BODY_COSMETIC::inventoryMenuSlot);
    assertThrows(IllegalStateException.class, ItemType.HELMET::inventoryMenuSlot);

    assertEquals(ItemType.HAT, ItemType.fromInventoryMenuSlot(5));
    assertEquals(ItemType.CHESTPLATE, ItemType.fromInventoryMenuSlot(6));
    assertEquals(ItemType.LEGGINGS, ItemType.fromInventoryMenuSlot(7));
    assertEquals(ItemType.BOOTS, ItemType.fromInventoryMenuSlot(8));
    assertNull(ItemType.fromInventoryMenuSlot(4));
  }

  @Test
  void mapsArmorInventorySlotsWithoutAcceptingVariants() {
    assertEquals(5, ItemType.HAT.armorInventoryMenuSlot());
    assertEquals(6, ItemType.CHESTPLATE.armorInventoryMenuSlot());
    assertEquals(7, ItemType.LEGGINGS.armorInventoryMenuSlot());
    assertEquals(8, ItemType.BOOTS.armorInventoryMenuSlot());
    assertThrows(
        IllegalArgumentException.class, ItemType.CHESTPLATE_BODY_COSMETIC::armorInventoryMenuSlot);
  }

  @Test
  void mapsMinecraftEquipmentSlotsWithoutBroadeningAcceptedTypes() {
    assertEquals(EquipmentSlot.HEAD, ItemType.HAT.equipmentSlot());
    assertEquals(EquipmentSlot.CHEST, ItemType.CHESTPLATE.equipmentSlot());
    assertEquals(EquipmentSlot.LEGS, ItemType.LEGGINGS.equipmentSlot());
    assertEquals(EquipmentSlot.FEET, ItemType.BOOTS.equipmentSlot());
    assertThrows(IllegalArgumentException.class, ItemType.HELMET::equipmentSlot);

    assertEquals(ItemType.HAT, ItemType.fromEquipmentSlot(EquipmentSlot.HEAD));
    assertEquals(ItemType.CHESTPLATE, ItemType.fromEquipmentSlot(EquipmentSlot.CHEST));
    assertEquals(ItemType.LEGGINGS, ItemType.fromEquipmentSlot(EquipmentSlot.LEGS));
    assertEquals(ItemType.BOOTS, ItemType.fromEquipmentSlot(EquipmentSlot.FEET));
    assertThrows(
        IllegalArgumentException.class, () -> ItemType.fromEquipmentSlot(EquipmentSlot.MAINHAND));
  }

  @Test
  void mapsSlotsToBodyCosmeticVariants() {
    assertEquals(ItemType.HAT_BODY_COSMETIC, ItemType.HAT.bodyCosmeticType());
    assertEquals(ItemType.CHESTPLATE_BODY_COSMETIC, ItemType.CHESTPLATE.bodyCosmeticType());
    assertEquals(ItemType.LEGGINGS_BODY_COSMETIC, ItemType.LEGGINGS.bodyCosmeticType());
    assertEquals(ItemType.BOOTS_BODY_COSMETIC, ItemType.BOOTS.bodyCosmeticType());
    assertEquals(ItemType.ITEM_SKIN, ItemType.ITEM_SKIN.bodyCosmeticType());
  }
}
