package com.zefir.servercosmetics.gui.filters;

import com.zefir.servercosmetics.config.entries.CustomItemEntry;
import com.zefir.servercosmetics.config.entries.ItemType;

import java.util.function.Predicate;

public class ItemTypeFilter implements Predicate<CustomItemEntry> {
    private final ItemType type;

    public ItemTypeFilter(ItemType type) {
        this.type = type;
    }

    @Override
    public boolean test(CustomItemEntry entry) {
        return entry.type() == this.type;
    }
}
