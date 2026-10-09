package tw.moses.sellchest;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

public final class SellPluginConfig {
    private final SellChest plugin;

    public SellPluginConfig(SellChest plugin) {
        this.plugin = plugin;
    }

    public Map<Material, BigDecimal> loadPrices() {
        File configFile = plugin.getSellPluginConfigFile();
        if (!configFile.isFile()) {
            plugin.getLogger().warning("SellPlugin config was not found: " + configFile);
            return Map.of();
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        ConfigurationSection prices = config.getConfigurationSection("prices");
        if (prices == null) {
            plugin.getLogger().warning("SellPlugin config does not contain a prices section.");
            return Map.of();
        }

        Map<Material, BigDecimal> result = new EnumMap<>(Material.class);
        for (String key : prices.getKeys(false)) {
            Object rawPrice = prices.get(key);
            if (!(rawPrice instanceof Number number)) {
                plugin.getLogger().warning("Ignoring non-numeric SellPlugin price for " + key + ".");
                continue;
            }

            Material material = Material.matchMaterial(key);
            BigDecimal price;
            try {
                price = new BigDecimal(number.toString());
            } catch (NumberFormatException exception) {
                plugin.getLogger().warning("Ignoring invalid SellPlugin price for " + key + ".");
                continue;
            }

            if (material == null || price.signum() < 0) {
                plugin.getLogger().warning("Ignoring invalid SellPlugin material or negative price: " + key + ".");
                continue;
            }
            result.put(material, price);
        }
        return result;
    }
}