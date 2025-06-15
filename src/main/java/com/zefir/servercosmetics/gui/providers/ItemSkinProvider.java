package com.zefir.servercosmetics.gui.providers;

import com.zefir.servercosmetics.config.entries.CustomItemEntry;
import com.zefir.servercosmetics.config.entries.CustomItemRegistry;
import com.zefir.servercosmetics.config.entries.ItemType;
import com.zefir.servercosmetics.gui.core.ICosmeticProvider;
import net.minecraft.item.Item;
import java.util.List;

public class ItemSkinProvider implements ICosmeticProvider {
    private final Item targetItem;

    public ItemSkinProvider(Item targetItem) {
        this.targetItem = targetItem;
    }

    @Override
    public List<CustomItemEntry> getItems() {
        return CustomItemRegistry.getAllCosmeticsForMaterial(ItemType.ITEM_SKIN, targetItem);
    }
}
