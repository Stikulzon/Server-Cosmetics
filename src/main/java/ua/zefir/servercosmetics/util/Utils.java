package ua.zefir.servercosmetics.util;

import static ua.zefir.servercosmetics.datafixer.NbtDataFixer.NEW_NBT_KEY_CUSTOM_ITEM_ID;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.equipment.Equippable;
import ua.zefir.servercosmetics.ModInit;
import ua.zefir.servercosmetics.data.CustomItemEntry;
import ua.zefir.servercosmetics.data.CustomItemRegistry;
import ua.zefir.servercosmetics.data.ItemType;
import ua.zefir.servercosmetics.datafixer.NbtDataFixer;
import ua.zefir.servercosmetics.gui.ColorPickerComponent;
import ua.zefir.servercosmetics.gui.actions.EquipCosmeticAction;

public class Utils {
  public static Component formatDisplayName(String st) {
    StringBuilder sb = new StringBuilder(st.length());
    // fixing unicodes
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
    String sf = sb.toString().replace("&", "§");

    return TextParser.format(sf);
  }

  public static int wearCosmeticById(CommandContext<CommandSourceStack> context) {
    final ServerPlayer player;
    try {
      player = EntityArgument.getPlayer(context, "player");
    } catch (CommandSyntaxException e) {
      context.getSource().sendFailure(Component.literal("Invalid player specified."));
      return 1;
    }

    String id = StringArgumentType.getString(context, "cosmeticId");
    if (id == null || id.isEmpty()) {
      context.getSource().sendFailure(Component.literal("Invalid cosmetic ID."));
      return 1;
    }

    CustomItemEntry entry = CustomItemRegistry.getCosmetic(id);

    if (entry == null) {
      context
          .getSource()
          .sendSuccess(() -> Component.literal("Cosmetic not found with ID: " + id), false);
      return 1;
    }

    if (!Permissions.check(player, entry.permission(), 4)) {
      context
          .getSource()
          .sendSuccess(
              () ->
                  Component.literal(
                      "Selected player does not have permission to use this cosmetic."),
              false);
      return 1;
    }

    ItemStack cosmeticItem = entry.itemStack();
    if (cosmeticItem == null || cosmeticItem.isEmpty()) {
      context
          .getSource()
          .sendFailure(Component.literal("Cosmetic item definition is empty or invalid."));
      return 1;
    }

    boolean isColorable =
        Items.LEATHER_HORSE_ARMOR.equals(
            BuiltInRegistries.ITEM.get(Identifier.tryParse(entry.baseItemForModel())));

    if (isColorable) {
      ItemStack itemForColorPicker = cosmeticItem.copy();
      itemForColorPicker.remove(DataComponents.DYED_COLOR);

      new ColorPickerComponent(
              player,
              itemForColorPicker,
              entry.type(),
              (coloredStack) -> {
                new EquipCosmeticAction().execute(player, coloredStack, entry.type());
                context
                    .getSource()
                    .sendSuccess(
                        () ->
                            Component.literal(
                                "Equipped colored cosmetic: " + entry.displayName().getString()),
                        false);
              },
              null)
          .open();

    } else {
      new EquipCosmeticAction().execute(player, entry.itemStack(), entry.type());
      context
          .getSource()
          .sendSuccess(
              () -> Component.literal("Equipped cosmetic: " + entry.displayName().getString()),
              false);
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

  public static int getSlotForType(ItemType type) {
    return switch (type) {
      case HAT -> 5;
      case CHESTPLATE, CHESTPLATE_BODY_COSMETIC -> 6;
      case LEGGINGS, LEGGINGS_BODY_COSMETIC -> 7;
      case BOOTS, BOOTS_BODY_COSMETIC -> 8;
      default ->
          throw new IllegalStateException("Unsupported item type for slot calculation: " + type);
    };
  }

  public static ItemStack getTiltedItemStack(ItemStack original, Identifier modelPath) {
    ItemStack itemStack = original.copy();
    itemStack.set(DataComponents.ITEM_MODEL, modelPath);
    return itemStack;
  }

  public static List<Path> listFiles(Path dir) {
    if (!Files.isDirectory(dir)) {
      ModInit.LOGGER.warn("Attempted to list files in a non-directory: {}", dir);
      return Collections.emptyList();
    }
    try (Stream<Path> walk = Files.walk(dir)) {
      return walk.filter(Files::isRegularFile).collect(Collectors.toList());
    } catch (IOException e) {
      ModInit.LOGGER.error("Failed to list files in directory: {}", dir, e);
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

  public static ItemStack filterItemStack(ItemStack originalStack, ServerPlayer player) {
    ItemStack stack = originalStack.copy();
    NbtDataFixer.fixItemStackNbt(stack);

    if (stack == null || stack.isEmpty()) {
      return stack;
    }

    CustomData customDataComponent = stack.get(DataComponents.CUSTOM_DATA);

    if (customDataComponent != null) {
      CompoundTag nbt = customDataComponent.copyTag();

      if (nbt.contains(NEW_NBT_KEY_CUSTOM_ITEM_ID)) {
        String itemSkinId = nbt.getStringOr(NEW_NBT_KEY_CUSTOM_ITEM_ID, "unknown");

        CustomItemEntry skinEntry = CustomItemRegistry.getCosmetic(itemSkinId);
        if (skinEntry == null) {
          skinEntry = CustomItemRegistry.getCosmetic(itemSkinId + "_" + stack.getItem());
        }

        if (skinEntry == null) {
          stack.update(
              DataComponents.CUSTOM_DATA,
              CustomData.EMPTY,
              comp -> comp.update(currentNbt -> currentNbt.remove(NEW_NBT_KEY_CUSTOM_ITEM_ID)));
          return stack;
        } else if (skinEntry.type() == ItemType.ITEM_SKIN) {
          if (player != null && !Permissions.check(player, skinEntry.permission(), 4)) {
            stack.update(
                DataComponents.CUSTOM_DATA,
                CustomData.EMPTY,
                comp -> comp.update(currentNbt -> currentNbt.remove(NEW_NBT_KEY_CUSTOM_ITEM_ID)));
            return stack;
          }
        }

        Identifier expectedItemModel = skinEntry.itemStack().get(DataComponents.ITEM_MODEL);
        stack.set(DataComponents.ITEM_MODEL, expectedItemModel);

        Equippable equippableComponent = skinEntry.itemStack().get(DataComponents.EQUIPPABLE);
        stack.set(DataComponents.EQUIPPABLE, equippableComponent);

        return stack;
      }
    }
    return originalStack;
  }

  public static ItemStack filterItemStack(ItemStack originalStack) {
    return filterItemStack(originalStack, null);
  }

  public static ItemStack reflectRealDurability(ItemStack displayStack, ItemStack realStack) {
    if (displayStack.isEmpty() || realStack.isEmpty()) {
      return displayStack;
    }

    int realDamage = realStack.getDamageValue();
    int realMaxDamage = realStack.getMaxDamage();

    if (realDamage > 0 && realMaxDamage > 0) {
      displayStack.set(DataComponents.DAMAGE, realDamage);
      displayStack.set(DataComponents.MAX_DAMAGE, realMaxDamage);
    }

    return displayStack;
  }
}
