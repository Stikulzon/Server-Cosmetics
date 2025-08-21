package com.zefir.servercosmetics.mixin;

import com.zefir.servercosmetics.data.ItemType;
import com.zefir.servercosmetics.ext.ICosmetic;
import com.zefir.servercosmetics.ext.ICosmetics;
import com.zefir.servercosmetics.util.ArmorCosmetic;
import com.zefir.servercosmetics.util.BodyCosmetic;
import com.zefir.servercosmetics.util.ArmorItemCosmetic;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityBackPackTestMixin implements ICosmetics {
    @Unique
    private BodyCosmetic bodyCosmetic;
    @Unique
    private ArmorItemCosmetic hatCosmetic;
    @Unique
    private ArmorCosmetic chestCosmetic;
    @Unique
    private ArmorCosmetic leggingsCosmetic;
    @Unique
    private ArmorCosmetic bootsCosmetic;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        bodyCosmetic = new BodyCosmetic(player, ItemType.BODY_COSMETIC);
        hatCosmetic = new ArmorItemCosmetic(player, ItemType.HAT);
        chestCosmetic = new ArmorCosmetic(player, ItemType.CHESTPLATE);
        leggingsCosmetic = new ArmorCosmetic(player, ItemType.LEGGINGS);
        bootsCosmetic = new ArmorCosmetic(player, ItemType.BOOTS);
    }

    @Inject(method = "playerTick", at = @At("TAIL"))
    private void sendBackpackCosmeticPacket(CallbackInfo ci) {
        tickArmor();
    }

    @Override
    public void tickArmor(){
        bodyCosmetic.tick();
        chestCosmetic.tick();
        leggingsCosmetic.tick();
        bootsCosmetic.tick();
    }

    @Override
    public void initCosmetics() {
        hatCosmetic.init();
        bodyCosmetic.init();
        chestCosmetic.init();
        leggingsCosmetic.init();
        bootsCosmetic.init();
    }

    @Override
    public ICosmetic getCosmeticFor(ItemType type) {
        return switch (type) {
            case ItemType.HAT -> this.hatCosmetic;
            case ItemType.CHESTPLATE -> this.chestCosmetic;
            case ItemType.LEGGINGS -> this.leggingsCosmetic;
            case ItemType.BOOTS -> this.bootsCosmetic;
            case ItemType.BODY_COSMETIC -> this.bodyCosmetic;
            default -> throw new IllegalArgumentException("Invalid ItemType");
        };
    }
}
