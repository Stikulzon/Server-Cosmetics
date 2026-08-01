package ua.zefir.servercosmetics.database;

import static ua.zefir.servercosmetics.datafixer.NbtDataFixer.NEW_NBT_KEY_CUSTOM_ITEM_ID;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.DyedItemColor;
import ua.zefir.servercosmetics.ModInit;
import ua.zefir.servercosmetics.data.CustomItemEntry;
import ua.zefir.servercosmetics.data.CustomItemRegistry;
import ua.zefir.servercosmetics.data.ItemType;

public class DatabaseManager {
  private static final String DATABASE_URL = "jdbc:sqlite:cosmetics.db";
  private static final Dao<CosmeticEntry, Integer> cosmeticDao;
  private static final Dao<PresetEntry, Integer> presetsDao;
  private static final Dao<RecentCosmeticEntry, Integer> recentDao;

  static {
    try {
      Class.forName("org.sqlite.JDBC");
      ConnectionSource connectionSource = new JdbcConnectionSource(DATABASE_URL);
      TableUtils.createTableIfNotExists(connectionSource, CosmeticEntry.class);
      cosmeticDao = DaoManager.createDao(connectionSource, CosmeticEntry.class);
      TableUtils.createTableIfNotExists(connectionSource, PresetEntry.class);
      presetsDao = DaoManager.createDao(connectionSource, PresetEntry.class);
      TableUtils.createTableIfNotExists(connectionSource, RecentCosmeticEntry.class);
      recentDao = DaoManager.createDao(connectionSource, RecentCosmeticEntry.class);
    } catch (Exception e) {
      ModInit.LOGGER.error("Failed to initialize database", e);
      throw new RuntimeException("Error initializing database", e);
    }
  }

  public static void init() {}

