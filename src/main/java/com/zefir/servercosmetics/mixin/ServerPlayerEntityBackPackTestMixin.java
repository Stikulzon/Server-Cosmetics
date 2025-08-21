package com.zefir.servercosmetics.mixin;

import com.zefir.servercosmetics.data.ItemType;
import com.zefir.servercosmetics.ext.ICosmetics;
import com.zefir.servercosmetics.util.ArmorCosmetic;
import com.zefir.servercosmetics.util.BodyCosmetic;
import com.zefir.servercosmetics.util.HatCosmetic;
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
    private HatCosmetic hatCosmetic;
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
        hatCosmetic = new HatCosmetic(player);
        chestCosmetic = new ArmorCosmetic(player, ItemType.CHESTPLATE);
        leggingsCosmetic = new ArmorCosmetic(player, ItemType.LEGGINGS);
        bootsCosmetic = new ArmorCosmetic(player, ItemType.BOOTS);
    }

    @Inject(method = "playerTick", at = @At("TAIL"))
    private void sendBackpackCosmeticPacket(CallbackInfo ci) {
        bodyCosmetic.tick();
        chestCosmetic.tick();
        leggingsCosmetic.tick();
        bootsCosmetic.tick();
    }

    @Unique
    public BodyCosmetic getBodyCosmetics() {
        return bodyCosmetic;
    }
    @Unique
    public HatCosmetic getHatCosmetic() {
        return hatCosmetic;
    }
    @Unique
    public ArmorCosmetic getChestCosmetic() {
        return chestCosmetic;
    }
    @Unique
    public ArmorCosmetic getLeggingsCosmetic() {
        return leggingsCosmetic;
    }
    @Unique
    public ArmorCosmetic getBootsCosmetic() {
        return bootsCosmetic;
    }

}
