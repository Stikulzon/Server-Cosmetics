package ua.zefir.servercosmetics.gui;

import com.mojang.brigadier.context.CommandContext;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import ua.zefir.servercosmetics.ModInit;
import ua.zefir.servercosmetics.config.ConfigManager;
import ua.zefir.servercosmetics.config.CosmeticsGuiConfig;
import ua.zefir.servercosmetics.cosmetic.Cosmetic;
import ua.zefir.servercosmetics.cosmetic.CosmeticHolder;
import ua.zefir.servercosmetics.data.CustomItemEntry;
import ua.zefir.servercosmetics.data.CustomItemRegistry;
import ua.zefir.servercosmetics.data.EquipmentSlotConfig;
import ua.zefir.servercosmetics.data.ItemType;
import ua.zefir.servercosmetics.database.DatabaseManager;
import ua.zefir.servercosmetics.gui.actions.EquipCosmeticAction;
import ua.zefir.servercosmetics.gui.actions.OpenColorPickerAction;
import ua.zefir.servercosmetics.gui.paperdoll.EquipmentSlotRenderer;
import ua.zefir.servercosmetics.gui.paperdoll.PresetSlotHandler;
import ua.zefir.servercosmetics.util.Utils;

public class CosmeticsGui extends SimpleGui {

  public static int openGui(CommandContext<ServerCommandSource> ctx) {
    ServerPlayerEntity player = ctx.getSource().getPlayer();
    if (player == null) {
      ctx.getSource()
          .sendFeedback(() -> Text.literal("This command can only be run by a player."), false);
      return 1;
    }

    try {
      CosmeticsGui gui =
          new CosmeticsGui(player, (CosmeticsGuiConfig) ConfigManager.COSMETICS_GUI_CONFIG);
      gui.openAndPopulate();
    } catch (Exception e) {
      ctx.getSource()
          .sendError(
              Text.literal(
                  "An error occurred opening the Cosmetics GUI. See console for details."));
      ModInit.LOGGER.error(
          "Failed to open cosmetics GUI for player {}", player.getName().getString(), e);
    }
    return 0;
  }

  private final CosmeticsGuiConfig config;
  private final List<EquipmentSlotConfig> visibleEquipmentSlots;
  private EquipmentSlotConfig selectedSlot = null;
  private int currentPage = 0;

  public CosmeticsGui(ServerPlayerEntity player, CosmeticsGuiConfig config) {
    super(ScreenHandlerType.GENERIC_9X6, player, config.isReplaceInventory());
    this.config = config;
    this.visibleEquipmentSlots =
        config.getEquipmentSlots().stream().filter(EquipmentSlotConfig::visible).toList();
    setTitle(config.getGuiName());
  }

  public void openAndPopulate() {
    populateGui();
    open();
  }

  public void populateGui() {
    clearSlots();
    drawEquipmentSlots();
    drawPresetSlots();
    drawSelectedSlotIndicator();
    drawRemoveButton();
    drawUnequipAllButton();
    drawGridItems();
    drawNavigation();
    drawFiller();
  }

  private void clearSlots() {
    for (int i = 0; i < getSize(); i++) {
      clearSlot(i);
    }
  }

  private void drawEquipmentSlots() {
    CosmeticHolder holder = (CosmeticHolder) player;
    for (EquipmentSlotConfig slotConfig : visibleEquipmentSlots) {
      int slotIndex = slotConfig.slotIndex();
      ItemType type = slotConfig.type();
      Cosmetic cosmetic = holder.getCosmeticFor(type);
      ItemStack equippedStack = cosmetic.getCosmeticItemStack();

      ItemStack displayStack;
      if (equippedStack != null && !equippedStack.isEmpty()) {
        displayStack = equippedStack.copy();
      } else {
        displayStack = EquipmentSlotRenderer.getPlaceholderItem(type);
      }

      GuiElementBuilder builder = new GuiElementBuilder(displayStack);

      if (selectedSlot != null && selectedSlot.key().equals(slotConfig.key())) {
        builder.addLoreLine(config.getMessageSelected());
      }

      builder.setCallback(
          (clickIndex, clickType, actionType) -> {
            if (selectedSlot != null && selectedSlot.key().equals(slotConfig.key())) {
              selectedSlot = null;
            } else {
              selectedSlot = slotConfig;
            }
            currentPage = 0;
            populateGui();
          });

      setSlot(slotIndex, builder);
    }
  }

