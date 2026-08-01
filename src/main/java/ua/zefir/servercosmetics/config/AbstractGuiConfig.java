package ua.zefir.servercosmetics.config;

import static ua.zefir.servercosmetics.ModInit.id;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.comments.format.YamlCommentFormat;
import org.simpleyaml.configuration.file.YamlFile;
import ua.zefir.servercosmetics.ModInit;
import ua.zefir.servercosmetics.datagen.RuntimeModelManager;
import ua.zefir.servercosmetics.util.Utils;

public abstract class AbstractGuiConfig {

  private static final List<Integer> DEFAULT_DISPLAY_SLOTS =
      List.of(19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43);

  private static final List<ButtonDefinition> COMMON_BUTTONS =
      List.of(
          new ButtonDefinition(
              "search",
              "&eSearch",
              "minecraft:oak_sign",
              null,
              48,
              List.of("&7Click to search for items", "&7by name.")),
          new ButtonDefinition("next", "Next", "minecraft:paper", "next", 51, List.of()),
          new ButtonDefinition("previous", "Back", "minecraft:paper", "previous", 47, List.of()),
          new ButtonDefinition(
              "removeSkin", "Remove skin", "minecraft:paper", "remove", 49, List.of()),
          new ButtonDefinition(
              "filter.show-owned-skins-enabled",
              "&bOwned Cosmetics Filter",
              "minecraft:diamond_chestplate",
              null,
              10,
              List.of(
                  "&aShow owned cosmetics only <green>(Enabled)",
                  "",
                  "&aClick to change the mode!",
                  "")),
          new ButtonDefinition(
              "filter.show-owned-skins-disabled",
              "&bOwned Cosmetics Filter",
              "minecraft:golden_chestplate",
              null,
              10,
              List.of(
                  "&7Show owned cosmetics only <blue>(Disabled)",
                  "",
                  "&aClick to change the mode!",
                  "")),
          new ButtonDefinition(
              "noCosmeticsAvailable",
              "No cosmetics available",
              "minecraft:barrier",
              null,
              -1,
              List.of()),
          new ButtonDefinition("pageIndicator", "Page", "minecraft:paper", null, 53, List.of()));

  protected final Path configFilePath;
  protected YamlFile yamlFile;

  protected String guiNameString;
  protected int[] displaySlots;
  protected String permissionOpenGui;
  protected String messageUnlockedString;
  protected String messageLockedString;
  protected boolean pageIndicatorEnabled;
  protected boolean replaceInventory;
  protected List<String> disabledFilters;
  protected MenuType<ChestMenu> screenHandlerType;

  protected final Map<String, ButtonConfig> navigationButtons = new HashMap<>();

  protected AbstractGuiConfig(String configFileName) {
    this.configFilePath = MainConfig.SERVER_COSMETICS_DIR.resolve(configFileName);
  }

  public void init() {
    this.yamlFile = new YamlFile(configFilePath.toAbsolutePath().toString());
    try {
      yamlFile.createOrLoadWithComments();
      setupDefaultConfig(yamlFile);
      yamlFile.loadWithComments();

      loadCommonConfig(yamlFile);
      loadSpecificConfig(yamlFile);
      loadAllNavigationButtons(yamlFile);

    } catch (IOException e) {
      throw new RuntimeException(
          "Failed to create or load " + configFilePath.getFileName().toString(), e);
    }
  }

  private void setupDefaultConfig(YamlFile file) {
    file.setCommentFormat(YamlCommentFormat.PRETTY);
    file.options()
        .headerFormatter()
        .prefixFirst("###############################")
        .commentPrefix("## ")
        .commentSuffix(" ##")
        .suffixLast("###############################");
    file.setHeader(getGuiConfigHeader());

    addCommonDefaults(file);
    addSpecificDefaults(file);

    ConfigurationSection buttonsSection = file.getConfigurationSection("buttons");
    if (buttonsSection == null) {
      buttonsSection = file.createSection("buttons");
    }
    addDefaultButtons(buttonsSection);

    try {
      file.save();
    } catch (IOException e) {
      throw new RuntimeException(
          "Failed to save default yml configuration for " + file.getFilePath(), e);
    }
  }

  private void loadCommonConfig(YamlFile file) {
    this.guiNameString = file.getString("guiName");
    this.displaySlots =
        file.getIntegerList("displaySlots").stream().mapToInt(Integer::intValue).toArray();
    this.permissionOpenGui = file.getString("permissions.openGui");
    this.messageUnlockedString = file.getString("messages.unlocked");
    this.messageLockedString = file.getString("messages.locked");
    this.pageIndicatorEnabled = file.getBoolean("pageIndicatorEnabled", false);
    this.replaceInventory = file.getBoolean("replaceInventory");
    this.disabledFilters = file.getStringList("disabledFilters");

    loadGuiSize(file.getInt("guiRows", 6));
  }

  private void addCommonDefaults(YamlFile file) {
    file.addDefault("guiName", "Default GUI Name");
    file.addDefault("displaySlots", DEFAULT_DISPLAY_SLOTS);
    file.addDefault("messages.unlocked", "&a(Unlocked)");
    file.addDefault("messages.locked", "&c(Locked)");
    file.addDefault("pageIndicatorEnabled", false);
    file.addDefault("replaceInventory", false);
    file.addDefault("guiRows", 6);
    file.addDefault("disabledFilters", List.of());
  }

