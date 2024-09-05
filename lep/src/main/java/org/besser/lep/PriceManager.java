package org.besser.lep;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.Map;

public class PriceManager {

    private static List<Map<?, ?>> sellItems;

    public static void loadPricesConfig(Lep plugin) {
        File pricesFile = new File(plugin.getDataFolder(), "prices.yml");
        if (!pricesFile.exists()) {
            plugin.saveResource("prices.yml", false); // Copy the default prices.yml from resources if it doesn't exist
        }
        FileConfiguration pricesConfig = YamlConfiguration.loadConfiguration(pricesFile);
        sellItems = (List<Map<?, ?>>) pricesConfig.getList("sell");
    }

    public static List<Map<?, ?>> getSellItems() {
        return sellItems;
    }
}
