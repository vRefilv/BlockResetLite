package Refil.blockResetLite.commands;

import Refil.blockResetLite.BlockResetLite;
import Refil.blockResetLite.utils.MineResetUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BlockResetLiteCommand implements CommandExecutor {
    private final BlockResetLite plugin;

    public BlockResetLiteCommand(BlockResetLite plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cUsage: /blockresetlite <subcommand>");
            sender.sendMessage("§aAvailable subcommands: reload, give, reset");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;

            case "reset":
                handleReset(sender, args);
                break;

            default:
                sender.sendMessage("§cUnknown subcommand. Use /blockresetlite for help.");
                break;
        }
        return true;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("blockresetlite.reload")) {
            sender.sendMessage("§cYou do not have permission to reload the configuration.");
            return;
        }
        plugin.reloadConfig();
        sender.sendMessage("§aConfiguration reloaded successfully.");
        plugin.getLogger().info("Configuration reloaded by " + sender.getName());
    }

    private void handleReset(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /blockresetlite reset <mine name>");
            return;
        }

        String mineName = args[1];
        if (!sender.hasPermission("blockresetlite.reset")) {
            sender.sendMessage("§cYou do not have permission to reset mines.");
            return;
        }

        Player player = (Player) sender;

        // Get the RegionManager for the player's current world
        RegionManager regionManager = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .get(BukkitAdapter.adapt(player.getWorld()));

        if (regionManager == null) {
            sender.sendMessage("§cNo regions are defined for this world.");
            return;
        }

        ProtectedRegion region = regionManager.getRegion(mineName);
        if (region == null) {
            sender.sendMessage("§cMine region '" + mineName + "' does not exist.");
            return;
        }

        sender.sendMessage("§aResetting the mine: " + mineName + "...");

        new BukkitRunnable() {
            @Override
            public void run() {
                boolean success = MineResetUtil.resetRegion(plugin, region, player.getWorld());
                if (success) {
                    sender.sendMessage("§aMine " + mineName + " has been successfully reset.");
                } else {
                    sender.sendMessage("§cFailed to reset mine " + mineName + ". Check the configuration.");
                }
            }
        }.runTask(plugin);
    }
}