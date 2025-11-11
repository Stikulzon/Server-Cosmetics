package com.zefir.servercosmetics.gui.actions;

import com.zefir.servercosmetics.data.CustomItemEntry;
import com.zefir.servercosmetics.gui.core.IItemAction;
import com.zefir.servercosmetics.util.Utils;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.zefir.servercosmetics.datafixer.NbtDatafixer.NEW_NBT_KEY_CUSTOM_ITEM_ID;

public class ApplySkinAction implements IItemAction {
    private final ItemStack targetItemStack;
    private final int itemDisplaySlot;

    public ApplySkinAction(ItemStack targetItemStack, int itemDisplaySlot) {
        this.targetItemStack = targetItemStack;
        this.itemDisplaySlot = itemDisplaySlot;
    }

    @Override
    public void execute(ServerPlayerEntity player, CustomItemEntry entry, SimpleGui gui) {

        targetItemStack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, comp ->
                comp.apply(nbt -> nbt.putString(NEW_NBT_KEY_CUSTOM_ITEM_ID, entry.id()))
        );
        CustomModelDataComponent modelData = entry.itemStack().get(DataComponentTypes.CUSTOM_MODEL_DATA);
        if (modelData != null) {
            targetItemStack.set(DataComponentTypes.CUSTOM_MODEL_DATA, modelData);
            Utils.applyModelOverride(targetItemStack, modelData.value());
        } else {
            targetItemStack.remove(DataComponentTypes.CUSTOM_MODEL_DATA);
            Utils.clearModelOverride(targetItemStack);
        }

        if (entry.itemStack().getItem() instanceof ArmorItem armorItem && armorItem.getType() != ArmorItem.Type.BODY) {
            // TODO: Rewrite the whole itemskins thing
        }

        gui.setSlot(itemDisplaySlot, targetItemStack.copy());
    }
}
