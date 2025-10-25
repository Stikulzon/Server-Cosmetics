package com.zefir.servercosmetics.mixin;

import com.zefir.servercosmetics.data.ItemType;
import com.zefir.servercosmetics.ext.ICosmetics;
import com.zefir.servercosmetics.util.Utils;
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
public class ServerPlayerEntity$1Mixin {
    @Final
    @Shadow
    ServerPlayerEntity field_29182;
    @ModifyVariable(
            method = "updateSlot",
            at = @At("HEAD"),
            argsOnly = true
    )
    private ItemStack modifyArmorItemStack(ItemStack stack, ScreenHandler handler, int slot) {
        ICosmetics cosmetics = (ICosmetics) field_29182;
        ItemType itemType = Utils.getItemTypeForSlot(slot);
        if(itemType != null) {
            cosmetics.getCosmeticFor(itemType).tick();
        }
        return Utils.filterItemStack(stack, field_29182);
    }
    @Inject(
            method = "updateState",
            at = @At(
                    value = "TAIL"
            )
    )
    void modifyArmorItemStack (ScreenHandler handler, DefaultedList<ItemStack> stacks, ItemStack cursorStack, int[] properties, CallbackInfo ci) {
        if(handler instanceof PlayerScreenHandler) {
            ICosmetics cosmetics = (ICosmetics) field_29182;
            cosmetics.tickArmor();
        }
    }

    @ModifyVariable(method = "updateState",
            at = @At(
                    value = "HEAD"
            ),
            argsOnly = true
    )
    private DefaultedList<ItemStack> injectFilterItems(DefaultedList<ItemStack> list) {
        list.replaceAll(stack -> Utils.filterItemStack(stack, field_29182));
        return list;
    }

    @ModifyVariable(method = "updateCursorStack",
            at = @At(
                    value = "HEAD"
            ),
            argsOnly = true
    )
    private ItemStack injectFilterItems1(ItemStack stack) {
        return Utils.filterItemStack(stack, field_29182);
    }

    @ModifyVariable(method = "updateState",
            at = @At(
                    value = "HEAD"
            ),
            argsOnly = true
    )
    private ItemStack injectFilterItems(ItemStack cursorStack) {
        return Utils.filterItemStack(cursorStack, field_29182);
    }
}