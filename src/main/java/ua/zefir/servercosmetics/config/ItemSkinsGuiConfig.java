package ua.zefir.servercosmetics.config;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;
import org.simpleyaml.configuration.file.YamlFile;
import ua.zefir.servercosmetics.datagen.ui.GuiTextures;
import ua.zefir.servercosmetics.util.Utils;

public class ItemSkinsGuiConfig extends AbstractGuiConfig {

  private static final List<ButtonDefinition> ITEM_SKINS_BUTTONS =
      List.of(
          new ButtonDefinition(
              "selectItem",
              "<red>Select item",
              "minecraft:barrier",
              null,
              4,
              List.of("<dark_red>Click on the item in your inventory below.", "", "", "")));

  private int itemSlot;

  public ItemSkinsGuiConfig() {
    super("ItemSkinsGUI.yml");
  }

  @Override
  protected List<ButtonDefinition> getButtonDefinitions() {
    return Stream.concat(super.getButtonDefinitions().stream(), ITEM_SKINS_BUTTONS.stream())
        .toList();
  }

  @Override
  protected String getGuiConfigHeader() {
    return "ItemSkins GUI Config File";
  }

  @Override
  protected void addSpecificDefaults(YamlFile file) {
    file.addDefault("slots.itemSlot", 4);
    file.addDefault("permissions.openGui", "servercosmetics.gui.itemskins");
  }

  @Override
  protected void loadSpecificConfig(YamlFile file) {
    itemSlot = file.getInt("slots.itemSlot");
  }

  @Override
  public Component getGuiName() {
    return GuiTextures.ITEM_SKINS_MENU.apply(Utils.formatDisplayName(this.guiNameString));
  }

  public int getItemSlot() {
    return itemSlot;
  }
}
