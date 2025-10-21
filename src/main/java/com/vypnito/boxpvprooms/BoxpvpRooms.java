package com.vypnito.boxpvprooms;

import com.vypnito.arena.player.PlayerManager;
import com.vypnito.boxpvprooms.arenas.ArenaManager;
import com.vypnito.boxpvprooms.player.SelectionManager;
import com.vypnito.boxpvprooms.commands.RoomsCommand;
import com.vypnito.boxpvprooms.commands.RoomsTabCompleter;
import com.vypnito.boxpvprooms.stats.StatisticsManager;
import com.vypnito.boxpvprooms.placeholders.BoxpvpPlaceholderExpansion;
import com.vypnito.boxpvprooms.game.PvPArenaListener;
import com.vypnito.boxpvprooms.player.WandListener;

import com.vypnito.boxpvprooms.libs.bstats.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;

public final class BoxpvpRooms extends JavaPlugin {

    private ArenaManager arenaManager;
    private SelectionManager selectionManager;
    private PlayerManager playerManager;
    private StatisticsManager statisticsManager;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.reloadConfig();

        int pluginId = 27652;
        Metrics metrics = new Metrics(this, pluginId);
        getLogger().info("bStats metrics initialized with ID: " + pluginId);

        statisticsManager = new StatisticsManager(this);
        arenaManager = new ArenaManager(this);
        selectionManager = new SelectionManager(this);
        playerManager = new PlayerManager();

        Bukkit.getPluginManager().registerEvents(new PvPArenaListener(this), this);
        Bukkit.getPluginManager().registerEvents(new WandListener(this), this);

        this.getCommand("rooms").setExecutor(new RoomsCommand(this, arenaManager, selectionManager, statisticsManager));
        this.getCommand("rooms").setTabCompleter(new RoomsTabCompleter(arenaManager));

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            try {
                new BoxpvpPlaceholderExpansion(this, statisticsManager).register();
                getLogger().info("PlaceholderAPI found! Registered placeholders.");
            } catch (Exception e) {
                getLogger().warning("PlaceholderAPI found but registration failed: " + e.getMessage());
            }
        } else {
            getLogger().info("PlaceholderAPI not found - placeholders disabled (optional)");
        }

        getLogger().info("BoxpvpRooms v1.0 has been enabled!");
        getLogger().info("Advanced Rooms and Betting System loaded successfully!");
        getLogger().info("bStats metrics active - helping improve the plugin!");
    }

    @Override
    public void onDisable() {
        if (statisticsManager != null) {
            statisticsManager.closeDatabase();
        }
        getLogger().info("BoxpvpRooms has been disabled!");
    }

    // Getters for managers
    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }


    public StatisticsManager getStatisticsManager() {
        return statisticsManager;
    }
}