package ua.zefir.servercosmetics.gui.filters;

import ua.zefir.servercosmetics.config.ConfigManager;
import ua.zefir.servercosmetics.data.CustomItemEntry;
import ua.zefir.servercosmetics.data.ItemType;
import ua.zefir.servercosmetics.gui.PagedItemDisplayGui;
import ua.zefir.servercosmetics.util.GUIUtils;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class FilterManager {
    private final PagedItemDisplayGui gui;
    private final Map<String, FilterRegistration> registeredFilters = new HashMap<>();
    private final Map<String, Boolean> activeStates = new HashMap<>();
    @Getter
    private List<ItemType> targetTypes;

    public record FilterRegistration(Predicate<CustomItemEntry> filter, ConfigManager.NavigationButton activeButton, ConfigManager.NavigationButton inactiveButton, boolean isHidden) {}

    public FilterManager(PagedItemDisplayGui gui) {
        this.gui = gui;
    }

    public void addFilter(String key, Predicate<CustomItemEntry> filter, ConfigManager.NavigationButton inactiveButton, ConfigManager.NavigationButton activeButton, boolean initiallyActive) {
        addFilter(key, filter, inactiveButton, activeButton, initiallyActive, false);
    }
    public void addFilter(String key, Predicate<CustomItemEntry> filter, ConfigManager.NavigationButton inactiveButton, ConfigManager.NavigationButton activeButton, boolean initiallyActive, boolean isHidden) {
        if(gui.getGuiConfig().getDisabledFilters() != null && gui.getGuiConfig().getDisabledFilters().contains(key)) {
            return;
        }
        registeredFilters.put(key, new FilterRegistration(filter, inactiveButton, activeButton, isHidden));
        activeStates.put(key, initiallyActive);
    }

    public FilterRegistration getFilter(String key) {
        return registeredFilters.get(key);
    }

    private void disableOtherItemTypeFilters(String key) {
        for (Map.Entry<String, FilterRegistration> entry : registeredFilters.entrySet()) {
            if(entry.getValue().filter() instanceof ItemTypeFilter && !entry.getKey().equals(key)){
                activeStates.put(entry.getKey(), false);
            }
        }
    }

    public Predicate<CustomItemEntry> getCombinedPredicate() {
        return entry -> registeredFilters.entrySet().stream()
                .filter(mapEntry -> activeStates.getOrDefault(mapEntry.getKey(), false))
                .allMatch(mapEntry -> mapEntry.getValue().filter().test(entry));
    }

    public void drawFilterButtons() {
        for (var entry : registeredFilters.entrySet()) {
            String key = entry.getKey();
            FilterRegistration reg = entry.getValue();
            boolean isActive = activeStates.getOrDefault(key, false);

            ConfigManager.NavigationButton button = isActive ? reg.inactiveButton : reg.activeButton;

            if (registeredFilters.get(key).filter() instanceof ItemTypeFilter(List<ItemType> types) && isActive) {
                this.targetTypes = types;
            }

            if (!reg.isHidden) {
                GUIUtils.setUpButton(gui, button, () -> {
                    if (registeredFilters.get(key).filter() instanceof ItemTypeFilter(List<ItemType> types)) {
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
}