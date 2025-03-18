package com.zefir.servercosmetics.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

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

    public static void setHeadCosmetics(UUID playerUUID, ItemStack is) {
        try {
            CosmeticTable cosmetic = new CosmeticTable();
            cosmetic.setUuid(playerUUID.toString());

            if (is.isEmpty()) {
                cosmetic.setItemId(null);
                cosmetic.setCustomModelData(null);
                cosmetic.setDyedColorComponent(null);
                cosmetic.setDisplayName(null);
            } else {
                cosmetic.setItemId(is.getItem().toString());
                cosmetic.setCustomModelData(is.getOrDefault(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(0)).value());

                if (is.get(DataComponentTypes.DYED_COLOR) != null) {
                    cosmetic.setDyedColorComponent(Objects.requireNonNull(is.get(DataComponentTypes.DYED_COLOR)).rgb());
                } else {
                    cosmetic.setDyedColorComponent(null);
                }

                if (is.get(DataComponentTypes.CUSTOM_NAME) != null) {
                    cosmetic.setDisplayName(Objects.requireNonNull(is.get(DataComponentTypes.CUSTOM_NAME)).getString());
                } else {
                    cosmetic.setDisplayName(null);
                }
            }

            cosmeticDao.createOrUpdate(cosmetic);
        } catch (SQLException e) {
            throw new RuntimeException("Error saving cosmetics data", e);
        }
    }



    public static ItemStack getHeadCosmetics(UUID playerUUID) {
        try {
            CosmeticTable cosmetic = cosmeticDao.queryForId(playerUUID.toString());

            if (cosmetic == null || cosmetic.getItemId() == null) {
                return ItemStack.EMPTY;
            }

            ItemStack itemStack = new ItemStack(Registries.ITEM.get(Identifier.of(cosmetic.getItemId())));

            if (cosmetic.getCustomModelData() != null) {
                itemStack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(cosmetic.getCustomModelData()));
            }

            if (cosmetic.getDyedColorComponent() != null) {
                itemStack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(cosmetic.getDyedColorComponent(), true));
            }

            if (cosmetic.getDisplayName() != null) {
                itemStack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(cosmetic.getDisplayName()));
            }

            return itemStack;

        } catch (SQLException e) {
            throw new RuntimeException("Error loading cosmetics data", e);
        }
    }
}