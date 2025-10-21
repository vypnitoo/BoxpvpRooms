package com.vypnito.boxpvprooms.commands;

import com.vypnito.boxpvprooms.BoxpvpRooms;
import com.vypnito.boxpvprooms.arenas.ArenaManager;
import com.vypnito.boxpvprooms.arenas.Arenas;
import com.vypnito.boxpvprooms.player.SelectionManager;
import com.vypnito.arena.player.PlayerState;
import com.vypnito.boxpvprooms.stats.StatisticsManager;
import com.vypnito.boxpvprooms.enums.RoomType;
import com.vypnito.boxpvprooms.enums.SelectionMode;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class RoomsCommand implements CommandExecutor {

    private final BoxpvpRooms plugin;
    private final ArenaManager arenaManager;
    private final SelectionManager selectionManager;
    private final StatisticsManager statisticsManager;

    public RoomsCommand(BoxpvpRooms plugin, ArenaManager arenaManager, SelectionManager selectionManager,
                        StatisticsManager statisticsManager) {
        this.plugin = plugin;
        this.arenaManager = arenaManager;
        this.selectionManager = selectionManager;
        this.statisticsManager = statisticsManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(getPluginMessage("player-only", "§cThis command can only be used by players!"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "wand":
                return handleWandCommand(player, args);
            case "create":
                return handleCreateCommand(player, args);
            case "edit":
                return handleEditCommand(player, args);
            case "delete":
                return handleDeleteCommand(player, args);
            case "mode":
                return handleModeCommand(player, args);
            case "save":
                return handleSaveCommand(player);
            case "cancel":
                return handleCancelCommand(player);
            case "reload":
                return handleReloadCommand(player);
            case "prop":
                return handlePropCommand(player, args);
            case "stats":
                return handleStatsCommand(player, args);
            case "leaderboard":
            case "top":
                return handleLeaderboardCommand(player, args);
            default:
                sendHelpMessage(player);
                return true;
        }
    }

    private boolean handleWandCommand(Player player, String[] args) {
        if (!player.hasPermission("zoneplugin.create")) {
            player.sendMessage(getPluginMessage("no-permission", "§cYou don't have permission to use this command!"));
            return true;
        }

        RoomType roomType = RoomType.NORMAL;
        if (args.length > 1) {
            String typeArg = args[1].toLowerCase();
            switch (typeArg) {
                case "clan":
                    roomType = RoomType.CLAN;
                    break;
                case "team":
                    roomType = RoomType.TEAM;
                    break;
                case "normal":
                default:
                    roomType = RoomType.NORMAL;
                    break;
            }
        }

        selectionManager.giveWand(player, roomType);
        player.sendMessage("§aReceived selection wand for " + roomType.toString().toLowerCase() + " rooms!");
        return true;
    }

    private boolean handleCreateCommand(Player player, String[] args) {
        if (!player.hasPermission("zoneplugin.create")) {
            player.sendMessage(getPluginMessage("no-permission", "§cYou don't have permission to use this command!"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /rooms create <name>");
            return true;
        }

        String arenaName = args[1];
        if (arenaManager.getArena(arenaName) != null) {
            player.sendMessage("§cArena '" + arenaName + "' already exists!");
            return true;
        }

        plugin.getPlayerManager().setPlayerState(player.getUniqueId(), PlayerState.CREATING_ARENA);
        plugin.getPlayerManager().setArenaName(player.getUniqueId(), arenaName);

        player.sendMessage("§aArena creation started! Use /rooms wand to get the selection tool.");
        player.sendMessage("§7- Left click to set position 1");
        player.sendMessage("§7- Right click to set position 2");
        player.sendMessage("§7- Use /rooms save when ready");
        return true;
    }

    private boolean handleEditCommand(Player player, String[] args) {
        if (!player.hasPermission("zoneplugin.create")) {
            player.sendMessage(getPluginMessage("no-permission", "§cYou don't have permission to use this command!"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /rooms edit <name>");
            return true;
        }

        String arenaName = args[1];
        Arenas arena = arenaManager.getArena(arenaName);
        if (arena == null) {
            player.sendMessage(getPluginMessage("arena-not-found", "§cArena '{name}' not found!", "{name}", arenaName));
            return true;
        }

        plugin.getPlayerManager().setPlayerState(player.getUniqueId(), PlayerState.EDITING_ARENA);
        plugin.getPlayerManager().setArena(player.getUniqueId(), arena);

        player.sendMessage("§aEditing arena: §e" + arenaName);
        player.sendMessage("§7Use /rooms prop " + arenaName + " <property> <value> to edit properties");
        return true;
    }

    private boolean handleDeleteCommand(Player player, String[] args) {
        if (!player.hasPermission("zoneplugin.admin")) {
            player.sendMessage(getPluginMessage("no-permission", "§cYou don't have permission to use this command!"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /rooms delete <name>");
            return true;
        }

        String arenaName = args[1];
        if (arenaManager.deleteArena(arenaName)) {
            player.sendMessage(getPluginMessage("arena-deleted", "§aArena '{name}' has been deleted!", "{name}", arenaName));
        } else {
            player.sendMessage(getPluginMessage("arena-not-found", "§cArena '{name}' not found!", "{name}", arenaName));
        }
        return true;
    }

    private boolean handleModeCommand(Player player, String[] args) {
        if (!player.hasPermission("zoneplugin.create")) {
            player.sendMessage(getPluginMessage("no-permission", "§cYou don't have permission to use this command!"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("§cUsage: /rooms mode <zone/barrier/teleport>");
            return true;
        }

        String modeArg = args[1].toLowerCase();
        SelectionMode mode;

        switch (modeArg) {
            case "zone":
                mode = SelectionMode.ZONE;
                break;
            case "barrier":
                mode = SelectionMode.BARRIER;
                break;
            case "teleport":
                mode = SelectionMode.TELEPORT;
                break;
            default:
                player.sendMessage("§cInvalid mode! Use: zone, barrier, or teleport");
                return true;
        }

        selectionManager.setSelectionMode(player, mode);
        player.sendMessage("§aSelection mode changed to: §e" + mode.toString().toLowerCase());
        return true;
    }

    private boolean handleSaveCommand(Player player) {
        if (!player.hasPermission("zoneplugin.create")) {
            player.sendMessage(getPluginMessage("no-permission", "§cYou don't have permission to use this command!"));
            return true;
        }

        PlayerState state = plugin.getPlayerManager().getPlayerState(player.getUniqueId());
        if (state != PlayerState.CREATING_ARENA && state != PlayerState.EDITING_ARENA) {
            player.sendMessage("§cYou are not currently creating or editing an arena!");
            return true;
        }

        if (state == PlayerState.CREATING_ARENA) {
            String arenaName = plugin.getPlayerManager().getArenaName(player.getUniqueId());
            if (selectionManager.hasCompleteSelection(player)) {
                player.sendMessage("§aArena '" + arenaName + "' created successfully!");
                plugin.getPlayerManager().clearPlayerData(player.getUniqueId());
            } else {
                player.sendMessage("§cPlease select both positions first!");
            }
        } else {
            arenaManager.saveArenas();
            player.sendMessage("§aArena changes saved successfully!");
            plugin.getPlayerManager().clearPlayerData(player.getUniqueId());
        }
        return true;
    }

    private boolean handleCancelCommand(Player player) {
        if (!player.hasPermission("zoneplugin.create")) {
            player.sendMessage(getPluginMessage("no-permission", "§cYou don't have permission to use this command!"));
            return true;
        }

        plugin.getPlayerManager().clearPlayerData(player.getUniqueId());
        selectionManager.clearSelection(player);
        player.sendMessage("§aCancelled current operation.");
        return true;
    }

    private boolean handleReloadCommand(Player player) {
        if (!player.hasPermission("zoneplugin.admin")) {
            player.sendMessage(getPluginMessage("no-permission", "§cYou don't have permission to use this command!"));
            return true;
        }

        plugin.reloadConfig();
        arenaManager.loadArenas();
        player.sendMessage("§aBoxpvpRooms configuration reloaded!");
        return true;
    }

    private boolean handlePropCommand(Player player, String[] args) {
        if (!player.hasPermission("zoneplugin.create")) {
            player.sendMessage(getPluginMessage("no-permission", "§cYou don't have permission to use this command!"));
            return true;
        }

        if (args.length < 4) {
            player.sendMessage("§cUsage: /rooms prop <room> <property> <value>");
            return true;
        }

        String arenaName = args[1];
        String property = args[2].toLowerCase();
        String value = args[3];

        Arenas arena = arenaManager.getArena(arenaName);
        if (arena == null) {
            player.sendMessage(getPluginMessage("arena-not-found", "§cArena '{name}' not found!", "{name}", arenaName));
            return true;
        }

        switch (property) {
            case "keepinventory":
                boolean keepInv = Boolean.parseBoolean(value);
                arena.getSettings().setKeepInventory(keepInv);
                player.sendMessage("§aKeep inventory set to: §e" + keepInv);
                break;
            case "combatlog":
                boolean combatLog = Boolean.parseBoolean(value);
                arena.getSettings().setCombatLog(combatLog);
                player.sendMessage("§aCombat log set to: §e" + combatLog);
                break;
            case "preventblocks":
                boolean preventBlocks = Boolean.parseBoolean(value);
                arena.getSettings().setAllowBlockBreak(!preventBlocks);
                arena.getSettings().setAllowBlockPlace(!preventBlocks);
                player.sendMessage("§aBlock protection set to: §e" + preventBlocks);
                break;
            case "barrier":
                try {
                    Material material = Material.valueOf(value.toUpperCase());
                    arena.getSettings().setWallMaterial(material.toString());
                    player.sendMessage("§aBarrier material set to: §e" + material.toString());
                } catch (IllegalArgumentException e) {
                    player.sendMessage("§cInvalid material: " + value);
                }
                break;
            case "effect":
                return handleEffectProperty(player, arena, args);
            default:
                player.sendMessage("§cInvalid property! Available: keepinventory, combatlog, preventblocks, barrier, effect");
                break;
        }

        arenaManager.saveArenas();
        return true;
    }

    private boolean handleEffectProperty(Player player, Arenas arena, String[] args) {
        if (args.length < 4) {
            player.sendMessage("§cUsage: /rooms prop <room> effect <add/remove/clear> [effect] [level]");
            return true;
        }

        String action = args[3].toLowerCase();

        switch (action) {
            case "add":
                if (args.length < 6) {
                    player.sendMessage("§cUsage: /rooms prop <room> effect add <effect> <level>");
                    return true;
                }
                String effectName = args[4].toUpperCase();
                try {
                    PotionEffectType effect = PotionEffectType.getByName(effectName);
                    if (effect == null) {
                        player.sendMessage("§cInvalid effect: " + effectName);
                        return true;
                    }
                    int level = Integer.parseInt(args[5]);
                    arena.getSettings().addEffect(effectName + ":" + level);
                    player.sendMessage("§aAdded effect: §e" + effectName + " " + (level + 1));
                } catch (NumberFormatException e) {
                    player.sendMessage("§cInvalid level number!");
                }
                break;
            case "remove":
                if (args.length < 5) {
                    player.sendMessage("§cUsage: /rooms prop <room> effect remove <effect>");
                    return true;
                }
                String removeEffect = args[4].toUpperCase();
                arena.getSettings().removeEffect(removeEffect);
                player.sendMessage("§aRemoved effect: §e" + removeEffect);
                break;
            case "clear":
                arena.getSettings().clearEffects();
                player.sendMessage("§aCleared all effects from arena.");
                break;
            default:
                player.sendMessage("§cInvalid action! Use: add, remove, or clear");
                break;
        }
        return true;
    }

    private boolean handleStatsCommand(Player player, String[] args) {
        if (!player.hasPermission("zoneplugin.stats")) {
            player.sendMessage(getPluginMessage("no-permission", "§cYou don't have permission to use this command!"));
            return true;
        }

        String targetPlayer = args.length > 1 ? args[1] : player.getName();

        player.sendMessage("§6Stats for " + targetPlayer + ":");
        player.sendMessage("§7Statistics display will be added soon!");
        return true;
    }

    private boolean handleLeaderboardCommand(Player player, String[] args) {
        if (!player.hasPermission("zoneplugin.stats")) {
            player.sendMessage(getPluginMessage("no-permission", "§cYou don't have permission to use this command!"));
            return true;
        }

        String category = args.length > 1 ? args[1] : "kills";
        player.sendMessage("§6Leaderboard (" + category + "):");
        player.sendMessage("§7Leaderboard display will be added soon!");
        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage("§8§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage("§6§lBoxpvpRooms §7- Advanced Rooms and Betting System");
        player.sendMessage("");
        player.sendMessage("§e/rooms wand [clan/team] §7- Get creation tool");
        player.sendMessage("§e/rooms create <name> §7- Create new zone");
        player.sendMessage("§e/rooms edit <name> §7- Edit existing zone");
        player.sendMessage("§e/rooms delete <name> §7- Delete zone");
        player.sendMessage("§e/rooms mode <zone/barrier/teleport> §7- Change selection mode");
        player.sendMessage("§e/rooms save §7- Save current zone");
        player.sendMessage("§e/rooms cancel §7- Cancel current edit");
        player.sendMessage("§e/rooms reload §7- Reload configuration");
        player.sendMessage("");
        player.sendMessage("§e/rooms prop <room> <property> <value> §7- Set room properties");
        player.sendMessage("§e/rooms stats [player] §7- View statistics");
        player.sendMessage("§e/rooms leaderboard [category] §7- View leaderboards");
        player.sendMessage("§8§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
    }

    private String getPluginMessage(String key, String fallback) {
        return fallback;
    }

    private String getPluginMessage(String key, String fallback, String placeholder, String replacement) {
        String message = getPluginMessage(key, fallback);
        return message.replace(placeholder, replacement);
    }
}