package Refil.blockResetLite;

import Refil.blockResetLite.utils.MineResetUtil;
import Refil.blockResetLite.utils.RegisterCommands;
import Refil.blockResetLite.utils.RegisterEvents;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.configuration.file.FileConfiguration;

public class BlockResetLite extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("BlockResetLite is enabling...");

        // Reload configuration
        saveDefaultConfig();

        // Register commands and tab completer
        RegisterCommands.registerCommands(this);
        RegisterEvents.registerListeners(this);

        // Reset all mines on start
        resetAllMinesOnStart();

        getLogger().info("BlockResetLite has been successfully enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("BlockResetLite is disabling...");
        getLogger().info("BlockResetLite has been successfully disabled!");
    }

    /**
     * Reset all mines in all worlds when the plugin starts.
     */
    private void resetAllMinesOnStart() {
        getLogger().info("Checking if mines should be reset on server start...");

        FileConfiguration config = getConfig();
        // Check if resetting all mines on start is enabled in configuration
        if (!config.getBoolean("reset-mines-on-start", true)) {
            getLogger().info("Skipping mine resets on plugin start (disabled in config).");
            return;
        }

        getLogger().info("Resetting all mines on plugin start...");

        // Run the reset logic asynchronously to prevent blocking the main thread
        new BukkitRunnable() {
            @Override
            public void run() {
                boolean allSuccess = true;

                // Iterate through all worlds
                for (World world : Bukkit.getWorlds()) {
                    // Retrieve the WorldGuard region manager for this world
                    RegionManager regionManager = WorldGuard.getInstance()
                            .getPlatform()
                            .getRegionContainer()
                            .get(BukkitAdapter.adapt(world));

                    if (regionManager == null) {
                        getLogger().warning("No regions defined for world: " + world.getName());
                        continue;
                    }

                    // Iterate over all regions in the world
                    for (ProtectedRegion region : regionManager.getRegions().values()) {
                        boolean success = MineResetUtil.resetRegion(BlockResetLite.this, region, world);
                        if (!success) {
                            getLogger().severe("Failed to reset mine: " + region.getId() + " in world: " + world.getName());
                            allSuccess = false;
                        }
                    }
                }

                // Log final result
                if (allSuccess) {
                    getLogger().info("All mines have been successfully reset on plugin start!");
                } else {
                    getLogger().warning("Some mines could not be reset. Check the configuration or logs.");
                }
            }
        }.runTask(this); // Run synchronously to ensure it's on the main thread.
    }
}