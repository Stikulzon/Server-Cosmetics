package ua.zefir.servercosmetics.mixin;

import ua.zefir.servercosmetics.util.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {
    @Final
    @Shadow
    public PlayerEntity player;
    @ModifyVariable(
            method = "setStack",
            at = @At("HEAD"),
            argsOnly = true
    )
    public ItemStack injectedSetStack(ItemStack stack){
        if(player instanceof ServerPlayerEntity serverPlayerEntity) {
            Utils.filterItemStack(stack, serverPlayerEntity);
        }
        return stack;
    }

    @ModifyVariable(
            method = "insertStack(ILnet/minecraft/item/ItemStack;)Z",
            at = @At("HEAD"),
            argsOnly = true
    )
    public ItemStack injectedInsertStack(ItemStack stack) {
        if(player instanceof ServerPlayerEntity serverPlayerEntity) {
            Utils.filterItemStack(stack, serverPlayerEntity);
        }
        return stack;
    }
}
