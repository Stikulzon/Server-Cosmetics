# Adding Cosmetics and Skins

Cosmetics and item skins are defined as YAML files inside the `ServerCosmetics/` config directory. The filename (minus `.yml`) becomes the cosmetic/skin ID, which is used to reference its texture and model.

## Cosmetic Types

| Type                                                                                            | Description                                    | Base Item                                                                                 |
|-------------------------------------------------------------------------------------------------|------------------------------------------------|-------------------------------------------------------------------------------------------|
| `HAT`                                                                                           | A hat cosmetic rendered on the head slot       | Can be specified via `material` (e.g. `minecraft:paper`, `minecraft:leather_horse_armor`) |
| `BODY_COSMETIC`                                                                                 | A body cosmetic rendered as a passenger entity | Can be specified via `material`                                                           |
| `HELMET`, `CHESTPLATE`, `LEGGINGS`, `BOOTS`                                                     | Armor cosmetic — helmet                        | Automatically uses chain mail                                                             |
| `CHESTPLATE_BODY_COSMETIC`,`LEGGINGS_BODY_COSMETIC`, `BOOTS_BODY_COSMETIC`, `HAT_BODY_COSMETIC` | Body cosmetic occupying armor slots            | Automatically determined by type                                                          |
| _(no `type` field)_                                                                             | Item skin                                      | Specified via `material` (can be a list)                                                  |

## Hats

**File: `ServerCosmetics/Cosmetics/hats/top_hat.yml`**
```yaml
type: HAT
material: minecraft:leather_horse_armor   # Use leather_horse_armor to make it dyeable via the color picker
display-name: "&bFancy Paintable Hat"

permission: "cosmetics.hat.top_hat"
```

**File: `ServerCosmetics/Cosmetics/hats/shogun.yml`**
```yaml
type: HAT
material: PAPER          # Short form without namespace defaults to minecraft:
display-name: "&bShogun"
sortingPriority: 1        # Lower priority number = sorted first. Default 0 means no sorting (appears last).
lore:
  - "Some description"

permission: "cosmetics.hat.shogun"
```

### Fields

- `type` (required): Must be `HAT` for hat cosmetics.
- `material` (required for hats): The base Minecraft item. `minecraft:leather_horse_armor` makes the cosmetic dyeable via the color picker. You can omit the `minecraft:` prefix.
- `display-name` (string): Name shown in the GUI. Supports QuickText tags and legacy color codes (see [Formatting](Formatting.md)).
- `lore` (list of strings, optional): Description lines shown in the GUI.
- `sortingPriority` (integer, optional): Lower values are sorted first. `0` (default) pushes items to the end.
- `permission` (required): Permission node required to equip this cosmetic.

### Paintable hats

When `material` is `minecraft:leather_horse_armor`, clicking the cosmetic in the GUI opens a color picker instead of equipping it directly. The player can pick a color, then confirm to equip the dyed cosmetic.

## Armor Cosmetics

Armor cosmetics ride on vanilla chainmail armor items using `ArmorTrim` overlays. Because of this, **you do not specify `material`** — it is automatically set to the corresponding chainmail armor piece.

**File: `ServerCosmetics/Cosmetics/armor/flamebringer_helmet.yml`**
```yaml
type: HELMET
display-name: "<color #FF5555>Flame Bringer"
lore:
  - "<white>Author: <blue>Myllyd_Ogoy"

permission: "cosmetics.armor.flamebringer"
```

### Texture naming convention

For a cosmetic set named `flamebringer`, you need these texture files:

```
ServerCosmetics/Assets/textures/armor/
├── flamebringer_helmet.png        (helmet icon)
├── flamebringer_chestplate.png     (chestplate icon)
├── flamebringer_leggings.png       (leggings icon)
├── flamebringer_boots.png          (boots icon)
├── flamebringer_layer_1.png        (armor on body — upper)
└── flamebringer_layer_2.png        (armor on body — lower/legs)
```

