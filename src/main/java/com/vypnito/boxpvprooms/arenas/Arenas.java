package com.vypnito.boxpvprooms.arenas;

import com.vypnito.boxpvprooms.enums.RoomType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.*;

public class Arenas {

    private final String name;
    private Location pos1;
    private Location pos2;
    private ArenaSettings settings;
    private RoomType roomType;

    // Player management
    private final Set<UUID> playersInArena = new HashSet<>();
    private final Map<UUID, String> playerTeams = new HashMap<>(); // For team/clan modes
    private boolean wallActive = false;
    private final Map<Location, BlockData> originalWallBlocks = new HashMap<>();

    // Enhanced location management
    private Location spawnPoint1;
    private Location spawnPoint2;
    private final List<Location> spectatorSpawns = new ArrayList<>();

    // Match management
    private long matchStartTime;
    private boolean matchActive = false;

    public Arenas(String name, Location pos1, Location pos2, ArenaSettings settings) {
        this(name, pos1, pos2, settings, RoomType.NORMAL);
    }

    public Arenas(String name, Location pos1, Location pos2, ArenaSettings settings, RoomType roomType) {
        this.name = name;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.settings = settings;
        this.roomType = roomType != null ? roomType : RoomType.NORMAL;
    }

    public String getName() {
        return name;
    }

    public Location getPos1() {
        return pos1;
    }

    public void setPos1(Location pos1) {
        this.pos1 = pos1;
    }

    public Location getPos2() {
        return pos2;
    }

    public void setPos2(Location pos2) {
        this.pos2 = pos2;
    }

    public ArenaSettings getSettings() {
        return settings;
    }