  private void drawPresetSlots() {
    int[] presetSlots = config.getPresetSlots();
    for (int i = 0; i < presetSlots.length; i++) {
      int slotIndex = presetSlots[i];
      int presetIndex = i;
      String presetData = DatabaseManager.loadPreset(player, presetIndex);

      if (presetData != null && !presetData.isEmpty()) {
        ItemStack presetItem = PresetSlotHandler.getPresetDisplayItem(presetData);
        presetItem.set(DataComponentTypes.ITEM_NAME, Text.literal("Preset " + (presetIndex + 1)));
        GuiElementBuilder builder =
            new GuiElementBuilder(presetItem)
                .addLoreLine(config.getMessagePresetLoad())
                .addLoreLine(config.getMessagePresetReset())
                .addLoreLine(config.getMessagePresetOverwrite());
        PresetSlotHandler.appendPresetLore(builder, presetData, config.getEquipmentSlots(), config);
        builder.setCallback(
            (clickIndex, clickType, actionType) -> {
              if (clickType.shift && clickType.isLeft) {
                DatabaseManager.deletePreset(player, presetIndex);
                populateGui();
              } else if (clickType.isRight) {
                PresetSlotHandler.savePreset(player, presetIndex, config.getEquipmentSlots());
                populateGui();
              } else {
                PresetSlotHandler.loadPreset(player, presetIndex, config.getEquipmentSlots());
                populateGui();
              }
            });
        setSlot(slotIndex, builder);
      } else {
        ItemStack emptyPresetItem = new ItemStack(Items.PAPER);
        emptyPresetItem.set(
            DataComponentTypes.ITEM_NAME, Text.literal("Preset " + (presetIndex + 1)));
        GuiElementBuilder builder =
            new GuiElementBuilder(emptyPresetItem).addLoreLine(config.getMessagePresetSave());
        builder.setCallback(
            (clickIndex, clickType, actionType) -> {
              if (clickType.isRight) {
                PresetSlotHandler.savePreset(player, presetIndex, config.getEquipmentSlots());
                populateGui();
              }
            });
        setSlot(slotIndex, builder);
      }
    }
  }

  private void drawSelectedSlotIndicator() {
    if (selectedSlot == null) {
      return;
    }
    int slotIndex = config.getSelectedSlotIndex();
    ItemStack indicatorStack = EquipmentSlotRenderer.getPlaceholderItem(selectedSlot.type());
    GuiElementBuilder builder =
        new GuiElementBuilder(indicatorStack)
            .setName(config.getMessageSelectedSlot(selectedSlot.displayName()));
    builder.setCallback(
        (clickIndex, clickType, actionType) -> {
          selectedSlot = null;
          currentPage = 0;
          populateGui();
        });
    setSlot(slotIndex, builder);
  }

  private void drawRemoveButton() {
    if (selectedSlot == null) {
      return;
    }
    int removeIndex = config.getRemoveButtonIndex();
    ItemStack removeItem = new ItemStack(Items.BARRIER);
    GuiElementBuilder builder =
        new GuiElementBuilder(removeItem)
            .setName(config.getMessageRemoveCosmetic())
            .addLoreLine(config.getMessageRemoveCosmeticLore(selectedSlot.displayName()));
    builder.setCallback(
        (clickIndex, clickType, actionType) -> {
          new EquipCosmeticAction().execute(player, ItemStack.EMPTY, selectedSlot.type());
          populateGui();
        });
    setSlot(removeIndex, builder);
  }

  private void drawUnequipAllButton() {
    int slotIndex = config.getUnequipAllButtonIndex();
    ItemStack clearItem = new ItemStack(Items.TNT);
    GuiElementBuilder builder =
        new GuiElementBuilder(clearItem)
            .setName(config.getMessageUnequipAll())
            .addLoreLine(config.getMessageUnequipAllLore());
    builder.setCallback(
        (clickIndex, clickType, actionType) -> {
          for (EquipmentSlotConfig slot : config.getEquipmentSlots()) {
            new EquipCosmeticAction().execute(player, ItemStack.EMPTY, slot.type());
          }
          populateGui();
        });
    setSlot(slotIndex, builder);
  }

  private List<CustomItemEntry> getFilteredItems() {
    if (selectedSlot == null) {
      return List.of();
    }
    List<ItemType> matchingTypes = selectedSlot.type().getMatchingTypes();
    Set<ItemType> typeSet = Set.copyOf(matchingTypes);
    return CustomItemRegistry.getCosmeticsList().stream()
        .filter(entry -> typeSet.contains(entry.type()))
        .filter(entry -> Permissions.check(player, entry.permission(), 4))
        .sorted(
            Comparator.comparing(
                (CustomItemEntry entry) -> Utils.getSortablePriority(entry.sortingPriority())))
        .collect(Collectors.toList());
  }

