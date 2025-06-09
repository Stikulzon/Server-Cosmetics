package com.zefir.servercosmetics.gui.filters;

import com.zefir.servercosmetics.config.ConfigManager;
import com.zefir.servercosmetics.config.entries.CustomItemEntry;
import com.zefir.servercosmetics.gui.PagedItemDisplayGui;
import com.zefir.servercosmetics.gui.core.IFilter;
import com.zefir.servercosmetics.util.GUIUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class FilterManager {
    private final PagedItemDisplayGui gui;
    private final ServerPlayerEntity player;
    private final Map<String, FilterRegistration> registeredFilters = new HashMap<>();
    private final Map<String, Boolean> activeStates = new HashMap<>();

    private record FilterRegistration(IFilter filter, ConfigManager.NavigationButton activeButton, ConfigManager.NavigationButton inactiveButton) {}

    public FilterManager(PagedItemDisplayGui gui, ServerPlayerEntity player) {
        this.gui = gui;
        this.player = player;
    }

    public void addFilter(String key, IFilter filter, ConfigManager.NavigationButton inactiveButton, ConfigManager.NavigationButton activeButton, boolean initiallyActive) {
        registeredFilters.put(key, new FilterRegistration(filter, inactiveButton, activeButton));
        activeStates.put(key, initiallyActive);
    }

    public Predicate<CustomItemEntry> getCombinedPredicate() {
        return entry -> registeredFilters.entrySet().stream()
                .allMatch(mapEntry -> {
                    String key = mapEntry.getKey();
                    IFilter filter = mapEntry.getValue().filter();
                    boolean isActive = activeStates.getOrDefault(key, false);

                    return !isActive || filter.test(player, entry);
                });
    }

    public void drawFilterButtons() {
        for (var entry : registeredFilters.entrySet()) {
            String key = entry.getKey();
            FilterRegistration reg = entry.getValue();
            boolean isActive = activeStates.getOrDefault(key, false);

            ConfigManager.NavigationButton button = isActive ?  reg.inactiveButton: reg.activeButton;

            GUIUtils.setUpButton(gui, button, () -> {
                activeStates.put(key, !isActive);
                gui.onFilterStateChanged();
            });
        }
    }
}
