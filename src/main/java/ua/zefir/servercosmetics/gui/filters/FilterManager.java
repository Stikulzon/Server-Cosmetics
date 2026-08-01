package ua.zefir.servercosmetics.gui.filters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import ua.zefir.servercosmetics.config.ButtonConfig;
import ua.zefir.servercosmetics.config.FilterButtonPair;
import ua.zefir.servercosmetics.data.CustomItemEntry;
import ua.zefir.servercosmetics.data.ItemType;
import ua.zefir.servercosmetics.gui.PagedItemDisplayGui;
import ua.zefir.servercosmetics.util.GuiUtils;

public class FilterManager {
  private final PagedItemDisplayGui gui;
  private final Map<String, FilterRegistration> registeredFilters = new HashMap<>();
  private final Map<String, Boolean> activeStates = new HashMap<>();
  private List<ItemType> targetTypes;
  private String searchTerm = "";

  public record FilterRegistration(
      Predicate<CustomItemEntry> filter, FilterButtonPair buttons, boolean isHidden) {}

  public FilterManager(PagedItemDisplayGui gui) {
    this.gui = gui;
  }

  public void addFilter(
      String key,
      Predicate<CustomItemEntry> filter,
      FilterButtonPair buttons,
      boolean initiallyActive) {
    addFilter(key, filter, buttons, initiallyActive, false);
  }

  public void addFilter(
      String key,
      Predicate<CustomItemEntry> filter,
      FilterButtonPair buttons,
      boolean initiallyActive,
      boolean isHidden) {
    if (gui.getGuiConfig().getDisabledFilters() != null
        && gui.getGuiConfig().getDisabledFilters().contains(key)) {
      return;
    }
    registeredFilters.put(key, new FilterRegistration(filter, buttons, isHidden));
    activeStates.put(key, initiallyActive);
  }

  public FilterRegistration getFilter(String key) {
    return registeredFilters.get(key);
  }

  private void disableOtherItemTypeFilters(String key) {
    for (Map.Entry<String, FilterRegistration> entry : registeredFilters.entrySet()) {
      if (entry.getValue().filter() instanceof ItemTypeFilter && !entry.getKey().equals(key)) {
        activeStates.put(entry.getKey(), false);
      }
    }
  }

  public Predicate<CustomItemEntry> getCombinedPredicate() {
    Predicate<CustomItemEntry> basePredicate =
        entry ->
            registeredFilters.entrySet().stream()
                .filter(mapEntry -> activeStates.getOrDefault(mapEntry.getKey(), false))
                .allMatch(mapEntry -> mapEntry.getValue().filter().test(entry));

    if (searchTerm != null && !searchTerm.isEmpty()) {
      String lowerTerm = searchTerm.toLowerCase();
      basePredicate =
          basePredicate.and(
              entry ->
                  entry.itemStack().getHoverName().getString().toLowerCase().contains(lowerTerm));
    }

    return basePredicate;
  }

  public void drawFilterButtons() {
    for (var entry : registeredFilters.entrySet()) {
      String key = entry.getKey();
      FilterRegistration reg = entry.getValue();
      boolean isActive = activeStates.getOrDefault(key, false);

      if (!reg.isHidden && reg.buttons() != null) {
        ButtonConfig button = isActive ? reg.buttons().inactive() : reg.buttons().active();
        GuiUtils.setUpButton(
            gui,
            button,
            () -> {
              if (registeredFilters.get(key).filter()
                  instanceof ItemTypeFilter(List<ItemType> types)) {
                if (isActive) {
                  return;
                }
                this.targetTypes = types;
                disableOtherItemTypeFilters(key);
                activeStates.put(key, true);
              } else {
                activeStates.put(key, !isActive);
              }
              gui.onFilterStateChanged();
            });
      }
    }
  }

  public List<ItemType> getTargetTypes() {
    return targetTypes;
  }

  public String getSearchTerm() {
    return searchTerm;
  }

  public void setSearchTerm(String searchTerm) {
    this.searchTerm = searchTerm;
  }
}
