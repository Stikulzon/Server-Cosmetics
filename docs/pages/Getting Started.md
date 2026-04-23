TODO: Update docs to 1.21.10
# Getting Started

ServerCosmetics is a **server-side Fabric mod** available on [Modrinth](https://modrinth.com/mod/server-cosmetics).

### Prerequisites

- **Minecraft 1.21.10**
- **Fabric API**
- **Polymer** (auto-hosted resource pack support recommended, see [Polymer autohost docs](https://polymer.pb4.eu/latest/user/resource-pack-hosting/))
- **LuckPerms** (recommended for permission management)

### Resource Pack

On first run, demo cosmetics and their textures are extracted to `<server>/config/ServerCosmetics/`. Players need the resource pack to see cosmetics — either host it via Polymer autohost or distribute it manually. The resource pack is automatically generated from assets in the `Assets/` directory at server startup or [mod reload](Commands.md).

## Configuration Directory

After first run, configs live in `<server>/config/ServerCosmetics/`:

```
config/ServerCosmetics/
├── config.yml              — Main config (permissions, reload messages, legacy mode, chainmail rendering)
├── cosmeticsGUI.yml        — Cosmetics GUI layout, buttons, color picker, filters
├── ItemSkinsGUI.yml        — Item Skins GUI layout, buttons, filters
├── Cosmetics/              — Cosmetic definition YAML files (hats, armor, body cosmetics)
│   ├── hats/
│   ├── armor/
│   └── body/
├── ItemSkins/               — Item skin definition YAML files
│   ├── swords/
│   └── axes/
└── Assets/                 — Custom textures and models for the resource pack
    ├── textures/
    │   ├── hats/
    │   ├── armor/
    │   ├── item/
    │   └── ui/
    └── models/
        ├── hats/
        ├── armor/
        └── item/
```

Demo configs are extracted on first run if they don't already exist.