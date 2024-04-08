package com.zefir.servercosmetics;


import com.zefir.servercosmetics.util.IEntityDataSaver;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.Objects;

public class CosmeticsData {
    public static void setHeadCosmetics(IEntityDataSaver player, ItemStack is){
        String cosmetics;
        NbtCompound nbt = player.getPersistentData();
        if(is != ItemStack.EMPTY) {
            cosmetics = is.getItem().toString() + "," + Objects.requireNonNull(is.getNbt()).getInt("CustomModelData");
        } else {
            cosmetics = is.getItem().toString();
        }
        nbt.putString("head_cosmetics", cosmetics);
    }
    public static ItemStack getHeadCosmetics(IEntityDataSaver player){
        NbtCompound nbt = player.getPersistentData();
        String headCosmeticsString = nbt.getString("head_cosmetics");
        if (Objects.equals(headCosmeticsString, "air") || headCosmeticsString == null) {
            return ItemStack.EMPTY;
        }

        String[] parts = headCosmeticsString.split(",", 2);

        String itemId = parts[0].toLowerCase();

        ItemStack itemStack = new ItemStack(Registries.ITEM.get(new Identifier("minecraft", itemId)));
        if(parts.length > 1) {
            int itemCMD = Integer.parseInt(parts[1]);
            NbtCompound itemStackNbtData = new NbtCompound();
            itemStackNbtData.putInt("CustomModelData", itemCMD);
            itemStack.setNbt(itemStackNbtData);
        }

        return itemStack;
    }
}
