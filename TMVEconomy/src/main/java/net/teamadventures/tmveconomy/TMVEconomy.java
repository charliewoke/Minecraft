package net.teamadventures.tmveconomy;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.*;

public class TMVEconomy extends JavaPlugin implements Listener {

    private Map<UUID, Integer> pouches;
    private File dataFile;

    @Override
    public void onEnable() {
        pouches = new HashMap<>();
        dataFile = new File(getDataFolder(), "data.yml");

        // Create plugin folder if it doesn't exist
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Load pouch data from file
        loadPouches();

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Save pouch data to file
        savePouches();
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntityType() == EntityType.PLAYER) {
            Player player = (Player) event.getEntity();
            Item item = event.getItem();
            ItemStack itemStack = item.getItemStack();

            if (isCurrency(itemStack.getType())) {
                event.setCancelled(true);
                item.remove();

                String currencyName = itemStack.getType().name();
                int amount = itemStack.getAmount();
                int totalAmount = pouches.getOrDefault(player.getUniqueId(), 0) + amount;

                if (totalAmount <= 1000000) {
                    pouches.put(player.getUniqueId(), totalAmount);
                    sendPickupMessage(player, currencyName, amount);
                } else {
                    player.sendMessage("Your pouch is full! You can't pick up more currencies.");
                }
            }
        }
    }

    private boolean isCurrency(Material material) {
        return material.equals(Material.EMERALD)
                || material.equals(Material.GOLD_INGOT)
                || material.equals(Material.IRON_INGOT)
                || material.equals(Material.BRICK);
    }

    private void sendPickupMessage(Player player, String currencyName, int amount) {
        new BukkitRunnable() {
            @Override
            public void run() {
                player.sendMessage("You picked up " + amount + " " + currencyName);
            }
        }.runTaskLater(this, 30 * 20L); // 30 seconds delay (20 ticks per second)
    }

    private void loadPouches() {
        if (!dataFile.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    UUID uuid = UUID.fromString(parts[0]);
                    int amount = Integer.parseInt(parts[1]);
                    pouches.put(uuid, amount);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void savePouches() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile))) {
            for (Map.Entry<UUID, Integer> entry : pouches.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("balance")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                int balance = pouches.getOrDefault(player.getUniqueId(), 0);
                player.sendMessage("Your current balance: " + balance);
            } else {
                sender.sendMessage("This command can only be executed by a player.");
            }
            return true;
        } else if (command.getName().equalsIgnoreCase("baltop")) {
            List<Map.Entry<UUID, Integer>> sortedPouches = new ArrayList<>(pouches.entrySet());
            sortedPouches.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

            int count = Math.min(sortedPouches.size(), 10); // Top 10 balances
            sender.sendMessage("Balance Top List:");
            for (int i = 0; i < count; i++) {
                Map.Entry<UUID, Integer> entry = sortedPouches.get(i);
                Player player = Bukkit.getPlayer(entry.getKey());
                String playerName = (player != null) ? player.getName() : entry.getKey().toString();
                sender.sendMessage((i + 1) + ". " + playerName + ": " + entry.getValue());
            }
            return true;
        } else if (command.getName().equalsIgnoreCase("balset")) {
            if (args.length < 2) {
                sender.sendMessage("Usage: /balset <player> <amount>");
                return true;
            }

            String playerName = args[0];
            Player targetPlayer = Bukkit.getPlayer(playerName);
            if (targetPlayer == null) {
                sender.sendMessage("Player not found: " + playerName);
                return true;
            }

            int amount;
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid amount specified.");
                return true;
            }

            pouches.put(targetPlayer.getUniqueId(), amount);
            sender.sendMessage("Set balance for " + targetPlayer.getName() + " to: " + amount);
            return true;
        } else if (command.getName().equalsIgnoreCase("baladd") || command.getName().equalsIgnoreCase("balremove")) {
            if (!sender.hasPermission("TMRE." + command.getName())) {
                sender.sendMessage("You don't have permission to use this command.");
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage("Usage: /baladd <player> <amount>");
                return true;
            }

            String playerName = args[0];
            Player targetPlayer = Bukkit.getPlayer(playerName);
            if (targetPlayer == null) {
                sender.sendMessage("Player not found: " + playerName);
                return true;
            }

            int amount;
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid amount specified.");
                return true;
            }

            if (command.getName().equalsIgnoreCase("baladd")) {
                int currentBalance = pouches.getOrDefault(targetPlayer.getUniqueId(), 0);
                int newBalance = currentBalance + amount;
                pouches.put(targetPlayer.getUniqueId(), newBalance);
                sender.sendMessage("Added " + amount + " to " + targetPlayer.getName() + "'s balance. New balance: " + newBalance);
            } else {
                int currentBalance = pouches.getOrDefault(targetPlayer.getUniqueId(), 0);
                int newBalance = Math.max(0, currentBalance - amount);
                pouches.put(targetPlayer.getUniqueId(), newBalance);
                sender.sendMessage("Removed " + amount + " from " + targetPlayer.getName() + "'s balance. New balance: " + newBalance);
            }
            return true;
        }

        return false;
    }
}

