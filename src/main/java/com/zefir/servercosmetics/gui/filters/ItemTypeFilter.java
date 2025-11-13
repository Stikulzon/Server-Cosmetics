package com.zefir.servercosmetics.gui.filters;

import com.zefir.servercosmetics.data.CustomItemEntry;
import com.zefir.servercosmetics.data.ItemType;

import java.util.List;
import java.util.function.Predicate;

public record ItemTypeFilter(List<ItemType> type) implements Predicate<CustomItemEntry> {
    @Override
    public boolean test(CustomItemEntry entry) {
        for (ItemType itemType : type) {
            if(entry.type() == itemType){
                return true;
            }
        }
        return false;
    }
}
