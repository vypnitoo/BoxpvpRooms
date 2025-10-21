package com.vypnito.boxpvprooms.commands;

import com.vypnito.boxpvprooms.arenas.ArenaManager;
import com.vypnito.boxpvprooms.arenas.Arenas;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RoomsTabCompleter implements TabCompleter {

    private final ArenaManager arenaManager;

    public RoomsTabCompleter(ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        Player player = (Player) sender;
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Main subcommands
            completions.addAll(Arrays.asList(
                    "wand", "create", "edit", "delete", "mode", "save", "cancel", "reload", "prop", "stats", "leaderboard"
            ));
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
                case "wand":
                    completions.addAll(Arrays.asList("normal", "team", "clan"));
                    break;
                case "edit":
                case "delete":
                case "prop":
                    // Arena names
                    completions.addAll(arenaManager.getArenaNames());
                    break;
                case "mode":
                    completions.addAll(Arrays.asList("zone", "barrier", "teleport"));
                    break;
                case "stats":
                    // Add online player names
                    completions.addAll(player.getServer().getOnlinePlayers().stream()
                            .map(Player::getName)
                            .collect(Collectors.toList()));
                    break;
                case "leaderboard":
                    completions.addAll(Arrays.asList("kills", "deaths", "wins", "losses", "normal_kills", "clan_kills"));
                    break;
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("prop")) {
            // Property names
            completions.addAll(Arrays.asList(
                    "keepinventory", "combatlog", "preventblocks", "barrier", "effect"
            ));
        } else if (args.length == 4 && args[0].equalsIgnoreCase("prop")) {
            String property = args[2].toLowerCase();
            switch (property) {
                case "keepinventory":
                case "combatlog":
                case "preventblocks":
                    completions.addAll(Arrays.asList("true", "false"));
                    break;
                case "barrier":
                    // Common barrier materials
                    completions.addAll(Arrays.asList(
                            "GLASS", "STONE", "COBBLESTONE", "IRON_BARS", "OAK_FENCE", "BEDROCK"
                    ));
                    break;
                case "effect":
                    completions.addAll(Arrays.asList("add", "remove", "clear"));
                    break;
            }
        } else if (args.length == 5 && args[0].equalsIgnoreCase("prop") && args[2].equalsIgnoreCase("effect")) {
            String action = args[3].toLowerCase();
            if (action.equals("add") || action.equals("remove")) {
                // Potion effect types
                for (PotionEffectType effect : PotionEffectType.values()) {
                    if (effect != null) {
                        completions.add(effect.getName());
                    }
                }
            }
        } else if (args.length == 6 && args[0].equalsIgnoreCase("prop") &&
                  args[2].equalsIgnoreCase("effect") && args[3].equalsIgnoreCase("add")) {
            // Effect levels
            completions.addAll(Arrays.asList("0", "1", "2", "3", "4"));
        }

        // Filter completions based on what the player has typed
        String currentInput = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(currentInput))
                .collect(Collectors.toList());
    }
}