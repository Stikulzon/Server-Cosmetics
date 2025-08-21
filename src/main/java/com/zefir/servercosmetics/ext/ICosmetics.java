package com.zefir.servercosmetics.ext;

import com.zefir.servercosmetics.data.ItemType;

public interface ICosmetics {
    ICosmetic getCosmeticFor(ItemType type);
    void initCosmetics();
    void tickArmor();
}
