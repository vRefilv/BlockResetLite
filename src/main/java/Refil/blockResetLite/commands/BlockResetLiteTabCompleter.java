package Refil.blockResetLite.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BlockResetLiteTabCompleter implements TabCompleter {

    private final FileConfiguration config;

    public BlockResetLiteTabCompleter(FileConfiguration config) {
        this.config = config;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.add("reload");
            suggestions.add("reset");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            suggestions.add("all"); // Suggest "all" for resetting all mines
            suggestions.addAll(config.getConfigurationSection("regions").getKeys(false));
        }

        return suggestions.stream()
                .filter(suggestion -> suggestion.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}