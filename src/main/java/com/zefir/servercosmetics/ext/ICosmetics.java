package com.zefir.servercosmetics.ext;

import com.zefir.servercosmetics.data.ItemType;

import java.util.List;

public interface ICosmetics {
    ICosmetic getCosmeticFor(ItemType type);
    void initCosmetics();
    void removeCosmetics();
    void tickArmor();
    List<ICosmetic> getCosmeticsList();
}
