package com.ashkiano.randomcommand;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.List;
import java.util.Random;

public class RandomCommand extends JavaPlugin {
    // Define variables to store the settings from the config.yml file
    private List<String> commands;
    private int minInterval, maxInterval;
    private boolean fixedInterval;
    private boolean chatNotification;
    private String chatMessage;

    // Called when the plugin is enabled
    @Override
    public void onEnable() {
        // Save a copy of the default config.yml if one is not present
        saveDefaultConfig();

        Metrics metrics = new Metrics(this, 18814);

        // Load the settings from the config.yml file
        commands = getConfig().getStringList("commands");
        minInterval = getConfig().getInt("minInterval");
        maxInterval = getConfig().getInt("maxInterval");
        fixedInterval = getConfig().getBoolean("fixedInterval");
        chatNotification = getConfig().getBoolean("chatNotification");
        chatMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("chatMessage"));

        // Start the task to execute commands
        runCommandTask();
    }

    private void runCommandTask() {
        // Calculate the interval until the next command execution
        int interval = fixedInterval ? minInterval : new Random().nextInt(maxInterval - minInterval + 1) + minInterval;

        // Schedule the new task
        new BukkitRunnable() {
            @Override
            public void run() {
                // Only execute a command if there are online players
                if (Bukkit.getOnlinePlayers().size() > 0) {
                    // Choose a random command from the list
                    String command = commands.get(new Random().nextInt(commands.size()));

                    // Choose a random online player
                    Player player = (Player) Bukkit.getOnlinePlayers().toArray()[new Random().nextInt(Bukkit.getOnlinePlayers().size())];

                    // Execute the command
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("<player>", player.getName()));

                    // If chatNotification is enabled, send a message in the chat
                    if(chatNotification) {
                        Bukkit.broadcastMessage(chatMessage.replace("<time>", String.valueOf(interval)));
                    }
                }

                // Print a message in the console indicating the interval until the next command execution
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Next command will be executed in " + interval + " minutes.");

                // Schedule the next command execution
                runCommandTask();
            }
        }.runTaskLater(this, (long) interval * 60 * 20); // The interval is multiplied by 60*20 to convert minutes to Minecraft ticks (20 ticks = 1 second)
    }
}

