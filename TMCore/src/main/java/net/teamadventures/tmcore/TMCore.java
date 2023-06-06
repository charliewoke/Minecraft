package net.teamadventures.tmcore;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.logging.Logger;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;


public class TMCore extends JavaPlugin {

    private Logger logger;

    @Override
    public void onEnable() {
        logger = getLogger();
        loadAndEnablePlugin("Plugin1.jar");
        loadAndEnablePlugin("Plugin2.jar");
        // Add more plugins as needed
    }

    @Override
    public void onDisable() {
        // Disable code for your core plugin
    }

    private void loadAndEnablePlugin(String pluginFileName) {
        File pluginFile = new File(getDataFolder().getParent(), pluginFileName);

        if (!pluginFile.exists()) {
            logger.warning("Plugin file not found: " + pluginFileName);
            return;
        }

        try {
            Plugin plugin = Bukkit.getPluginManager().loadPlugin(pluginFile);
            if (plugin == null) {
                logger.warning("Failed to load plugin: " + pluginFileName);
                return;
            }

            Bukkit.getPluginManager().enablePlugin(plugin);
            logger.info("Plugin enabled: " + plugin.getName());
        } catch (InvalidPluginException | InvalidDescriptionException e) {
            logger.warning("Failed to load or enable plugin: " + pluginFileName);
            e.printStackTrace();
        }
    }
}