package com.zefir.servercosmetics.gui;

import com.zefir.servercosmetics.config.AbstractGuiConfig;
import com.zefir.servercosmetics.config.entries.CustomItemEntry;
import com.zefir.servercosmetics.config.entries.ItemType;
import com.zefir.servercosmetics.gui.actions.OpenColorPickerAction;
import com.zefir.servercosmetics.gui.core.ICosmeticProvider;
import com.zefir.servercosmetics.gui.core.IItemAction;
import com.zefir.servercosmetics.gui.filters.FilterManager;
import com.zefir.servercosmetics.util.GUIUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import lombok.Getter;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PagedItemDisplayGui extends SimpleGui {

    private ICosmeticProvider provider;
    private IItemAction defaultClickAction;
    private final AbstractGuiConfig guiConfig;
    private int currentPage = 0;
    @Getter
    private final FilterManager filterManager;

    public PagedItemDisplayGui(ServerPlayerEntity player, AbstractGuiConfig config, ICosmeticProvider provider, IItemAction defaultClickAction) {
        super(config.getScreenHandlerType(), player, config.isReplaceInventory());
        this.guiConfig = config;
        this.provider = provider;
        this.defaultClickAction = defaultClickAction;
        this.filterManager = new FilterManager(this);
        setTitle(config.getGuiName());
        populateGui();
    }

    public void reinitialize(ICosmeticProvider newProvider, IItemAction newAction) {
        this.provider = newProvider;
        this.defaultClickAction = newAction;
        this.currentPage = 0;
        this.populateGui();
    }

    public void populateGui() {
        List<CustomItemEntry> allItems = provider.getItems();

        Predicate<CustomItemEntry> combinedFilter = filterManager.getCombinedPredicate();
        List<CustomItemEntry> filteredItems = allItems.stream()
                .filter(combinedFilter)
                .sorted(Comparator.comparing(CustomItemEntry::id))
                .collect(Collectors.toList());

        drawItems(filteredItems);

        setupNavigation(filteredItems.size());
        filterManager.drawFilterButtons();
    }

    private void drawItems(List<CustomItemEntry> itemsToDisplay) {
        int[] displaySlots = guiConfig.getDisplaySlots();
        int itemsPerPage = displaySlots.length;
        int startIndex = currentPage * itemsPerPage;

        for (int i = 0; i < itemsPerPage; i++) {
            int itemIndex = startIndex + i;
            int slot = displaySlots[i];

            if (itemIndex < itemsToDisplay.size()) {
                CustomItemEntry entry = itemsToDisplay.get(itemIndex);
                ItemStack displayStack = entry.itemStack().copy();
                GuiElementBuilder element = new GuiElementBuilder(displayStack);

                if (Permissions.check(player, entry.permission())) {
                    element.addLoreLine(guiConfig.getMessageUnlocked());
                    element.setCallback(() -> {
                        determineAction(entry).execute(player, entry, this);
                    });
                } else {
                    element.addLoreLine(guiConfig.getMessageLocked());
                }
                setSlot(slot, element);
            } else {
                clearSlot(slot);
            }
        }
    }

    private IItemAction determineAction(CustomItemEntry entry) {
        Item baseItem = Registries.ITEM.get(Identifier.tryParse(entry.baseItemForModel()));
        if (Items.LEATHER_HORSE_ARMOR.equals(baseItem)) {
            return new OpenColorPickerAction();
        }
        return defaultClickAction;
    }

    private void setupNavigation(int totalFilteredItems) {
        int itemsPerPage = guiConfig.getDisplaySlots().length;

        // Next Button
        if ((currentPage + 1) * itemsPerPage < totalFilteredItems) {
            GUIUtils.setUpButton(this, guiConfig.getButtonConfig("next"), () -> {
                currentPage++;
                populateGui();
            });
        }

        // Previous Button
        if (currentPage > 0) {
            GUIUtils.setUpButton(this, guiConfig.getButtonConfig("previous"), () -> {
                currentPage--;
                populateGui();
            });
        }
    }

    public void onFilterStateChanged() {
        this.currentPage = 0;
        this.populateGui();
    }
}