  public static void setCosmetic(ServerPlayer player, ItemType type, ItemStack itemStack) {
    try {
      CosmeticEntry existingEntry = findEntry(player.getStringUUID(), type);

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
            player.getStringUUID());
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
        newEntry.setUuid(player.getStringUUID());
        newEntry.setCosmeticType(type.toString());
        newEntry.setCosmeticId(cosmeticId);
        newEntry.setDyedColor(dyedColor);
        cosmeticDao.create(newEntry);
      }
    } catch (SQLException e) {
      ModInit.LOGGER.error(
          "Error saving cosmetic data for player {} and type {}", player.getStringUUID(), type, e);
      throw new RuntimeException("Error saving cosmetic data", e);
    }
  }

  public static ItemStack getCosmeticItemStack(ServerPlayer player, ItemType type) {
    try {
      CosmeticEntry cosmeticData = findEntry(player.getStringUUID(), type);

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
          "Error loading cosmetic data for player {} and type {}", player.getStringUUID(), type, e);
      throw new RuntimeException("Error loading cosmetic data", e);
    }
  }

  public static CustomItemEntry getCosmeticEntry(ServerPlayer player, ItemType type) {
    try {
      CosmeticEntry cosmeticData = findEntry(player.getStringUUID(), type);

      if (cosmeticData == null || cosmeticData.getCosmeticId() == null) {
        return null;
      }

      return CustomItemRegistry.getCosmetic(cosmeticData.getCosmeticId());
    } catch (SQLException e) {
      ModInit.LOGGER.error(
          "Error loading cosmetic data for player {} and type {}", player.getStringUUID(), type, e);
      throw new RuntimeException("Error loading cosmetic data", e);
    }
  }

  private static CosmeticEntry findEntry(String playerUUID, ItemType type) throws SQLException {
    Map<String, Object> queryFields = new HashMap<>();
    queryFields.put("uuid", playerUUID);
    queryFields.put("cosmetic_type", type.toString());
    return cosmeticDao.queryForFieldValues(queryFields).stream().findFirst().orElse(null);
  }

  public static String getCosmeticIdFromStack(ItemStack stack) {
    CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
    if (customData != null) {
      CompoundTag nbt = customData.copyTag();
      if (nbt.contains(NEW_NBT_KEY_CUSTOM_ITEM_ID)) {
        return nbt.getStringOr(NEW_NBT_KEY_CUSTOM_ITEM_ID, "unknown");
      }
    }
    return null;
  }

  private static Integer getDyedColorFromStack(ItemStack stack) {
    DyedItemColor dyedColor = stack.get(DataComponents.DYED_COLOR);
    return dyedColor != null ? dyedColor.rgb() : null;
  }

  public static void savePreset(ServerPlayer player, int slot, String cosmeticIds) {
    try {
      PresetEntry existing = findPresetEntry(player.getStringUUID(), slot);
      if (existing != null) {
        existing.setCosmeticIds(cosmeticIds);
        presetsDao.update(existing);
      } else {
        PresetEntry entry = new PresetEntry();
        entry.setUuid(player.getStringUUID());
        entry.setSlot(slot);
        entry.setCosmeticIds(cosmeticIds);
        presetsDao.create(entry);
      }
    } catch (SQLException e) {
      ModInit.LOGGER.error(
          "Error saving preset for player {} slot {}", player.getStringUUID(), slot, e);
    }
  }

  public static String loadPreset(ServerPlayer player, int slot) {
    try {
      PresetEntry entry = findPresetEntry(player.getStringUUID(), slot);
      return entry != null ? entry.getCosmeticIds() : null;
    } catch (SQLException e) {
      ModInit.LOGGER.error(
          "Error loading preset for player {} slot {}", player.getStringUUID(), slot, e);
      return null;
    }
  }

  public static void deletePreset(ServerPlayer player, int slot) {
    try {
      PresetEntry entry = findPresetEntry(player.getStringUUID(), slot);
      if (entry != null) {
        presetsDao.delete(entry);
      }
    } catch (SQLException e) {
      ModInit.LOGGER.error(
          "Error deleting preset for player {} slot {}", player.getStringUUID(), slot, e);
    }
  }

  private static PresetEntry findPresetEntry(String playerUUID, int slot) throws SQLException {
    Map<String, Object> queryFields = new HashMap<>();
    queryFields.put("uuid", playerUUID);
    queryFields.put("slot", slot);
    return presetsDao.queryForFieldValues(queryFields).stream().findFirst().orElse(null);
  }

  public static void recordRecentCosmetic(ServerPlayer player, String cosmeticId) {
    if (cosmeticId == null || cosmeticId.isEmpty()) {
      return;
    }
    try {
      RecentCosmeticEntry existing = findRecentEntry(player.getStringUUID(), cosmeticId);
      long now = System.currentTimeMillis();
      if (existing != null) {
        existing.setLastUsed(now);
        recentDao.update(existing);
      } else {
        RecentCosmeticEntry entry = new RecentCosmeticEntry();
        entry.setUuid(player.getStringUUID());
        entry.setCosmeticId(cosmeticId);
        entry.setLastUsed(now);
        recentDao.create(entry);
      }
    } catch (SQLException e) {
      ModInit.LOGGER.error(
          "Error recording recent cosmetic for player {} id {}",
          player.getStringUUID(),
          cosmeticId,
          e);
    }
  }

  public static Map<String, Long> getRecentCosmetics(ServerPlayer player) {
    try {
      QueryBuilder<RecentCosmeticEntry, Integer> qb = recentDao.queryBuilder();
      qb.where().eq("uuid", player.getStringUUID());
      qb.orderBy("last_used", false);
      List<RecentCosmeticEntry> entries = recentDao.query(qb.prepare());
      Map<String, Long> recent = new LinkedHashMap<>();
      for (RecentCosmeticEntry e : entries) {
        recent.put(e.getCosmeticId(), e.getLastUsed());
      }
      return recent;
    } catch (SQLException e) {
      ModInit.LOGGER.error(
          "Error loading recent cosmetics for player {}", player.getStringUUID(), e);
      return Map.of();
    }
  }

  private static RecentCosmeticEntry findRecentEntry(String playerUUID, String cosmeticId)
      throws SQLException {
    Map<String, Object> queryFields = new HashMap<>();
    queryFields.put("uuid", playerUUID);
    queryFields.put("cosmetic_id", cosmeticId);
    return recentDao.queryForFieldValues(queryFields).stream().findFirst().orElse(null);
  }
}
