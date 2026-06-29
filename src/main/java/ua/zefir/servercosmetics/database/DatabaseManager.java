package ua.zefir.servercosmetics.database;

import static ua.zefir.servercosmetics.datafixer.NbtDataFixer.NEW_NBT_KEY_CUSTOM_ITEM_ID;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import ua.zefir.servercosmetics.ModInit;
import ua.zefir.servercosmetics.data.CustomItemEntry;
import ua.zefir.servercosmetics.data.CustomItemRegistry;
import ua.zefir.servercosmetics.data.ItemType;

public class DatabaseManager {
  private static final String DATABASE_URL = "jdbc:sqlite:cosmetics.db";
  private static final Dao<CosmeticEntry, Integer> cosmeticDao;
  private static final Dao<PresetEntry, Integer> presetsDao;

  static {
    try {
      Class.forName("org.sqlite.JDBC");
      ConnectionSource connectionSource = new JdbcConnectionSource(DATABASE_URL);
      TableUtils.createTableIfNotExists(connectionSource, CosmeticEntry.class);
      cosmeticDao = DaoManager.createDao(connectionSource, CosmeticEntry.class);
      TableUtils.createTableIfNotExists(connectionSource, PresetEntry.class);
      presetsDao = DaoManager.createDao(connectionSource, PresetEntry.class);
    } catch (Exception e) {
      ModInit.LOGGER.error("Failed to initialize database", e);
      throw new RuntimeException("Error initializing database", e);
    }
  }

  public static void init() {}

  public static void setCosmetic(ServerPlayerEntity player, ItemType type, ItemStack itemStack) {
    try {
      CosmeticEntry existingEntry = findEntry(player.getUuidAsString(), type);

      if (itemStack == null || itemStack.isEmpty()) {
        if (existingEntry != null) {
          cosmeticDao.delete(existingEntry);
        }
        return;
      }

      String cosmeticId = getCosmeticIdFromStack(itemStack);
      if (cosmeticId == null) {
        ModInit.LOGGER.warn(
            "Attempted to set a cosmetic with an ItemStack lacking a 'cosmeticItemId' for player {}",
            player.getUuidAsString());
        if (existingEntry != null) {
          cosmeticDao.delete(existingEntry);
        }
        return;
      }

      Integer dyedColor = getDyedColorFromStack(itemStack);

      if (existingEntry != null) {
        existingEntry.setCosmeticId(cosmeticId);
        existingEntry.setDyedColor(dyedColor);
        cosmeticDao.update(existingEntry);
      } else {
        CosmeticEntry newEntry = new CosmeticEntry();
        newEntry.setUuid(player.getUuidAsString());
        newEntry.setCosmeticType(type.toString());
        newEntry.setCosmeticId(cosmeticId);
        newEntry.setDyedColor(dyedColor);
        cosmeticDao.create(newEntry);
      }
    } catch (SQLException e) {
      ModInit.LOGGER.error(
          "Error saving cosmetic data for player {} and type {}",
          player.getUuidAsString(),
          type,
          e);
      throw new RuntimeException("Error saving cosmetic data", e);
    }
  }

  public static ItemStack getCosmeticItemStack(ServerPlayerEntity player, ItemType type) {
    try {
      CosmeticEntry cosmeticData = findEntry(player.getUuidAsString(), type);

      if (cosmeticData == null || cosmeticData.getCosmeticId() == null) {
        return ItemStack.EMPTY;
      }

      CustomItemEntry cosmeticDefinition =
          CustomItemRegistry.getCosmetic(cosmeticData.getCosmeticId());
      if (cosmeticDefinition == null) {
        ModInit.LOGGER.warn(
            "Player {} has cosmetic '{}' equipped, but it's no longer registered.",
            player.getName().getString(),
            cosmeticData.getCosmeticId());
        return ItemStack.EMPTY;
      }

      if (!Permissions.check(player, cosmeticDefinition.permission(), 4)) {
        return ItemStack.EMPTY;
      }

      return cosmeticDefinition.itemStack().copy();
    } catch (SQLException e) {
      ModInit.LOGGER.error(
          "Error loading cosmetic data for player {} and type {}",
          player.getUuidAsString(),
          type,
          e);
      throw new RuntimeException("Error loading cosmetic data", e);
    }
  }

