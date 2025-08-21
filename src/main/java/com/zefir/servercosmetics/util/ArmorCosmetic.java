package com.zefir.servercosmetics.util;

import com.zefir.servercosmetics.data.ArmorCosmeticsData;
import com.zefir.servercosmetics.data.BodyCosmeticsData;
import com.zefir.servercosmetics.data.ItemType;
import com.zefir.servercosmetics.database.DatabaseManager;
import com.zefir.servercosmetics.ext.ICosmetic;
import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Objects;

public class ArmorCosmetic implements ICosmetic {
    final ServerPlayerEntity player;
    public final ItemType itemType;
    @Getter
    private ICosmetic armorCosmetic;

    public ArmorCosmetic(ServerPlayerEntity player, ItemType itemType) {
        this.player = player;
        this.itemType = itemType;
    }

    public void init() {
        try {
            switch (Objects.requireNonNull(DatabaseManager.getCosmeticEntry(player, itemType)).cosmeticData()) {
                case BodyCosmeticsData _data -> armorCosmetic = new ArmorBodyCosmetic(player, itemType);
                case ArmorCosmeticsData _data -> armorCosmetic = new ArmorItemCosmetic(player, itemType);
                default ->
                        throw new IllegalStateException("Unexpected value: " + Objects.requireNonNull(DatabaseManager.getCosmeticEntry(player, itemType)).cosmeticData());
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        armorCosmetic.init();
    }

    @Override
    public void equip(ItemStack cosmeticStack) {
        armorCosmetic.equip(cosmeticStack);
    }

    @Override
    public void tick() {
        armorCosmetic.tick();
    }
}
