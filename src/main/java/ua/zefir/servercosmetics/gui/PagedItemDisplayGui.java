package ua.zefir.servercosmetics.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import ua.zefir.servercosmetics.config.AbstractGuiConfig;
import ua.zefir.servercosmetics.config.ButtonConfig;
import ua.zefir.servercosmetics.data.CustomItemEntry;
import ua.zefir.servercosmetics.datagen.ui.GuiTextures;
import ua.zefir.servercosmetics.gui.actions.OpenColorPickerAction;
import ua.zefir.servercosmetics.gui.core.CosmeticProvider;
import ua.zefir.servercosmetics.gui.core.ItemAction;
import ua.zefir.servercosmetics.gui.filters.FilterManager;
import ua.zefir.servercosmetics.util.GuiUtils;
import ua.zefir.servercosmetics.util.Utils;

public class PagedItemDisplayGui extends SimpleGui {

  private CosmeticProvider provider;
  private ItemAction defaultClickAction;
  private final AbstractGuiConfig guiConfig;
  private int currentPage = 0;
  private final FilterManager filterManager;

  public PagedItemDisplayGui(
      ServerPlayer player,
      AbstractGuiConfig config,
      CosmeticProvider provider,
      ItemAction defaultClickAction) {
    super(config.getScreenHandlerType(), player, config.isReplaceInventory());
    this.guiConfig = config;
    this.provider = provider;
    this.defaultClickAction = defaultClickAction;
    this.filterManager = new FilterManager(this);
    setTitle(config.getGuiName());
    populateGui();
  }

  public void reinitialize(CosmeticProvider newProvider, ItemAction newAction) {
    this.provider = newProvider;
    this.defaultClickAction = newAction;
    this.currentPage = 0;
    this.populateGui();
  }

  public void populateGui() {
    List<CustomItemEntry> allItems = provider.getItems();

    Predicate<CustomItemEntry> combinedFilter = filterManager.getCombinedPredicate();
    Set<String> duplicates = new HashSet<>();

    List<CustomItemEntry> filteredItems =
        allItems.stream()
            .filter(combinedFilter)
            .filter(
                entry -> {
                  if (entry.id().endsWith(entry.baseItemForModel())) {
                    return duplicates.add(entry.id().replace("_" + entry.baseItemForModel(), ""));
                  }
                  return duplicates.add(entry.id());
                })
            .sorted(Comparator.comparing(CustomItemEntry::id))
            .collect(Collectors.toList());

    drawItems(filteredItems);
    setupNavigation(filteredItems.size());
    setupSearchButton();
    filterManager.drawFilterButtons();
  }

  private void drawItems(List<CustomItemEntry> itemsToDisplay) {
    int[] displaySlots = guiConfig.getDisplaySlots();
    int itemsPerPage = displaySlots.length;
    int startIndex = currentPage * itemsPerPage;

    itemsToDisplay.sort(
        Comparator.comparing(entry -> Utils.getSortablePriority(entry.sortingPriority())));

    for (int i = 0; i < itemsPerPage; i++) {
      int itemIndex = startIndex + i;
      int slot = displaySlots[i];

      if (itemIndex < itemsToDisplay.size()) {
        CustomItemEntry entry = itemsToDisplay.get(itemIndex);
        ItemStack displayStack = entry.itemStack().copy();

        GuiElementBuilder element = new GuiElementBuilder(displayStack);

        if (Permissions.check(player, entry.permission(), 4)) {
          element.addLoreLine(guiConfig.getMessageUnlocked());
          element.setCallback(() -> determineAction(entry).execute(player, entry, this));
        } else {
          element.addLoreLine(guiConfig.getMessageLocked());
        }
        setSlot(slot, element);
      } else {
        clearSlot(slot);
      }
    }

    if (itemsToDisplay.isEmpty()) {
      GuiUtils.setUpButton(
          this, guiConfig.getButtonConfig("noCosmeticsAvailable"), () -> {}, displaySlots[0]);
    }
  }

  private ItemAction determineAction(CustomItemEntry entry) {
    if (entry.dyeable()) {
      return new OpenColorPickerAction(entry.type());
    }
    return defaultClickAction;
  }

  private void setupNavigation(int totalFilteredItems) {
    int itemsPerPage = guiConfig.getDisplaySlots().length;

    // Next Button
    if ((currentPage + 1) * itemsPerPage < totalFilteredItems) {
      GuiUtils.setUpButton(
          this,
          guiConfig.getButtonConfig("next"),
          () -> {
            currentPage++;
            populateGui();
          });
    } else {
      this.setSlot(
          guiConfig.getButtonConfig("next").slotIndex(), new GuiElementBuilder(ItemStack.EMPTY));
    }

    // Previous Button
    if (currentPage - 1 >= 0) {
      GuiUtils.setUpButton(
          this,
          guiConfig.getButtonConfig("previous"),
          () -> {
            currentPage--;
            populateGui();
          });
    } else {
      this.setSlot(
          guiConfig.getButtonConfig("previous").slotIndex(),
          new GuiElementBuilder(ItemStack.EMPTY));
    }
  }

  public void onFilterStateChanged() {
    this.currentPage = 0;
    this.populateGui();
  }

  private void setupSearchButton() {
    ButtonConfig btnConfig = guiConfig.getButtonConfig("search");
    if (btnConfig == null) return;

    String currentTerm = filterManager.getSearchTerm();
    String displayTerm = (currentTerm == null || currentTerm.isEmpty()) ? "None" : currentTerm;

    List<String> dynamicLore =
        btnConfig.lore().stream().map(line -> line.replace("%search_term%", displayTerm)).toList();

    ButtonConfig dynamicBtn =
        new ButtonConfig(
            btnConfig.name(),
            btnConfig.baseItem(),
            btnConfig.modelPath(),
            btnConfig.slotIndex(),
            dynamicLore);

    //    GuiUtils.setUpButton(this, dynamicBtn, this::openSearchGui);
  }

  public void openSearchGui() {
    AnvilInputGui anvilGui =
        new AnvilInputGui(this.player, false) {
          @Override
          public void onManualClose() {
            confirmSearch(this.getInput());
          }
        };

    ButtonConfig btnConfig = guiConfig.getButtonConfig("search");
    anvilGui.setTitle(GuiTextures.SEARCH_MENU.apply(Component.literal("")));

    anvilGui.setDefaultInputValue(filterManager.getSearchTerm());

    anvilGui.setSlot(
        2,
        ItemStack.EMPTY,
        (index, type, action, gui) -> {
          if (type.isRight) {
            confirmSearch("");
          } else {
            String input = anvilGui.getInput();
            confirmSearch(input);
          }
        });

    anvilGui.open();
  }

  private void confirmSearch(String term) {
    this.filterManager.setSearchTerm(term);
    this.open();
    this.onFilterStateChanged();
  }

  public AbstractGuiConfig getGuiConfig() {
    return guiConfig;
  }

  public FilterManager getFilterManager() {
    return filterManager;
  }
}
