package ua.zefir.servercosmetics.cosmetic;

import net.minecraft.world.item.ItemStack;
import ua.zefir.servercosmetics.data.ItemType;

public interface Cosmetic {
  ItemType getItemType();

  ItemStack getCosmeticItemStack();

  void tick();

  void init();

  void onUnload();

  void equip(ItemStack cosmeticStack, ItemType type);
}
