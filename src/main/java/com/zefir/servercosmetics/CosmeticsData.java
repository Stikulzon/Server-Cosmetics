package com.zefir.servercosmetics;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.sql.*;
import java.util.Objects;

public class CosmeticsData {

    private static final String DATABASE_URL = "jdbc:sqlite:cosmetics.db";
    private static Connection conn;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection(DATABASE_URL);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS cosmetics (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "item_id TEXT," +
                    "custom_model_data INTEGER," +
                    "dyed_color_component INTEGER," +
                    "display_name TEXT" +
                    ")");

        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found.");
        } catch (SQLException e) {
            throw new RuntimeException("Error initializing database", e);
        }
    }

    public static void init(){}

    public static void setHeadCosmetics(PlayerEntity player, ItemStack is) {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT OR REPLACE INTO cosmetics (uuid, item_id, custom_model_data, dyed_color_component, display_name) VALUES (?, ?, ?, ?, ?)")) {
            pstmt.setString(1, player.getUuid().toString());

            if (is.isEmpty()) {
                pstmt.setNull(2, Types.VARCHAR);
                pstmt.setNull(3, Types.INTEGER);
                pstmt.setNull(4, Types.INTEGER);
                pstmt.setNull(5, Types.VARCHAR);
            } else {
                pstmt.setString(2, is.getItem().toString());
                pstmt.setInt(3, Objects.requireNonNull(is.getNbt()).getInt("CustomModelData"));

                NbtCompound nbtDataDisplay = is.getOrCreateSubNbt("display");
                if (nbtDataDisplay.get("color") != null) {
                    pstmt.setInt(4, nbtDataDisplay.getInt("color"));
                } else {
                    pstmt.setNull(4, Types.INTEGER);
                }

                if (is.getName() != null) {
                    pstmt.setString(5, Objects.requireNonNull(is.getName()).getString());
                } else {
                    pstmt.setNull(5, Types.VARCHAR);
                }
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving cosmetics data", e);
        }
    }
    public static ItemStack getHeadCosmetics(PlayerEntity player) {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT item_id, custom_model_data, dyed_color_component, display_name FROM cosmetics WHERE uuid = ?")) {
            pstmt.setString(1, player.getUuid().toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String itemId = rs.getString("item_id");
                    if (itemId == null) {
                        return ItemStack.EMPTY;
                    }

                    ItemStack itemStack = new ItemStack(Registries.ITEM.get(new Identifier(itemId)));
                    NbtCompound itemStackNbtData = new NbtCompound();

                    int customModelData = rs.getInt("custom_model_data");
                    if (customModelData != 0) {
                        itemStackNbtData.putInt("CustomModelData", customModelData);
                    }

                    // Dyed color component
                    int dyedColor = rs.getInt("dyed_color_component");
                    NbtCompound nbtDataDisplay = itemStack.getOrCreateSubNbt("display");
                    if (!rs.wasNull()) {
                        nbtDataDisplay.putInt("color", dyedColor);
                    }

                    // Display name
                    String displayName = rs.getString("display_name");
                    if (displayName != null) {
                        nbtDataDisplay.putString("Name", displayName);
                    }

                    return itemStack;
                } else {
                    return ItemStack.EMPTY;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading cosmetics data", e);
        }
    }
}
