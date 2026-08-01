package ua.zefir.servercosmetics.gui.paperdoll;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import java.util.ArrayList;
import java.util.List;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import ua.zefir.servercosmetics.config.CosmeticsGuiConfig;
import ua.zefir.servercosmetics.cosmetic.CosmeticHolder;
import ua.zefir.servercosmetics.data.CustomItemEntry;
import ua.zefir.servercosmetics.data.CustomItemRegistry;
import ua.zefir.servercosmetics.data.EquipmentSlotConfig;
import ua.zefir.servercosmetics.database.DatabaseManager;
import ua.zefir.servercosmetics.datafixer.NbtDataFixer;

public class PresetSlotHandler {

  public static String savePreset(
      ServerPlayer player, int presetIndex, List<EquipmentSlotConfig> equipmentSlots) {
    CosmeticHolder holder = (CosmeticHolder) player;
    List<String> cosmeticIds = new ArrayList<>(equipmentSlots.size());
    for (EquipmentSlotConfig slot : equipmentSlots) {
      ItemStack equipped = holder.getCosmeticFor(slot.type()).getCosmeticItemStack();
      String id = "";
      if (equipped != null && !equipped.isEmpty()) {
        CustomData customData = equipped.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
          id = customData.copyTag().getStringOr(NbtDataFixer.NEW_NBT_KEY_CUSTOM_ITEM_ID, "");
        }
      }
      cosmeticIds.add(id);
    }
    String presetData = PresetCodec.encode(cosmeticIds);
    DatabaseManager.savePreset(player, presetIndex, presetData);
    return presetData;
  }

  public static void loadPreset(
      ServerPlayer player, int presetIndex, List<EquipmentSlotConfig> equipmentSlots) {
    String presetData = DatabaseManager.loadPreset(player, presetIndex);
    if (presetData == null || presetData.isEmpty()) {
      return;
    }
    List<String> ids = PresetCodec.decode(presetData);
    CosmeticHolder holder = (CosmeticHolder) player;
    for (int i = 0; i < equipmentSlots.size() && i < ids.size(); i++) {
      String cosmeticId = ids.get(i).trim();
      EquipmentSlotConfig slot = equipmentSlots.get(i);
      if (cosmeticId.isEmpty()) {
        holder.getCosmeticFor(slot.type()).equip(ItemStack.EMPTY, slot.type());
        continue;
      }
      CustomItemEntry entry = CustomItemRegistry.getCosmetic(cosmeticId);
      if (entry == null) {
        continue;
      }
      if (!Permissions.check(player, entry.permission(), 4)) {
        holder.getCosmeticFor(slot.type()).equip(ItemStack.EMPTY, slot.type());
        continue;
      }
      new ua.zefir.servercosmetics.gui.actions.EquipCosmeticAction()
          .execute(player, entry.itemStack().copy(), slot.type());
    }
  }

  public static ItemStack getPresetDisplayItem(String presetData) {
    List<String> ids = PresetCodec.decode(presetData);
    for (String id : ids) {
      if (!id.trim().isEmpty()) {
        CustomItemEntry entry = CustomItemRegistry.getCosmetic(id.trim());
        if (entry != null) {
          return entry.itemStack().copy();
        }
      }
    }
    return new ItemStack(Items.PAPER);
  }

  public static void appendPresetLore(
      GuiElementBuilder element,
      String presetData,
      List<EquipmentSlotConfig> equipmentSlots,
      CosmeticsGuiConfig config) {
    List<String> ids = PresetCodec.decode(presetData);
    for (int i = 0; i < equipmentSlots.size() && i < ids.size(); i++) {
      String cosmeticId = ids.get(i).trim();
      String slotName = equipmentSlots.get(i).displayName();
      if (cosmeticId.isEmpty()) {
        element.addLoreLine(config.getMessagePresetSlotNone(slotName));
      } else {
        CustomItemEntry entry = CustomItemRegistry.getCosmetic(cosmeticId);
        if (entry != null) {
          element.addLoreLine(
              config.getMessagePresetSlotCosmetic(slotName, entry.displayName().getString()));
        } else {
          element.addLoreLine(config.getMessagePresetSlotUnknown(slotName));
        }
      }
    }
  }
}
