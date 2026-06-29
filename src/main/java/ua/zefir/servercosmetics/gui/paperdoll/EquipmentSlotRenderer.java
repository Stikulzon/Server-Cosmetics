package ua.zefir.servercosmetics.gui.paperdoll;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import ua.zefir.servercosmetics.data.ItemType;

public class EquipmentSlotRenderer {

  public static ItemStack getPlaceholderItem(ItemType type) {
    return switch (type) {
      case HAT, HAT_BODY_COSMETIC -> new ItemStack(Items.LEATHER_HELMET);
      case HELMET -> new ItemStack(Items.IRON_HELMET);
      case CHESTPLATE, CHESTPLATE_BODY_COSMETIC -> new ItemStack(Items.IRON_CHESTPLATE);
      case LEGGINGS, LEGGINGS_BODY_COSMETIC -> new ItemStack(Items.IRON_LEGGINGS);
      case BOOTS, BOOTS_BODY_COSMETIC -> new ItemStack(Items.IRON_BOOTS);
      case BODY_COSMETIC -> new ItemStack(Items.ARMOR_STAND);
      case ITEM_SKIN -> new ItemStack(Items.PAPER);
    };
  }

  public static String formatSlotName(ItemType type) {
    return switch (type) {
      case HAT -> "Hat";
      case HELMET -> "Helmet";
      case CHESTPLATE -> "Chestplate";
      case LEGGINGS -> "Leggings";
      case BOOTS -> "Boots";
      case BODY_COSMETIC -> "Body Cosmetic";
      case ITEM_SKIN -> "Item Skin";
      case HAT_BODY_COSMETIC -> "Hat/Body";
      case CHESTPLATE_BODY_COSMETIC -> "Chestplate/Body";
      case LEGGINGS_BODY_COSMETIC -> "Leggings/Body";
      case BOOTS_BODY_COSMETIC -> "Boots/Body";
    };
  }
}
