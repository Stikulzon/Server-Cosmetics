package com.zefir.servercosmetics.config;

import com.zefir.servercosmetics.config.entries.CustomItemEntry;
import com.zefir.servercosmetics.config.entries.CustomItemRegistry;
import com.zefir.servercosmetics.config.entries.ItemType;
import com.zefir.servercosmetics.gui.resources.GuiTextures;
import com.zefir.servercosmetics.util.Utils;
import lombok.Getter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

import java.util.*;

public class ItemSkinsGUIConfig extends AbstractGuiConfig {

    @Getter
    private static int itemSlot;

    private static ItemSkinsGUIConfig instance;

    public ItemSkinsGUIConfig() {
        super("ItemSkinsGUI.yml");
    }

    @Override
    protected String getGuiConfigHeader() {
        return "ItemSkins GUI Config File";
    }

    @Override
    protected void addSpecificDefaults(YamlFile file) {
        file.addDefault("slots.itemSlot", 4);
        if (!file.contains("displaySlots")) {
            file.set("displaySlots", List.of(
                    19,20,21,22,23,24,25,
                    28,29,30,31,32,33,34,
                    37,38,39,40,41,42,43
            ));
        }
        file.addDefault("permissions.openGui", "servercosmetics.gui.itemskins");
    }

    @Override
    protected void loadSpecificConfig(YamlFile file) {
        itemSlot = file.getInt("slots.itemSlot");
    }

    @Override
    protected void addDefaultButtons(ConfigurationSection buttonsSection) {
        Map<String, Map<String, Object>> buttonDefaults = new java.util.HashMap<>();

        buttonDefaults.put("next", Map.of(
                "name", "Next", "item", "minecraft:paper", "textureName", "next", "slotIndex", 51));
        buttonDefaults.put("previous", Map.of(
                "name", "Back", "item", "minecraft:paper", "textureName", "previous", "slotIndex", 47));
        buttonDefaults.put("removeSkin", Map.of(
                "name", "Remove skin", "item", "minecraft:paper", "textureName", "remove", "slotIndex", 49));
        buttonDefaults.put("filter.show-all-skins", Map.of(
                "name", "&bCosmetic Filter", "item", "minecraft:diamond_chestplate", "slotIndex", 10,
                "lore", List.of("&aAll skins &7(Selected)", "&7Available skins", "", "&aClick to change mode!")));
        buttonDefaults.put("filter.show-owned-skins", Map.of(
                "name", "&bCosmetic Filter", "item", "minecraft:golden_chestplate", "slotIndex", 10,
                "lore", List.of("&7All skins", "&aAvailable skins &7(Selected)", "", "&aClick to change mode!")));
        buttonDefaults.put("pageIndicator", Map.of(
                "name", "Page", "item", "minecraft:paper", "slotIndex", 53));

        buttonDefaults.forEach((buttonName, properties) -> addDefaultButtonToSection(buttonsSection, buttonName, properties));
    }
    @Override
    protected void loadAllNavigationButtons(YamlFile file) {
        loadNavigationButton(file, "next");
        loadNavigationButton(file, "previous");
        loadNavigationButton(file, "removeSkin");
        loadNavigationButton(file, "filter.show-all-skins");
        loadNavigationButton(file, "filter.show-owned-skins");
        loadNavigationButton(file, "pageIndicator");
    }

    public Text getGuiName() {
        return GuiTextures.ITEM_SKINS_MENU.apply(Utils.formatDisplayName(this.guiNameString));
    }

}
