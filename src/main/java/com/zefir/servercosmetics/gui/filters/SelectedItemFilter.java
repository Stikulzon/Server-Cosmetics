package com.zefir.servercosmetics.gui.filters;

import com.zefir.servercosmetics.data.CustomItemEntry;
import lombok.Setter;
import net.minecraft.item.Item;

import java.util.function.Predicate;

public class SelectedItemFilter implements Predicate<CustomItemEntry> {
    @Setter
    private Item selectedItem;

    @Override
    public boolean test(CustomItemEntry entry) {
        if(selectedItem == null){
            return true;
        }
        return entry.itemStack().getItem() == selectedItem;
    }
}
