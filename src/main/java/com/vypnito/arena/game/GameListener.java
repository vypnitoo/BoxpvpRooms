package com.vypnito.arena.game;

import com.vypnito.arena.arenas.Arenas;
import com.vypnito.arena.arenas.ArenaManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class GameListener implements Listener {
	private final GameManager gameManager;
	private final ArenaManager arenaManager;

	public GameListener(GameManager gameManager) {
		this.gameManager = gameManager;
		this.arenaManager = gameManager.getArenaManager();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		if (event instanceof PlayerTeleportEvent) return;
		if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockY() == event.getTo().getBlockY() && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
			return;
		}
		handleMovement(event.getPlayer(), event.getFrom(), event.getTo());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		handleMovement(event.getPlayer(), event.getFrom(), event.getTo());
	}

	private void handleMovement(Player player, Location from, Location to) {
		Arenas fromArena = arenaManager.findArenaByRegion(from);
		Arenas toArena = arenaManager.findArenaByRegion(to);
		if (fromArena != toArena) {
			if (fromArena != null) {
				fromArena.removePlayer(player.getUniqueId());
				gameManager.onPlayerLeaveArena(player, fromArena);
			}
			if (toArena != null) {
				toArena.addPlayer(player.getUniqueId());
				gameManager.onPlayerEnterArena(player, toArena);
			}
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		Arenas arena = arenaManager.findArenaByRegion(player.getLocation());
		if (arena != null) {
			arena.removePlayer(player.getUniqueId());
			gameManager.onPlayerLeaveArena(player, arena);
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		Arenas arena = arenaManager.findArenaByRegion(player.getLocation());
		if (arena != null) {
			arena.removePlayer(player.getUniqueId());
			gameManager.onPlayerDieInArena(player, arena);
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Arenas blockArena = arenaManager.findArenaByRegion(event.getBlock().getLocation());
		if (blockArena != null && blockArena.isWallActive() && blockArena.isLocationInWall(event.getBlock().getLocation())) {
			event.setCancelled(true);
		} else {
			Arenas playerArena = arenaManager.findArenaByRegion(event.getPlayer().getLocation());
			if (playerArena != null && !playerArena.getSettings().isAllowBlockBreak()) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Arenas arena = arenaManager.findArenaByRegion(event.getBlock().getLocation());
		if (arena != null && !arena.getSettings().isAllowBlockPlace()) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event) {
		Arenas arena = arenaManager.findArenaByRegion(event.getPlayer().getLocation());
		if (arena != null && !arena.getSettings().isAllowItemDrop()) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if (!(event.getEntity() instanceof Player player)) return;
		Arenas arena = arenaManager.findArenaByRegion(player.getLocation());
		if (arena != null && arena.getSettings().isDisableHunger()) {
			event.setCancelled(true);
		}
	}
}