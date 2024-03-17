package com.zefir.servercosmetics.mixin;

import com.zefir.servercosmetics.CosmeticsData;
import com.zefir.servercosmetics.ext.CosmeticSlotExt;
import com.zefir.servercosmetics.util.IEntityDataSaver;
import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(
            method = "onPlayerConnect",
            at = @At( value = "TAIL" )
    )
    void modifyHeadSlotItem (ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        ScreenHandler handler = player.currentScreenHandler;
        ItemStack itemStack = CosmeticsData.getHeadCosmetics((IEntityDataSaver) player);
        ((CosmeticSlotExt) handler).setHeadCosmetics(itemStack);

        player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(handler.syncId, handler.nextRevision(), 5, itemStack));
    }
}
