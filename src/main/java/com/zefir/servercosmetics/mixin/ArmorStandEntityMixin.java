package com.zefir.servercosmetics.mixin;

import com.zefir.servercosmetics.ext.ArmorStandAcess;
import net.minecraft.entity.decoration.ArmorStandEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ArmorStandEntity.class)
public abstract class ArmorStandEntityMixin implements ArmorStandAcess {
    @Shadow
    protected abstract void setMarker(boolean marker);
    @Override
    public void accessedSetMarker(boolean marker){
        setMarker(marker);
    };
}
