package com.zefir.servercosmetics.util;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.zefir.servercosmetics.ServerCosmetics;
import com.zefir.servercosmetics.data.CustomItemEntry;
import com.zefir.servercosmetics.data.CustomItemRegistry;
import com.zefir.servercosmetics.data.ItemType;
import com.zefir.servercosmetics.datafixer.NbtDatafixer;
import com.zefir.servercosmetics.ext.IItemStack;
import com.zefir.servercosmetics.gui.ColorPickerComponent;
import com.zefir.servercosmetics.gui.actions.EquipCosmeticAction;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.zefir.servercosmetics.datafixer.NbtDatafixer.NEW_NBT_KEY_CUSTOM_ITEM_ID;

public class Utils {
    public static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder().hexColors().useUnusualXRepeatedCharacterHexFormat().build();
    public static final MiniMessage MINI_MESSAGE = MiniMessage.builder().tags(StandardTags.defaults()).build();
    private static final String MODEL_OVERRIDE_KEY = "servercosmeticsModelOverride";
    private static final String MODEL_BASE_KEY = "servercosmeticsModelBase";
    private static final String MODEL_BLOCKING_KEY = "servercosmeticsBlockingModel";

    public static Text formatDisplayName(String st) {
        StringBuilder sb = new StringBuilder(st.length());

        for (int i = 0; i < st.length(); i++) {
            char ch = st.charAt(i);
            if (ch == '\\' && i < st.length() - 1) {
                char nextChar = st.charAt(i + 1);
                if (nextChar == 'u') {
                    String hex = st.substring(i + 2, i + 6);
                    ch = (char) Integer.parseInt(hex, 16);
                    i += 5;
                }
            }
            sb.append(ch);
        }
        String sf = sb.toString().replace("§", "&");
        String formatted = LegacyComponentSerializer.legacySection().serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(SERIALIZER.serialize(MINI_MESSAGE.deserialize(sf))));

