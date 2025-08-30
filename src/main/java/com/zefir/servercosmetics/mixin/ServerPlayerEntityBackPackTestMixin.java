package com.zefir.servercosmetics.mixin;

import com.zefir.servercosmetics.data.ItemType;
import com.zefir.servercosmetics.ext.ICosmetic;
import com.zefir.servercosmetics.ext.ICosmetics;
import com.zefir.servercosmetics.util.ArmorBodyCosmetic;
import com.zefir.servercosmetics.util.BodyCosmetic;
import com.zefir.servercosmetics.util.ArmorItemCosmetic;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityBackPackTestMixin implements ICosmetics {
    @Unique
    private final List<ICosmetic> cosmeticsList = new ArrayList<>();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;


        cosmeticsList.addAll(List.of(
                new BodyCosmetic(player, ItemType.BODY_COSMETIC),
                new ArmorItemCosmetic(player, ItemType.HAT),
                new ArmorItemCosmetic(player, ItemType.CHESTPLATE),
                new ArmorItemCosmetic(player, ItemType.LEGGINGS),
                new ArmorItemCosmetic(player, ItemType.BOOTS),
                new ArmorBodyCosmetic(player, ItemType.HAT_BODY_COSMETIC),
                new ArmorBodyCosmetic(player, ItemType.CHESTPLATE_BODY_COSMETIC),
                new ArmorBodyCosmetic(player, ItemType.LEGGINGS_BODY_COSMETIC),
                new ArmorBodyCosmetic(player, ItemType.BOOTS_BODY_COSMETIC)
        ));
    }

    @Inject(method = "playerTick", at = @At("TAIL"))
    private void sendBackpackCosmeticPacket(CallbackInfo ci) {
        tickArmor();
    }

    @Override
    public void tickArmor(){
        cosmeticsList.forEach(ICosmetic::tick);
    }

    @Override
    public void initCosmetics() {
        cosmeticsList.forEach(ICosmetic::init);
    }

    @Override
    public ICosmetic getCosmeticFor(ItemType type) {
        for (ICosmetic cosmetic : cosmeticsList) {
            if(cosmetic.getItemType() == type) {
                return cosmetic;
            }
        }
        throw new IllegalArgumentException("No cosmetic found for type: " + type);
    }
}
