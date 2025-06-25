package com.zefir.servercosmetics.mixin;

import com.zefir.servercosmetics.ext.ICosmetics;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.network.ServerPlayerEntity$1")
public class ArmorHeadSlotMixin {
    @Final
    @Shadow
    ServerPlayerEntity field_29182;
    @ModifyVariable(
            method = "updateSlot",
            at = @At("HEAD"),
            argsOnly = true
    )
    private ItemStack modifyHeadSlotItem(ItemStack stack, ScreenHandler handler, int slot) {
        if(slot == 5) {
            ((ICosmetics) field_29182).getHatCosmetic().tick();
        }
        return stack;
    }
    @Inject(
            method = "updateState",
            at = @At(
                    value = "TAIL"
            )
    )
    void modifyHeadSlotItem (ScreenHandler handler, DefaultedList<ItemStack> stacks, ItemStack cursorStack, int[] properties, CallbackInfo ci) {
        if(handler instanceof PlayerScreenHandler) {
            ((ICosmetics) field_29182).getHatCosmetic().tick();
        }
    }
}