    public void setSettings(ArenaSettings settings) {
        this.settings = settings;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    // Player management methods
    public void addPlayer(UUID playerUuid) {
        playersInArena.add(playerUuid);
    }

    public void removePlayer(UUID playerUuid) {
        playersInArena.remove(playerUuid);
        playerTeams.remove(playerUuid);
    }

    public Set<UUID> getPlayers() {
        return new HashSet<>(playersInArena);
    }

    public boolean isPlayerInArena(Player player) {
        return playersInArena.contains(player.getUniqueId());
    }

    // Team management for team/clan modes
    public void assignPlayerToTeam(UUID playerUuid, String team) {
        if (roomType == RoomType.TEAM || roomType == RoomType.CLAN) {
            playerTeams.put(playerUuid, team);
        }
    }

    public String getPlayerTeam(UUID playerUuid) {
        return playerTeams.get(playerUuid);
    }

    public Set<UUID> getPlayersInTeam(String team) {
        Set<UUID> teamPlayers = new HashSet<>();
        for (Map.Entry<UUID, String> entry : playerTeams.entrySet()) {
            if (team.equals(entry.getValue())) {
                teamPlayers.add(entry.getKey());
            }
        }
        return teamPlayers;
    }

    public Set<String> getTeams() {
        return new HashSet<>(playerTeams.values());
    }

    // Enhanced spawn point management
    public Location getSpawnPoint1() {
        return spawnPoint1;
    }

    public void setSpawnPoint1(Location spawnPoint1) {
        this.spawnPoint1 = spawnPoint1;
    }

    public Location getSpawnPoint2() {
        return spawnPoint2;
    }

    public void setSpawnPoint2(Location spawnPoint2) {
        this.spawnPoint2 = spawnPoint2;
    }

    public List<Location> getSpectatorSpawns() {
        return new ArrayList<>(spectatorSpawns);
    }

    public void addSpectatorSpawn(Location location) {
        spectatorSpawns.add(location);
    }

    public void removeSpectatorSpawn(Location location) {
        spectatorSpawns.remove(location);
    }

    public void clearSpectatorSpawns() {
        spectatorSpawns.clear();
    }

    // Match management
    public boolean isMatchActive() {
        return matchActive;
    }

    public void startMatch() {
        this.matchActive = true;
        this.matchStartTime = System.currentTimeMillis();
    }

    public void endMatch() {
        this.matchActive = false;
        this.matchStartTime = 0;
    }

    public long getMatchDuration() {
        if (matchStartTime == 0) {
            return 0;
        }
        return System.currentTimeMillis() - matchStartTime;
    }

    // Wall management methods
    public boolean isWallActive() {
        return wallActive;
    }

    public void createBoundaryWall() {
        if (pos1 == null || pos2 == null || settings.getWallMaterial() == null) {
            return;
        }

        Material wallMaterial = settings.getWallMaterial();
        originalWallBlocks.clear();

        Set<Material> replaceableMaterials = ArenaManager.getReplaceableWallMaterials();
        if (replaceableMaterials.isEmpty()) {
            return;
        }

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Location currentLocation = new Location(pos1.getWorld(), x, y, z);

                    if (isLocationInWall(currentLocation)) {
                        Block block = currentLocation.getBlock();

                        if (replaceableMaterials.contains(block.getType())) {
                            originalWallBlocks.put(currentLocation.clone(), block.getBlockData());
                            block.setType(wallMaterial);
                        } else if (block.getType() == wallMaterial) {
                            originalWallBlocks.put(currentLocation.clone(), block.getBlockData());
                        }
                    }
                }
            }
        }
        wallActive = true;
    }

    public void removeBoundaryWall() {
        if (pos1 == null || pos2 == null) {
            return;
        }

        for (Map.Entry<Location, BlockData> entry : originalWallBlocks.entrySet()) {
            Location loc = entry.getKey();
            BlockData data = entry.getValue();
            if (loc.getWorld() != null && loc.getWorld().isChunkLoaded(loc.getChunk())) {
                loc.getBlock().setBlockData(data);
            }
        }
        originalWallBlocks.clear();
        wallActive = false;
    }

    public boolean isLocationInWall(Location location) {
        if (pos1 == null || pos2 == null || location.getWorld() == null ||
                !location.getWorld().equals(pos1.getWorld())) {
            return false;
        }

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        boolean isInBoundedRegion = (x >= minX && x <= maxX &&
                y >= minY && y <= maxY &&
                z >= minZ && z <= maxZ);

        if (!isInBoundedRegion) {
            return false;
        }

        boolean isXBoundary = (x == minX || x == maxX);
        boolean isYBoundary = (y == minY || y == maxY);
        boolean isZBoundary = (z == minZ || z == maxZ);

        return isXBoundary || isYBoundary || isZBoundary;
    }

    // Enhanced location checking for different areas
    public boolean isLocationInArena(Location location) {
        if (pos1 == null || pos2 == null || location.getWorld() == null ||
                !location.getWorld().equals(pos1.getWorld())) {
            return false;
        }

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        return (x >= minX && x <= maxX &&
                y >= minY && y <= maxY &&
                z >= minZ && z <= maxZ);
    }

    // Utility methods for team balancing
    public boolean canAddMorePlayers() {
        int currentPlayers = playersInArena.size();
        int requiredPlayers = settings.getRequiredPlayers();
        return currentPlayers < requiredPlayers;
    }

    public boolean isReadyToStart() {
        int currentPlayers = playersInArena.size();
        int requiredPlayers = settings.getRequiredPlayers();

        if (currentPlayers < requiredPlayers) {
            return false;
        }

        // For team/clan modes, check if teams are balanced
        if (roomType == RoomType.TEAM || roomType == RoomType.CLAN) {
            return areTeamsBalanced();
        }

        return true;
    }

    private boolean areTeamsBalanced() {
        Set<String> teams = getTeams();
        if (teams.size() < 2) {
            return false; // Need at least 2 teams
        }

        // Check if all teams have equal number of players (or differ by at most 1)
        int[] teamSizes = teams.stream()
                .mapToInt(team -> getPlayersInTeam(team).size())
                .toArray();

        int minSize = Arrays.stream(teamSizes).min().orElse(0);
        int maxSize = Arrays.stream(teamSizes).max().orElse(0);

        return (maxSize - minSize) <= 1;
    }

    @Override
    public String toString() {
        return "Arena{" +
                "name='" + name + '\'' +
                ", roomType=" + roomType +
                ", players=" + playersInArena.size() +
                ", wallActive=" + wallActive +
                ", matchActive=" + matchActive +
                '}';
    }
}