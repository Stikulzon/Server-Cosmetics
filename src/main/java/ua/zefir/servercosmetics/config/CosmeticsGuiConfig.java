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
          new EquipmentSlotConfig("head", 1, 3, ItemType.HAT, true),
          new EquipmentSlotConfig("body", 2, 2, ItemType.BODY_COSMETIC, true),
          new EquipmentSlotConfig("chest", 2, 3, ItemType.CHESTPLATE, true),
          new EquipmentSlotConfig("legs", 3, 3, ItemType.LEGGINGS, true),
          new EquipmentSlotConfig("feet", 4, 3, ItemType.BOOTS, true));

  private static final int[] DEFAULT_PRESET_SLOTS = {8, 17, 26, 35, 44, 53};
  private static final int[] DEFAULT_GRID_SLOTS = {
    55, 56, 57, 58, 59, 60, 64, 65, 66, 67, 68, 69, 73, 74, 75, 76, 77, 78
  };
  private static final int DEFAULT_SELECTED_SLOT_INDEX = 54;
  private static final int DEFAULT_REMOVE_BUTTON_INDEX = 72;
  private static final int DEFAULT_PREVIOUS_PAGE_SLOT = 82;
  private static final int DEFAULT_NEXT_PAGE_SLOT = 87;

  private List<EquipmentSlotConfig> equipmentSlots;
  private int[] presetSlots;
  private int[] gridSlots;
  private int selectedSlotIndex;
  private int removeButtonIndex;
  private int previousPageSlot;
  private int nextPageSlot;

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
    file.addDefault("previousPageSlot", 82);
    file.addDefault("nextPageSlot", 87);

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
    }
  }

  @Override
  protected void loadSpecificConfig(YamlFile file) {
    equipmentSlots = loadEquipmentSlots(file);
    presetSlots = file.getIntegerList("presetSlots").stream().mapToInt(Integer::intValue).toArray();
    gridSlots = file.getIntegerList("gridSlots").stream().mapToInt(Integer::intValue).toArray();
    selectedSlotIndex = file.getInt("selectedSlotIndex", DEFAULT_SELECTED_SLOT_INDEX);
    removeButtonIndex = file.getInt("removeButtonIndex", DEFAULT_REMOVE_BUTTON_INDEX);
    previousPageSlot = file.getInt("previousPageSlot", DEFAULT_PREVIOUS_PAGE_SLOT);
    nextPageSlot = file.getInt("nextPageSlot", DEFAULT_NEXT_PAGE_SLOT);
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

      slots.add(new EquipmentSlotConfig(key, row, col, type, visible));
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

  public int getPreviousPageSlot() {
    return previousPageSlot;
  }

  public int getNextPageSlot() {
    return nextPageSlot;
  }
}
