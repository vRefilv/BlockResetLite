package Refil.blockResetLite.utils;

import Refil.blockResetLite.BlockResetLite;
import Refil.blockResetLite.commands.BlockResetLiteCommand;
import Refil.blockResetLite.commands.BlockResetLiteTabCompleter;

public final class RegisterCommands {

    // Private constructor to prevent instantiation
    private RegisterCommands() {
    }

    // Static method to register commands
    public static void registerCommands(BlockResetLite plugin) {
        plugin.getLogger().info("Registering commands...");
        try {
            plugin.getCommand("blockresetlite").setExecutor(new BlockResetLiteCommand(plugin));
            registerTabCompleter(plugin); // Register TabCompleter
            plugin.getLogger().info("Commands registered successfully.");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register commands: " + e.getMessage());
        }
    }

    // Static method to register or re-register the TabCompleter
    public static void registerTabCompleter(BlockResetLite plugin) {
        try {
            plugin.getCommand("blockresetlite").setTabCompleter(new BlockResetLiteTabCompleter(plugin.getConfig()));
            plugin.getLogger().info("TabCompleter registered successfully.");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register TabCompleter: " + e.getMessage());
        }
    }
}