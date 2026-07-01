package ua.zefir.servercosmetics.gui;

import com.mojang.brigadier.context.CommandContext;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
import ua.zefir.servercosmetics.cosmetic.GuiStateHolder;
import ua.zefir.servercosmetics.data.CustomItemEntry;
import ua.zefir.servercosmetics.data.CustomItemRegistry;
import ua.zefir.servercosmetics.data.EquipmentSlotConfig;
import ua.zefir.servercosmetics.data.ItemType;
import ua.zefir.servercosmetics.data.SortMode;
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
  private final GuiStateHolder state;
  private EquipmentSlotConfig selectedSlot = null;
  private int currentPage = 0;
  private ItemType typeFilter = null;
  private SortMode sortMode = SortMode.DEFAULT;
  private boolean availableOnly = true;
  private Map<String, Long> recentCosmetics = Map.of();

  public CosmeticsGui(ServerPlayerEntity player, CosmeticsGuiConfig config) {
    super(ScreenHandlerType.GENERIC_9X6, player, config.isReplaceInventory());
    this.config = config;
    this.state = (GuiStateHolder) player;
    this.visibleEquipmentSlots =
        config.getEquipmentSlots().stream().filter(EquipmentSlotConfig::visible).toList();
    setTitle(config.getGuiName());
    loadState();
  }

  private void loadState() {
    String key = state.getGuiSelectedSlotKey();
    if (key != null) {
      this.selectedSlot =
          visibleEquipmentSlots.stream().filter(s -> s.key().equals(key)).findFirst().orElse(null);
    }
    this.currentPage = state.getGuiCurrentPage();
    this.sortMode = state.getGuiSortMode();
    this.typeFilter = state.getGuiTypeFilter();
    this.availableOnly = state.isGuiAvailableOnly();
    refreshRecentCosmetics();
  }

  private void refreshRecentCosmetics() {
    this.recentCosmetics = DatabaseManager.getRecentCosmetics(player);
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
    drawTypeFilterButton();
    drawAvailableFilterButton();
    drawSortByButton();
    drawGridItems();
    drawNavigation();
  }

  private void clearSlots() {
    for (int i = 0; i < getSize(); i++) {
      clearSlot(i);
    }
  }

  private void selectSlot(EquipmentSlotConfig slotConfig) {
    if (selectedSlot != null && selectedSlot.key().equals(slotConfig.key())) {
      selectedSlot = null;
    } else {
      selectedSlot = slotConfig;
      validateTypeFilter();
    }
    state.setGuiSelectedSlotKey(selectedSlot == null ? null : selectedSlot.key());
    currentPage = 0;
    state.setGuiCurrentPage(0);
  }

  private void validateTypeFilter() {
    if (typeFilter == null) {
      return;
    }
    if (selectedSlot != null && !selectedSlot.type().getMatchingTypes().contains(typeFilter)) {
      typeFilter = null;
      state.setGuiTypeFilter(null);
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
            selectSlot(slotConfig);
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
                refreshRecentCosmetics();
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
          state.setGuiSelectedSlotKey(null);
          currentPage = 0;
          state.setGuiCurrentPage(0);
          populateGui();
        });
    setSlot(slotIndex, builder);
  }

  private void drawRemoveButton() {
    if (selectedSlot == null) {
      return;
    }
    CosmeticHolder holder = (CosmeticHolder) player;
    ItemStack equipped = holder.getCosmeticFor(selectedSlot.type()).getCosmeticItemStack();
    if (equipped == null || equipped.isEmpty()) {
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

  private boolean hasAnyCosmeticEquipped() {
    CosmeticHolder holder = (CosmeticHolder) player;
    for (EquipmentSlotConfig slot : config.getEquipmentSlots()) {
      ItemStack equipped = holder.getCosmeticFor(slot.type()).getCosmeticItemStack();
      if (equipped != null && !equipped.isEmpty()) {
        return true;
      }
    }
    return false;
  }

  private void drawUnequipAllButton() {
    if (!hasAnyCosmeticEquipped()) {
      return;
    }
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

  private List<ItemType> getTypeFilterOptions() {
    if (selectedSlot == null) {
      return List.of();
    }
    return selectedSlot.type().getMatchingTypes().stream()
        .filter(t -> CustomItemRegistry.getCosmeticsList().stream().anyMatch(e -> e.type() == t))
        .toList();
  }

  private void drawTypeFilterButton() {
    if (selectedSlot == null) {
      return;
    }
    List<ItemType> options = getTypeFilterOptions();
    if (options.size() <= 1) {
      return;
    }
    int slotIndex = config.getTypeFilterButtonIndex();
    ItemStack item = new ItemStack(Items.HOPPER);
    Text name =
        typeFilter == null
            ? config.getMessageTypeFilterAll()
            : config.getMessageTypeFilterSpecific(config.getTypeDisplayName(typeFilter));
    GuiElementBuilder builder =
        new GuiElementBuilder(item).setName(name).addLoreLine(config.getMessageTypeFilterLore());
    builder.setCallback(
        (clickIndex, clickType, actionType) -> {
          List<ItemType> currentOptions = getTypeFilterOptions();
          int currentIdx = typeFilter == null ? -1 : currentOptions.indexOf(typeFilter);
          int nextIdx = (currentIdx + 1) % (currentOptions.size() + 1);
          typeFilter = nextIdx == currentOptions.size() ? null : currentOptions.get(nextIdx);
          state.setGuiTypeFilter(typeFilter);
          currentPage = 0;
          state.setGuiCurrentPage(0);
          populateGui();
        });
    setSlot(slotIndex, builder);
  }

  private void drawAvailableFilterButton() {
    if (selectedSlot == null || selectedSlot.type().getMatchingTypes().isEmpty()) {
      return;
    }
    int slotIndex = config.getAvailableFilterButtonIndex();
    ItemStack item = new ItemStack(Items.EMERALD);
    Text name =
        availableOnly
            ? config.getMessageAvailableOnlyEnabled()
            : config.getMessageAvailableOnlyDisabled();
    GuiElementBuilder builder = new GuiElementBuilder(item).setName(name);
    builder.setCallback(
        (clickIndex, clickType, actionType) -> {
          availableOnly = !availableOnly;
          state.setGuiAvailableOnly(availableOnly);
          currentPage = 0;
          state.setGuiCurrentPage(0);
          populateGui();
        });
    setSlot(slotIndex, builder);
  }

  private void drawSortByButton() {
    if (selectedSlot == null || getFilteredItems().isEmpty()) {
      return;
    }
    int slotIndex = config.getSortByButtonIndex();
    ItemStack item = new ItemStack(Items.NETHER_STAR);
    Text name =
        switch (sortMode) {
          case DEFAULT -> config.getMessageSortByDefault();
          case NAME -> config.getMessageSortByName();
          case RECENT -> config.getMessageSortByRecent();
        };
    GuiElementBuilder builder = new GuiElementBuilder(item).setName(name);
    builder.setCallback(
        (clickIndex, clickType, actionType) -> {
          sortMode = sortMode.next();
          state.setGuiSortMode(sortMode);
          populateGui();
        });
    setSlot(slotIndex, builder);
  }

  private List<CustomItemEntry> getFilteredItems() {
    if (selectedSlot == null) {
      return List.of();
    }
    Set<ItemType> typeSet = Set.copyOf(selectedSlot.type().getMatchingTypes());
    return CustomItemRegistry.getCosmeticsList().stream()
        .filter(entry -> typeFilter == null || entry.type() == typeFilter)
        .filter(entry -> typeSet.contains(entry.type()))
        .filter(entry -> !availableOnly || Permissions.check(player, entry.permission(), 4))
        .sorted(getSortComparator())
        .collect(Collectors.toList());
  }

  private Comparator<CustomItemEntry> getSortComparator() {
    return switch (sortMode) {
      case NAME ->
          Comparator.comparing((CustomItemEntry e) -> e.displayName().getString())
              .thenComparing(e -> Utils.getSortablePriority(e.sortingPriority()));
      case RECENT ->
          Comparator.comparingLong((CustomItemEntry e) -> recentCosmetics.getOrDefault(e.id(), 0L))
              .reversed()
              .thenComparing(e -> Utils.getSortablePriority(e.sortingPriority()));
      default ->
          Comparator.comparing(
              (CustomItemEntry e) -> Utils.getSortablePriority(e.sortingPriority()));
    };
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

        boolean owned = Permissions.check(player, entry.permission(), 4);
        element.addLoreLine(owned ? config.getMessageUnlocked() : config.getMessageLocked());

        if (entry.dyeable()) {
          OpenColorPickerAction colorPickerAction = new OpenColorPickerAction(selectedSlot.type());
          element.setCallback(
              (clickIndex, clickType, actionType) -> {
                colorPickerAction.execute(player, entry, this);
                refreshRecentCosmetics();
                populateGui();
              });
        } else {
          EquipCosmeticAction equipAction = new EquipCosmeticAction();
          element.setCallback(
              (clickIndex, clickType, actionType) -> {
                equipAction.execute(player, entry.itemStack().copy(), selectedSlot.type());
                refreshRecentCosmetics();
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
            state.setGuiCurrentPage(currentPage);
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
            state.setGuiCurrentPage(currentPage);
            populateGui();
          });
      setSlot(nextSlot, nextBuilder);
    } else {
      setSlot(nextSlot, new GuiElementBuilder(ItemStack.EMPTY));
    }
  }
}
