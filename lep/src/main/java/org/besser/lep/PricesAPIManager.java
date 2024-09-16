package org.besser.lep;

import org.bukkit.configuration.file.FileConfiguration;
import static spark.Spark.*;


import java.io.File;
import java.io.FileWriter;

import static java.util.logging.Level.*;
import static org.besser.lep.LepLogger.log;

public class PricesAPIManager {
    private final Lep plugin;
    private boolean enablePricingUpdates;
    private String authorizedKey;
    private int apiPort;

    // TODO: Spark is deprecated. Transition to Javalin

    public PricesAPIManager(Lep plugin) {
        this.plugin = plugin;
        loadConfig();
        initRoutes();
    }

    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        enablePricingUpdates = config.getBoolean("prices.enable_pricing_updates", true);
        authorizedKey = config.getString("prices.authorized_key");
        apiPort = config.getInt("prices.api_port", 1851);
    }

    private void initRoutes() {
        port(apiPort);

        put("/api/prices/update_prices", (request, response) -> {
            String requestKey = request.queryParams("api_key");

            if (!authorizedKey.equals(requestKey)) {
                response.status(401);
                log(INFO, "Unauthorized API request attempted to update prices from IP: " + request.ip());
                return "Unauthorized";
            }

            String newPrices = request.body();

            if (savePricesFile(newPrices)) {
                PriceManager.loadPricesConfig(plugin);
                log(INFO, "Prices updated successfully from IP: " + request.ip());
                response.status(200);
                return "OK";
            } else {
                response.status(500);
                return "Internal Error";
            }
        });

//        get("/api/prices/get_prices", (request, response) -> {
//        });
    }

    private boolean savePricesFile(String content) {
        try {
            File pricesFile = new File(plugin.getDataFolder(), "prices.yml");
            if (!pricesFile.exists()) {
                pricesFile.createNewFile();
            }

            FileWriter writer = new FileWriter(pricesFile);
            writer.write(content);
            writer.close();
            return true;
        } catch (Exception e) {
            log(SEVERE, "An error occurred while saving prices.yml: " + e.getMessage());
            return false;
        }
    }
}
