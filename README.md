# Sell Chest

SellChest is a Paper plugin that sells priced items from a chest when it is closed or when the chest receives a redstone signal. Redstone-triggered automatic sales are limited to once every 5 seconds per chest. A specially tagged name tag named with the exact prefix `SellChest-` selects the payment recipient; the tag stays in the chest and is never sold. Single and double chests are supported, and no owner or persisted chest state is used.

Administrators can give a player a tagged name tag with `/sellchest give <player>`. The item is named `自動販賣牌` and explains how to rename it. Rename it to `SellChest-玩家名稱`, then place it in a chest to select the payment recipient. Ordinary name tags do not activate sales.

## Requirements

- Java 25
- Paper 26.2
- Vault and a Vault-compatible economy provider
- SellPlugin installed at `plugins/SellPlugin/` with its existing `config.yml`

SellChest reads `plugins/SellPlugin/config.yml` directly each time a marked chest is closed. It expects the observed config format:

```yaml
prices:
  DIAMOND: 30
  IRON_INGOT: 3
```

Each key is a Bukkit material name and each value is the price per item. Items without a price remain in the chest. No prices are copied into SellChest, and SellPlugin's API is not used.

## Build

Run `gradle build` from this project directory. The plugin jar is created in `build/libs/`.