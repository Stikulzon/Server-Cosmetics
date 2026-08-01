package ua.zefir.servercosmetics.gui.actions;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import ua.zefir.servercosmetics.cosmetic.CosmeticHolder;
import ua.zefir.servercosmetics.data.CustomItemEntry;
import ua.zefir.servercosmetics.data.ItemType;
import ua.zefir.servercosmetics.database.DatabaseManager;
import ua.zefir.servercosmetics.gui.core.ItemAction;

public class EquipCosmeticAction implements ItemAction {
  @Override
  public void execute(ServerPlayer player, CustomItemEntry entry, SimpleGui gui) {
    execute(player, entry.itemStack(), entry.type());
  }

  public void execute(ServerPlayer player, ItemStack cosmeticStack, ItemType type) {
    ((CosmeticHolder) player).getCosmeticFor(type).equip(cosmeticStack, type);
    if (cosmeticStack != null && !cosmeticStack.isEmpty()) {
      String cosmeticId = DatabaseManager.getCosmeticIdFromStack(cosmeticStack);
      if (cosmeticId != null) {
        DatabaseManager.recordRecentCosmetic(player, cosmeticId);
      }
    }
  }
}
