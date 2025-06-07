package com.zefir.servercosmetics.config;

import com.zefir.servercosmetics.config.entries.CustomItemEntry;
import com.zefir.servercosmetics.config.entries.CustomItemRegistry;
import lombok.Getter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
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

    public static void itemSkinsInit() {
        instance = new ItemSkinsGUIConfig();
        instance.init();
    }

    public static ItemSkinsGUIConfig get() {
        if (instance == null) {
            itemSkinsInit();
        }
        return instance;
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
        buttonDefaults.put("removeItem", Map.of(
                "name", "Remove item", "item", "minecraft:paper", "textureName", "remove", "slotIndex", 49));
        buttonDefaults.put("skinFilter.show-all-skins", Map.of(
                "name", "&bCosmetic Filter", "item", "minecraft:diamond_chestplate", "slotIndex", 10,
                "lore", List.of("&aAll skins &7(Selected)", "&7Available skins", "", "&aClick to change mode!")));
        buttonDefaults.put("skinFilter.show-owned-skins", Map.of(
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
        loadNavigationButton(file, "removeItem");
        loadNavigationButton(file, "skinFilter.show-all-skins");
        loadNavigationButton(file, "skinFilter.show-owned-skins");
        loadNavigationButton(file, "pageIndicator");
    }

    public static int[] getCosmeticSlots() {
        return get().getDisplaySlots();
    }

    public static ItemStack getItemStackFromSkinIdAndItem(String skinId, Item item) {
        String targetMaterialId = Registries.ITEM.getId(item).toString();
        CustomItemEntry entry = CustomItemRegistry.getItemSkin(targetMaterialId, skinId);
        return entry != null ? entry.itemStack() : null;
    }

    public static Map<String, CustomItemEntry> getAllSkinsForMaterial(Item item) {
        String targetMaterialId = Registries.ITEM.getId(item).toString();
        return CustomItemRegistry.getAllSkinsForMaterial(targetMaterialId);
    }

//    /**
//     * Gets a paginated map of skin entries for the given item.
//     * The key is an integer index (0 to itemsPerPage-1) for display purposes on the current page.
//     * The value is the CustomItemEntry.
//     */
//    public static Map<Integer, CustomItemEntry> getPaginatedSkinsForMaterial(Item item, int page, int itemsPerPage) {
//        String targetMaterialId = Registries.ITEM.getId(item).toString();
//        return CustomItemRegistry.getPaginatedSkinsForMaterial(targetMaterialId, page, itemsPerPage);
//    }
}
