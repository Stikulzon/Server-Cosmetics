package ua.zefir.servercosmetics.gui.actions;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import ua.zefir.servercosmetics.cosmetic.CosmeticHolder;
import ua.zefir.servercosmetics.data.CustomItemEntry;
import ua.zefir.servercosmetics.data.ItemType;
import ua.zefir.servercosmetics.gui.core.ItemAction;

public class EquipCosmeticAction implements ItemAction {
  @Override
  public void execute(ServerPlayerEntity player, CustomItemEntry entry, SimpleGui gui) {
    execute(player, entry.itemStack(), entry.type());
  }

  public void execute(ServerPlayerEntity player, ItemStack cosmeticStack, ItemType type) {
    ((CosmeticHolder) player).getCosmeticFor(type).equip(cosmeticStack, type);
  }
}
