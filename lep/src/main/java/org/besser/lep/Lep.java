package org.besser.lep;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;




import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Instrument;
import org.bukkit.Note;


import static java.util.logging.Level.*;
import static org.besser.lep.LepLogger.log;

public final class Lep extends JavaPlugin implements Listener {
    private PricesAPIManager autoUpdatePricesManager;
    private AutoUpdatePluginManager autoUpdatePlugin;

    @Override
    public void onEnable() {
        LepLogger.initialize(this);

        boolean isEnabledInConfig = getConfig().getBoolean("lep.enable", true);
        if (!isEnabledInConfig) {
            log(WARNING, "LEP is disabled in config.yml and will not be fully initialized.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        PriceManager.loadPricesConfig(this);

        autoUpdatePlugin = new AutoUpdatePluginManager(this);

        autoUpdatePricesManager = new PricesAPIManager(this);

        this.getCommand("lep").setExecutor(new LepCommand(this));

        getServer().getPluginManager().registerEvents(this, this);

        log(INFO, ChatColor.AQUA + "LEP " + ChatColor.GOLD + "v" + getDescription().getVersion() + ChatColor.RESET + " started!");
    }

    @EventHandler
    public void onPotionEffectAdd(EntityPotionEffectEvent event) {
        if (event.getEntity() instanceof Player player && event.getNewEffect() != null) {
            // Despite saying water breathing, its actually invisibility.
            // Bukkit's Material system is bunged.
            if (event.getNewEffect().getType().equals(PotionEffectType.WATER_BREATHING)) {
                event.setCancelled(true);

                player.sendMessage(ChatColor.RED + "toes AC: Invisibility is disabled!");
                player.playNote(player.getLocation(), Instrument.DIDGERIDOO, Note.natural(1, Note.Tone.G));
            }
        }
    }

    @Override
    public void onDisable() {
        log(INFO,ChatColor.AQUA + "LEP " + ChatColor.GOLD + "v" + getDescription().getVersion() + ChatColor.RESET + " stopped!");
    }
}
