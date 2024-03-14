package com.zefir.servercosmetics.mixin;

import com.zefir.servercosmetics.util.IEntityDataSaver;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityDataSaverMixin implements IEntityDataSaver {
    @Unique
    private NbtCompound persistentData;

    @Override
    public NbtCompound getPersistentData(){
        if(persistentData == null){
            this.persistentData = new NbtCompound();
        }
        return persistentData;
    }

    @Inject(method = "writeNbt", at = @At("HEAD"))
    protected void injectWriteMethod (NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir){
        if(persistentData != null){
            nbt.put("server_cosmetics.head_cosmetics", persistentData);
        }
    }

    @Inject(method = "readNbt", at = @At("HEAD"))
    protected void injectReadMethod (NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("server_cosmetics.head_cosmetics", 10)){
            persistentData = nbt.getCompound("server_cosmetics.head_cosmetics");
        }
    }
}
