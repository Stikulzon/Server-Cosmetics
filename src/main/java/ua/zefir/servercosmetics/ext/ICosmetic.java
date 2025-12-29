package ua.zefir.servercosmetics.ext;

import net.minecraft.item.ItemStack;
import ua.zefir.servercosmetics.data.ItemType;

public interface ICosmetic {
  ItemType getItemType();

  ItemStack getCosmeticItemStack();

  void tick();

  void init();

  void onUnload();

  void equip(ItemStack cosmeticStack, ItemType type);
}
