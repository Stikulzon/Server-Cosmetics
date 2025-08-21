package com.zefir.servercosmetics.gui.core;

import com.zefir.servercosmetics.data.CustomItemEntry;

import java.util.List;

@FunctionalInterface
public interface ICosmeticProvider {
    List<CustomItemEntry> getItems();
}