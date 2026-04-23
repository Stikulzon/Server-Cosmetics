## Commands

All commands require permissions configured in the YAML files. Without a permissions plugin (like LuckPerms), only operators (OP level 4) can use reload commands.

| Command                               | Permission                         | Description                                                                                    |
|---------------------------------------|------------------------------------|------------------------------------------------------------------------------------------------|
| `/cm`, `/cosmetics`                   | `servercosmetics.gui.cosmetics`    | Opens the Cosmetics GUI                                                                        |
| `/cm reload`                          | `servercosmetics.reload.cosmetics` | Reloads cosmetics configs                                                                      |
| `/cosmetics reload`                   | `servercosmetics.reload.cosmetics` | Alias for `/cm reload`                                                                         |
| `/is`, `/itemskins`                   | `servercosmetics.gui.itemskins`    | Opens the Item Skins GUI                                                                       |
| `/is reload`                          | `servercosmetics.reload.itemskins` | Reloads item skins configs                                                                     |
| `/itemskins reload`                   | `servercosmetics.reload.itemskins` | Alias for `/is reload`                                                                         |
| `/sc reload`                          | `servercosmetics.reload`           | Reloads all configs                                                                            |
| `/wearcosmetic <player> <cosmeticId>` | `servercosmetics.wearcosmetic`     | !EXPERIMENTAL! Equips a cosmetic on a player by ID (supports paintable items via color picker) |