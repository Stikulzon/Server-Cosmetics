package com.zefir.servercosmetics.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.mojang.authlib.GameProfile;
import com.zefir.servercosmetics.ServerCosmetics;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Objects;
import java.util.UUID;

import static com.zefir.servercosmetics.config.CosmeticsGUIConfig.*;

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
                cosmetic.setName(null);
                cosmetic.setDyedColorComponent(null);
            } else {
                NbtCompound copiedCustomData = is.getComponents().get(DataComponentTypes.CUSTOM_DATA).copyNbt();
                if(!copiedCustomData.contains("itemSkinsID")){
                    throw new NullPointerException("cosmeticsID is missing in cosmetic " + is);
                }
                cosmetic.setName(copiedCustomData.getString("itemSkinsID"));

                if (is.get(DataComponentTypes.DYED_COLOR) != null) {
                    cosmetic.setDyedColorComponent(Objects.requireNonNull(is.get(DataComponentTypes.DYED_COLOR)).rgb());
                } else {
                    cosmetic.setDyedColorComponent(null);
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

            if (cosmetic == null) {
                return ItemStack.EMPTY;
            }

            ItemStack itemStack = getItemStackFromCosmeticsNameWithPermissionCheck(cosmetic.getName(), ServerCosmetics.SERVER.getPlayerManager().getPlayer(playerUUID));

            if (cosmetic.getName() == null || itemStack == null) {
                return ItemStack.EMPTY;
            }

            if (cosmetic.getDyedColorComponent() != null) {
                itemStack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(cosmetic.getDyedColorComponent(), true));
            }

            return itemStack;

        } catch (SQLException e) {
            throw new RuntimeException("Error loading cosmetics data", e);
        }
    }
}