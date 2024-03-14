package com.zefir.servercosmetics.mixin;

import com.zefir.servercosmetics.ext.CosmeticSlotExt;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin implements CosmeticSlotExt {
    @Unique
    public ItemStack headCosmetics = ItemStack.EMPTY;
    public void setHeadCosmetics (ItemStack itemStack) {
        headCosmetics = itemStack;
    }
    public ItemStack getHeadCosmetics () {
        return headCosmetics;
    }
}
