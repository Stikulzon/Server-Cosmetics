package com.zefir.servercosmetics.config;

import com.zefir.servercosmetics.gui.resources.GuiTextures;
import com.zefir.servercosmetics.util.Utils;
import lombok.Getter;
import net.minecraft.text.Text;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;

import java.util.*;

public class ItemSkinsGUIConfig extends AbstractGuiConfig {

    @Getter
    private static int itemSlot;

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
        super.addDefaultButtons(buttonsSection);

//        buttonDefaults.put("filter.show-skins-for-selected-item-enabled", Map.of(
//                "name", "&bSelected Cosmetics Filter", "item", "minecraft:diamond_chestplate", "slotIndex", 11,
//                "lore", List.of("&aShow skins for selected item only <green>(Enabled)", "", "&aClick to change the mode!", "")));
//        buttonDefaults.put("filter.show-skins-for-selected-item-disabled", Map.of(
//                "name", "&bSelected Cosmetics Filter", "item", "minecraft:golden_chestplate", "slotIndex", 11,
//                "lore", List.of("&7Show skins for selected item only <blue>(Disabled)", "", "&aClick to change the mode!", "")));

        buttonDefaults.forEach((buttonName, properties) -> addDefaultButtonToSection(buttonsSection, buttonName, properties));
    }
    @Override
    protected void loadAllNavigationButtons(YamlFile file) {
        super.loadAllNavigationButtons(file);

//        loadNavigationButton(file, "filter.show-skins-for-selected-item-enabled");
//        loadNavigationButton(file, "filter.show-skins-for-selected-item-disabled");
    }

    public Text getGuiName() {
        return GuiTextures.ITEM_SKINS_MENU.apply(Utils.formatDisplayName(this.guiNameString));
    }

}
