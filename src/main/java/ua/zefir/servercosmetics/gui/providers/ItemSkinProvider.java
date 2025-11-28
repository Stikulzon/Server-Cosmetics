package ua.zefir.servercosmetics.gui.providers;

import ua.zefir.servercosmetics.data.CustomItemEntry;
import ua.zefir.servercosmetics.data.CustomItemRegistry;
import ua.zefir.servercosmetics.data.ItemType;
import ua.zefir.servercosmetics.gui.core.ICosmeticProvider;

import java.util.List;

public class ItemSkinProvider implements ICosmeticProvider {
    @Override
    public List<CustomItemEntry> getItems() {
        return CustomItemRegistry.getAllCosmeticsForType(ItemType.ITEM_SKIN);
    }
}
