package ua.zefir.servercosmetics.config;

import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import lombok.Getter;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.comments.format.YamlCommentFormat;
import org.simpleyaml.configuration.file.YamlFile;
import ua.zefir.servercosmetics.ServerCosmetics;
import ua.zefir.servercosmetics.util.Utils;

public abstract class AbstractGuiConfig {

  protected final Path configFilePath;
  protected YamlFile yamlFile;

  @Getter protected String guiNameString;
  @Getter protected int[] displaySlots;
  @Getter protected String permissionOpenGui;
  @Getter protected String messageUnlockedString;
  @Getter protected String messageLockedString;
  @Getter protected boolean pageIndicatorEnabled;
  @Getter protected boolean replaceInventory;
  @Getter private List<String> disabledFilters;
  @Getter ScreenHandlerType<GenericContainerScreenHandler> screenHandlerType;
  static final Map<String, Map<String, Object>> buttonDefaults = new java.util.HashMap<>();

  @Getter
  protected final Map<String, ConfigManager.NavigationButton> navigationButtons = new HashMap<>();

  public AbstractGuiConfig(String configFileName) {
    this.configFilePath = ConfigManager.SERVER_COSMETICS_DIR.resolve(configFileName);
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
  }

  protected void addCommonDefaults(YamlFile file) {
    file.addDefault("guiName", "Default GUI Name");
    file.addDefault(
        "displaySlots",
        List.of(
            19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43));
    file.addDefault("permissions.openGui", "servercosmetics.gui.default");
    file.addDefault("messages.unlocked", "&a(Unlocked)");
    file.addDefault("messages.locked", "&c(Locked)");
    file.addDefault("pageIndicatorEnabled", false);
    file.addDefault("replaceInventory", false);
    loadGuiSize(file.getInt("guiRows", 6));
    file.addDefault("disabledFilters", List.of());
  }

  private void loadGuiSize(int guiRows) {
    if (guiRows < 1 || guiRows > 6) {
      ServerCosmetics.LOGGER.warn(
          "Invalid guiRows value '{}' in {}. Must be between 1 and 6. Defaulting to 6.",
          guiRows,
          this.configFilePath.getFileName());
      guiRows = 6;
    }
    this.screenHandlerType =
        switch (guiRows) {
          case 1 -> ScreenHandlerType.GENERIC_9X1;
          case 2 -> ScreenHandlerType.GENERIC_9X2;
          case 3 -> ScreenHandlerType.GENERIC_9X3;
          case 4 -> ScreenHandlerType.GENERIC_9X4;
          case 5 -> ScreenHandlerType.GENERIC_9X5;
          default -> ScreenHandlerType.GENERIC_9X6;
        };
  }

  protected void loadNavigationButton(YamlFile yamlFile, String buttonKey) {
    String basePath = "buttons." + buttonKey;
    if (!yamlFile.isConfigurationSection(basePath)) {
      ServerCosmetics.LOGGER.warn(
          "Button configuration for '{}' not found in {}.",
          buttonKey,
          this.configFilePath.getFileName());
      return;
    }
    String baseItemString = yamlFile.getString(basePath + ".item");
    if (baseItemString == null || baseItemString.isEmpty()) {
      ServerCosmetics.LOGGER.error(
          "Button '{}' in {} is missing 'item' field.",
          buttonKey,
          this.configFilePath.getFileName());
      return;
    }
    String complitedItemString =
        baseItemString.contains(":") ? baseItemString : "minecraft:" + baseItemString.toLowerCase();

    Item item = Registries.ITEM.get(Identifier.of(complitedItemString));
    if (item == Registries.ITEM.get(Registries.ITEM.getDefaultId())
        && !complitedItemString.equals(Registries.ITEM.getDefaultId().toString())) {
      ServerCosmetics.LOGGER.error(
          "Button '{}' in {} has invalid item id: {}. Defaulting to paper.",
          buttonKey,
          this.configFilePath.getFileName(),
          complitedItemString);
      item = Registries.ITEM.get(Identifier.of("minecraft:paper"));
      complitedItemString = "minecraft:paper";
    }

    PolymerModelData polymerModelData = null;
    if (yamlFile.isSet(basePath + ".textureName")) {
      String textureName = yamlFile.getString(basePath + ".textureName");
      if (textureName != null && !textureName.isEmpty()) {
        try {
          polymerModelData =
              PolymerResourcePackUtils.requestModel(
                  item, Identifier.of(ServerCosmetics.MOD_ID, "item/" + textureName));
        } catch (Exception e) {
          ServerCosmetics.LOGGER.error(
              "Failed to request model for button '{}' (item: {}, texture: {}): {}",
              buttonKey,
              complitedItemString,
              textureName,
              e.getMessage());
        }
      }
    }

    List<String> loreStrings = yamlFile.getStringList(basePath + ".lore");

    int slotIndex = yamlFile.getInt(basePath + ".slotIndex");
    if (yamlFile.getString(basePath + ".slotIndex") == null
        || yamlFile.getString(basePath + ".slotIndex").isEmpty()) {
      slotIndex = -1;
    }

    navigationButtons.put(
        buttonKey,
        new ConfigManager.NavigationButton(
            Utils.formatDisplayName(yamlFile.getString(basePath + ".name", "Button " + buttonKey)),
            item,
            polymerModelData,
            slotIndex,
            loreStrings));
  }

