package ua.zefir.servercosmetics.config;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.text.Text;
import org.simpleyaml.configuration.file.YamlFile;
import ua.zefir.servercosmetics.datagen.ui.GuiTextures;
import ua.zefir.servercosmetics.util.Utils;

public class CosmeticsGuiConfig extends AbstractGuiConfig {

  private static final List<ButtonDefinition> COSMETICS_BUTTONS =
      List.of(
          new ButtonDefinition(
              "toggleColorView",
              "Toggle view",
              "minecraft:diamond_chestplate",
              null,
              10,
              List.of()),
          new ButtonDefinition(
              "enterColor",
              "Enter custom color",
              "minecraft:oak_sign",
              null,
              9,
              List.of(
                  "\u00a7eEnter the color in HEX format", "\u00a7ein the first line of the sign")),
          new ButtonDefinition(
              "decreaseBrightness",
              "Decrease brightness",
              "minecraft:paper",
              "previous",
              15,
              List.of()),
          new ButtonDefinition(
              "increaseBrightness",
              "Increase brightness",
              "minecraft:paper",
              "next",
              16,
              List.of()),
          new ButtonDefinition(
              "filter.hats-disabled",
              "<blue>Hats",
              "minecraft:leather_helmet",
              null,
              12,
              List.of()),
          new ButtonDefinition(
              "filter.hats-enabled", "<blue>Hats", "minecraft:diamond_helmet", null, 12, List.of()),
          new ButtonDefinition(
              "filter.body-cosmetics-disabled",
              "<blue>Body Cosmetics",
              "minecraft:chainmail_chestplate",
              null,
              13,
              List.of()),
          new ButtonDefinition(
              "filter.body-cosmetics-enabled",
              "<blue>Body Cosmetics",
              "minecraft:diamond_chestplate",
              null,
              13,
              List.of()),
          new ButtonDefinition(
              "filter.armor-cosmetics-disabled",
              "<blue>Chestplate Cosmetics",
              "minecraft:leather_chestplate",
              null,
              14,
              List.of()),
          new ButtonDefinition(
              "filter.armor-cosmetics-enabled",
              "<blue>Chestplate Cosmetics",
              "minecraft:diamond_chestplate",
              null,
              14,
              List.of()));

  private final ColorPickerConfig colorPickerConfig = new ColorPickerConfig();

  public CosmeticsGuiConfig() {
    super("CosmeticsGUI.yml");
  }

  @Override
  protected List<ButtonDefinition> getButtonDefinitions() {
    return Stream.concat(super.getButtonDefinitions().stream(), COSMETICS_BUTTONS.stream())
        .toList();
  }

  @Override
  protected String getGuiConfigHeader() {
    return "Cosmetics GUI Config File";
  }

  @Override
  protected void addSpecificDefaults(YamlFile file) {
    file.addDefault("permissions.openGui", "servercosmetics.gui.cosmetics");
    colorPickerConfig.addDefaults(file);
  }

  @Override
  protected void loadSpecificConfig(YamlFile file) {
    colorPickerConfig.load(file);
  }

  @Override
  public Text getGuiName() {
    return GuiTextures.COSMETICS_MENU.apply(Utils.formatDisplayName(this.guiNameString));
  }

  public ColorPickerConfig getColorPickerConfig() {
    return colorPickerConfig;
  }
}
