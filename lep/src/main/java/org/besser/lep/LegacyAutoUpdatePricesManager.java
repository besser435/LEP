//package org.besser.lep;
//
//import org.bukkit.Bukkit;
//import org.bukkit.ChatColor;
//import org.bukkit.configuration.file.FileConfiguration;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URL;
//
//import static java.util.logging.Level.*;
//import static org.besser.lep.LepLogger.log;
//
//public class LegacyAutoUpdatePricesManager {
//    private final Lep plugin;
//    private boolean enableAutoUpdates;
//    private String apiUrl;
//    private int requestInterval;
//
//    public LegacyAutoUpdatePricesManager(Lep plugin) {
//        this.plugin = plugin;
//        loadConfig();
//
//        if (enableAutoUpdates) {
//            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::checkForUpdates, 0L, requestInterval * 20L);
//        } else {
//            log(WARNING, ChatColor.RED + "Automatic pricing updates are disabled");
//        }
//    }
//
//    private void loadConfig() {
//        FileConfiguration config = plugin.getConfig();
//        enableAutoUpdates = config.getBoolean("prices.enable_auto_updates", true);
//        apiUrl = config.getString("prices.api_url");
//        requestInterval = config.getInt("prices.request_interval", 3600);
//    }
//
//    private void checkForUpdates() {
//        if (!enableAutoUpdates) {
//            log(WARNING, ChatColor.RED + "Automatic pricing updates are disabled");
//            return;
//        }
//
//        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
//            try {
//                URL url = new URL(apiUrl);
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestMethod("GET");
//                connection.setRequestProperty("Accept", "application/x-yaml");
//
//                int responseCode = connection.getResponseCode();
//                if (responseCode == 200) {
//                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                    StringBuilder content = new StringBuilder();
//                    String line;
//
//                    while ((line = in.readLine()) != null) {
//                        content.append(line).append("\n");
//                    }
//
//                    in.close();
//
//                    savePricesFile(content.toString());
//
//                    PriceManager.loadPricesConfig(plugin);
//                    log(INFO, "Prices updated successfully.");
//                    // TODO: add a line at the bottom saying what time and from what URL prices were fetched
//
//                } else {
//                    log(WARNING, "Failed to fetch prices. HTTP code: " + responseCode);
//                }
//
//                connection.disconnect();
//            } catch (Exception e) {
//                log(SEVERE, "An error occurred while checking for updates: " + e.getMessage());
//            }
//        });
//    }
//
//    private void savePricesFile(String content) {
//        try {
//            File pricesFile = new File(plugin.getDataFolder(), "prices.yml");
//            if (!pricesFile.exists()) {
//                pricesFile.createNewFile();
//            }
//
//            FileWriter writer = new FileWriter(pricesFile);
//            writer.write(content);
//            writer.close();
//        } catch (Exception e) {
//            log(SEVERE, "An error occurred while saving prices.yml: " + e.getMessage());
//        }
//    }
//}
