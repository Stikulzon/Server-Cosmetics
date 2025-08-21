package com.zefir.servercosmetics.ext;

import net.minecraft.item.ItemStack;

public interface ICosmetic {
    void tick();
    void init();
    void equip(ItemStack cosmeticStack);
}
