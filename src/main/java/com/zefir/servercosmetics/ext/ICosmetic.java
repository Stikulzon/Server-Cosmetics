package com.zefir.servercosmetics.ext;

import com.zefir.servercosmetics.data.ItemType;
import net.minecraft.item.ItemStack;

public interface ICosmetic {
    ItemType getItemType();
    void tick();
    void init();
    void equip(ItemStack cosmeticStack, ItemType type);
}
