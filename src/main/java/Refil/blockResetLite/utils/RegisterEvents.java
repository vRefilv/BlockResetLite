package Refil.blockResetLite.utils;

import Refil.blockResetLite.BlockResetLite;
import Refil.blockResetLite.events.BlockBreakListener;

public final class RegisterEvents {

    // Private constructor to prevent instantiation
    private RegisterEvents() {
    }

    // Static method to register event listeners
    public static void registerListeners(BlockResetLite plugin) {
        plugin.getLogger().info("Registering event listeners...");
        try {
            plugin.getServer().getPluginManager().registerEvents(new BlockBreakListener(plugin), plugin);
            plugin.getLogger().info("Event listeners registered successfully.");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register event listeners: " + e.getMessage());
        }
    }
}