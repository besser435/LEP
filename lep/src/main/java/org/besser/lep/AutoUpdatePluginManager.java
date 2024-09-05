package org.besser.lep;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import static java.util.logging.Level.*;
import static org.besser.lep.LepLogger.log;

// TODO: Check for Minecraft version compatibility. This is not done, and may lead to issues, but its probably fine for TEAW
// TODO: Does not check for config option enable_auto_updates: true
// TODO: There is a bug where it prints that updates are disabled twice, even when enabled. also, why is it printing twice?

public class AutoUpdatePluginManager {
    private final Lep plugin;
    private  boolean enableAutoUpdates;
    private final String githubApiUrl = "https://api.github.com/repos/besser435/LEP/releases/latest";
    private int requestInterval;

    public AutoUpdatePluginManager(Lep plugin) {
        this.plugin = plugin;
        loadConfig();

        if (enableAutoUpdates) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::checkForUpdates, 0L, requestInterval * 20L);
        } else {
            log(WARNING, ChatColor.RED + "Automatic plugin updates are disabled");
        }
    }

    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        enableAutoUpdates = config.getBoolean("lep.enable_auto_updates");
        requestInterval = config.getInt("lep.update_check_interval", 3600);
    }

    public void checkForUpdates() {
        if (!enableAutoUpdates) {
            log(WARNING, ChatColor.RED + "Automatic plugin updates are disabled");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PluginDescriptionFile pdf = plugin.getDescription();
                String currentVersion = pdf.getVersion();

                URL url = new URL(githubApiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Scanner scanner = new Scanner(connection.getInputStream());
                    StringBuilder jsonResponse = new StringBuilder();
                    while (scanner.hasNext()) {
                        jsonResponse.append(scanner.nextLine());
                    }
                    scanner.close();

                    JSONObject jsonObject = new JSONObject(jsonResponse.toString());
                    String latestVersion = jsonObject.getString("tag_name").replace("v", ""); // removes 'v' from version if it's in the tag

                    // TODO: Add proper version checking. This does not test if the remote version is > current version
                    if (currentVersion.equalsIgnoreCase(latestVersion)) {
                        log(INFO, "Plugin is up to date");
                        return;
                    }

                    log(INFO, "Plugin update available: " + latestVersion);

                    JSONArray assets = jsonObject.getJSONArray("assets");
                    if (!assets.isEmpty()) {
                        String downloadUrl = assets.getJSONObject(0).getString("browser_download_url");
                        downloadUpdate(downloadUrl);
                    } else {
                        log(WARNING, "No downloadable assets found for the latest release");
                    }

                } else {
                    log(WARNING, ChatColor.YELLOW + "Failed to fetch latest release info. HTTP code: " + responseCode);
                }

            } catch (Exception e) {
                log(SEVERE, "An Error occurred checking for updates: " + e.getMessage());
            }
        });
    }

    private void downloadUpdate(String downloadUrl) {
        try {
            URL url = new URL(downloadUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/octet-stream");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                log(INFO, "Downloading update...");

                try (InputStream in = new BufferedInputStream(connection.getInputStream())) {
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1);
                    File newJar = new File(plugin.getDataFolder().getParentFile(), fileName);
                    try (FileOutputStream out = new FileOutputStream(newJar)) {
                        byte[] buffer = new byte[1024];
                        int count;
                        while ((count = in.read(buffer)) != -1) {
                            out.write(buffer, 0, count);
                        }
                        log(INFO, "Download complete, update will be applied on next restart");
                    }
                }
            } else {
                log(WARNING, "Failed to download the update. HTTP Response code: " + responseCode);
            }
        } catch (Exception e) {
            log(SEVERE, "An Error occurred downloading the update: " + e.getMessage());
        }
    }
}
