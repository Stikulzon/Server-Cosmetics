package ua.zefir.servercosmetics.config;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.text.Text;
import org.simpleyaml.configuration.ConfigurationSection;
import org.simpleyaml.configuration.file.YamlFile;
import ua.zefir.servercosmetics.data.EquipmentSlotConfig;
import ua.zefir.servercosmetics.data.ItemType;
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
              List.of("§eEnter the color in HEX format", "§ein the first line of the sign")),
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
              List.of()));

  private static final List<EquipmentSlotConfig> DEFAULT_EQUIPMENT_SLOTS =
      List.of(
          new EquipmentSlotConfig("head", 1, 3, ItemType.HAT, true, "Hat"),
          new EquipmentSlotConfig("body", 2, 2, ItemType.BODY_COSMETIC, true, "Body Cosmetic"),
          new EquipmentSlotConfig("chest", 2, 3, ItemType.CHESTPLATE, true, "Chestplate"),
          new EquipmentSlotConfig("legs", 3, 3, ItemType.LEGGINGS, true, "Leggings"),
          new EquipmentSlotConfig("feet", 4, 3, ItemType.BOOTS, true, "Boots"));

  private static final int[] DEFAULT_PRESET_SLOTS = {8, 17, 26, 35, 44, 53};
  private static final int[] DEFAULT_GRID_SLOTS = {
    55, 56, 57, 58, 59, 60, 64, 65, 66, 67, 68, 69, 73, 74, 75, 76, 77, 78
  };
  private static final int DEFAULT_SELECTED_SLOT_INDEX = 54;
  private static final int DEFAULT_REMOVE_BUTTON_INDEX = 72;
  private static final int DEFAULT_UNEQUIP_ALL_BUTTON_INDEX = 79;
  private static final int DEFAULT_TYPE_FILTER_BUTTON_INDEX = 71;
  private static final int DEFAULT_AVAILABLE_FILTER_BUTTON_INDEX = 80;
  private static final int DEFAULT_SORT_BY_BUTTON_INDEX = 81;
  private static final int DEFAULT_PREVIOUS_PAGE_SLOT = 82;
  private static final int DEFAULT_NEXT_PAGE_SLOT = 87;

  private List<EquipmentSlotConfig> equipmentSlots;
  private int[] presetSlots;
  private int[] gridSlots;
  private int selectedSlotIndex;
  private int removeButtonIndex;
  private int unequipAllButtonIndex;
  private int typeFilterButtonIndex;
  private int availableFilterButtonIndex;
  private int sortByButtonIndex;
  private int previousPageSlot;
  private int nextPageSlot;

  private String messageSelected;
  private String messagePresetLoad;
  private String messagePresetReset;
  private String messagePresetOverwrite;
  private String messagePresetSave;
  private String messageSelectedSlot;
  private String messageRemoveCosmetic;
  private String messageRemoveCosmeticLore;
  private String messageUnequipAll;
  private String messageUnequipAllLore;
  private String messageSelectSlotPrompt;
  private String messagePreviousPage;
  private String messageNextPage;
  private String messagePresetSlotNone;
  private String messagePresetSlotCosmetic;
  private String messagePresetSlotUnknown;

  private String messageTypeFilterAll;
  private String messageTypeFilterSpecific;
  private String messageTypeFilterLore;
  private String messageAvailableOnlyEnabled;
  private String messageAvailableOnlyDisabled;
  private String messageSortByDefault;
  private String messageSortByName;
  private String messageSortByRecent;
  private final java.util.Map<ItemType, String> typeDisplayNames = new java.util.HashMap<>();

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
    file.addDefault("replaceInventory", true);

    ConfigurationSection slotsSection = file.getConfigurationSection("equipmentSlots");
    if (slotsSection == null) {
      slotsSection = file.createSection("equipmentSlots");
    }
    addDefaultEquipmentSlots(slotsSection);

    file.addDefault("presetSlots", List.of(8, 17, 26, 35, 44, 53));
    file.addDefault(
        "gridSlots",
        List.of(55, 56, 57, 58, 59, 60, 64, 65, 66, 67, 68, 69, 73, 74, 75, 76, 77, 78));
    file.addDefault("selectedSlotIndex", 54);
    file.addDefault("removeButtonIndex", 72);
    file.addDefault("unequipAllButtonIndex", 79);
    file.addDefault("typeFilterButtonIndex", 71);
    file.addDefault("availableFilterButtonIndex", 80);
    file.addDefault("sortByButtonIndex", 81);
    file.addDefault("previousPageSlot", 82);
    file.addDefault("nextPageSlot", 87);

    file.addDefault("messages.selected", "&a&lSelected");
    file.addDefault("messages.presetLoad", "&7Left-click to load");
    file.addDefault("messages.presetReset", "&7Shift+Left-click to reset");
    file.addDefault("messages.presetOverwrite", "&7Right-click to overwrite");
    file.addDefault("messages.presetSave", "&7Right-click to save");
    file.addDefault("messages.selectedSlot", "&eSelected: %s");
    file.addDefault("messages.removeCosmetic", "&cRemove Cosmetic");
    file.addDefault("messages.removeCosmeticLore", "&7Click to remove from %s");
    file.addDefault("messages.unequipAll", "&cUnequip All");
    file.addDefault("messages.unequipAllLore", "&7Click to remove all cosmetics");
    file.addDefault("messages.selectSlotPrompt", "&eSelect a slot");
    file.addDefault("messages.previousPage", "&7\u2190 Previous Page");
    file.addDefault("messages.nextPage", "&7Next Page \u2192");
    file.addDefault("messages.presetSlotNone", "&8%s: None");
    file.addDefault("messages.presetSlotCosmetic", "&7%s: &f%s");
    file.addDefault("messages.presetSlotUnknown", "&8%s: Unknown");

    file.addDefault("messages.typeFilterAll", "&bType: &fAll");
    file.addDefault("messages.typeFilterSpecific", "&bType: &f%s");
    file.addDefault("messages.typeFilterLore", "&7Click to cycle type filter");
    file.addDefault("messages.availableOnlyEnabled", "&bAvailable Only: &aYes");
    file.addDefault("messages.availableOnlyDisabled", "&bAvailable Only: &7No");
    file.addDefault("messages.sortByDefault", "&bSort By: &fDefault");
    file.addDefault("messages.sortByName", "&bSort By: &fName");
    file.addDefault("messages.sortByRecent", "&bSort By: &fRecently Weared");
    file.addDefault("messages.type.HAT", "Hat");
    file.addDefault("messages.type.HELMET", "Helmet");
    file.addDefault("messages.type.CHESTPLATE", "Chestplate");
    file.addDefault("messages.type.LEGGINGS", "Leggings");
    file.addDefault("messages.type.BOOTS", "Boots");
    file.addDefault("messages.type.BODY_COSMETIC", "Body Cosmetic");
    file.addDefault("messages.type.ITEM_SKIN", "Item Skin");
    file.addDefault("messages.type.HAT_BODY_COSMETIC", "Hat/Body");
    file.addDefault("messages.type.CHESTPLATE_BODY_COSMETIC", "Chestplate/Body");
    file.addDefault("messages.type.LEGGINGS_BODY_COSMETIC", "Leggings/Body");
    file.addDefault("messages.type.BOOTS_BODY_COSMETIC", "Boots/Body");

    colorPickerConfig.addDefaults(file);
  }

  private void addDefaultEquipmentSlots(ConfigurationSection section) {
    for (EquipmentSlotConfig slot : DEFAULT_EQUIPMENT_SLOTS) {
      ConfigurationSection slotSection = section.getConfigurationSection(slot.key());
      if (slotSection == null) {
        slotSection = section.createSection(slot.key());
      }
      slotSection.addDefault("position", List.of(slot.row(), slot.col()));
      slotSection.addDefault("type", slot.type().toString());
      slotSection.addDefault("visible", slot.visible());
      slotSection.addDefault("displayName", slot.displayName());
    }
  }

  @Override
  protected void loadSpecificConfig(YamlFile file) {
    equipmentSlots = loadEquipmentSlots(file);
    presetSlots = file.getIntegerList("presetSlots").stream().mapToInt(Integer::intValue).toArray();
    gridSlots = file.getIntegerList("gridSlots").stream().mapToInt(Integer::intValue).toArray();
    selectedSlotIndex = file.getInt("selectedSlotIndex", DEFAULT_SELECTED_SLOT_INDEX);
    removeButtonIndex = file.getInt("removeButtonIndex", DEFAULT_REMOVE_BUTTON_INDEX);
    unequipAllButtonIndex = file.getInt("unequipAllButtonIndex", DEFAULT_UNEQUIP_ALL_BUTTON_INDEX);
    typeFilterButtonIndex = file.getInt("typeFilterButtonIndex", DEFAULT_TYPE_FILTER_BUTTON_INDEX);
    availableFilterButtonIndex =
        file.getInt("availableFilterButtonIndex", DEFAULT_AVAILABLE_FILTER_BUTTON_INDEX);
    sortByButtonIndex = file.getInt("sortByButtonIndex", DEFAULT_SORT_BY_BUTTON_INDEX);
    previousPageSlot = file.getInt("previousPageSlot", DEFAULT_PREVIOUS_PAGE_SLOT);
    nextPageSlot = file.getInt("nextPageSlot", DEFAULT_NEXT_PAGE_SLOT);

    messageSelected = file.getString("messages.selected", "&a&lSelected");
    messagePresetLoad = file.getString("messages.presetLoad", "&7Left-click to load");
    messagePresetReset = file.getString("messages.presetReset", "&7Shift+Left-click to reset");
    messagePresetOverwrite =
        file.getString("messages.presetOverwrite", "&7Right-click to overwrite");
    messagePresetSave = file.getString("messages.presetSave", "&7Right-click to save");
    messageSelectedSlot = file.getString("messages.selectedSlot", "&eSelected: %s");
    messageRemoveCosmetic = file.getString("messages.removeCosmetic", "&cRemove Cosmetic");
    messageRemoveCosmeticLore =
        file.getString("messages.removeCosmeticLore", "&7Click to remove from %s");
    messageUnequipAll = file.getString("messages.unequipAll", "&cUnequip All");
    messageUnequipAllLore =
        file.getString("messages.unequipAllLore", "&7Click to remove all cosmetics");
    messageSelectSlotPrompt = file.getString("messages.selectSlotPrompt", "&eSelect a slot");
    messagePreviousPage = file.getString("messages.previousPage", "&7\u2190 Previous Page");
    messageNextPage = file.getString("messages.nextPage", "&7Next Page \u2192");
    messagePresetSlotNone = file.getString("messages.presetSlotNone", "&8%s: None");
    messagePresetSlotCosmetic = file.getString("messages.presetSlotCosmetic", "&7%s: &f%s");
    messagePresetSlotUnknown = file.getString("messages.presetSlotUnknown", "&8%s: Unknown");

    messageTypeFilterAll = file.getString("messages.typeFilterAll", "&bType: &fAll");
    messageTypeFilterSpecific = file.getString("messages.typeFilterSpecific", "&bType: &f%s");
    messageTypeFilterLore =
        file.getString("messages.typeFilterLore", "&7Click to cycle type filter");
    messageAvailableOnlyEnabled =
        file.getString("messages.availableOnlyEnabled", "&bAvailable Only: &aYes");
    messageAvailableOnlyDisabled =
        file.getString("messages.availableOnlyDisabled", "&bAvailable Only: &7No");
    messageSortByDefault = file.getString("messages.sortByDefault", "&bSort By: &fDefault");
    messageSortByName = file.getString("messages.sortByName", "&bSort By: &fName");
    messageSortByRecent = file.getString("messages.sortByRecent", "&bSort By: &fRecently Weared");

    typeDisplayNames.clear();
    for (ItemType t : ItemType.values()) {
      typeDisplayNames.put(t, file.getString("messages.type." + t, t.name()));
    }

    colorPickerConfig.load(file);
  }

  private List<EquipmentSlotConfig> loadEquipmentSlots(YamlFile file) {
    ConfigurationSection section = file.getConfigurationSection("equipmentSlots");
    if (section == null) {
      return new ArrayList<>(DEFAULT_EQUIPMENT_SLOTS);
    }

    List<EquipmentSlotConfig> slots = new ArrayList<>();
    for (String key : section.getKeys(false)) {
      ConfigurationSection slotSection = section.getConfigurationSection(key);
      if (slotSection == null) continue;

      List<Integer> position = slotSection.getIntegerList("position");
      int row = position.size() > 0 ? position.get(0) : 0;
      int col = position.size() > 1 ? position.get(1) : 0;
      String typeString = slotSection.getString("type", "HAT");
      boolean visible = slotSection.getBoolean("visible", true);
      String displayName = slotSection.getString("displayName", key);

      ItemType type;
      try {
        type = ItemType.valueOf(typeString);
      } catch (IllegalArgumentException e) {
        ua.zefir.servercosmetics.ModInit.LOGGER.warn(
            "Unknown ItemType '{}' in equipment slot config '{}', defaulting to HAT",
            typeString,
            key);
        type = ItemType.HAT;
      }

      slots.add(new EquipmentSlotConfig(key, row, col, type, visible, displayName));
    }
    return slots;
  }

  @Override
  public Text getGuiName() {
    return GuiTextures.COSMETICS_MENU.apply(Utils.formatDisplayName(this.guiNameString));
  }

  public ColorPickerConfig getColorPickerConfig() {
    return colorPickerConfig;
  }

  public List<EquipmentSlotConfig> getEquipmentSlots() {
    return equipmentSlots;
  }

  public int[] getPresetSlots() {
    return presetSlots;
  }

  public int[] getGridSlots() {
    return gridSlots;
  }

  public int getSelectedSlotIndex() {
    return selectedSlotIndex;
  }

  public int getRemoveButtonIndex() {
    return removeButtonIndex;
  }

  public int getUnequipAllButtonIndex() {
    return unequipAllButtonIndex;
  }

  public int getPreviousPageSlot() {
    return previousPageSlot;
  }

  public int getNextPageSlot() {
    return nextPageSlot;
  }

  public Text getMessageSelected() {
    return Utils.formatDisplayName(messageSelected);
  }

  public Text getMessagePresetLoad() {
    return Utils.formatDisplayName(messagePresetLoad);
  }

  public Text getMessagePresetReset() {
    return Utils.formatDisplayName(messagePresetReset);
  }

  public Text getMessagePresetOverwrite() {
    return Utils.formatDisplayName(messagePresetOverwrite);
  }

  public Text getMessagePresetSave() {
    return Utils.formatDisplayName(messagePresetSave);
  }

  public Text getMessageSelectedSlot(String slotDisplayName) {
    return Utils.formatDisplayName(String.format(messageSelectedSlot, slotDisplayName));
  }

  public Text getMessageRemoveCosmetic() {
    return Utils.formatDisplayName(messageRemoveCosmetic);
  }

  public Text getMessageRemoveCosmeticLore(String slotDisplayName) {
    return Utils.formatDisplayName(String.format(messageRemoveCosmeticLore, slotDisplayName));
  }

  public Text getMessageUnequipAll() {
    return Utils.formatDisplayName(messageUnequipAll);
  }

  public Text getMessageUnequipAllLore() {
    return Utils.formatDisplayName(messageUnequipAllLore);
  }

  public Text getMessageSelectSlotPrompt() {
    return Utils.formatDisplayName(messageSelectSlotPrompt);
  }

  public Text getMessagePreviousPage() {
    return Utils.formatDisplayName(messagePreviousPage);
  }

  public Text getMessageNextPage() {
    return Utils.formatDisplayName(messageNextPage);
  }

  public Text getMessagePresetSlotNone(String slotDisplayName) {
    return Utils.formatDisplayName(String.format(messagePresetSlotNone, slotDisplayName));
  }

  public Text getMessagePresetSlotCosmetic(String slotDisplayName, String cosmeticDisplayName) {
    return Utils.formatDisplayName(
        String.format(messagePresetSlotCosmetic, slotDisplayName, cosmeticDisplayName));
  }

  public Text getMessagePresetSlotUnknown(String slotDisplayName) {
    return Utils.formatDisplayName(String.format(messagePresetSlotUnknown, slotDisplayName));
  }

  public int getTypeFilterButtonIndex() {
    return typeFilterButtonIndex;
  }

  public int getAvailableFilterButtonIndex() {
    return availableFilterButtonIndex;
  }

  public int getSortByButtonIndex() {
    return sortByButtonIndex;
  }

  public String getTypeDisplayName(ItemType type) {
    return typeDisplayNames.getOrDefault(type, type.name());
  }

  public Text getMessageTypeFilterAll() {
    return Utils.formatDisplayName(messageTypeFilterAll);
  }

  public Text getMessageTypeFilterSpecific(String typeName) {
    return Utils.formatDisplayName(String.format(messageTypeFilterSpecific, typeName));
  }

  public Text getMessageTypeFilterLore() {
    return Utils.formatDisplayName(messageTypeFilterLore);
  }

  public Text getMessageAvailableOnlyEnabled() {
    return Utils.formatDisplayName(messageAvailableOnlyEnabled);
  }

  public Text getMessageAvailableOnlyDisabled() {
    return Utils.formatDisplayName(messageAvailableOnlyDisabled);
  }

  public Text getMessageSortByDefault() {
    return Utils.formatDisplayName(messageSortByDefault);
  }

  public Text getMessageSortByName() {
    return Utils.formatDisplayName(messageSortByName);
  }

  public Text getMessageSortByRecent() {
    return Utils.formatDisplayName(messageSortByRecent);
  }
}
