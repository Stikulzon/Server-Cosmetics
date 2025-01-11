package com.zefir.servercosmetics.mixin;

import com.zefir.servercosmetics.ext.CosmeticSlotExt;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Setter
@Getter
@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin implements CosmeticSlotExt {
    @Unique
    public ItemStack headCosmetics = ItemStack.EMPTY;
}
