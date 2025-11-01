package com.zefir.servercosmetics.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.zefir.servercosmetics.util.Utils;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(DataTracker.class)
public class DataTrackerMixin {

    @ModifyReturnValue(
            method = "getChangedEntries",
            at = @At("RETURN")
    )
    private @Nullable List<DataTracker.SerializedEntry<?>> onGetChangedEntries(@Nullable List<DataTracker.SerializedEntry<?>> original) {
        return this.filterCosmeticItemStackEntries(original);
    }

    @ModifyReturnValue(
            method = "getDirtyEntries",
            at = @At(
                    value = "RETURN",
                    ordinal = 1
            )
    )
    private @Nullable <T> List<DataTracker.SerializedEntry<?>> onGetDirtyEntries(@Nullable List<DataTracker.SerializedEntry<?>> original) {
        return this.filterCosmeticItemStackEntries(original);
    }

    @Unique
    private @Nullable List<DataTracker.SerializedEntry<?>> filterCosmeticItemStackEntries(@Nullable List<DataTracker.SerializedEntry<?>> entries) {
        if (entries == null) {
            return null;
        }

        return entries.stream()
                .map(entry -> {
                    if (entry.id() == ItemEntityMixin.getStackConstant().id() && entry.value() instanceof ItemStack itemStack) {
                        return DataTracker.SerializedEntry.of(ItemEntityMixin.getStackConstant(), Utils.filterItemStack(itemStack));
                    }
                    return entry;
                })
                .collect(Collectors.toList());
    }
}
