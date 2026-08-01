package ua.zefir.servercosmetics.data;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class CustomItemEntry {
  private final String id;
  private final String permission;
  private final Component displayName;
  private final List<Component> lore;
  private final Supplier<ItemStack> itemStackFactory;
  private final ItemType type;
  private final String baseItemForModel;
  private final int sortingPriority;
  private final boolean dyeable;
  private final List<String> tags;
  private final CosmeticData cosmeticData;
  private ItemStack itemStack;

  public CustomItemEntry(
      String id,
      String permission,
      Component displayName,
      List<Component> lore,
      Supplier<ItemStack> itemStackFactory,
      ItemType type,
      String baseItemForModel,
      int sortingPriority,
      boolean dyeable,
      List<String> tags,
      CosmeticData cosmeticData) {
    this.id = id;
    this.permission = permission;
    this.displayName = displayName;
    this.lore = lore;
    this.itemStackFactory = itemStackFactory;
    this.type = type;
    this.baseItemForModel = baseItemForModel;
    this.sortingPriority = sortingPriority;
    this.dyeable = dyeable;
    this.tags = tags;
    this.cosmeticData = cosmeticData;
  }

  public String id() {
    return id;
  }

  public String permission() {
    return permission;
  }

  public Component displayName() {
    return displayName;
  }

  public List<Component> lore() {
    return lore;
  }

  public synchronized ItemStack itemStack() {
    if (itemStack == null) {
      itemStack = itemStackFactory.get();
    }
    return itemStack;
  }

  public ItemType type() {
    return type;
  }

  public String baseItemForModel() {
    return baseItemForModel;
  }

  public int sortingPriority() {
    return sortingPriority;
  }

  public boolean dyeable() {
    return dyeable;
  }

  public List<String> tags() {
    return tags;
  }

  public CosmeticData cosmeticData() {
    return cosmeticData;
  }
}
