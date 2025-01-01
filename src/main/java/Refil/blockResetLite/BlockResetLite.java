package Refil.blockResetLite;

import Refil.blockResetLite.utils.RegisterPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class BlockResetLite extends JavaPlugin {

    private final RegisterPlugin RegisterUtil = new RegisterPlugin();

    @Override
    public void onEnable() {
        if (!setupDependencies()) {
            getLogger().severe("Missing required dependencies! Disabling BlockResetLite...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        getLogger().info("BlockResetLite is enabled!");
        RegisterUtil.registerPlugin(this); //Register plugin in one line
    }

    @Override
    public void onDisable() {
        getLogger().info("BlockResetLite is disabled!");
    }

    private boolean setupDependencies() {
        // Check for WorldGuard
        Plugin worldGuard = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (worldGuard == null || !worldGuard.isEnabled()) {
            getLogger().severe("WorldGuard is not installed or enabled!");
            return false;
        }

        // Check for FastAsyncWorldEdit (optional dependency)
        Plugin fawe = Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit");
        if (fawe == null || !fawe.isEnabled()) {
            getLogger().warning("FastAsyncWorldEdit is not installed or enabled. Proceeding without it...");
        }

        return true;
    }
}
