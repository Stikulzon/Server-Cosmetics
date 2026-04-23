package ua.zefir.servercosmetics.gui.actions;

import static ua.zefir.servercosmetics.datafixer.NbtDataFixer.NEW_NBT_KEY_CUSTOM_ITEM_ID;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import ua.zefir.servercosmetics.data.CustomItemEntry;
import ua.zefir.servercosmetics.gui.core.ItemAction;

public class ApplySkinAction implements ItemAction {
  private final ItemStack targetItemStack;
  private final int itemDisplaySlot;

  public ApplySkinAction(ItemStack targetItemStack, int itemDisplaySlot) {
    this.targetItemStack = targetItemStack;
    this.itemDisplaySlot = itemDisplaySlot;
  }

  @Override
  public void execute(ServerPlayerEntity player, CustomItemEntry entry, SimpleGui gui) {

    targetItemStack.apply(
        DataComponentTypes.CUSTOM_DATA,
        NbtComponent.DEFAULT,
        comp -> comp.apply(nbt -> nbt.putString(NEW_NBT_KEY_CUSTOM_ITEM_ID, entry.id())));

    gui.setSlot(itemDisplaySlot, targetItemStack.copy());
  }
}
