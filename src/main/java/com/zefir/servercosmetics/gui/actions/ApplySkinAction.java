package com.zefir.servercosmetics.gui.actions;

import com.zefir.servercosmetics.config.entries.CustomItemEntry;
import com.zefir.servercosmetics.gui.core.IItemAction;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

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
                comp.apply(nbt -> nbt.putString("cosmeticItemId", entry.id()))
        );
        targetItemStack.set(DataComponentTypes.CUSTOM_MODEL_DATA, entry.itemStack().getOrDefault(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(0)));

        if (entry.itemStack().getItem() instanceof ArmorItem armorItem && armorItem.getType() != ArmorItem.Type.BODY) {
            // TODO: Rewrite the whole itemskins thing
        }

        gui.setSlot(itemDisplaySlot, targetItemStack.copy());
    }
}
