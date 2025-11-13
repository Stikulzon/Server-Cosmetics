package com.zefir.servercosmetics.mixin;

import com.zefir.servercosmetics.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.network.EntityTrackerEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

//    @Inject(
//            method = "createSpawnPacket",
//            at = @At(
//                    value = "HEAD"
//            ),
//            cancellable = true
//    )
//    void modifyItemStack (EntityTrackerEntry entityTrackerEntry, CallbackInfoReturnable<Packet<ClientPlayPacketListener>> cir) {
//        Entity entity = (Entity) (Object) this;
//        if(entity instanceof ItemEntity itemEntity) {
//            ItemEntity newItemEntity = itemEntity.copy();
//            newItemEntity.setStack(Utils.filterItemStack(itemEntity.getStack()));
//            cir.setReturnValue(new EntitySpawnS2CPacket(newItemEntity, entityTrackerEntry));
//        }
//    }
}