  public static CustomItemEntry getCosmeticEntry(ServerPlayerEntity player, ItemType type) {
    try {
      CosmeticEntry cosmeticData = findEntry(player.getUuidAsString(), type);

      if (cosmeticData == null || cosmeticData.getCosmeticId() == null) {
        return null;
      }

      return CustomItemRegistry.getCosmetic(cosmeticData.getCosmeticId());
    } catch (SQLException e) {
      ModInit.LOGGER.error(
          "Error loading cosmetic data for player {} and type {}",
          player.getUuidAsString(),
          type,
          e);
      throw new RuntimeException("Error loading cosmetic data", e);
    }
  }

  private static CosmeticEntry findEntry(String playerUUID, ItemType type) throws SQLException {
    Map<String, Object> queryFields = new HashMap<>();
    queryFields.put("uuid", playerUUID);
    queryFields.put("cosmetic_type", type.toString());
    return cosmeticDao.queryForFieldValues(queryFields).stream().findFirst().orElse(null);
  }

  private static String getCosmeticIdFromStack(ItemStack stack) {
    NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
    if (customData != null) {
      NbtCompound nbt = customData.copyNbt();
      if (nbt.contains(NEW_NBT_KEY_CUSTOM_ITEM_ID)) {
        return nbt.getString(NEW_NBT_KEY_CUSTOM_ITEM_ID, "unknown");
      }
    }
    return null;
  }

  private static Integer getDyedColorFromStack(ItemStack stack) {
    DyedColorComponent dyedColor = stack.get(DataComponentTypes.DYED_COLOR);
    return dyedColor != null ? dyedColor.rgb() : null;
  }

  public static void savePreset(ServerPlayerEntity player, int slot, String cosmeticIds) {
    try {
      PresetEntry existing = findPresetEntry(player.getUuidAsString(), slot);
      if (existing != null) {
        existing.setCosmeticIds(cosmeticIds);
        presetsDao.update(existing);
      } else {
        PresetEntry entry = new PresetEntry();
        entry.setUuid(player.getUuidAsString());
        entry.setSlot(slot);
        entry.setCosmeticIds(cosmeticIds);
        presetsDao.create(entry);
      }
    } catch (SQLException e) {
      ModInit.LOGGER.error(
          "Error saving preset for player {} slot {}", player.getUuidAsString(), slot, e);
    }
  }

  public static String loadPreset(ServerPlayerEntity player, int slot) {
    try {
      PresetEntry entry = findPresetEntry(player.getUuidAsString(), slot);
      return entry != null ? entry.getCosmeticIds() : null;
    } catch (SQLException e) {
      ModInit.LOGGER.error(
          "Error loading preset for player {} slot {}", player.getUuidAsString(), slot, e);
      return null;
    }
  }

  public static void deletePreset(ServerPlayerEntity player, int slot) {
    try {
      PresetEntry entry = findPresetEntry(player.getUuidAsString(), slot);
      if (entry != null) {
        presetsDao.delete(entry);
      }
    } catch (SQLException e) {
      ModInit.LOGGER.error(
          "Error deleting preset for player {} slot {}", player.getUuidAsString(), slot, e);
    }
  }

  private static PresetEntry findPresetEntry(String playerUUID, int slot) throws SQLException {
    Map<String, Object> queryFields = new HashMap<>();
    queryFields.put("uuid", playerUUID);
    queryFields.put("slot", slot);
    return presetsDao.queryForFieldValues(queryFields).stream().findFirst().orElse(null);
  }
}