  public static void addDefaultButtonToSection(
      ConfigurationSection buttonsSection, String buttonName, Map<String, Object> properties) {
    ConfigurationSection buttonSection = buttonsSection.getConfigurationSection(buttonName);
    if (buttonSection == null) {
      buttonSection = buttonsSection.createSection(buttonName);
    }
    final ConfigurationSection finalButtonSection = buttonSection;
    properties.forEach(
        (key, value) -> {
          if (!finalButtonSection.contains(key) && value != null && !value.equals("")) {
            finalButtonSection.set(key, value);
          }
        });
  }

  public Text getGuiName() {
    return Utils.formatDisplayName(this.guiNameString);
  }

  public Text getMessageUnlocked() {
    return Utils.formatDisplayName(this.messageUnlockedString);
  }

  public Text getMessageLocked() {
    return Utils.formatDisplayName(this.messageLockedString);
  }

  public ConfigManager.NavigationButton getButtonConfig(String buttonKey) {
    ConfigManager.NavigationButton button = navigationButtons.get(buttonKey);
    if (button == null) {
      ServerCosmetics.LOGGER.warn(
          "Requested non-existent button config: '{}' from {}",
          buttonKey,
          this.configFilePath.getFileName());
      return new ConfigManager.NavigationButton(
          Text.literal("Error"),
          Registries.ITEM.get(Identifier.of("minecraft:barrier")),
          null,
          0,
          Collections.emptyList());
    }
    return button;
  }

  // --- Abstract methods for subclasses ---
  protected abstract String getGuiConfigHeader();

  protected abstract void addSpecificDefaults(YamlFile file);

  protected abstract void loadSpecificConfig(YamlFile file);

  protected void addDefaultButtons(ConfigurationSection buttonsSection) {
    buttonDefaults.put(
        "next",
        Map.of("name", "Next", "item", "minecraft:paper", "textureName", "next", "slotIndex", 51));

    buttonDefaults.put(
        "previous",
        Map.of(
            "name", "Back", "item", "minecraft:paper", "textureName", "previous", "slotIndex", 47));

    buttonDefaults.put(
        "removeSkin",
        Map.of(
            "name",
            "Remove skin",
            "item",
            "minecraft:paper",
            "textureName",
            "remove",
            "slotIndex",
            49));

    buttonDefaults.put(
        "filter.show-owned-skins-enabled",
        Map.of(
            "name",
            "&bOwned Cosmetics Filter",
            "item",
            "minecraft:diamond_chestplate",
            "slotIndex",
            10,
            "lore",
            List.of(
                "&aShow owned cosmetics only <green>(Enabled)",
                "",
                "&aClick to change the mode!",
                "")));
    buttonDefaults.put(
        "filter.show-owned-skins-disabled",
        Map.of(
            "name",
            "&bOwned Cosmetics Filter",
            "item",
            "minecraft:golden_chestplate",
            "slotIndex",
            10,
            "lore",
            List.of(
                "&7Show owned cosmetics only <blue>(Disabled)",
                "",
                "&aClick to change the mode!",
                "")));

    buttonDefaults.put(
        "noCosmeticsAvailable",
        Map.of(
            "name",
            "No cosmetics available",
            "item",
            "minecraft:barrier",
            "slotIndex",
            "",
            "lore",
            List.of()));

    buttonDefaults.put(
        "pageIndicator", Map.of("name", "Page", "item", "minecraft:paper", "slotIndex", 53));
  }

  protected void loadAllNavigationButtons(YamlFile file) {
    loadNavigationButton(file, "next");
    loadNavigationButton(file, "previous");
    loadNavigationButton(file, "removeSkin");
    loadNavigationButton(file, "pageIndicator");
    loadNavigationButton(file, "noCosmeticsAvailable");

    loadNavigationButton(file, "filter.show-owned-skins-enabled");
    loadNavigationButton(file, "filter.show-owned-skins-disabled");
  }
}
