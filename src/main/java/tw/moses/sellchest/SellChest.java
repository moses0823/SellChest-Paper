package tw.moses.sellchest;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class SellChest extends JavaPlugin {
    private Economy economy;

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> registration =
                    getServer().getServicesManager().getRegistration(Economy.class);
            if (registration != null) {
                economy = registration.getProvider();
            }
        }

        getServer().getPluginManager().registerEvents(
                new SellChestListener(this, new SellProcessor(this)), this);
        getCommand("sellchest").setExecutor(new SellChestCommand(this));
        if (economy == null) {
            getLogger().warning("Vault economy is unavailable; sales will not be processed.");
        }
    }

    public Economy getEconomy() {
        return economy;
    }

    public java.io.File getSellPluginConfigFile() {
        return new java.io.File(Bukkit.getPluginsFolder(), "SellPlugin/config.yml");
    }
}