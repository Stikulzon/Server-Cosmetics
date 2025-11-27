package ua.zefir.servercosmetics.ext;

import ua.zefir.servercosmetics.data.ItemType;
import net.minecraft.item.ItemStack;

public interface ICosmetic {
    ItemType getItemType();
    void tick();
    void init();
    void onUnload();
    void equip(ItemStack cosmeticStack, ItemType type);
}
