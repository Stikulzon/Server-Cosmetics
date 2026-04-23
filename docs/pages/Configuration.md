# Configuration

After first run, ServerCosmetics generates config files in `<server>/config/ServerCosmetics/`.

## config.yml

Main configuration file. Controls permissions, reload messages, and global behavior.

```yaml
######################
## Main Config File ##
######################

debug: false

# If the mod cannot get permissions from config, the default one will be used
permissions:
  reloadAllConfigs: servercosmetics.reload
  reloadItemSkins: servercosmetics.reload.itemskins
  reloadCosmetics: servercosmetics.reload.cosmetics
configReload:
  message:
    success: '&aConfig successfully reloaded!'
    error: '&cAn error occurred during configs reload!'
enableExperimentalFeatures: false
legacyMode: false # If true, tries to read some fields from older config structures. Recommended: false for new setups.
renderChainmailAsTrim: true # If true, vanilla chainmail armor appears as a trim overlay. If false, chainmail armor becomes transparent.
```

- `renderChainmailAsTrim`: When `true`, any chainmail armor worn by a player is rendered as an `ArmorTrim` overlay (transparent otherwise). This is how armor cosmetics work — they ride on chainmail items. **Do not set this to `false` if you use armor cosmetics.**

## cosmeticsGUI.yml

Controls the Cosmetics GUI layout, slot assignment, color picker, and filter buttons.

```yaml
###############################
## Cosmetics GUI Config File ##
###############################

guiName: '&f솯䍒䍒䍒䍒䍒䍒䍒䍒䍒䍒' # GUI title. Invisible chars + custom model data for background texture.
replaceInventory: false # If true, uses the player's inventory slots as additional GUI display slots.
guiRows: 6 # Number of rows (1-6). Determines the screen handler type.
displaySlots: # Slots used for cosmetic items
  - 19
  - 20
  - 21
  - 22
  - 23
  - 24
  - 25
  - 28
  - 29
  - 30
  - 31
  - 32
  - 33
  - 34
  - 37
  - 38
  - 39
  - 40
  - 41
  - 42
  - 43
permissions:
  openGui: servercosmetics.gui.cosmetics
messages:
  unlocked: §a(Unlocked)
  locked: §c(Locked)
disabledFilters: [] # List of filter keys to disable, e.g. ["hat", "body-cosmetic"]
sortingPriority: 0 # Default sorting priority for items without one defined

# Color picker settings (used for paintable cosmetics with leather_horse_armor base)
slots:
  colorInput: 28
  colorOutput: 34
  color:
    - 21
    - 22
    - 23
    - 30
    - 31
    - 32
    - 39
    - 40
    - 41
  colorGradient:
    - 1
    - 2
    - 3
    - 4
    - 5
    - 6
    - 7
colorPicker:
  hexValues:
    - ff0000
    - ff7700
    - ffff00
    - ff0099
    - ffffff
    - 09ff00
    - 8800ff
    - 0000ff
    - 00ffff
  name: §f섈 # Color picker title
  saturationAdjustmentValue: 20.0
paintItemModelPath: "paint_button" # Polymer model path for the paint brush item in the color picker
bodyCosmeticsAutoAlignment: true # Whether body cosmetics auto-align to the player model
colorInput:
  signType: minecraft:acacia_wall_sign
  signColor: WHITE
  textLines:
    - Enter the color in
    - HEX format
    - 'Example: #FFFFFF'
  messages:
    success: §aColor successfully changed!
    error: §cIncorrect color format!
pageIndicatorEnabled: false

buttons:
  # ... (see defaults in CosmeticsGUIConfig.java)
  # Each button supports: item, slotIndex, name, textureName (optional), lore (optional list)
```

### Slot limits

With `guiRows: 6`: max slot index is **53** (9x6, count starts from 0). `replaceInventory: true` adds 36 more.

### Filter buttons

The cosmetics GUI has built-in filter toggles. Each filter has an "enabled" and "disabled" button config:

| Filter key | Description |
|---|---|
| `filter.show-owned-skins-enabled` / `disabled` | Shows only cosmetics the player has permission for |
| `filter.hats-enabled` / `disabled` | Shows only `HAT` type cosmetics |
| `filter.body-cosmetics-enabled` / `disabled` | Shows only `BODY_COSMETIC` type |
| `filter.chestplate-cosmetics-enabled` / `disabled` | Shows armor cosmetics (`HELMET`, `CHESTPLATE`, `LEGGINGS`, `BOOTS`, and all `*_BODY_COSMETIC` types) |
| `filter.boots-cosmetics-enabled` / `disabled` | Shows `BOOTS` type |

Filters can be disabled entirely by adding their key to the `disabledFilters` list.

## ItemSkinsGUI.yml

Controls the Item Skins GUI layout and buttons.

```yaml
###############################
## ItemSkins GUI Config File ##
###############################

guiName: '&f솱䍒䍒䍒䍒䍒䍒䍒䍒䍒䍒'
replaceInventory: false
guiRows: 6
displaySlots:
  - 19
  # ... same as cosmetics GUI defaults
permissions:
  openGui: servercosmetics.gui.itemskins
messages:
  unlocked: §a(Unlocked)
  locked: §c(Locked)
pageIndicatorEnabled: false
disabledFilters: []

slots:
  itemSlot: 4 # Slot where the player's held item is displayed for skin application

buttons:
  # ... (next, previous, removeSkin, pageIndicator, selectItem, filter buttons)
```

The Item Skins GUI has a `selectItem` placeholder button that appears when the player is not holding an item. The `itemSlot` is where the player's currently held item is displayed — clicking an item in your inventory while the GUI is open switches the target item.