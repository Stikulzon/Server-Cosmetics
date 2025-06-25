package com.zefir.servercosmetics.mixin;

import com.zefir.servercosmetics.config.entries.CustomItemEntry;
import com.zefir.servercosmetics.config.entries.CustomItemRegistry;
import com.zefir.servercosmetics.datafixer.NbtDatafixer;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {
    @Final
    @Shadow
    public PlayerEntity player;
    @ModifyVariable(
            method = "setStack",
            at = @At("HEAD"),
            argsOnly = true
    )
    public ItemStack injectedSetStack(ItemStack stack){
        return checkItemSkinPermission(stack);
    }

    @ModifyVariable(
            method = "insertStack(ILnet/minecraft/item/ItemStack;)Z",
            at = @At("HEAD"),
            argsOnly = true
    )
    public ItemStack injectedInsertStack(ItemStack stack) {
        return checkItemSkinPermission(stack);
    }

    @Unique
    public ItemStack checkItemSkinPermission(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return stack;
        }
        NbtDatafixer.fixItemStackNbt(stack);

        NbtComponent customDataComponent = stack.get(DataComponentTypes.CUSTOM_DATA);

        if (customDataComponent != null) {
            NbtCompound nbt = customDataComponent.copyNbt();
            if (nbt.contains("cosmeticItemId", NbtCompound.STRING_TYPE)) {
                String itemSkinId = nbt.getString("cosmeticItemId");

                CustomItemEntry skinEntry = CustomItemRegistry.getCosmetic(itemSkinId);

                if (skinEntry == null) {
                    stack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, comp -> comp.apply(currentNbt -> currentNbt.remove("cosmeticItemId")));
                    stack.remove(DataComponentTypes.CUSTOM_MODEL_DATA);
                    return stack;
                }

                if (!Permissions.check(this.player, skinEntry.permission())) {
                    stack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, comp -> comp.apply(currentNbt -> currentNbt.remove("cosmeticItemId")));
                    stack.remove(DataComponentTypes.CUSTOM_MODEL_DATA);
                    return stack;
                }

                ItemStack skinDefinitionStack = skinEntry.itemStack();
                CustomModelDataComponent expectedModelData = skinDefinitionStack.get(DataComponentTypes.CUSTOM_MODEL_DATA);
                CustomModelDataComponent currentModelData = stack.get(DataComponentTypes.CUSTOM_MODEL_DATA);

                if (expectedModelData != null) {
                    if (currentModelData == null || currentModelData.value() != expectedModelData.value()) {
                        stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, expectedModelData);
                    }
                } else {
                    stack.remove(DataComponentTypes.CUSTOM_MODEL_DATA);
                }

                return stack;
            }
        }
        return stack;
    }

}
