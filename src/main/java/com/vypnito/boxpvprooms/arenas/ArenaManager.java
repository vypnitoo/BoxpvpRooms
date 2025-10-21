package com.vypnito.boxpvprooms.arenas;

import com.vypnito.boxpvprooms.BoxpvpRooms;
import com.vypnito.boxpvprooms.enums.RoomType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class ArenaManager {

    private final BoxpvpRooms plugin;
    private final Map<String, Arenas> arenas = new HashMap<>();
    private File arenasFile;
    private FileConfiguration arenasConfig;

    private static Set<Material> replaceableWallMaterials = new HashSet<>();

    public ArenaManager(BoxpvpRooms plugin) {
        this.plugin = plugin;
        setupFiles();
        loadArenas();
        loadReplaceableMaterials();
    }

    public static Set<Material> getReplaceableWallMaterials() {
        return replaceableWallMaterials;
    }

    public void loadReplaceableMaterials() {
        replaceableWallMaterials.clear();
        List<String> materialNames = plugin.getConfig().getStringList("arena-settings.replaceable-materials");
        for (String name : materialNames) {
            try {
                Material material = Material.valueOf(name.toUpperCase());
                replaceableWallMaterials.add(material);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid replaceable material in config.yml: " + name);
            }
        }
        plugin.getLogger().info("Loaded " + replaceableWallMaterials.size() + " replaceable wall materials.");
    }

    private void setupFiles() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
        arenasFile = new File(plugin.getDataFolder(), "arenas.yml");
        if (!arenasFile.exists()) {
            try {
                arenasFile.createNewFile();
                plugin.getLogger().info("Arena configuration file arenas.yml created.");
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Error creating arenas.yml file: " + e.getMessage(), e);
            }
        }
        arenasConfig = YamlConfiguration.loadConfiguration(arenasFile);
    }

    public void loadArenas() {
        arenas.clear(); // Clear existing arenas before loading
        if (arenasConfig.contains("arenas")) {
            ConfigurationSection arenasSection = arenasConfig.getConfigurationSection("arenas");
            if (arenasSection != null) {
                for (String arenaName : arenasSection.getKeys(false)) {
                    try {
                        Location pos1 = (Location) arenasSection.get(arenaName + ".pos1");
                        Location pos2 = (Location) arenasSection.get(arenaName + ".pos2");

                        // Load room type (default to NORMAL for backward compatibility)
                        String roomTypeString = arenasSection.getString(arenaName + ".room-type", "NORMAL");
                        RoomType roomType = RoomType.fromString(roomTypeString);

                        // Load spawn points
                        Location spawnPoint1 = (Location) arenasSection.get(arenaName + ".spawn-point-1");
                        Location spawnPoint2 = (Location) arenasSection.get(arenaName + ".spawn-point-2");

                        ConfigurationSection settingsSection = arenasSection.getConfigurationSection(arenaName + ".settings");
                        ArenaSettings settings = new ArenaSettings();

                        if (settingsSection != null) {
                            settings.setWallMaterial(settingsSection.getString("wall-material", "GLASS"));
                            settings.setAllowBlockBreak(settingsSection.getBoolean("allow-block-break", false));
                            settings.setAllowBlockPlace(settingsSection.getBoolean("allow-block-place", false));
                            settings.setAllowItemDrop(settingsSection.getBoolean("allow-item-drop", false));
                            settings.setDisableHunger(settingsSection.getBoolean("disable-hunger", true));
                            settings.setWallRemovalDelay(settingsSection.getInt("wall-removal-delay-seconds", 30));
                            settings.getEffects().clear();
                            settings.getEffects().addAll(settingsSection.getStringList("effects"));
                            settings.setRequiredPlayers(settingsSection.getInt("required-players", 2));

                            // Load enhanced BoxpvpRooms settings
                            settings.setKeepInventory(settingsSection.getBoolean("keep-inventory", false));
                            settings.setCombatLog(settingsSection.getBoolean("combat-log", true));
                            settings.setPreventBlocks(settingsSection.getBoolean("prevent-blocks", true));
                        }

                        Arenas arena = new Arenas(arenaName, pos1, pos2, settings, roomType);

                        // Set spawn points if they exist
                        if (spawnPoint1 != null) arena.setSpawnPoint1(spawnPoint1);
                        if (spawnPoint2 != null) arena.setSpawnPoint2(spawnPoint2);

                        // Load spectator spawns
                        if (arenasSection.contains(arenaName + ".spectator-spawns")) {
                            List<?> spectatorSpawns = arenasSection.getList(arenaName + ".spectator-spawns");
                            if (spectatorSpawns != null) {
                                for (Object spawn : spectatorSpawns) {
                                    if (spawn instanceof Location) {
                                        arena.addSpectatorSpawn((Location) spawn);
                                    }
                                }
                            }
                        }

                        arenas.put(arenaName, arena);
                        plugin.getLogger().info("Arena '" + arenaName + "' (" + roomType.getDisplayName() + ") loaded successfully.");
                    } catch (Exception e) {
                        plugin.getLogger().log(Level.WARNING, "Error loading arena '" + arenaName + "': " + e.getMessage(), e);
                    }
                }
            }
        }
        plugin.getLogger().info("Loaded " + arenas.size() + " arenas total.");
    }

    public void saveArena(Arenas arena) {
        String arenaPath = "arenas." + arena.getName();

        arenasConfig.set(arenaPath + ".pos1", arena.getPos1());
        arenasConfig.set(arenaPath + ".pos2", arena.getPos2());
        arenasConfig.set(arenaPath + ".room-type", arena.getRoomType().name());

        // Save spawn points
        if (arena.getSpawnPoint1() != null) {
            arenasConfig.set(arenaPath + ".spawn-point-1", arena.getSpawnPoint1());
        }
        if (arena.getSpawnPoint2() != null) {
            arenasConfig.set(arenaPath + ".spawn-point-2", arena.getSpawnPoint2());
        }

        // Save spectator spawns
        if (!arena.getSpectatorSpawns().isEmpty()) {
            arenasConfig.set(arenaPath + ".spectator-spawns", arena.getSpectatorSpawns());
        }

        ConfigurationSection settingsSection = arenasConfig.createSection(arenaPath + ".settings");
        ArenaSettings settings = arena.getSettings();

        settingsSection.set("wall-material", settings.getWallMaterial().name());
        settingsSection.set("allow-block-break", settings.isAllowBlockBreak());
        settingsSection.set("allow-block-place", settings.isAllowBlockPlace());
        settingsSection.set("allow-item-drop", settings.isAllowItemDrop());
        settingsSection.set("disable-hunger", settings.isDisableHunger());
        settingsSection.set("wall-removal-delay-seconds", settings.getWallRemovalDelay());
        settingsSection.set("effects", settings.getEffects());
        settingsSection.set("required-players", settings.getRequiredPlayers());

        // Save enhanced BoxpvpRooms settings
        settingsSection.set("keep-inventory", settings.isKeepInventory());
        settingsSection.set("combat-log", settings.isCombatLog());
        settingsSection.set("prevent-blocks", settings.isPreventBlocks());

        saveArenas();
    }

    public void saveArenas() {
        try {
            arenasConfig.save(arenasFile);
            plugin.getLogger().info("All arenas saved successfully.");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Error saving arenas: " + e.getMessage(), e);
        }
    }

    public void createArena(String name, Location pos1, Location pos2, int requiredPlayers, RoomType roomType) {
        if (arenas.containsKey(name)) {
            plugin.getLogger().warning("Cannot create arena: Arena '" + name + "' already exists!");
            return;
        }

        ArenaSettings defaultSettings = createDefaultSettings();
        defaultSettings.setRequiredPlayers(requiredPlayers);

        Arenas newArena = new Arenas(name, pos1, pos2, defaultSettings, roomType);
        arenas.put(name, newArena);
        saveArena(newArena);

        plugin.getLogger().info("New arena '" + name + "' (" + roomType.getDisplayName() + ") created with " + requiredPlayers + " required players.");
    }

    public void createArena(String name, Location pos1, Location pos2, int requiredPlayers) {
        createArena(name, pos1, pos2, requiredPlayers, RoomType.NORMAL);
    }

    private ArenaSettings createDefaultSettings() {
        ArenaSettings settings = new ArenaSettings();

        // Load defaults from config
        ConfigurationSection defaultSettings = plugin.getConfig().getConfigurationSection("default-arena-settings");
        if (defaultSettings != null) {
            settings.setWallMaterial(defaultSettings.getString("wall-material", "GLASS"));
            settings.setAllowBlockBreak(defaultSettings.getBoolean("allow-block-break", false));
            settings.setAllowBlockPlace(defaultSettings.getBoolean("allow-block-place", false));
            settings.setAllowItemDrop(defaultSettings.getBoolean("allow-item-drop", false));
            settings.setDisableHunger(defaultSettings.getBoolean("disable-hunger", true));
            settings.setWallRemovalDelay(defaultSettings.getInt("wall-removal-delay-seconds", 30));
            settings.getEffects().addAll(defaultSettings.getStringList("effects"));
            settings.setRequiredPlayers(defaultSettings.getInt("required-players", 2));
        }

        return settings;
    }

    public Arenas getArena(String name) {
        return arenas.get(name);
    }

    public boolean deleteArena(String name) {
        if (arenas.containsKey(name)) {
            Arenas arena = arenas.get(name);

            // Remove wall if active
            if (arena.isWallActive()) {
                arena.removeBoundaryWall();
            }

            // Clear players
            arena.getPlayers().clear();

            arenas.remove(name);
            arenasConfig.set("arenas." + name, null);
            saveArenas();

            plugin.getLogger().info("Arena '" + name + "' deleted successfully.");
            return true;
        } else {
            plugin.getLogger().warning("Cannot delete arena: Arena '" + name + "' does not exist!");
            return false;
        }
    }

    public List<String> getArenaNames() {
        return new ArrayList<>(arenas.keySet());
    }

    public Collection<Arenas> getAllArenas() {
        return new ArrayList<>(arenas.values());
    }

    public List<Arenas> getArenasByType(RoomType roomType) {
        return arenas.values().stream()
                .filter(arena -> arena.getRoomType() == roomType)
                .toList();
    }

    public Arenas findArenaByRegion(Location location) {
        if (location.getWorld() == null) {
            return null;
        }

        for (Arenas arena : arenas.values()) {
            if (arena.isLocationInArena(location)) {
                return arena;
            }
        }
        return null;
    }

    // Enhanced arena management methods
    public List<Arenas> getAvailableArenas() {
        return arenas.values().stream()
                .filter(arena -> !arena.isMatchActive() && arena.canAddMorePlayers())
                .toList();
    }

    public List<Arenas> getActiveArenas() {
        return arenas.values().stream()
                .filter(Arenas::isMatchActive)
                .toList();
    }

    public int getTotalArenas() {
        return arenas.size();
    }

    public int getActiveMatches() {
        return (int) arenas.values().stream()
                .filter(Arenas::isMatchActive)
                .count();
    }

    // Utility methods for arena validation
    public boolean isValidArenaName(String name) {
        return name != null && !name.trim().isEmpty() && !arenas.containsKey(name);
    }

    public boolean arePositionsValid(Location pos1, Location pos2) {
        if (pos1 == null || pos2 == null) {
            return false;
        }

        if (!pos1.getWorld().equals(pos2.getWorld())) {
            return false;
        }

        // Check if positions are different
        return !pos1.equals(pos2);
    }

    // Team management utilities for team/clan arenas
    public void balanceTeams(Arenas arena) {
        if (arena.getRoomType() != RoomType.TEAM && arena.getRoomType() != RoomType.CLAN) {
            return;
        }

        Set<UUID> players = arena.getPlayers();
        List<UUID> playerList = new ArrayList<>(players);

        // Clear existing teams
        for (UUID player : players) {
            arena.assignPlayerToTeam(player, null);
        }

        // Assign to teams alternately
        String[] teamNames = {"RED", "BLUE"};
        for (int i = 0; i < playerList.size(); i++) {
            String team = teamNames[i % teamNames.length];
            arena.assignPlayerToTeam(playerList.get(i), team);
        }
    }

    @Override
    public String toString() {
        return "ArenaManager{" +
                "totalArenas=" + arenas.size() +
                ", activeMatches=" + getActiveMatches() +
                ", availableArenas=" + getAvailableArenas().size() +
                '}';
    }
}