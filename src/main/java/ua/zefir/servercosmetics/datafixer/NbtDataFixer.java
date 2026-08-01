package ua.zefir.servercosmetics.datafixer;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;

public class NbtDataFixer {
  private static final String OLD_NBT_KEY_ITEM_SKIN_ID = "itemSkinsID";
  public static final String NEW_NBT_KEY_CUSTOM_ITEM_ID = "cosmeticItemId";

  public static void fixItemStackNbt(ItemStack stack) {
    if (stack == null || stack.isEmpty()) {
      return;
    }

    CustomData customDataComponent = stack.get(DataComponents.CUSTOM_DATA);

    if (customDataComponent != null) {
      CompoundTag nbt = customDataComponent.copyTag();

      if (nbt.contains(OLD_NBT_KEY_ITEM_SKIN_ID)) {
        if (!nbt.contains(NEW_NBT_KEY_CUSTOM_ITEM_ID)) {
          String idValue = nbt.getStringOr(OLD_NBT_KEY_ITEM_SKIN_ID, "unknown");
          nbt.putString(NEW_NBT_KEY_CUSTOM_ITEM_ID, idValue);
        }
        nbt.remove(OLD_NBT_KEY_ITEM_SKIN_ID);

        final CompoundTag finalNbt = nbt.copy();
        stack.update(
            DataComponents.CUSTOM_DATA, CustomData.EMPTY, existing -> CustomData.of(finalNbt));
      }

      if (nbt.contains(NbtDataFixer.NEW_NBT_KEY_CUSTOM_ITEM_ID)) {
        CustomModelData expectedModelData = stack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (expectedModelData != null) {
          stack.remove(DataComponents.CUSTOM_MODEL_DATA);
        }
      }
    }
  }
}
