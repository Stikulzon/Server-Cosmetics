package ua.zefir.servercosmetics.gui.core;

import ua.zefir.servercosmetics.data.CustomItemEntry;

import java.util.List;

@FunctionalInterface
public interface ICosmeticProvider {
    List<CustomItemEntry> getItems();
}