  private void loadGuiSize(int guiRows) {
    if (guiRows < 1 || guiRows > 6) {
      ModInit.LOGGER.warn(
          "Invalid guiRows value '{}' in {}. Must be between 1 and 6. Defaulting to 6.",
          guiRows,
          this.configFilePath.getFileName());
      guiRows = 6;
    }
    this.screenHandlerType =
        switch (guiRows) {
          case 1 -> MenuType.GENERIC_9x1;
          case 2 -> MenuType.GENERIC_9x2;
          case 3 -> MenuType.GENERIC_9x3;
          case 4 -> MenuType.GENERIC_9x4;
          case 5 -> MenuType.GENERIC_9x5;
          default -> MenuType.GENERIC_9x6;
        };
  }

  protected List<ButtonDefinition> getButtonDefinitions() {
    return COMMON_BUTTONS;
  }

  private void addDefaultButtons(ConfigurationSection buttonsSection) {
    for (ButtonDefinition def : getButtonDefinitions()) {
      addDefaultButtonToSection(buttonsSection, def.key(), def.toPropertyMap());
    }
  }

  private static void addDefaultButtonToSection(
      ConfigurationSection buttonsSection, String buttonName, Map<String, Object> properties) {
    ConfigurationSection buttonSection = buttonsSection.getConfigurationSection(buttonName);
    if (buttonSection == null) {
      buttonSection = buttonsSection.createSection(buttonName);
    }
    ConfigurationSection finalButtonSection = buttonSection;
    properties.forEach(
        (key, value) -> {
          if (!finalButtonSection.contains(key)
              && value != null
              && !value.equals("")
              && (!(value instanceof List<?>)
                  || (value instanceof List<?> list && !list.isEmpty()))) {
            finalButtonSection.set(key, value);
          }
        });
  }

  protected void loadAllNavigationButtons(YamlFile file) {
    for (ButtonDefinition def : getButtonDefinitions()) {
      loadNavigationButton(file, def.key());
    }
  }

  private void loadNavigationButton(YamlFile yamlFile, String buttonKey) {
    String basePath = "buttons." + buttonKey;
    if (!yamlFile.isConfigurationSection(basePath)) {
      ModInit.LOGGER.warn(
          "Button configuration for '{}' not found in {}.",
          buttonKey,
          this.configFilePath.getFileName());
      return;
    }

    String baseItemString = yamlFile.getString(basePath + ".item");
    if (baseItemString == null || baseItemString.isEmpty()) {
      ModInit.LOGGER.error(
          "Button '{}' in {} is missing 'item' field.",
          buttonKey,
          this.configFilePath.getFileName());
      return;
    }

    String formattedItemString =
        baseItemString.contains(":") ? baseItemString : "minecraft:" + baseItemString.toLowerCase();

    Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(formattedItemString));

    Identifier modelPath = null;
    if (yamlFile.isSet(basePath + ".textureName")) {
      String textureName = yamlFile.getString(basePath + ".textureName");
      if (textureName != null && !textureName.isEmpty()) {
        try {
          RuntimeModelManager.requestItemModel(textureName, false);
          modelPath = id(textureName);
        } catch (Exception e) {
          ModInit.LOGGER.error(
              "Failed to request model for button '{}' (item: {}, texture: {}): {}",
              buttonKey,
              formattedItemString,
              textureName,
              e.getMessage());
        }
      }
    }

    List<String> loreStrings = yamlFile.getStringList(basePath + ".lore");
    int slotIndex = yamlFile.getInt(basePath + ".slotIndex", -1);

    navigationButtons.put(
        buttonKey,
        new ButtonConfig(
            Utils.formatDisplayName(yamlFile.getString(basePath + ".name", "Button " + buttonKey)),
            item,
            modelPath,
            slotIndex,
            loreStrings));
  }

  public Component getGuiName() {
    return Utils.formatDisplayName(this.guiNameString);
  }

  public Component getMessageUnlocked() {
    return Utils.formatDisplayName(this.messageUnlockedString);
  }

  public Component getMessageLocked() {
    return Utils.formatDisplayName(this.messageLockedString);
  }

  public ButtonConfig getButtonConfig(String buttonKey) {
    ButtonConfig button = navigationButtons.get(buttonKey);
    if (button == null) {
      ModInit.LOGGER.warn(
          "Requested non-existent button config: '{}' from {}",
          buttonKey,
          this.configFilePath.getFileName());
      return new ButtonConfig(
          Component.literal("Error"),
          BuiltInRegistries.ITEM.getValue(Identifier.parse("minecraft:barrier")),
          null,
          0,
          Collections.emptyList());
    }
    return button;
  }

  public int[] getDisplaySlots() {
    return displaySlots;
  }

  public String getPermissionOpenGui() {
    return permissionOpenGui;
  }

  public boolean isReplaceInventory() {
    return replaceInventory;
  }

  public List<String> getDisabledFilters() {
    return disabledFilters;
  }

  public MenuType<ChestMenu> getScreenHandlerType() {
    return screenHandlerType;
  }

  protected abstract String getGuiConfigHeader();

  protected abstract void addSpecificDefaults(YamlFile file);

  protected abstract void loadSpecificConfig(YamlFile file);
}
