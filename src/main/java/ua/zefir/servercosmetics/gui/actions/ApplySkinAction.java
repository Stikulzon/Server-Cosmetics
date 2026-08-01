package ua.zefir.servercosmetics.gui.actions;

import static ua.zefir.servercosmetics.datafixer.NbtDataFixer.NEW_NBT_KEY_CUSTOM_ITEM_ID;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
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
  public void execute(ServerPlayer player, CustomItemEntry entry, SimpleGui gui) {

    targetItemStack.update(
        DataComponents.CUSTOM_DATA,
        CustomData.EMPTY,
        comp -> comp.update(nbt -> nbt.putString(NEW_NBT_KEY_CUSTOM_ITEM_ID, entry.id())));

    gui.setSlot(itemDisplaySlot, targetItemStack.copy());
  }
}
