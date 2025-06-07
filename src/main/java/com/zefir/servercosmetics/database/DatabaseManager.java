package com.zefir.servercosmetics.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.zefir.servercosmetics.ServerCosmetics;
import com.zefir.servercosmetics.config.entries.CustomItemEntry;
import com.zefir.servercosmetics.config.entries.CustomItemRegistry;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

public class DatabaseManager {
    private static final String DATABASE_URL = "jdbc:sqlite:cosmetics.db";
    private static final Dao<CosmeticTable, String> cosmeticDao;

    static {
        try {
            ConnectionSource connectionSource = new JdbcConnectionSource(DATABASE_URL);
            TableUtils.createTableIfNotExists(connectionSource, CosmeticTable.class);
            cosmeticDao = DaoManager.createDao(connectionSource, CosmeticTable.class);
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing database", e);
        }
    }

    public static void init() {}

    public static void setHeadCosmetics(UUID playerUUID, ItemStack itemStack) {
        try {
            CosmeticTable cosmeticEntry = new CosmeticTable();
            cosmeticEntry.setUuid(playerUUID.toString());

            if (itemStack == null || itemStack.isEmpty()) {
                cosmeticEntry.setName(null);
                cosmeticEntry.setDyedColorComponent(null);
            } else {
                NbtComponent customDataComponent = itemStack.get(DataComponentTypes.CUSTOM_DATA);
                String cosmeticId = null;

                if (customDataComponent != null) {
                    NbtCompound nbt = customDataComponent.copyNbt();
                    if (nbt.contains("cosmeticItemId", NbtCompound.STRING_TYPE)) {
                        cosmeticId = nbt.getString("cosmeticItemId");
                    }
                }

                if (cosmeticId == null) {
                    ServerCosmetics.LOGGER.warn("Warning: ItemStack provided to setHeadCosmetics is not empty but lacks 'cosmeticItemId' NBT: {} for player {}", itemStack.toString(), playerUUID);
                    cosmeticEntry.setName(null);
                    cosmeticEntry.setDyedColorComponent(null);

                } else {
                    cosmeticEntry.setName(cosmeticId);

                    DyedColorComponent dyedColor = itemStack.get(DataComponentTypes.DYED_COLOR);
                    if (dyedColor != null) {
                        cosmeticEntry.setDyedColorComponent(dyedColor.rgb());
                    } else {
                        cosmeticEntry.setDyedColorComponent(null);
                    }
                }
            }

            if (cosmeticDao != null) {
                cosmeticDao.createOrUpdate(cosmeticEntry);
            } else {
                throw new IllegalStateException("Cosmetic DAO not initialized.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving cosmetics data for player " + playerUUID, e);
        }
    }



    public static ItemStack getHeadCosmetics(UUID playerUUID) {
        try {
            CosmeticTable cosmetic = cosmeticDao.queryForId(playerUUID.toString());

            if (cosmetic == null || cosmetic.getName() == null) {
                return ItemStack.EMPTY;
            }

            PlayerEntity player = Objects.requireNonNull(ServerCosmetics.SERVER.getPlayerManager().getPlayer(playerUUID));
            CustomItemEntry entry = CustomItemRegistry.getStandaloneCosmetic(cosmetic.getName());
            ItemStack cosmeticStack;

            if (entry != null && Permissions.check(player, entry.permission())) {
                cosmeticStack = entry.itemStack().copy();
            } else {
                return ItemStack.EMPTY;
            }

            if (cosmetic.getDyedColorComponent() != null) {
                cosmeticStack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(cosmetic.getDyedColorComponent(), true));
            }

            return cosmeticStack;

        } catch (SQLException e) {
            throw new RuntimeException("Error loading cosmetics data", e);
        }
    }
}