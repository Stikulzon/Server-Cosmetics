package com.zefir.servercosmetics.mixin;

import com.mojang.serialization.Codec;
import com.zefir.servercosmetics.ext.IItemStack;
import com.zefir.servercosmetics.util.Utils;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.core.api.item.PolymerItemUtils;
import eu.pb4.polymer.core.impl.PolymerImplUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.function.Supplier;

@Mixin(ItemStack.class)
public class ItemStackMixin implements IItemStack {
    @Final
    @Shadow
    @Mutable
    private Item item;
    @Unique
    public void server_Cosmetics$setItem(Item item){
        this.item = item;
    }
}
