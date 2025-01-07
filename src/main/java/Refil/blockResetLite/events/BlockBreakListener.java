package Refil.blockResetLite.events;

import Refil.blockResetLite.BlockResetLite;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BlockBreakListener implements Listener {

    private static final Random RANDOM = new Random(); // Static Random instance
    private final BlockResetLite plugin;

    public BlockBreakListener(BlockResetLite plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();
        World world = location.getWorld();

        if (world == null) {
            return; // No world context; ignore this event
        }

        // Get the RegionManager for the world
        RegionManager regionManager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(world));

        if (regionManager == null) {
            return; // No regions are defined for this world
        }

        // Check if the block is inside any configured region
        BlockVector3 blockPosition = BukkitAdapter.asBlockVector(location);
        ApplicableRegionSet applicableRegions = regionManager.getApplicableRegions(blockPosition);

        for (ProtectedRegion region : applicableRegions) {
            String regionName = region.getId();
            ConfigurationSection regionConfig = plugin.getConfig().getConfigurationSection("regions." + regionName);

            if (regionConfig != null) {
                scheduleRegeneration(event.getBlock().getLocation(), region, regionConfig);
            }
        }
    }

    private void scheduleRegeneration(Location location, ProtectedRegion region, ConfigurationSection regionConfig) {
        int delay = regionConfig.getInt("regen-delay", 5) * 20; // Convert seconds to ticks
        List<String> blockPalette = regionConfig.getStringList("blocks");
        Map<Material, Double> blockMap = parseBlockPalette(blockPalette);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Material chosenMaterial = chooseRandomBlock(blockMap);
            if (chosenMaterial != null) {
                location.getBlock().setType(chosenMaterial);
            }
        }, delay);
    }

    private Map<Material, Double> parseBlockPalette(List<String> blockPalette) {
        Map<Material, Double> blockMap = new HashMap<>();
        for (String entry : blockPalette) {
            String[] parts = entry.split(":");
            Material material = Material.getMaterial(parts[0].toUpperCase());
            if (material != null) {
                try {
                    // Parse weight as a double to support small values like 0.01
                    double chance = Double.parseDouble(parts[1]);
                    if (chance > 0) { // Only include positive weights
                        blockMap.put(material, chance);
                    }
                } catch (NumberFormatException e) {
                    // Skip invalid entry
                    plugin.getLogger().warning("Invalid block weight format: " + entry);
                }
            }
        }
        return blockMap;
    }

    private Material chooseRandomBlock(Map<Material, Double> blockMap) {
        double totalWeight = blockMap.values().stream().mapToDouble(i -> i).sum(); // Calculate total weight
        double roll = RANDOM.nextDouble() * totalWeight; // Generate a random number with high precision

        double cumulativeWeight = 0;
        for (Map.Entry<Material, Double> entry : blockMap.entrySet()) {
            cumulativeWeight += entry.getValue();
            if (roll <= cumulativeWeight) {
                return entry.getKey();
            }
        }
        return null; // Default to null if no block is selected
    }
}