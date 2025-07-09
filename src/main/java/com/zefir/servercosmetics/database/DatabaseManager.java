package com.zefir.servercosmetics.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.zefir.servercosmetics.ServerCosmetics;
import com.zefir.servercosmetics.config.entries.CustomItemEntry;
import com.zefir.servercosmetics.config.entries.CustomItemRegistry;
import com.zefir.servercosmetics.config.entries.ItemType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DatabaseManager {
    private static final String DATABASE_URL = "jdbc:sqlite:cosmetics.db";
    private static final Dao<CosmeticTable, Integer> cosmeticDao;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            ConnectionSource connectionSource = new JdbcConnectionSource(DATABASE_URL);
            TableUtils.createTableIfNotExists(connectionSource, CosmeticTable.class);
            cosmeticDao = DaoManager.createDao(connectionSource, CosmeticTable.class);
        } catch (Exception e) {
            ServerCosmetics.LOGGER.error("Failed to initialize database", e);
            throw new RuntimeException("Error initializing database", e);
        }
    }

    public static void init() {}

    public static void setCosmetic(ServerPlayerEntity player, ItemType type, ItemStack itemStack) {
        try {
            CosmeticTable existingEntry = findEntry(player.getUuidAsString(), type);

            if (itemStack == null || itemStack.isEmpty()) {
                if (existingEntry != null) {
                    cosmeticDao.delete(existingEntry);
                }
                return;
            }

            String cosmeticId = getCosmeticIdFromStack(itemStack);
            if (cosmeticId == null) {
                ServerCosmetics.LOGGER.warn("Attempted to set a cosmetic with an ItemStack lacking a 'cosmeticItemId' for player {}", player.getUuidAsString());
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
                CosmeticTable newEntry = new CosmeticTable();
                newEntry.setUuid(player.getUuidAsString());
                newEntry.setCosmeticType(type.toString());
                newEntry.setCosmeticId(cosmeticId);
                newEntry.setDyedColor(dyedColor);
                cosmeticDao.create(newEntry);
            }
        } catch (SQLException e) {
            ServerCosmetics.LOGGER.error("Error saving cosmetic data for player {} and type {}", player.getUuidAsString(), type, e);
            throw new RuntimeException("Error saving cosmetic data", e);
        }
    }

    public static ItemStack getCosmetic(ServerPlayerEntity player, ItemType type) {
        try {
            CosmeticTable cosmeticData = findEntry(player.getUuidAsString(), type);

            if (cosmeticData == null || cosmeticData.getCosmeticId() == null) {
                return ItemStack.EMPTY;
            }

            CustomItemEntry cosmeticDefinition = CustomItemRegistry.getCosmetic(cosmeticData.getCosmeticId());
            if (cosmeticDefinition == null) {
                ServerCosmetics.LOGGER.warn("Player {} has cosmetic '{}' equipped, but it's no longer registered.", player.getName().getString(), cosmeticData.getCosmeticId());
                return ItemStack.EMPTY;
            }

            if (!Permissions.check(player, cosmeticDefinition.permission())) {
                return ItemStack.EMPTY;
            }

            ItemStack cosmeticStack = cosmeticDefinition.itemStack().copy();
            if (cosmeticData.getDyedColor() != null) {
                cosmeticStack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(cosmeticData.getDyedColor(), true));
            }
            return cosmeticStack;
        } catch (SQLException e) {
            ServerCosmetics.LOGGER.error("Error loading cosmetic data for player {} and type {}", player.getUuidAsString(), type, e);
            throw new RuntimeException("Error loading cosmetic data", e);
        }
    }

    private static CosmeticTable findEntry(String playerUUID, ItemType type) throws SQLException {
        Map<String, Object> queryFields = new HashMap<>();
        queryFields.put("uuid", playerUUID);
        queryFields.put("cosmetic_type", type.toString());
        return cosmeticDao.queryForFieldValues(queryFields).stream().findFirst().orElse(null);
    }

    private static String getCosmeticIdFromStack(ItemStack stack) {
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData != null) {
            NbtCompound nbt = customData.copyNbt();
            if (nbt.contains("cosmeticItemId", NbtCompound.STRING_TYPE)) {
                return nbt.getString("cosmeticItemId");
            }
        }
        return null;
    }

    private static Integer getDyedColorFromStack(ItemStack stack) {
        DyedColorComponent dyedColor = stack.get(DataComponentTypes.DYED_COLOR);
        return dyedColor != null ? dyedColor.rgb() : null;
    }
}