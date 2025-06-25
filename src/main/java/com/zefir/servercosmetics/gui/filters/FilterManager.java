package com.zefir.servercosmetics.gui.filters;

import com.zefir.servercosmetics.config.ConfigManager;
import com.zefir.servercosmetics.config.entries.CustomItemEntry;
import com.zefir.servercosmetics.config.entries.ItemType;
import com.zefir.servercosmetics.gui.PagedItemDisplayGui;
import com.zefir.servercosmetics.util.GUIUtils;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class FilterManager {
    private final PagedItemDisplayGui gui;
    private final Map<String, FilterRegistration> registeredFilters = new HashMap<>();
    private final Map<String, Boolean> activeStates = new HashMap<>();
    @Getter
    private ItemType targetType;

    private record FilterRegistration(Predicate<CustomItemEntry> filter, ConfigManager.NavigationButton activeButton, ConfigManager.NavigationButton inactiveButton) {}

    public FilterManager(PagedItemDisplayGui gui) {
        this.gui = gui;
    }

    public void addFilter(String key, Predicate<CustomItemEntry> filter, ConfigManager.NavigationButton inactiveButton, ConfigManager.NavigationButton activeButton, boolean initiallyActive) {
        registeredFilters.put(key, new FilterRegistration(filter, inactiveButton, activeButton));
        activeStates.put(key, initiallyActive);
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

            if (registeredFilters.get(key).filter() instanceof ItemTypeFilter targetFilter && isActive) {
                this.targetType = targetFilter.type();
            }

            GUIUtils.setUpButton(gui, button, () -> {
                if (registeredFilters.get(key).filter() instanceof ItemTypeFilter targetFilter) {
                    if (isActive) {
                        return;
                    }
                    this.targetType = targetFilter.type();
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