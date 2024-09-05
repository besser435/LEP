package org.besser.lep;

import org.bukkit.plugin.Plugin;
import java.util.logging.Level;
import java.util.logging.Logger;

// I am a Python person, having to type a billion things before logging is silly.

public class LepLogger {
    private static Logger logger;

    public static void initialize(Plugin plugin) {
        logger = plugin.getLogger();
    }

    public static void log(Level level, String message) {
        // TODO: allow any type for message
        // TODO: Import chat colors, so its not needed in each file, and so we can just call the color

        if (logger != null) {
            logger.log(level, message);
        } else {
            System.out.println("Logger not initialized: " + message);
        }
    }
}