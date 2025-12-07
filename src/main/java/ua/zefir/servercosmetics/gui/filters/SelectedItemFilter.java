package ua.zefir.servercosmetics.gui.filters;

import java.util.function.Predicate;
import lombok.Setter;
import net.minecraft.item.Item;
import ua.zefir.servercosmetics.data.CustomItemEntry;

public class SelectedItemFilter implements Predicate<CustomItemEntry> {
  @Setter private Item selectedItem;

  @Override
  public boolean test(CustomItemEntry entry) {
    if (selectedItem == null) {
      return true;
    }
    return entry.itemStack().getItem() == selectedItem;
  }
}