  private void drawGridItems() {
    int[] gridSlots = config.getGridSlots();
    if (selectedSlot == null) {
      ItemStack promptItem = new ItemStack(Items.PAPER);
      promptItem.set(DataComponentTypes.ITEM_NAME, config.getMessageSelectSlotPrompt());
      if (gridSlots.length > 0) {
        setSlot(gridSlots[0], new GuiElementBuilder(promptItem));
      }
      return;
    }

    List<CustomItemEntry> filteredItems = getFilteredItems();
    int itemsPerPage = gridSlots.length;
    int startIndex = currentPage * itemsPerPage;

    for (int i = 0; i < itemsPerPage; i++) {
      int itemIndex = startIndex + i;
      int slot = gridSlots[i];

      if (itemIndex < filteredItems.size()) {
        CustomItemEntry entry = filteredItems.get(itemIndex);
        ItemStack displayStack = entry.itemStack().copy();
        GuiElementBuilder element = new GuiElementBuilder(displayStack);
        element.addLoreLine(config.getMessageUnlocked());

        if (entry.dyeable()) {
          OpenColorPickerAction colorPickerAction = new OpenColorPickerAction(selectedSlot.type());
          element.setCallback(
              (clickIndex, clickType, actionType) -> {
                colorPickerAction.execute(player, entry, this);
              });
        } else {
          EquipCosmeticAction equipAction = new EquipCosmeticAction();
          element.setCallback(
              (clickIndex, clickType, actionType) -> {
                equipAction.execute(player, entry.itemStack().copy(), selectedSlot.type());
                populateGui();
              });
        }
        setSlot(slot, element);
      } else {
        clearSlot(slot);
      }
    }
  }

  private void drawNavigation() {
    int[] gridSlots = config.getGridSlots();
    int itemsPerPage = gridSlots.length;
    List<CustomItemEntry> filteredItems = getFilteredItems();
    int totalPages =
        itemsPerPage > 0 ? (filteredItems.size() + itemsPerPage - 1) / itemsPerPage : 1;

    int previousSlot = config.getPreviousPageSlot();
    int nextSlot = config.getNextPageSlot();

    if (currentPage > 0) {
      ItemStack prevItem = new ItemStack(Items.PAPER);
      GuiElementBuilder prevBuilder =
          new GuiElementBuilder(prevItem).setName(config.getMessagePreviousPage());
      prevBuilder.setCallback(
          (clickIndex, clickType, actionType) -> {
            currentPage--;
            populateGui();
          });
      setSlot(previousSlot, prevBuilder);
    } else {
      setSlot(previousSlot, new GuiElementBuilder(ItemStack.EMPTY));
    }

    if (currentPage < totalPages - 1) {
      ItemStack nextItem = new ItemStack(Items.PAPER);
      GuiElementBuilder nextBuilder =
          new GuiElementBuilder(nextItem).setName(config.getMessageNextPage());
      nextBuilder.setCallback(
          (clickIndex, clickType, actionType) -> {
            currentPage++;
            populateGui();
          });
      setSlot(nextSlot, nextBuilder);
    } else {
      setSlot(nextSlot, new GuiElementBuilder(ItemStack.EMPTY));
    }
  }

  private void drawFiller() {
    Set<Integer> occupiedSlots = getOccupiedSlots();
    ItemStack filler = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
    filler.set(DataComponentTypes.ITEM_NAME, Text.literal(""));
    for (int i = 0; i < getSize(); i++) {
      if (!occupiedSlots.contains(i) && getSlot(i) == null) {
        setSlot(i, new GuiElementBuilder(filler).setName(Text.empty()));
      }
    }
  }

  private Set<Integer> getOccupiedSlots() {
    Set<Integer> occupied = new java.util.HashSet<>();
    for (EquipmentSlotConfig slot : visibleEquipmentSlots) {
      occupied.add(slot.slotIndex());
    }
    for (int slot : config.getPresetSlots()) {
      occupied.add(slot);
    }
    if (selectedSlot != null) {
      occupied.add(config.getSelectedSlotIndex());
      occupied.add(config.getRemoveButtonIndex());
    }
    occupied.add(config.getUnequipAllButtonIndex());
    for (int slot : config.getGridSlots()) {
      occupied.add(slot);
    }
    occupied.add(config.getPreviousPageSlot());
    occupied.add(config.getNextPageSlot());
    return occupied;
  }
}
