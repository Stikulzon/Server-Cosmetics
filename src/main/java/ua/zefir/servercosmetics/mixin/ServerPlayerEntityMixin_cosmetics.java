package ua.zefir.servercosmetics.mixin;

import ua.zefir.servercosmetics.data.ItemType;
import ua.zefir.servercosmetics.ext.ICosmetic;
import ua.zefir.servercosmetics.ext.ICosmetics;
import ua.zefir.servercosmetics.util.ArmorCosmetic;
import ua.zefir.servercosmetics.util.BodyCosmetic;
import ua.zefir.servercosmetics.util.Utils;
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

    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void onRespawn(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        this.initCosmetics();
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
    public ICosmetic getCosmeticFor(ItemType itemType) {

        for (ICosmetic cosmetic : cosmeticsList) {
            if (cosmetic.getItemType() == Utils.getRealEquipedItemType(itemType)) {
                return cosmetic;
            }
        }
        throw new IllegalArgumentException("No cosmetic found for the type: " + itemType);
    }
}