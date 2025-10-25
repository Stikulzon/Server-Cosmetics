package com.zefir.servercosmetics.mixin;

import com.zefir.servercosmetics.ext.IItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.*;

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
