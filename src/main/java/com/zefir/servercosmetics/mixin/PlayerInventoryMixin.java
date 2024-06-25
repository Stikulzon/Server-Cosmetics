package com.zefir.servercosmetics.mixin;

import com.zefir.servercosmetics.config.ItemSkinsGUIConfig;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;

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
        return checkItemPermission(stack);
    }

    @ModifyVariable(
            method = "insertStack(ILnet/minecraft/item/ItemStack;)Z",
            at = @At("HEAD"),
            argsOnly = true
    )
    public ItemStack injectedInsertStack(ItemStack stack) {
        checkItemPermission(stack);
        return stack;
    }

    @Unique
    public ItemStack checkItemPermission(ItemStack stack) {
        ComponentMap components = stack.getComponents();
        NbtComponent customData = components.get(DataComponentTypes.CUSTOM_DATA);

        if (customData != null) {
            NbtCompound nbt = customData.copyNbt();
            if (nbt.contains("itemSkinsID")) {
                Map<Integer, AbstractMap.SimpleEntry<String, AbstractMap.SimpleEntry<String, ItemStack>>> ism = ItemSkinsGUIConfig.getItemSkinsItems(stack.getItem());
                if (ism != null) {
                    String itemSkinsID = nbt.getString("itemSkinsID");
                    for (int i = 0; i < ism.size(); i++) {
                        if (Objects.equals(ism.get(i).getKey(), itemSkinsID)) {
                            if (!Permissions.check(player, ism.get(i).getValue().getKey())) {
                                // Remove itemSkinsID and CustomModelData
                                nbt.remove("itemSkinsID");
                                nbt.remove("CustomModelData");

                                // Update the custom data component
                                stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
                            }
                            return stack;
                        }
                    }
                }
            }
        }
        return stack;
    }

}
