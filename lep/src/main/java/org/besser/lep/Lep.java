package org.besser.lep;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import static java.util.logging.Level.*;
import static org.besser.lep.LepLogger.log;

public final class Lep extends JavaPlugin {
    private AutoUpdatePricesManager autoUpdatePricesManager;
    private AutoUpdatePluginManager autoUpdatePlugin;


    @Override
    public void onEnable() {
        LepLogger.initialize(this);

        // Enable
        boolean isEnabledInConfig = getConfig().getBoolean("lep.enable", true);
        if (!isEnabledInConfig) {
            log(WARNING, "LEP is disabled in config.yml and will not be fully initialized.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Configs
        saveDefaultConfig();
        PriceManager.loadPricesConfig(this);

        // Auto plugin updates
        autoUpdatePlugin = new AutoUpdatePluginManager(this);

        // Auto pricing updates
        autoUpdatePricesManager = new AutoUpdatePricesManager(this);

        // Buy telemetry

        // Commands
        this.getCommand("lep").setExecutor(new LepCommand(this));

        log(INFO, ChatColor.AQUA + "LEP " + ChatColor.GOLD + "v" + getDescription().getVersion() + ChatColor.RESET + " started!");
    }


    @Override
    public void onDisable() {
        log(INFO,ChatColor.AQUA + "LEP " + ChatColor.GOLD + "v" + getDescription().getVersion() + ChatColor.RESET + " stopped!");
    }
}
