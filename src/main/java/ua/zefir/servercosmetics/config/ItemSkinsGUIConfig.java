package ua.zefir.servercosmetics.config;

import java.util.*;
import lombok.Getter;
import net.minecraft.text.Text;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;
import ua.zefir.servercosmetics.gui.resources.GuiTextures;
import ua.zefir.servercosmetics.util.Utils;

public class ItemSkinsGUIConfig extends AbstractGuiConfig {

  @Getter private static int itemSlot;

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
      file.set(
          "displaySlots",
          List.of(
              19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43));
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

    buttonDefaults.put(
        "selectItem",
        Map.of(
            "name",
            "<red>Select item",
            "item",
            "minecraft:barrier",
            "slotIndex",
            4,
            "lore",
            List.of("<dark_red>Click on the item in your inventory below.", "", "", "")));

    buttonDefaults.forEach(
        (buttonName, properties) ->
            addDefaultButtonToSection(buttonsSection, buttonName, properties));
  }

  @Override
  protected void loadAllNavigationButtons(YamlFile file) {
    super.loadAllNavigationButtons(file);

    loadNavigationButton(file, "selectItem");
  }

  public Text getGuiName() {
    return GuiTextures.ITEM_SKINS_MENU.apply(Utils.formatDisplayName(this.guiNameString));
  }
}
