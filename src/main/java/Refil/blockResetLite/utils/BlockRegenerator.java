package Refil.blockResetLite.utils;

import Refil.blockResetLite.BlockResetLite;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BlockRegenerator {

    private final BlockResetLite plugin;

    public BlockRegenerator(BlockResetLite plugin) {
        this.plugin = plugin;
    }

    public void regenerateRegionBlocks(ProtectedRegion region, World world, ConfigurationSection regionConfig) {
        if (region == null || world == null || regionConfig == null) {
            plugin.getLogger().warning("Invalid parameters for block regeneration.");
            return;
        }

        int delay = regionConfig.getInt("regen-delay", 5) * 20; // Convert seconds to ticks
        List<String> blockPalette = regionConfig.getStringList("blocks");
        Map<Material, Integer> blockMap = parseBlockPalette(blockPalette);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Random random = new Random();

            // Define the region boundaries
            BlockVector3 min = region.getMinimumPoint(); // No need for toImmutable()
            BlockVector3 max = region.getMaximumPoint(); // No need for toImmutable()

            for (int x = min.getX(); x <= max.getX(); x++) {
                for (int y = min.getY(); y <= max.getY(); y++) {
                    for (int z = min.getZ(); z <= max.getZ(); z++) {
                        Location blockLocation = new Location(world, x, y, z);
                        Material chosenMaterial = chooseRandomBlock(blockMap, random);

                        if (chosenMaterial != null) {
                            blockLocation.getBlock().setType(chosenMaterial);
                        }
                    }
                }
            }
        }, delay);
    }

    private Map<Material, Integer> parseBlockPalette(List<String> blockPalette) {
        Map<Material, Integer> blockMap = new HashMap<>();
        for (String entry : blockPalette) {
            String[] parts = entry.split(":");
            Material material = Material.getMaterial(parts[0].toUpperCase());
            int chance = Integer.parseInt(parts[1]);
            blockMap.put(material, chance);
        }
        return blockMap;
    }

    private Material chooseRandomBlock(Map<Material, Integer> blockMap, Random random) {
        int totalWeight = blockMap.values().stream().mapToInt(i -> i).sum();
        int roll = random.nextInt(totalWeight);

        int cumulativeWeight = 0;
        for (Map.Entry<Material, Integer> entry : blockMap.entrySet()) {
            cumulativeWeight += entry.getValue();
            if (roll < cumulativeWeight) {
                return entry.getKey();
            }
        }
        return null;
    }
}

//TODO air replace feature if in config