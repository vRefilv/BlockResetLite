package Refil.blockResetLite.utils;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MineResetUtil {

    private static final Random RANDOM = new Random(); // Static Random instance

    /**
     * Resets all blocks in the given region based on the configuration.
     *
     * @param plugin The plugin instance for accessing the configuration.
     * @param region The WorldGuard region to be reset.
     * @param world  The Bukkit world containing the region.
     * @return True if the reset is successful, false otherwise.
     */
    public static boolean resetRegion(Refil.blockResetLite.BlockResetLite plugin, ProtectedRegion region, World world) {
        String regionName = region.getId();
        FileConfiguration config = plugin.getConfig();

        // Fetch block palette from the configuration
        List<String> blockPalette = config.getStringList("regions." + regionName + ".blocks");
        if (blockPalette.isEmpty()) {
            plugin.getLogger().warning("No block palette defined for region: " + regionName);
            return false;
        }

        // Parse the block palette into a map with materials and weights
        Map<Material, Double> blockMap = parseBlockPalette(blockPalette);
        if (blockMap.isEmpty()) {
            plugin.getLogger().warning("Invalid block palette for region: " + regionName);
            return false;
        }

        // Iterate over all blocks in the region's boundaries
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    Material chosenMaterial = chooseRandomBlock(blockMap);
                    if (chosenMaterial != null) {
                        org.bukkit.block.Block block = world.getBlockAt(x, y, z);
                        block.setType(chosenMaterial);
                    }
                }
            }
        }

        plugin.getLogger().info("Region " + regionName + " reset successfully.");
        return true;
    }

    /**
     * Parses the block palette into a map of materials and weights, supporting decimal values.
     *
     * @param blockPalette The list of block configurations (e.g., "STONE:50.5").
     * @return A map of materials and their probabilities (weights).
     */
    private static Map<Material, Double> parseBlockPalette(List<String> blockPalette) {
        Map<Material, Double> blockMap = new HashMap<>();
        for (String entry : blockPalette) {
            String[] parts = entry.split(":");
            Material material = Material.getMaterial(parts[0].toUpperCase());

            if (material != null && parts.length > 1) {
                try {
                    // Parse weight as a double to support small values like 0.01
                    double chance = Double.parseDouble(parts[1]);
                    if (chance > 0) { // Only include positive weights
                        blockMap.put(material, chance);
                    }
                } catch (NumberFormatException e) {
                    // Log an invalid weight, skip this entry
                    continue;
                }
            }
        }
        return blockMap;
    }

    /**
     * Chooses a random block from the block map based on weighted probabilities.
     *
     * @param blockMap A map of materials and their respective weights.
     * @return The selected material or null if not found.
     */
    private static Material chooseRandomBlock(Map<Material, Double> blockMap) {
        // Calculate the total weight (sum of all chances)
        double totalWeight = blockMap.values().stream().mapToDouble(i -> i).sum();
        // Generate a random number between 0 and totalWeight
        double roll = RANDOM.nextDouble() * totalWeight;

        double cumulativeWeight = 0;
        for (Map.Entry<Material, Double> entry : blockMap.entrySet()) {
            cumulativeWeight += entry.getValue(); // Add double weights for each block
            if (roll < cumulativeWeight) {
                return entry.getKey(); // Return the material once the roll falls within this range
            }
        }
        return null; // If something goes wrong, return null (shouldn't happen if input is valid)
    }
}