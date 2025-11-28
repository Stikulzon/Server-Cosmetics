package ua.zefir.servercosmetics.datafixer;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class NbtDatafixer {
    private static final String OLD_NBT_KEY_ITEM_SKIN_ID = "itemSkinsID";
    public static final String NEW_NBT_KEY_CUSTOM_ITEM_ID = "cosmeticItemId";

    public static void fixItemStackNbt(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        NbtComponent customDataComponent = stack.get(DataComponentTypes.CUSTOM_DATA);

        if (customDataComponent != null) {
            NbtCompound nbt = customDataComponent.copyNbt();


            if (nbt.contains(OLD_NBT_KEY_ITEM_SKIN_ID, NbtCompound.STRING_TYPE)) {
                if (!nbt.contains(NEW_NBT_KEY_CUSTOM_ITEM_ID, NbtCompound.STRING_TYPE)) {
                    String idValue = nbt.getString(OLD_NBT_KEY_ITEM_SKIN_ID);
                    nbt.putString(NEW_NBT_KEY_CUSTOM_ITEM_ID, idValue);
                }
                nbt.remove(OLD_NBT_KEY_ITEM_SKIN_ID);

                final NbtCompound finalNbt = nbt.copy();
                stack.apply(DataComponentTypes.CUSTOM_DATA,
                        NbtComponent.DEFAULT,
                        existing -> NbtComponent.of(finalNbt)
                );
            }

            if (nbt.contains(NbtDatafixer.NEW_NBT_KEY_CUSTOM_ITEM_ID, NbtCompound.STRING_TYPE)) {
                CustomModelDataComponent expectedModelData = stack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
                if (expectedModelData != null) {
                    stack.remove(DataComponentTypes.CUSTOM_MODEL_DATA);
                }
            }
        }
    }
}
