package ua.zefir.servercosmetics.gui.filters;

import java.util.function.Predicate;
import net.minecraft.world.item.Item;
import ua.zefir.servercosmetics.data.CustomItemEntry;

public class SelectedItemFilter implements Predicate<CustomItemEntry> {
  private Item selectedItem;

  public void setSelectedItem(Item selectedItem) {
    this.selectedItem = selectedItem;
  }

  @Override
  public boolean test(CustomItemEntry entry) {
    if (selectedItem == null) {
      return true;
    }
    return entry.itemStack().getItem() == selectedItem;
  }
}