        return Text.of(formatted);
    }

    public static int wearCosmeticById(CommandContext<ServerCommandSource> context) {
        final ServerPlayerEntity player;
        try {
            player = EntityArgumentType.getPlayer(context, "player");
        } catch (CommandSyntaxException e) {
            context.getSource().sendError(Text.literal("Invalid player specified."));
            return 1;
        }

        String id = StringArgumentType.getString(context, "cosmeticId");
        if (id == null || id.isEmpty()) {
            context.getSource().sendError(Text.literal("Invalid cosmetic ID."));
            return 1;
        }

        CustomItemEntry entry = CustomItemRegistry.getCosmetic(id);

        if (entry == null) {
            context.getSource().sendFeedback(() -> Text.literal("Cosmetic not found with ID: " + id), false);
            return 1;
        }

        if (!Permissions.check(player, entry.permission(), 4)) {
            context.getSource().sendFeedback(() -> Text.literal("Selected player does not have permission to use this cosmetic."), false);
            return 1;
        }

        ItemStack cosmeticItem = entry.itemStack();
        if (cosmeticItem == null || cosmeticItem.isEmpty()) {
            context.getSource().sendError(Text.literal("Cosmetic item definition is empty or invalid."));
            return 1;
        }

        boolean isColorable = Items.LEATHER_HORSE_ARMOR.equals(
                Registries.ITEM.get(Identifier.tryParse(entry.baseItemForModel()))
        );

        if (isColorable) {
            ItemStack itemForColorPicker = cosmeticItem.copy();
            itemForColorPicker.remove(DataComponentTypes.DYED_COLOR);

            new ColorPickerComponent(player, itemForColorPicker, (coloredStack) -> {
                new EquipCosmeticAction().execute(player, coloredStack, entry.type());
                context.getSource().sendFeedback(() -> Text.literal("Equipped colored cosmetic: " + entry.displayName().getString()), false);
            }).open();

        } else {
            new EquipCosmeticAction().execute(player, entry.itemStack(), entry.type());
            context.getSource().sendFeedback(() -> Text.literal("Equipped cosmetic: " + entry.displayName().getString()), false);
        }
        return 0;
    }
    
    public static int getSlotForType(ItemType type) {
        return switch (type) {
            case HAT -> 5;
            case CHESTPLATE, CHESTPLATE_BODY_COSMETIC -> 6;
            case LEGGINGS, LEGGINGS_BODY_COSMETIC -> 7;
            case BOOTS, BOOTS_BODY_COSMETIC -> 8;
            default -> throw new IllegalStateException("Unsupported item type for slot calculation: " + type);
        };
    }

    public static ItemStack getTiltedItemStack(ItemStack original, PolymerModelData polymerModel){
        ItemStack itemStack = original.copy();
        ((IItemStack) (Object) itemStack).server_Cosmetics$setItem(polymerModel.item());
        initializeModelData(itemStack, polymerModel.value(), null);
        return itemStack;
    }

    public static List<Path> listFiles(Path dir) {
        if (!Files.isDirectory(dir)) {
            ServerCosmetics.LOGGER.warn("Attempted to list files in a non-directory: {}", dir);
            return Collections.emptyList();
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            return walk.filter(Files::isRegularFile).collect(Collectors.toList());
        } catch (IOException e) {
            ServerCosmetics.LOGGER.error("Failed to list files in directory: {}", dir, e);
            return Collections.emptyList();
        }
    }

    public static ItemType getItemTypeForSlot(int slot) {
        switch (slot) {
            case 5 -> {
                return ItemType.HAT;
            }
            case 6 -> {
                return ItemType.CHESTPLATE;
            }
            case 7 -> {
                return ItemType.LEGGINGS;
            }
            case 8 -> {
                return ItemType.BOOTS;
            }
            default -> {
                return null;
            }
        }
    }

    public static int getSortablePriority(int actualPriority) {
        return actualPriority == 0 ? Integer.MAX_VALUE : actualPriority;
    }

    public static ItemStack filterItemStack(ItemStack originalStack, ServerPlayerEntity player) {
        if (originalStack == null || originalStack.isEmpty()) {
            return originalStack;
        }

        boolean mutateOriginal = player != null;
        ItemStack workingStack = mutateOriginal ? originalStack : originalStack.copy();

        NbtDatafixer.fixItemStackNbt(workingStack);

        if (workingStack.isEmpty()) {
            return workingStack;
        }

        NbtComponent customDataComponent = workingStack.get(DataComponentTypes.CUSTOM_DATA);
        if (customDataComponent == null) {
            return workingStack;
        }

        NbtCompound nbt = customDataComponent.copyNbt();
        if (!nbt.contains(NEW_NBT_KEY_CUSTOM_ITEM_ID, NbtCompound.STRING_TYPE)) {
            return workingStack;
        }

        String itemSkinId = nbt.getString(NEW_NBT_KEY_CUSTOM_ITEM_ID);

        CustomItemEntry skinEntry = CustomItemRegistry.getCosmetic(itemSkinId);
        if (skinEntry == null) {
            Identifier itemId = Registries.ITEM.getId(workingStack.getItem());
            if (itemId != null) {
                skinEntry = CustomItemRegistry.getCosmetic(itemSkinId + "_" + itemId);
            }
        }

        if (skinEntry == null) {
            clearCosmeticIdentifiers(workingStack);
            return workingStack;
        }

        if (mutateOriginal) {
            NbtComponent definitionData = skinEntry.itemStack().get(DataComponentTypes.CUSTOM_DATA);
            if (definitionData != null) {
                NbtCompound definitionNbt = definitionData.copyNbt();
                if (!nbt.contains(MODEL_BASE_KEY, NbtCompound.INT_TYPE) && definitionNbt.contains(MODEL_BASE_KEY, NbtCompound.INT_TYPE)) {
                    int baseValue = definitionNbt.getInt(MODEL_BASE_KEY);
                    workingStack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT,
                            comp -> comp.apply(dataNbt -> dataNbt.putInt(MODEL_BASE_KEY, baseValue)));
                    nbt.putInt(MODEL_BASE_KEY, baseValue);
                }
                if (!nbt.contains(MODEL_BLOCKING_KEY, NbtCompound.INT_TYPE) && definitionNbt.contains(MODEL_BLOCKING_KEY, NbtCompound.INT_TYPE)) {
                    int blockingValue = definitionNbt.getInt(MODEL_BLOCKING_KEY);
                    workingStack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT,
                            comp -> comp.apply(dataNbt -> dataNbt.putInt(MODEL_BLOCKING_KEY, blockingValue)));
                    nbt.putInt(MODEL_BLOCKING_KEY, blockingValue);
                }
            }
        }

        int overrideModelData = readModelValue(nbt, MODEL_OVERRIDE_KEY);
        int baseModelData = readModelValue(nbt, MODEL_BASE_KEY);

        if (skinEntry.type() != ItemType.ITEM_SKIN) {
            CustomModelDataComponent expectedModelData = skinEntry.itemStack().get(DataComponentTypes.CUSTOM_MODEL_DATA);
            if (expectedModelData != null) {
                ensureBaseModelData(workingStack, expectedModelData.value());
                if (baseModelData == Integer.MIN_VALUE) {
                    baseModelData = expectedModelData.value();
                }
            }

            int desiredModel = overrideModelData != Integer.MIN_VALUE
                    ? overrideModelData
                    : (baseModelData != Integer.MIN_VALUE ? baseModelData
                    : (expectedModelData != null ? expectedModelData.value() : Integer.MIN_VALUE));

            if (desiredModel != Integer.MIN_VALUE) {
                setActiveModel(workingStack, desiredModel);
            } else {
                workingStack.remove(DataComponentTypes.CUSTOM_MODEL_DATA);
                clearModelOverride(workingStack);
            }
            return workingStack;
        }

        if (player != null && !Permissions.check(player, skinEntry.permission(), 4)) {
            clearCosmeticIdentifiers(workingStack);
            return workingStack;
        }

        CustomModelDataComponent expectedModelData = skinEntry.itemStack().get(DataComponentTypes.CUSTOM_MODEL_DATA);
        if (expectedModelData != null) {
            ensureBaseModelData(workingStack, expectedModelData.value());
            if (overrideModelData == Integer.MIN_VALUE) {
                setActiveModel(workingStack, expectedModelData.value());
            } else {
                workingStack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(overrideModelData));
            }
        } else {
            workingStack.remove(DataComponentTypes.CUSTOM_MODEL_DATA);
            clearModelOverride(workingStack);
        }

        return workingStack;
    }

    private static int readModelValue(NbtCompound nbt, String key) {
        return nbt.contains(key, NbtCompound.INT_TYPE) ? nbt.getInt(key) : Integer.MIN_VALUE;
    }

    public static void initializeModelData(ItemStack stack, int baseModelValue, @Nullable Integer blockingModelValue) {
        setActiveModel(stack, baseModelValue);
        stack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT,
                comp -> comp.apply(nbt -> {
                    nbt.putInt(MODEL_BASE_KEY, baseModelValue);
                    if (blockingModelValue != null) {
                        nbt.putInt(MODEL_BLOCKING_KEY, blockingModelValue);
                    } else {
                        nbt.remove(MODEL_BLOCKING_KEY);
                    }
                }));
    }

    public static void ensureBaseModelData(ItemStack stack, int baseModelValue) {
        stack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT,
                comp -> comp.apply(nbt -> {
                    if (!nbt.contains(MODEL_BASE_KEY, NbtCompound.INT_TYPE)) {
                        nbt.putInt(MODEL_BASE_KEY, baseModelValue);
                    }
                }));
    }

    public static void setActiveModel(ItemStack stack, int modelValue) {
        stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(modelValue));
        stack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT,
                comp -> comp.apply(nbt -> nbt.putInt(MODEL_OVERRIDE_KEY, modelValue)));
    }

    public static void clearModelOverride(ItemStack stack) {
        stack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT,
                comp -> comp.apply(nbt -> nbt.remove(MODEL_OVERRIDE_KEY)));
    }

    public static void clearModelOverride(ItemStack stack, boolean removeModelComponent) {
        clearModelOverride(stack);
        if (removeModelComponent) {
            stack.remove(DataComponentTypes.CUSTOM_MODEL_DATA);
        }
    }

    public static void clearCosmeticIdentifiers(ItemStack stack) {
        stack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT,
                comp -> comp.apply(nbt -> {
                    nbt.remove(NEW_NBT_KEY_CUSTOM_ITEM_ID);
                    nbt.remove(MODEL_OVERRIDE_KEY);
                    nbt.remove(MODEL_BASE_KEY);
                    nbt.remove(MODEL_BLOCKING_KEY);
                }));
        stack.remove(DataComponentTypes.CUSTOM_MODEL_DATA);
    }

    public static void copyModelData(ItemStack source, ItemStack target) {
        NbtComponent sourceData = source.get(DataComponentTypes.CUSTOM_DATA);
        if (sourceData == null) {
            clearModelOverride(target, true);
            target.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT,
                    comp -> comp.apply(nbt -> {
                        nbt.remove(MODEL_BASE_KEY);
                        nbt.remove(MODEL_BLOCKING_KEY);
                    }));
        } else {
            NbtCompound srcNbt = sourceData.copyNbt();
            target.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT,
                    comp -> comp.apply(nbt -> {
                        copyKey(srcNbt, nbt, MODEL_BASE_KEY);
                        copyKey(srcNbt, nbt, MODEL_BLOCKING_KEY);
                        copyKey(srcNbt, nbt, MODEL_OVERRIDE_KEY);
                    }));
        }

        CustomModelDataComponent sourceModel = source.get(DataComponentTypes.CUSTOM_MODEL_DATA);
        if (sourceModel != null) {
            target.set(DataComponentTypes.CUSTOM_MODEL_DATA, sourceModel);
        } else {
            target.remove(DataComponentTypes.CUSTOM_MODEL_DATA);
        }
    }

    private static void copyKey(NbtCompound src, NbtCompound dst, String key) {
        if (src.contains(key, NbtCompound.INT_TYPE)) {
            dst.putInt(key, src.getInt(key));
        } else {
            dst.remove(key);
        }
    }

    public static void updateShieldBlockingState(ServerPlayerEntity player) {
        boolean blocking = player.isBlocking();
        Hand activeHand = blocking ? player.getActiveHand() : null;

        boolean mainChanged = updateShieldStack(player.getMainHandStack(), blocking && activeHand == Hand.MAIN_HAND);
        boolean offChanged = updateShieldStack(player.getOffHandStack(), blocking && activeHand == Hand.OFF_HAND);

        if (mainChanged || offChanged) {
            player.playerScreenHandler.sendContentUpdates();

            List<Pair<EquipmentSlot, ItemStack>> updates = new ArrayList<>();
            if (mainChanged) {
                updates.add(new Pair<>(EquipmentSlot.MAINHAND, player.getMainHandStack().copy()));
            }
            if (offChanged) {
                updates.add(new Pair<>(EquipmentSlot.OFFHAND, player.getOffHandStack().copy()));
            }

            if (!updates.isEmpty()) {
                player.getServerWorld().getChunkManager().sendToNearbyPlayers(player,
                        new EntityEquipmentUpdateS2CPacket(player.getId(), updates));
            }
        }
    }

    private static boolean updateShieldStack(ItemStack stack, boolean useBlockingModel) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        NbtComponent data = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (data == null) {
            return false;
        }

        NbtCompound nbt = data.copyNbt();
        if (!nbt.contains(MODEL_BLOCKING_KEY, NbtCompound.INT_TYPE)) {
            return false;
        }

        int baseModel = readModelValue(nbt, MODEL_BASE_KEY);
        int blockingModel = nbt.getInt(MODEL_BLOCKING_KEY);
        int currentModel = readModelValue(nbt, MODEL_OVERRIDE_KEY);
        if (currentModel == Integer.MIN_VALUE) {
            currentModel = baseModel;
        }

        int desiredModel = useBlockingModel ? blockingModel : (baseModel != Integer.MIN_VALUE ? baseModel : blockingModel);

        if (currentModel == desiredModel) {
            return false;
        }

        setActiveModel(stack, desiredModel);
        return true;
    }

    public static ItemStack filterItemStack(ItemStack originalStack) {
        return filterItemStack(originalStack, null);
    }
}
