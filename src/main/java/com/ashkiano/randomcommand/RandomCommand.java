package com.ashkiano.randomcommand;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.List;
import java.util.Random;
//TODO přidat hex podporu do další verze
//TODO upravit zobrazování odpočtu a z minut zmenit na vteriny interval odpoctu
public class RandomCommand extends JavaPlugin {
    // Store the settings from the config.yml file in variables
    private List<String> commands;
    private int minInterval, maxInterval;
    private boolean fixedInterval;
    private boolean chatNotification;
    private String chatMessage;
    private boolean countdownEnabled;
    private int countdownInterval;
    // Create a single instance of Random to be used throughout the class
    private Random random = new Random();

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
        countdownEnabled = getConfig().getBoolean("countdownEnabled");
        countdownInterval = getConfig().getInt("countdownInterval");

        // Start the task to execute commands
        runCommandTask();
    }

    // Method to run the command task
    private void runCommandTask() {
        // Calculate the interval until the next command execution
        int interval = fixedInterval ? minInterval : random.nextInt(maxInterval - minInterval + 1) + minInterval;

        // Start the countdown if enabled
        if (countdownEnabled) {
            startCountdown(interval);
        }

        // Schedule the new task
        new BukkitRunnable() {
            @Override
            public void run() {
                // Only execute a command if there are online players
                if (Bukkit.getOnlinePlayers().size() > 0) {
                    // Choose a random command from the list
                    String command = commands.get(random.nextInt(commands.size()));

                    // Choose a random online player
                    Player player = (Player) Bukkit.getOnlinePlayers().toArray()[random.nextInt(Bukkit.getOnlinePlayers().size())];

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
        }.runTaskLater(this, (long) interval * 60 * 20); // Convert minutes to Minecraft ticks (20 ticks = 1 second)
    }

    // Method to start the countdown
    private void startCountdown(int interval) {
        new BukkitRunnable() {
            int countdownTime = interval;

            @Override
            public void run() {
                // Broadcast a countdown message at intervals defined in the config, and for the last 5 minutes
                if (countdownTime % countdownInterval == 0 || countdownTime <= 5) {
                    Bukkit.broadcastMessage(ChatColor.GREEN + "Next command will be executed in " + countdownTime + " minutes.");
                }

                // Cancel the countdown task when the time is up
                if (countdownTime-- <= 0) {
                    this.cancel();
                }
            }
        }.runTaskTimer(this, 0L, 60 * 20); // Convert minutes to Minecraft ticks (20 ticks = 1 second)
    }
}