package ua.zefir.servercosmetics.ext;

import ua.zefir.servercosmetics.data.ItemType;
import net.minecraft.item.ItemStack;

public interface ICosmetic {
    ItemType getItemType();
    ItemStack getCosmeticItemStack();
    void tick();
    void init();
    void onUnload();
    void equip(ItemStack cosmeticStack, ItemType type);
}
