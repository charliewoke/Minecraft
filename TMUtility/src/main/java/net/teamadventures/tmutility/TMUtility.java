package net.teamadventures.tmutility;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

@SuppressWarnings("ALL")
public class TMUtility extends JavaPlugin implements Listener {

    private static final String PREFIX = ChatColor.GRAY + "[" + ChatColor.GREEN + "Server" + ChatColor.GRAY + "] ";
    private static final String WEBSITE_URL = "example.com";

    @Override
    public void onEnable() {
        // Register the plugin's event listener
        getServer().getPluginManager().registerEvents(this, this);

        // Schedule a repeating task for announcements
        int announcementInterval = 1800; // in seconds (30 minutes)
        new BukkitRunnable() {
            @Override
            public void run() {
                broadcastMessage(ChatColor.YELLOW + "Check out our website at " + WEBSITE_URL + "!");
            }
        }.runTaskTimer(this, announcementInterval * 20, announcementInterval * 20);

        // Log that the plugin has been enabled
        getLogger().info("UtilityPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Log that the plugin has been disabled
        getLogger().info("UtilityPlugin has been disabled!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        event.setJoinMessage(PREFIX + ChatColor.GREEN + player.getName() + " has joined the server!");

        if (player.hasPlayedBefore()) {
            // Send private message to the joining player with information
            player.sendMessage(ChatColor.YELLOW + "Welcome back, " + player.getName() + "!");
            player.sendMessage(ChatColor.YELLOW + "Visit our website at " + WEBSITE_URL + " for more information.");
        } else {
            // Send private message to the joining player for the first time
            player.sendMessage(ChatColor.YELLOW + "Welcome to the server, " + player.getName() + "!");
            player.sendMessage(ChatColor.YELLOW + "Please read our rules at " + WEBSITE_URL + "/rules.");
        }

        // You can perform additional actions when a player joins here
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage(PREFIX + ChatColor.RED + player.getName() + " has left the server!");

        // You can perform additional actions when a player leaves here
    }

    public void broadcastMessage(String message) {
        Bukkit.broadcastMessage(PREFIX + message);
    }
}
