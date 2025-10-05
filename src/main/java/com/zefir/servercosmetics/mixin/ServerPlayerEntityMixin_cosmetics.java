package com.zefir.servercosmetics.mixin;

import com.zefir.servercosmetics.data.ItemType;
import com.zefir.servercosmetics.ext.ICosmetic;
import com.zefir.servercosmetics.ext.ICosmetics;
import com.zefir.servercosmetics.util.ArmorCosmetic;
import com.zefir.servercosmetics.util.BodyCosmetic;
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
public abstract class ServerPlayerEntityMixin_cosmetics implements ICosmetics {
    @Unique
    private final List<ICosmetic> cosmeticsList = new ArrayList<>();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        cosmeticsList.addAll(List.of(
                new BodyCosmetic(player, ItemType.BODY_COSMETIC),
                new ArmorCosmetic(player, ItemType.HAT),
                new ArmorCosmetic(player, ItemType.CHESTPLATE),
                new ArmorCosmetic(player, ItemType.LEGGINGS),
                new ArmorCosmetic(player, ItemType.BOOTS)
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
    public void removeCosmetics() {
        cosmeticsList.forEach(ICosmetic::onUnload);
    }

    @Override
    public List<ICosmetic> getCosmeticsList() {
        return cosmeticsList;
    }

    @Override
    public ICosmetic getCosmeticFor(ItemType type) {
        ItemType primaryType = switch (type) {
            case HELMET, HAT_BODY_COSMETIC -> ItemType.HAT;
            case CHESTPLATE_BODY_COSMETIC -> ItemType.CHESTPLATE;
            case LEGGINGS_BODY_COSMETIC -> ItemType.LEGGINGS;
            case BOOTS_BODY_COSMETIC -> ItemType.BOOTS;
            default -> type;
        };

        for (ICosmetic cosmetic : cosmeticsList) {
            if (cosmetic.getItemType() == primaryType) {
                return cosmetic;
            }
        }
        throw new IllegalArgumentException("No cosmetic found for type: " + type);
    }
}