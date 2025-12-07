package ua.zefir.servercosmetics.gui.core;

import java.util.List;
import ua.zefir.servercosmetics.data.CustomItemEntry;

@FunctionalInterface
public interface ICosmeticProvider {
  List<CustomItemEntry> getItems();
}
