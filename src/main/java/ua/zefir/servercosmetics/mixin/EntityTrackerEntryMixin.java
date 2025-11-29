package ua.zefir.servercosmetics.mixin;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import com.mojang.datafixers.util.Pair;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ua.zefir.servercosmetics.data.ItemType;
import ua.zefir.servercosmetics.ext.ICosmetic;
import ua.zefir.servercosmetics.ext.ICosmetics;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import ua.zefir.servercosmetics.util.ArmorCosmetic;

import java.util.ArrayList;
import java.util.List;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {
    @Shadow
    @Final
    private Entity entity;

    @ModifyVariable(
            method = "sendPackets",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/packet/s2c/play/EntityEquipmentUpdateS2CPacket;<init>(ILjava/util/List;)V"
            ),
            ordinal = 0
    )
    private List<Pair<EquipmentSlot, ItemStack>> modifyEquipmentOnSpawn(List<Pair<EquipmentSlot, ItemStack>> originalList) {
        if (!(this.entity instanceof ServerPlayerEntity trackedPlayer)) {
            return originalList;
        }

        ICosmetics cosmetics = (ICosmetics) trackedPlayer;
        if (cosmetics.getCosmeticsList().isEmpty()) {
            return originalList;
        }

        List<Pair<EquipmentSlot, ItemStack>> modifiedList = new ArrayList<>();

        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            ItemStack stackToSend = trackedPlayer.getEquippedStack(slot);

            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                try {
                    ItemType itemType = ArmorCosmetic.getItemTypeForSlot(slot);
                    ICosmetic cosmetic = cosmetics.getCosmeticFor(itemType);
                    ItemStack cosmeticStack = cosmetic.getCosmeticItemStack();

                    if (cosmeticStack != null && !cosmeticStack.isEmpty()) {
                        stackToSend = cosmeticStack;
                    }
                } catch (Exception ignored) {}
            }

            if (stackToSend != null && !stackToSend.isEmpty()) {
                modifiedList.add(Pair.of(slot, stackToSend.copy()));
            }
        }
        return modifiedList;
    }
}
