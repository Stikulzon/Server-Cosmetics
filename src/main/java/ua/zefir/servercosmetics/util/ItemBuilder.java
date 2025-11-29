package ua.zefir.servercosmetics.util;

import ua.zefir.servercosmetics.ModInit;
import ua.zefir.servercosmetics.datagen.RuntimeModelManager;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static ua.zefir.servercosmetics.ModInit.id;

public class ItemBuilder {
    private ItemStack stack;

    private ItemBuilder(Item item) {
        this.stack = new ItemStack(item);
    }

    private ItemBuilder(ItemStack stack) {
        this.stack = stack;
    }

    public static ItemBuilder fromId(String materialId) {
        Item item = Registries.ITEM.get(Identifier.of(materialId));
        if (item == Items.AIR) {
            ModInit.LOGGER.warn("Invalid materialId '{}'. Defaulting to minecraft:paper.", materialId);
            item = Items.PAPER;
        }
        return new ItemBuilder(item);
    }

    public static ItemBuilder fromItem(Item item) {
        return new ItemBuilder(item);
    }

    public ItemBuilder name(Text name) {
        if (name != null) {
            stack.set(DataComponentTypes.CUSTOM_NAME, name);
        }
        return this;
    }

    public ItemBuilder lore(List<Text> lore) {
        if (lore != null && !lore.isEmpty()) {
            stack.set(DataComponentTypes.LORE, new LoreComponent(lore));
        } else {
            stack.set(DataComponentTypes.LORE, new LoreComponent(Collections.emptyList()));
        }
        return this;
    }

    public ItemBuilder dye(int color) {
        stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(color));
        return this;
    }

    public ItemBuilder customData(String key, String value) {
        stack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, comp ->
                comp.apply(nbt -> nbt.putString(key, value))
        );
        return this;
    }

    public ItemBuilder model(Identifier modelId, boolean registerRequest) {
        if (registerRequest) {
            try {
                RuntimeModelManager.requestItemModel(modelId.getPath(), false);
            } catch (Exception e) {
                ModInit.LOGGER.error("Failed to request item model '{}': {}", modelId, e.getMessage());
            }
        }
        stack.set(DataComponentTypes.ITEM_MODEL, modelId);
        return this;
    }

    public ItemBuilder applyCosmeticLogic(String cosmeticId, boolean dyable) {
        EquippableComponent equippable = stack.get(DataComponentTypes.EQUIPPABLE);

        if (dyable) {
            this.dye(16777215);
        }

        if (equippable != null && equippable.slot() != EquipmentSlot.BODY) {
            EquipmentSlot slot = equippable.slot();
            String armorId = cosmeticId.replace("_" + slot.getName().toLowerCase(), "");

            RuntimeModelManager.requestArmorModel(armorId, slot);

            this.stack = new ItemStack(getLeatherArmorFor(slot));

            Identifier itemModelId = id(cosmeticId);
            stack.set(DataComponentTypes.ITEM_MODEL, itemModelId);

            Identifier armorModelId = id(armorId);
            RegistryKey<EquipmentAsset> layers = RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, armorModelId);

            EquippableComponent currentLeatherComp = stack.get(DataComponentTypes.EQUIPPABLE);

            if (currentLeatherComp != null) {
                EquippableComponent newComp = new EquippableComponent(
                        currentLeatherComp.slot(),
                        currentLeatherComp.equipSound(),
                        Optional.of(layers), // The Magic Layer Key
                        currentLeatherComp.cameraOverlay(),
                        currentLeatherComp.allowedEntities(),
                        currentLeatherComp.dispensable(),
                        currentLeatherComp.swappable(),
                        currentLeatherComp.damageOnHurt(),
                        currentLeatherComp.equipOnInteract(),
                        currentLeatherComp.canBeSheared(),
                        currentLeatherComp.shearingSound()
                );
                stack.set(DataComponentTypes.EQUIPPABLE, newComp);
            }

        } else {
            try {
                RuntimeModelManager.requestItemModel(cosmeticId, dyable);
                stack.set(DataComponentTypes.ITEM_MODEL, id(cosmeticId));
            } catch (Exception e) {
                ModInit.LOGGER.error("Failed to request item model '{}': {}", cosmeticId, e.getMessage());
            }
        }

        return this;
    }

    private Item getLeatherArmorFor(EquipmentSlot type) {
        return switch (type) {
            case HEAD -> Items.LEATHER_HELMET;
            case CHEST -> Items.LEATHER_CHESTPLATE;
            case LEGS -> Items.LEATHER_LEGGINGS;
            case FEET -> Items.LEATHER_BOOTS;
            default -> Items.PAPER;
        };
    }

    public ItemStack build() {
        return stack;
    }
}