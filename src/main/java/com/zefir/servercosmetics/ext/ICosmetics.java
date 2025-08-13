package com.zefir.servercosmetics.ext;

import com.zefir.servercosmetics.util.ArmorCosmetic;
import com.zefir.servercosmetics.util.BodyCosmetic;
import com.zefir.servercosmetics.util.HatCosmetic;

public interface ICosmetics {
    BodyCosmetic getBodyCosmetics();
    HatCosmetic getHatCosmetic();
    ArmorCosmetic getChestCosmetic();
    ArmorCosmetic getLeggingsCosmetic();
    ArmorCosmetic getBootsCosmetic();
}
