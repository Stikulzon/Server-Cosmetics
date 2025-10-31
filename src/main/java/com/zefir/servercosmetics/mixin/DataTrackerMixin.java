package com.zefir.servercosmetics.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.zefir.servercosmetics.util.Utils;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(DataTracker.class)
public class DataTrackerMixin {

    @ModifyReturnValue(
            method = "getChangedEntries",
            at = @At(
                    value = "RETURN"
            )
    )
    private @Nullable <T> List<DataTracker.SerializedEntry<?>> onGetChangedEntries(@Nullable List<DataTracker.SerializedEntry<?>> original) {
        if(original == null) return original;
        List<DataTracker.SerializedEntry<?>> entries = new java.util.ArrayList<>(List.copyOf(original));

        for(DataTracker.SerializedEntry<?> entry : entries){
            if(entry.id() == ItemEntityMixin.getStackConstant().id() && entry.value() instanceof ItemStack itemStack){
                entries.remove(entry);
                entries.add(DataTracker.SerializedEntry.of(ItemEntityMixin.getStackConstant(), Utils.filterItemStack(itemStack)));
            }
        }

        return entries;
    }

    @ModifyReturnValue(
            method = "getDirtyEntries",
            at = @At(
                    value = "RETURN",
                    ordinal = 1
            )
    )
    private @Nullable <T> List<DataTracker.SerializedEntry<?>> onGetDirtyEntries(@Nullable List<DataTracker.SerializedEntry<?>> original) {
        if(original == null) return original;
        List<DataTracker.SerializedEntry<?>> entries = new java.util.ArrayList<>(List.copyOf(original));

        for(DataTracker.SerializedEntry<?> entry : entries){
            if(entry.id() == ItemEntityMixin.getStackConstant().id() && entry.value() instanceof ItemStack itemStack){
                entries.remove(entry);
                entries.add(DataTracker.SerializedEntry.of(ItemEntityMixin.getStackConstant(), Utils.filterItemStack(itemStack)));
            }
        }

        return entries;
    }
}
