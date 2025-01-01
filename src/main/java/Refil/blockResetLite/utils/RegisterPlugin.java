package Refil.blockResetLite.utils;

import Refil.blockResetLite.BlockResetLite;
import Refil.blockResetLite.events.BlockBreakListener;

public class RegisterPlugin {

    public void registerPlugin(BlockResetLite plugin) {
        registerCommands(plugin);
        registerListeners(plugin);
    }

    private void registerCommands(BlockResetLite plugin) {
        // Register commands (if needed in the future).
        // plugin.getCommand("exampleCommand").setExecutor(new ExampleCommand(plugin));
        // TODO force regenerate /regenerate command
        // TODO command to reload config
        // TODO Customizable Messages
    }

    private void registerListeners(BlockResetLite plugin) {
        // Register the BlockBreakListener.
        plugin.getServer().getPluginManager().registerEvents(new BlockBreakListener(plugin), plugin);
    }
}
