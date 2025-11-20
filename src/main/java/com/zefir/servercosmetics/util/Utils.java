package com.zefir.servercosmetics.util;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.zefir.servercosmetics.ServerCosmetics;
import com.zefir.servercosmetics.data.CustomItemEntry;
import com.zefir.servercosmetics.data.CustomItemRegistry;
import com.zefir.servercosmetics.data.ItemType;
import com.zefir.servercosmetics.datafixer.NbtDatafixer;
import com.zefir.servercosmetics.gui.ColorPickerComponent;
import com.zefir.servercosmetics.gui.actions.EquipCosmeticAction;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.zefir.servercosmetics.datafixer.NbtDatafixer.NEW_NBT_KEY_CUSTOM_ITEM_ID;

public class Utils {
    public static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder().hexColors().useUnusualXRepeatedCharacterHexFormat().build();
    public static final MiniMessage MINI_MESSAGE = MiniMessage.builder().tags(StandardTags.defaults()).build();

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
                Registries.ITEM.getEntry(Identifier.tryParse(entry.baseItemForModel()))
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

    public static ItemStack getTiltedItemStack(ItemStack original, Identifier modelPath){
        ItemStack itemStack = original.copy();
//        ((IItemStack) (Object) itemStack).server_Cosmetics$setItem(modelPath.item());
//        itemStack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(modelPath.value()));
        itemStack.set(DataComponentTypes.ITEM_MODEL, modelPath);
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
        ItemStack stack = originalStack.copy();
        NbtDatafixer.fixItemStackNbt(stack);

        if (stack == null || stack.isEmpty()) {
            return stack;
        }

        NbtComponent customDataComponent = stack.get(DataComponentTypes.CUSTOM_DATA);

        if (customDataComponent != null) {
            NbtCompound nbt = customDataComponent.copyNbt();

            if (nbt.contains(NEW_NBT_KEY_CUSTOM_ITEM_ID)) {
                String itemSkinId = nbt.getString(NEW_NBT_KEY_CUSTOM_ITEM_ID, "unknown");

                CustomItemEntry skinEntry = CustomItemRegistry.getCosmetic(itemSkinId);
                if (skinEntry == null) {
                    skinEntry = CustomItemRegistry.getCosmetic(itemSkinId + "_" + stack.getItem());
                }

                if (skinEntry == null) {
                    stack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, comp -> comp.apply(currentNbt -> currentNbt.remove(NEW_NBT_KEY_CUSTOM_ITEM_ID)));
                    return stack;
                } else if (skinEntry.type() == ItemType.ITEM_SKIN) {
                    if(player != null && !Permissions.check(player, skinEntry.permission(), 4)) {
                        originalStack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT, comp -> comp.apply(currentNbt -> currentNbt.remove(NEW_NBT_KEY_CUSTOM_ITEM_ID)));
                        return originalStack;
                    }
                }

                Identifier expectedItemModel = skinEntry.itemStack().get(DataComponentTypes.ITEM_MODEL);
                stack.set(DataComponentTypes.ITEM_MODEL, expectedItemModel);


                EquippableComponent equippableComponent = skinEntry.itemStack().get(DataComponentTypes.EQUIPPABLE);
                stack.set(DataComponentTypes.EQUIPPABLE, equippableComponent);

                return stack;
            }
        }
        return originalStack;
    }

    public static ItemStack filterItemStack(ItemStack originalStack) {
        return filterItemStack(originalStack, null);
    }
}
