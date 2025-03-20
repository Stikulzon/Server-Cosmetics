package com.zefir.servercosmetics.util;

import com.zefir.servercosmetics.ServerCosmetics;
import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class SimpleCosmeticPolymerItem extends Item implements PolymerItem {
    private final PolymerModelData polymerModel;

    public SimpleCosmeticPolymerItem(String textureName) {
        super(new Settings());
        polymerModel = PolymerResourcePackUtils.requestModel(Items.IRON_SWORD, Identifier.of(ServerCosmetics.MOD_ID, "item/" + textureName));
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity serverPlayerEntity) {
        return polymerModel.item();
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return polymerModel.value();
    }
}
