package com.vypnito.arena.player;

import com.vypnito.boxpvprooms.arenas.Arenas;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {
	private final Map<UUID, PlayerState> playerStates = new HashMap<>();
	private final Map<UUID, String> playerArenaNames = new HashMap<>();
	private final Map<UUID, Arenas> playerArenas = new HashMap<>();
	private final Map<UUID, String> matchStates = new HashMap<>();

	// Player state management
	public void setPlayerState(UUID uuid, PlayerState state) {
		playerStates.put(uuid, state);
	}

	public void setPlayerState(Player player, PlayerState state) {
		playerStates.put(player.getUniqueId(), state);
	}

	public PlayerState getPlayerState(UUID uuid) {
		return playerStates.getOrDefault(uuid, PlayerState.IDLE);
	}

	public PlayerState getPlayerState(Player player) {
		return playerStates.getOrDefault(player.getUniqueId(), PlayerState.IDLE);
	}

	public void clearPlayerState(Player player) {
		clearPlayerData(player.getUniqueId());
	}

	// Arena name management for creation/editing
	public void setArenaName(UUID uuid, String arenaName) {
		playerArenaNames.put(uuid, arenaName);
	}

	public String getArenaName(UUID uuid) {
		return playerArenaNames.get(uuid);
	}

	// Arena instance management
	public void setArena(UUID uuid, Arenas arena) {
		playerArenas.put(uuid, arena);
	}

	public Arenas getArena(UUID uuid) {
		return playerArenas.get(uuid);
	}

	// Match state management
	public void setMatchState(UUID uuid, String matchState) {
		matchStates.put(uuid, matchState);
	}

	public String getMatchState(UUID uuid) {
		return matchStates.get(uuid);
	}

	public boolean isInMatch(UUID uuid) {
		return getPlayerState(uuid) == PlayerState.IN_MATCH;
	}

	// Convenience methods
	public boolean isCreatingArena(UUID uuid) {
		return getPlayerState(uuid) == PlayerState.CREATING_ARENA;
	}

	public boolean isEditingArena(UUID uuid) {
		return getPlayerState(uuid) == PlayerState.EDITING_ARENA;
	}

	// Clear all player data
	public void clearPlayerData(UUID uuid) {
		playerStates.remove(uuid);
		playerArenaNames.remove(uuid);
		playerArenas.remove(uuid);
		matchStates.remove(uuid);
	}
}