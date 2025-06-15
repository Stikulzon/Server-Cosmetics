package com.zefir.servercosmetics.gui.filters;

import com.zefir.servercosmetics.config.ConfigManager;
import com.zefir.servercosmetics.config.entries.CustomItemEntry;
import com.zefir.servercosmetics.gui.PagedItemDisplayGui;
import com.zefir.servercosmetics.util.GUIUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class FilterManager {
    private final PagedItemDisplayGui gui;
    private final Map<String, FilterRegistration> registeredFilters = new HashMap<>();
    private final Map<String, Boolean> activeStates = new HashMap<>();
    private final List<List<String>> canBeActiveOnlyOne = new ArrayList<>();

    private record FilterRegistration(Predicate<CustomItemEntry> filter, ConfigManager.NavigationButton activeButton, ConfigManager.NavigationButton inactiveButton) {}

    public FilterManager(PagedItemDisplayGui gui) {
        this.gui = gui;
    }

    public void addFilter(String key, Predicate<CustomItemEntry> filter, ConfigManager.NavigationButton inactiveButton, ConfigManager.NavigationButton activeButton, boolean initiallyActive) {
        registeredFilters.put(key, new FilterRegistration(filter, inactiveButton, activeButton));
        activeStates.put(key, initiallyActive);
    }

    public void canBeActiveOnlyOne(List<String> keys) {
        canBeActiveOnlyOne.add(keys);
    }

    private void ifCanBeActiveOnlyOne(String key){
        for (List<String> list : canBeActiveOnlyOne) {
            if(list.contains(key)){
                for (String s : list) {
                    activeStates.put(s, false);
                }
                activeStates.put(key, true);
                return;
            }
        }
    }

    private boolean isCanBeActiveOnlyOne(String key){
        for (List<String> list : canBeActiveOnlyOne) {
            return list.contains(key);
        }
        return false;
    }

    public Predicate<CustomItemEntry> getCombinedPredicate() {
        return entry -> registeredFilters.entrySet().stream()
                .filter(mapEntry -> activeStates.getOrDefault(mapEntry.getKey(), false)) // Consider only active filters
                .allMatch(mapEntry -> mapEntry.getValue().filter().test(entry));         // Check if the entry passes all of them
    }

    public void drawFilterButtons() {
        for (var entry : registeredFilters.entrySet()) {
            String key = entry.getKey();
            FilterRegistration reg = entry.getValue();
            boolean isActive = activeStates.getOrDefault(key, false);

            ConfigManager.NavigationButton button = isActive ? reg.activeButton : reg.inactiveButton;

            GUIUtils.setUpButton(gui, button, () -> {
                if (isCanBeActiveOnlyOne(key)) {
                    if (isActive) {
                        return;
                    }
                    ifCanBeActiveOnlyOne(key);
                } else {
                    activeStates.put(key, !isActive);
                }
                gui.onFilterStateChanged();
            });
        }
    }
}