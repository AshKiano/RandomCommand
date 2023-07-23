package com.ashkiano.randomcommand;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
//TODO přidat hex podporu do další verze
//TODO přidat podporu různých jazyků pro hlášku odpočtu do chatu
//TODO upravit zobrezení odpočtu aby kombinovaně zobrazoval minuty a vteřiny a ne jen vteřiny
public class RandomCommand extends JavaPlugin {
    // Variables to store the settings from the config.yml file
    private List<List<String>> commands;
    private int minInterval, maxInterval;
    private boolean fixedInterval;
    private boolean chatNotification;
    private String chatMessage;
    private boolean countdownEnabled;
    private int countdownInterval;
    private String noPermissionMessage, reloadSuccessfulMessage, countdownMessage, nextCommandMessage, commandExecutedMessage;

    // Create a single instance of Random to be used throughout the class
    private Random random = new Random();

    // Called when the plugin is enabled
    @Override
    public void onEnable() {
        // Save a copy of the default config.yml if one is not present
        saveDefaultConfig();

        Metrics metrics = new Metrics(this, 18814);

        this.getCommand("randomcommandreload").setExecutor(this);

        // Load the settings from the config.yml file
        List<List<String>> commandGroups = new ArrayList<>();
        for (Object commandGroupObject : getConfig().getList("commands")) {
            if (commandGroupObject instanceof List) {
                List<String> commandGroup = new ArrayList<>();
                for (Object commandObject : (List<?>) commandGroupObject) {
                    if (commandObject instanceof String) {
                        commandGroup.add((String) commandObject);
                    }
                }
                commandGroups.add(commandGroup);
            }
        }
        commands = commandGroups;
        minInterval = getConfig().getInt("minInterval");
        maxInterval = getConfig().getInt("maxInterval");
        fixedInterval = getConfig().getBoolean("fixedInterval");
        chatNotification = getConfig().getBoolean("chatNotification");
        chatMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("chatMessage", "&6A command has been executed!"));
        countdownEnabled = getConfig().getBoolean("countdownEnabled");
        countdownInterval = getConfig().getInt("countdownInterval");
        noPermissionMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("noPermissionMessage", "&cYou do not have permission to reload the configuration."));
        reloadSuccessfulMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("reloadSuccessfulMessage", "&aConfiguration of the RandomCommand plugin has been successfully reloaded."));
        countdownMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("countdownMessage", "&aNext command will be executed in <time> seconds."));
        nextCommandMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("nextCommandMessage", "Next command will be executed in <time> seconds."));
        commandExecutedMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("commandExecutedMessage", "&6A command has been executed!"));

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
                    // Choose a random command group from the list
                    List<String> commandGroup = commands.get(random.nextInt(commands.size()));

                    // Choose a random online player
                    Player player = (Player) Bukkit.getOnlinePlayers().toArray()[random.nextInt(Bukkit.getOnlinePlayers().size())];

                    // Execute all commands in the group
                    for (String command : commandGroup) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("<player>", player.getName()));
                    }

                    // If chatNotification is enabled, send a message in the chat
                    if(chatNotification) {
                        Bukkit.broadcastMessage(chatMessage.replace("<time>", String.valueOf(interval)));
                    }
                }

                // Print a message in the console indicating the interval until the next command execution
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + nextCommandMessage.replace("<time>", String.valueOf(interval)));

                // Schedule the next command execution
                runCommandTask();
            }
        }.runTaskLater(this, (long) interval * 20); // Convert seconds to Minecraft ticks (20 ticks = 1 second)
    }

    // Method to start the countdown
    private void startCountdown(int interval) {
        new BukkitRunnable() {
            int countdownTime = interval;

            @Override
            public void run() {
                // Broadcast a countdown message at intervals defined in the config, and for the last 5 seconds
                if (countdownTime % countdownInterval == 0 || countdownTime <= 5) {
                    Bukkit.broadcastMessage(ChatColor.GREEN + countdownMessage.replace("<time>", String.valueOf(countdownTime)));
                }

                // Cancel the countdown task when the time is up
                if (countdownTime-- <= 0) {
                    this.cancel();
                }
            }
        }.runTaskTimer(this, 0L, 20); // Convert seconds to Minecraft ticks (20 ticks = 1 second)
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("randomcommandreload")) {
            if (sender.hasPermission("randomcommand.reload")) {
                reloadConfig();
                List<List<String>> commandGroups = new ArrayList<>();
                for (Object commandGroupObject : getConfig().getList("commands")) {
                    if (commandGroupObject instanceof List) {
                        List<String> commandGroup = new ArrayList<>();
                        for (Object commandObject : (List<?>) commandGroupObject) {
                            if (commandObject instanceof String) {
                                commandGroup.add((String) commandObject);
                            }
                        }
                        commandGroups.add(commandGroup);
                    }
                }
                commands = commandGroups;
                minInterval = getConfig().getInt("minInterval");
                maxInterval = getConfig().getInt("maxInterval");
                fixedInterval = getConfig().getBoolean("fixedInterval");
                chatNotification = getConfig().getBoolean("chatNotification");
                chatMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("chatMessage", "&6A command has been executed!"));
                countdownEnabled = getConfig().getBoolean("countdownEnabled");
                countdownInterval = getConfig().getInt("countdownInterval");
                noPermissionMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("noPermissionMessage", "&cYou do not have permission to reload the configuration."));
                reloadSuccessfulMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("reloadSuccessfulMessage", "&aConfiguration of the RandomCommand plugin has been successfully reloaded."));
                countdownMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("countdownMessage", "&aNext command will be executed in <time> seconds."));
                nextCommandMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("nextCommandMessage", "Next command will be executed in <time> seconds."));
                commandExecutedMessage = ChatColor.translateAlternateColorCodes('&', getConfig().getString("commandExecutedMessage", "&6A command has been executed!"));
                sender.sendMessage(ChatColor.GREEN + reloadSuccessfulMessage);
            } else {
                sender.sendMessage(ChatColor.RED + noPermissionMessage);
            }
            return true;
        }
        return false;
    }
}