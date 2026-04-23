# Formatting

Display names and lore use **QuickText** format (from [Placeholder API](https://placeholders.pb4.eu/user/quicktext/)), with legacy `&`/`§` color codes also supported. Both can be mixed in the same string.

## QuickText (primary format)

QuickText uses HTML-style tags. See the [full QuickText reference](https://placeholders.pb4.eu/user/quicktext/) for all available tags.

```yaml
# Named colors
display-name: "<blue>Colored</blue> and <green>like this</green>"

# HEX colors
display-name: "<color #d24c9f>HEX color like this</color>"

# Short form: <c> works as an alias for <color>
display-name: "<c #FF5555>Short HEX color</c>"

# Decorations: <bold>/<b>, <italic>/<i>, <underline>, <strikethrough>/<st>, <obfuscated>/<obf>
display-name: "<bold>Important</bold>"

# Gradients
display-name: "<gradient #ff0000 #0000ff>Gradient text</gradient>"

# Rainbow
display-name: "<rainbow>Rainbow text</rainbow>"

# Hover and click
display-name: "<hover 'Tooltip text!'><red>Hover me</red></hover>"
display-name: "<run_cmd '/cm'><yellow>Click to open cosmetics</yellow></run_cmd>"
```

## Legacy format (`&` and `§` codes)

Legacy `&` codes are automatically converted to `§` before QuickText parsing, so they can be freely mixed:

```yaml
# Legacy codes
display-name: "&f Color &c§lcan be§o like this"

# Mixed — legacy and QuickText
display-name: "&f&n Underlined <red>and red</red> in one string"
```

Unicode escapes (`\uXXXX`) are also supported (e.g. `\u00a7` for `§`).

## Example configs

**`shogun.yml`** (cosmetic)
```yaml
type: HAT
material: PAPER
display-name: "<blue>Shogun</blue>"
sortingPriority: 1
lore:
  - "<yellow>Some description with <gradient #ff0 #f00>formatting</gradient></yellow>"

permission: "cosmetics.hat.shogun"
```

**`sword_ruby.yml`** (item skin)
```yaml
material: netherite_sword
display-name: "<color #d24c9f>Ruby Sword</color>"

permission: "itemskins.sword.sword_ruby"
```

**Legacy format** (supported when `legacyMode: true` in `config.yml`):
```yaml
cosmetic-item:
  material: PAPER
  display-name: "&bBeekeeper"
  lore:
    - "<blue>Some </blue> description & &bformatting"

permission: "cosmetics.helmet.beekeeper"
```