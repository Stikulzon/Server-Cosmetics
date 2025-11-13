package com.zefir.servercosmetics.gui.providers;

import com.zefir.servercosmetics.data.CustomItemEntry;
import com.zefir.servercosmetics.data.CustomItemRegistry;
import com.zefir.servercosmetics.data.ItemType;
import com.zefir.servercosmetics.gui.core.ICosmeticProvider;
import net.minecraft.item.Item;
import java.util.List;

public class ItemSkinProvider implements ICosmeticProvider {
    @Override
    public List<CustomItemEntry> getItems() {
        return CustomItemRegistry.getAllCosmeticsForType(ItemType.ITEM_SKIN);
    }
}
