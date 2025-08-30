package com.zefir.servercosmetics.util;

import com.zefir.servercosmetics.data.ItemType;
import com.zefir.servercosmetics.data.Tags;
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
        if(DatabaseManager.getCosmeticEntry(player, itemType) == null) {
            return;
        }
        try {
            if(Objects.requireNonNull(DatabaseManager.getCosmeticEntry(player, itemType)).tags().contains(Tags.BODY_COSMETIC)){
                armorCosmetic = new ArmorBodyCosmetic(player, itemType);
            } else {
                armorCosmetic = new ArmorItemCosmetic(player, itemType);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            return;
        }
        armorCosmetic.init();
    }

    @Override
    public void equip(ItemStack cosmeticStack) {
        if(DatabaseManager.getCosmeticEntry(player, itemType) == null) {
            return;
        }
        try {
            if(Objects.requireNonNull(DatabaseManager.getCosmeticEntry(player, itemType)).tags().contains(Tags.BODY_COSMETIC)){
                armorCosmetic = new ArmorBodyCosmetic(player, itemType);
            } else {
                armorCosmetic = new ArmorItemCosmetic(player, itemType);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            return;
        }
        armorCosmetic.equip(cosmeticStack);
    }

    @Override
    public void tick() {
        if(armorCosmetic == null) {
            if(DatabaseManager.getCosmeticEntry(player, itemType) == null) {
                return;
            }
            try {
                if(Objects.requireNonNull(DatabaseManager.getCosmeticEntry(player, itemType)).tags().contains(Tags.BODY_COSMETIC)){
                    armorCosmetic = new ArmorBodyCosmetic(player, itemType);
                } else {
                    armorCosmetic = new ArmorItemCosmetic(player, itemType);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
                return;
            }
        }
        armorCosmetic.tick();
    }
}
