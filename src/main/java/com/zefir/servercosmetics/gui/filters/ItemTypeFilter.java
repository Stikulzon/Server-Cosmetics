package com.zefir.servercosmetics.gui.filters;

import com.zefir.servercosmetics.data.CustomItemEntry;
import com.zefir.servercosmetics.data.ItemType;

import java.util.function.Predicate;

public record ItemTypeFilter(ItemType type) implements Predicate<CustomItemEntry> {
    @Override
    public boolean test(CustomItemEntry entry) {
        return entry.type() == this.type;
    }
}
