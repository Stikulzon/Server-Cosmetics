package com.zefir.servercosmetics.gui.providers;

import com.zefir.servercosmetics.config.ConfigManager;
import com.zefir.servercosmetics.config.ItemSkinsGUIConfig;
import com.zefir.servercosmetics.config.entries.CustomItemEntry;
import com.zefir.servercosmetics.config.entries.CustomItemRegistry;
import com.zefir.servercosmetics.config.entries.ItemType;
import com.zefir.servercosmetics.gui.core.ICosmeticProvider;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemSkinProvider implements ICosmeticProvider {
    private final Item targetItem;

    public ItemSkinProvider(Item targetItem) {
        this.targetItem = targetItem;
    }

    @Override
    public List<CustomItemEntry> getItems(ItemType type) {
        return CustomItemRegistry.getAllCosmeticsForMaterial(type, targetItem);
    }
}
