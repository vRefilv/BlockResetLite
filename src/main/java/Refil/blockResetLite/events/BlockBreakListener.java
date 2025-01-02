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
        Map<Material, Integer> blockMap = parseBlockPalette(blockPalette);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Material chosenMaterial = chooseRandomBlock(blockMap);
            if (chosenMaterial != null) {
                location.getBlock().setType(chosenMaterial);
            }
        }, delay);
    }

    private Map<Material, Integer> parseBlockPalette(List<String> blockPalette) {
        Map<Material, Integer> blockMap = new HashMap<>();
        for (String entry : blockPalette) {
            String[] parts = entry.split(":");
            Material material = Material.getMaterial(parts[0].toUpperCase());
            if (material != null) {
                int chance = Integer.parseInt(parts[1]); // The weight for this block
                blockMap.put(material, chance);
            }
        }
        return blockMap;
    }

    private Material chooseRandomBlock(Map<Material, Integer> blockMap) {
        int totalWeight = blockMap.values().stream().mapToInt(i -> i).sum(); // Calculate total weight
        int roll = RANDOM.nextInt(totalWeight); // Use static RANDOM instance

        int cumulativeWeight = 0;
        for (Map.Entry<Material, Integer> entry : blockMap.entrySet()) {
            cumulativeWeight += entry.getValue();
            if (roll < cumulativeWeight) {
                return entry.getKey();
            }
        }
        return null; // Default to null if no block is selected
    }
}
