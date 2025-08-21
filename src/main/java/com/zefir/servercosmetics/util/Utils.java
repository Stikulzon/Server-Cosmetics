package com.zefir.servercosmetics.util;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.zefir.servercosmetics.ServerCosmetics;
import com.zefir.servercosmetics.data.CustomItemEntry;
import com.zefir.servercosmetics.data.CustomItemRegistry;
import com.zefir.servercosmetics.data.ItemType;
import com.zefir.servercosmetics.datagen.RuntimeModelManager;
import com.zefir.servercosmetics.ext.IItemStack;
import com.zefir.servercosmetics.gui.ColorPickerComponent;
import com.zefir.servercosmetics.gui.actions.EquipCosmeticAction;
import eu.pb4.polymer.resourcepack.api.PolymerArmorModel;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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

import static com.zefir.servercosmetics.ServerCosmetics.id;

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
    
    public static int getSlotForType(ItemType type) {
        return switch (type) {
            case HAT -> 5;
            case CHESTPLATE -> 6;
            case LEGGINGS -> 7;
            case BOOTS -> 8;
            default -> throw new IllegalStateException("Unsupported item type for slot calculation: " + type);
        };
    }

    public static ItemStack createItemStack(String baseMaterialId, Text displayName, String cosmeticOrSkinId, List<Text> loreTexts) {
        Item baseItem = Registries.ITEM.get(Identifier.of(baseMaterialId));
        if (baseItem == Registries.ITEM.get(Registries.ITEM.getDefaultId()) && !baseMaterialId.equals(Registries.ITEM.getDefaultId().toString())) {
            ServerCosmetics.LOGGER.warn("Invalid baseMaterialId '{}' for item '{}'. Defaulting to minecraft:paper.", baseMaterialId, cosmeticOrSkinId);
            baseItem = Registries.ITEM.get(Identifier.of("minecraft:paper")); // Fallback
        }

        PolymerModelData polymerModel;
        try {
            if (baseItem instanceof ArmorItem armorItem && armorItem.getType() != ArmorItem.Type.BODY) {
                RuntimeModelManager.requestArmorModel(cosmeticOrSkinId, armorItem.getType());

                String modelIdPath = "item/armor/" + cosmeticOrSkinId + "_" + armorItem.getType().getName().toLowerCase();
                polymerModel = PolymerResourcePackUtils.requestModel(getItemFor(armorItem.getType()), id(modelIdPath));
            } else {
                polymerModel = PolymerResourcePackUtils.requestModel(baseItem, Identifier.of(ServerCosmetics.MOD_ID, "item/" + cosmeticOrSkinId));
            }
        } catch (Exception e) {
            ServerCosmetics.LOGGER.error("Failed to request model for item id '{}' with base item '{}': {}", cosmeticOrSkinId, baseMaterialId, e.getMessage());
            ItemStack errorStack = new ItemStack(baseItem);
            errorStack.set(DataComponentTypes.CUSTOM_NAME, Text.literal("Error: " + cosmeticOrSkinId));
            return errorStack;
        }


        ItemStack itemStack = new ItemStack(polymerModel.item());

        itemStack.apply(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT,
                comp -> comp.apply(
                        nbt -> nbt.putString("cosmeticItemId", cosmeticOrSkinId))
        );

        if (baseItem instanceof ArmorItem armorItem && armorItem.getType() != ArmorItem.Type.BODY) {
            PolymerArmorModel armorModel = PolymerResourcePackUtils.requestArmor(id(cosmeticOrSkinId));
            itemStack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(armorModel.color(), true));
        }

        if (loreTexts != null && !loreTexts.isEmpty()) {
            itemStack.set(DataComponentTypes.LORE, new LoreComponent(loreTexts));
        } else {
            itemStack.set(DataComponentTypes.LORE, new LoreComponent(Collections.emptyList()));
        }

        itemStack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(polymerModel.value()));
        itemStack.set(DataComponentTypes.CUSTOM_NAME, displayName);

        return itemStack;
    }

    public static ItemStack getTiltedItemStack(ItemStack original, PolymerModelData polymerModel){
        ItemStack itemStack = original.copy();
        ((IItemStack) (Object) itemStack).setItem(polymerModel.item());
        itemStack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(polymerModel.value()));
        return itemStack;
    }

    private static Item getItemFor(ArmorItem.Type type) {
        return switch (type) {
            case ArmorItem.Type.HELMET -> Items.LEATHER_HELMET;
            case ArmorItem.Type.CHESTPLATE -> Items.LEATHER_CHESTPLATE;
            case ArmorItem.Type.LEGGINGS -> Items.LEATHER_LEGGINGS;
            case ArmorItem.Type.BOOTS -> Items.LEATHER_BOOTS;
            default -> Items.STONE;
        };
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
}
