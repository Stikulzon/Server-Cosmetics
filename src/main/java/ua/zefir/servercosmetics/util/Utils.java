package ua.zefir.servercosmetics.util;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ua.zefir.servercosmetics.ServerCosmetics;
import ua.zefir.servercosmetics.data.CustomItemEntry;
import ua.zefir.servercosmetics.data.CustomItemRegistry;
import ua.zefir.servercosmetics.data.ItemType;
import ua.zefir.servercosmetics.datafixer.NbtDatafixer;
import ua.zefir.servercosmetics.ext.IItemStack;
import ua.zefir.servercosmetics.gui.ColorPickerComponent;
import ua.zefir.servercosmetics.gui.actions.EquipCosmeticAction;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
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

import static ua.zefir.servercosmetics.datafixer.NbtDatafixer.NEW_NBT_KEY_CUSTOM_ITEM_ID;

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

    public static ItemType getRealEquipedItemType(ItemType type) {
        return switch (type) {
            case HELMET, HAT_BODY_COSMETIC -> ItemType.HAT;
            case CHESTPLATE_BODY_COSMETIC -> ItemType.CHESTPLATE;
            case LEGGINGS_BODY_COSMETIC -> ItemType.LEGGINGS;
            case BOOTS_BODY_COSMETIC -> ItemType.BOOTS;
            default -> type;
        };
    }

    public static ItemStack getTiltedItemStack(ItemStack original, PolymerModelData polymerModel){
        ItemStack itemStack = original.copy();
        ((IItemStack) (Object) itemStack).server_Cosmetics$setItem(polymerModel.item());
        itemStack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(polymerModel.value()));
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
        NbtDatafixer.fixItemStackNbt(originalStack);
        ItemStack stack = originalStack.copy();


        if (stack == null || stack.isEmpty()) {
            return stack;
        }

        NbtComponent customDataComponent = stack.get(DataComponentTypes.CUSTOM_DATA);

        if (customDataComponent != null) {
            NbtCompound nbt = customDataComponent.copyNbt();

            if (nbt.contains(NEW_NBT_KEY_CUSTOM_ITEM_ID, NbtCompound.STRING_TYPE)) {
                String itemSkinId = nbt.getString(NEW_NBT_KEY_CUSTOM_ITEM_ID);

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

                CustomModelDataComponent expectedModelData = skinEntry.itemStack().get(DataComponentTypes.CUSTOM_MODEL_DATA);
                stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, expectedModelData);

                return stack;
            }
        }
        return originalStack;
    }

    public static ItemStack filterItemStack(ItemStack originalStack) {
        return filterItemStack(originalStack, null);
    }
}