The `_layer_1` / `_layer_2` textures are used as trim pattern textures on chainmail armor. Config filenames must follow: `<name>_<armortype>.yml` (e.g. `flamebringer_helmet.yml`.

### Armor types

`HELMET`, `CHESTPLATE`, `LEGGINGS`, `BOOTS` — each requires its own `.yml` file.

## Body Cosmetics

Body cosmetics are rendered as invisible passenger entities on the player.

**File: `ServerCosmetics/Cosmetics/body/body_cosmetic.yml`**
```yaml
type: BODY_COSMETIC
material: PAPER
display-name: "<blue>Body cosmetic"
lore:
  - "Some description"

# Body cosmetic model options (all default to false)
isMirrored: true        # Mirror the model (flip 180° on Y axis)
autoAlignment: true     # Auto-position the model on the player body
offsetWhenSneaking: true # Apply a Y-offset when player is sneaking
autoscale: true          # Auto-scale the model to fit the player

permission: "cosmetics.body.body_cosmetic"
```

### Body cosmetic sub-types

- `BODY_COSMETIC` — standalone body cosmetic (no armor slot)
- `HAT_BODY_COSMETIC` — body cosmetic that occupies the hat slot
- `CHESTPLATE_BODY_COSMETIC` — body cosmetic that occupies the chestplate slot
- `LEGGINGS_BODY_COSMETIC` — body cosmetic that occupies the leggings slot
- `BOOTS_BODY_COSMETIC` — body cosmetic that occupies the boots slot

### Sneaking models

Body cosmetics with `autoAlignment` or `offsetWhenSneaking` require a second model file with the `_sneaking` suffix. For `body_cosmetic.yml`, the mod expects:

```
ServerCosmetics/Assets/models/item/
├── body_cosmetic.json             (standing model)
└── body_cosmetic_sneaking.json     (sneaking model)
```

These sneaking model files are **automatically generated** at runtime by `ConfigManager` — it takes the base model JSON and applies the display transform offsets. You only need to provide the standing model; the sneaking variant is created automatically.

## Item Skins

Item skins override the appearance of vanilla items (like swords, axes, etc.) for players who have the skin equipped. Unlike cosmetics, item skins do **not** have a `type` field.

**File: `ServerCosmetics/ItemSkins/swords/sword_ruby.yml`**
```yaml
material: netherite_sword       # Can be a single item or a list (see below)
display-name: "&dRuby Sword"

permission: "itemskins.sword.sword_ruby"
```

### Multi-material skins

A single skin can apply to multiple item types:

```yaml
material:
  - minecraft:diamond_sword
  - minecraft:netherite_sword
display-name: "&cFlaming Sword Skin"
lore:
  - "A Sword"
  - ""
  - "<red>Makes your sword hot!"
permission: "servercosmetics.skin.flaming_sword"
```

When `material` is a list, each material gets its own internal ID (`<filename>_<material>`), e.g. `flaming_sword_minecraft:diamond_sword`. Textures/models must match each generated ID.

### Fields

- `material` (required): The Minecraft item ID(s) this skin applies to. Can be a string or a list.
- `display-name` (string): Name shown in the Item Skins GUI.
- `lore` (list of strings, optional): Description lines.
- `sortingPriority` (integer, optional): Lower values sorted first.
- `permission` (required): Permission node required to use this skin.

## Adding Textures and Models

ServerCosmetics uses Polymer to generate a server-side resource pack. Custom textures and models are placed in the `Assets/` directory.

### Directory structure

```
ServerCosmetics/Assets/
├── textures/
│   ├── item/            # Standard item textures (placed here by default)
│   ├── item/armor/      # Armor icon textures (*_helmet.png, *_chestplate.png, etc.)
│   ├── hats/            # Hat textures
│   ├── body/            # Body cosmetic textures
│   ├── swords/          # Sword skin textures
│   ├── axes/            # Axe skin textures
│   └── ui/              # GUI background textures
├── models/
│   ├── item/            # Standard item models (placed here by default)
│   ├── item/armor/      # Armor item models
│   ├── hats/            # Hat models
│   ├── body/            # Body cosmetic models
│   ├── swords/          # Sword skin models
│   ├── axes/            # Axe skin models
│   └── ui/              # GUI button models
```

Files can technically be placed in any subdirectory of `Assets/` — the mod scans recursively for `.png`, `.json`, and `.mcmeta` files and routes them to the correct resource pack path based on their filename pattern.

### Filename routing rules

| Filename pattern                                                    | Resource pack target path                                                                           |
|---------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------|
| `*_helmet.png`, `*_chestplate.png`, `*_leggings.png`, `*_boots.png` | `assets/servercosmetics/textures/item/armor/`                                                       |
| `*_layer_1.png`                                                     | `assets/servercosmetics/textures/trims/models/armor/` (renamed to `<name>_<material>.png`)          |
| `*_layer_2.png`                                                     | `assets/servercosmetics/textures/trims/models/armor/` (renamed to `<name>_leggings_<material>.png`) |
| Other `.png`                                                        | `assets/servercosmetics/textures/item/`                                                             |
| `.json` (body cosmetic)                                             | Processed to generate standing + sneaking variants                                                  |
| `.json` (other)                                                     | `assets/servercosmetics/models/item/`                                                               |
| `.mcmeta`                                                           | `assets/servercosmetics/textures/item/`                                                             |

### Texture/model naming

The cosmetic/skin ID (YAML filename without `.yml`) must match the texture and model filenames. For example:
- `top_hat.yml` expects `top_hat.png` and `top_hat.json` in the Asset directories.
- Armor set `flamebringer_helmet.yml` expects `flamebringer_helmet.png`, plus the shared `flamebringer_layer_1.png` / `flamebringer_layer_2.png`.

### Model JSON format

Item models must reference textures using the `servercosmetics:item/<id>` path:

```json
{
  "textures": {
    "0": "servercosmetics:item/cool_hat"
  }
}
```

Animations are also supported (via `.mcmeta` files alongside textures).

### Button textures

GUI buttons defined in config with a `textureName` property will look for a corresponding model at `assets/servercosmetics/models/item/<textureName>.json`. The built-in button textures (`next`, `previous`, `remove`, `paint_button`) are provided by the demo assets.

## Polymer Autohost

To automatically serve the resource pack to players, see the [Official Polymer Documentation](https://polymer.pb4.eu/latest/user/resource-pack-hosting/).