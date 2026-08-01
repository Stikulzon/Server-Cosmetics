package ua.zefir.servercosmetics.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;

final class CustomItemCatalog {

  private final List<CustomItemEntry> entries = new CopyOnWriteArrayList<>();

  void clear() {
    entries.clear();
  }

  void removeIf(Predicate<CustomItemEntry> predicate) {
    entries.removeIf(predicate);
  }

  void add(CustomItemEntry entry) {
    entries.add(entry);
  }

  CustomItemEntry get(String id) {
    for (CustomItemEntry entry : entries) {
      if (entry.id().equals(id)) {
        return entry;
      }
    }
    return null;
  }

  List<CustomItemEntry> entries() {
    return Collections.unmodifiableList(entries);
  }

  List<CustomItemEntry> forMaterial(ItemType type, String targetMaterialId) {
    List<CustomItemEntry> filtered = new ArrayList<>();
    for (CustomItemEntry entry : entries) {
      if (entry.type() == type && entry.baseItemForModel().equals(targetMaterialId)) {
        filtered.add(entry);
      }
    }
    return filtered;
  }

  List<CustomItemEntry> forType(ItemType type) {
    List<CustomItemEntry> filtered = new ArrayList<>();
    for (CustomItemEntry entry : entries) {
      if (entry.type() == type) {
        filtered.add(entry);
      }
    }
    return filtered;
  }

  List<ItemStack> itemStacks() {
    return entries.stream().map(CustomItemEntry::itemStack).toList();
  }

  void materializeAll() {
    entries.forEach(CustomItemEntry::itemStack);
  }
}
