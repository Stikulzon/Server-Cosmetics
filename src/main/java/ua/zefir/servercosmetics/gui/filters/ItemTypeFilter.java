package ua.zefir.servercosmetics.gui.filters;

import java.util.List;
import java.util.function.Predicate;
import ua.zefir.servercosmetics.data.CustomItemEntry;
import ua.zefir.servercosmetics.data.ItemType;

public record ItemTypeFilter(List<ItemType> type) implements Predicate<CustomItemEntry> {
  @Override
  public boolean test(CustomItemEntry entry) {
    for (ItemType itemType : type) {
      if (entry.type() == itemType) {
        return true;
      }
    }
    return false;
  }
}
