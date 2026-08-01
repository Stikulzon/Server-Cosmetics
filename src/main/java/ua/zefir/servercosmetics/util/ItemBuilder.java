package ua.zefir.servercosmetics.util;

import static ua.zefir.servercosmetics.ModInit.id;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import ua.zefir.servercosmetics.ModInit;
import ua.zefir.servercosmetics.datagen.RuntimeModelManager;

public class ItemBuilder {
  private ItemStack stack;

  private ItemBuilder(Item item) {
    this.stack = new ItemStack(item);
  }

  private ItemBuilder(ItemStack stack) {
    this.stack = stack;
  }

  public static ItemBuilder fromId(String materialId) {
    Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(materialId));
    if (item == Items.AIR) {
      ModInit.LOGGER.warn("Invalid materialId '{}'. Defaulting to minecraft:paper.", materialId);
      item = Items.PAPER;
    }
    return new ItemBuilder(item);
  }

  public static ItemBuilder fromItem(Item item) {
    return new ItemBuilder(item);
  }

  public ItemBuilder name(Component name) {
    if (name != null) {
      stack.set(DataComponents.CUSTOM_NAME, name);
    }
    return this;
  }

  public ItemBuilder lore(List<Component> lore) {
    if (lore != null && !lore.isEmpty()) {
      stack.set(DataComponents.LORE, new ItemLore(lore));
    } else {
      stack.set(DataComponents.LORE, new ItemLore(Collections.emptyList()));
    }
    return this;
  }

  public ItemBuilder dye(int color) {
    stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color));
    return this;
  }

  public ItemBuilder customData(String key, String value) {
    stack.update(
        DataComponents.CUSTOM_DATA,
        CustomData.EMPTY,
        comp -> comp.update(nbt -> nbt.putString(key, value)));
    return this;
  }

  public ItemBuilder model(Identifier modelId, boolean registerRequest) {
    if (registerRequest) {
      try {
        RuntimeModelManager.requestItemModel(modelId.getPath(), false);
      } catch (Exception e) {
        ModInit.LOGGER.error("Failed to request item model '{}': {}", modelId, e.getMessage());
      }
    }
    stack.set(DataComponents.ITEM_MODEL, modelId);
    return this;
  }

  public ItemBuilder applyCosmeticLogic(String cosmeticId, boolean dyeable) {
    Equippable equippable = stack.get(DataComponents.EQUIPPABLE);

    if (dyeable) {
      this.dye(16777215);
    }

    if (equippable != null && equippable.slot() != EquipmentSlot.BODY) {
      EquipmentSlot slot = equippable.slot();
      String armorId = cosmeticId.replace("_" + slot.getName().toLowerCase(), "");

      RuntimeModelManager.requestArmorModel(armorId, slot);

      this.stack = new ItemStack(getLeatherArmorFor(slot));

      Identifier itemModelId = id(cosmeticId);
      stack.set(DataComponents.ITEM_MODEL, itemModelId);

      Identifier armorModelId = id(armorId);
      ResourceKey<EquipmentAsset> layers =
          ResourceKey.create(EquipmentAssets.ROOT_ID, armorModelId);

      Equippable currentLeatherComp = stack.get(DataComponents.EQUIPPABLE);

      if (currentLeatherComp != null) {
        Equippable newComp =
            new Equippable(
                currentLeatherComp.slot(),
                currentLeatherComp.equipSound(),
                Optional.of(layers), // The Magic Layer Key
                currentLeatherComp.cameraOverlay(),
                currentLeatherComp.allowedEntities(),
                currentLeatherComp.dispensable(),
                currentLeatherComp.swappable(),
                currentLeatherComp.damageOnHurt(),
                currentLeatherComp.equipOnInteract(),
                currentLeatherComp.canBeSheared(),
                currentLeatherComp.shearingSound());
        stack.set(DataComponents.EQUIPPABLE, newComp);
      }

    } else {
      try {
        RuntimeModelManager.requestItemModel(cosmeticId, dyeable);
        stack.set(DataComponents.ITEM_MODEL, id(cosmeticId));
      } catch (Exception e) {
        ModInit.LOGGER.error("Failed to request item model '{}': {}", cosmeticId, e.getMessage());
      }
    }

    return this;
  }

  private Item getLeatherArmorFor(EquipmentSlot type) {
    return switch (type) {
      case HEAD -> Items.LEATHER_HELMET;
      case CHEST -> Items.LEATHER_CHESTPLATE;
      case LEGS -> Items.LEATHER_LEGGINGS;
      case FEET -> Items.LEATHER_BOOTS;
      default -> Items.PAPER;
    };
  }

  public ItemStack build() {
    return stack;
  }
}
