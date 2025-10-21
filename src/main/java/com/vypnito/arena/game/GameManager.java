package com.vypnito.arena.game;

import com.vypnito.arena.Arena;
import com.vypnito.arena.arenas.Arenas;
import com.vypnito.arena.arenas.ArenaManager;
import com.vypnito.boxpvprooms.BoxpvpRooms;
import com.vypnito.boxpvprooms.stats.StatisticsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class GameManager {
	private final Plugin plugin;
	private final ArenaManager arenaManager;
	private final StatisticsManager statisticsManager;
	private final Map<UUID, PlayerEffectSnapshot> savedPlayerEffects = new HashMap<>();

	private record PlayerEffectSnapshot(long timestamp, Collection<PotionEffect> effects) {}

	// Constructor for legacy Arena plugin
	public GameManager(Arena plugin, ArenaManager arenaManager) {
		this.plugin = plugin;
		this.arenaManager = arenaManager;
		this.statisticsManager = null;
	}

	// Constructor for BoxpvpRooms plugin with statistics
	public GameManager(BoxpvpRooms plugin, com.vypnito.boxpvprooms.arenas.ArenaManager arenaManager, StatisticsManager statisticsManager) {
		this.plugin = plugin;
		this.arenaManager = null; // We'll need to convert between arena managers
		this.statisticsManager = statisticsManager;
	}

	public ArenaManager getArenaManager() {
		return arenaManager;
	}

	public void onPlayerEnterArena(Player player, Arenas arena) {
		Collection<PotionEffect> currentEffects = new ArrayList<>(player.getActivePotionEffects());
		savedPlayerEffects.put(player.getUniqueId(), new PlayerEffectSnapshot(System.currentTimeMillis(), currentEffects));
		applyArenaEffects(player, arena);
		checkWallCreation(arena);
	}

	public void onPlayerLeaveArena(Player player, Arenas arena) {
		restoreOriginalEffects(player);
		if (arena.getPlayers().size() < arena.getSettings().getRequiredPlayers()) {
			removeWallInstantly(arena);
		}
	}

	public void onPlayerDieInArena(Player player, Arenas arena) {
		restoreOriginalEffects(player);
		if (arena.getPlayers().size() < arena.getSettings().getRequiredPlayers()) {
			sendDelayedWallMessage(player, arena);
			removeWallWithDelay(arena);
		}
	}

	public void checkWallCreation(Arenas arena) {
		if (arena == null) return;
		if (arena.getPlayers().size() >= arena.getSettings().getRequiredPlayers() && !arena.isWallActive()) {
			arena.createBoundaryWall();
			arena.getPlayers().forEach(uuid -> {
				Player p = Bukkit.getPlayer(uuid);
				if (p != null) {
					p.sendMessage(Component.text("The arena has been sealed!", NamedTextColor.YELLOW));
				}
			});
		}
	}

	public void removeWallInstantly(Arenas arena) {
		if (arena != null && arena.isWallActive()) {
			arena.removeBoundaryWall();
		}
	}

	public void removeWallWithDelay(Arenas arena) {
		if (arena != null && arena.isWallActive()) {
			final Arenas finalArena = arena;
			new BukkitRunnable() {
				@Override
				public void run() {
					if (finalArena.getPlayers().size() < finalArena.getSettings().getRequiredPlayers()) {
						finalArena.removeBoundaryWall();
						finalArena.getPlayers().forEach(uuid -> {
							Player p = Bukkit.getPlayer(uuid);
							if (p != null) {
								p.sendMessage(Component.text("The arena wall has been removed.", NamedTextColor.GRAY));
							}
						});
					}
				}
			}.runTaskLater(plugin, arena.getSettings().getWallRemovalDelay() * 20L);
		}
	}

	private void applyArenaEffects(Player player, Arenas arena) {
		// Remove all current effects efficiently
		for (PotionEffect effect : new ArrayList<>(player.getActivePotionEffects())) {
			player.removePotionEffect(effect.getType());
		}

		for (String effectString : arena.getSettings().getEffects()) {
			try {
				String[] parts = effectString.split(":");
				String effectName = parts[0].toLowerCase().trim();

				// Use modern Registry API with NamespacedKey for 1.17+
				NamespacedKey key = NamespacedKey.minecraft(effectName);
				PotionEffectType type = Registry.POTION_EFFECT_TYPE.get(key);

				int amplifier = Integer.parseInt(parts[1].trim());
				if (type != null) {
					player.addPotionEffect(new PotionEffect(type, Integer.MAX_VALUE, amplifier, false, false));
				} else {
					plugin.getLogger().warning("Unknown potion effect: " + effectName);
				}
			} catch (Exception e) {
				plugin.getLogger().warning("Error parsing effect '" + effectString + "'. Check format (e.g., 'speed:1').");
			}
		}
	}

	private void restoreOriginalEffects(Player player) {
		// Remove all current effects efficiently
		for (PotionEffect effect : new ArrayList<>(player.getActivePotionEffects())) {
			player.removePotionEffect(effect.getType());
		}
		PlayerEffectSnapshot snapshot = savedPlayerEffects.remove(player.getUniqueId());
		if (snapshot == null) return;

		long elapsedTicks = (System.currentTimeMillis() - snapshot.timestamp()) / 50;

		for (PotionEffect savedEffect : snapshot.effects()) {
			int newDuration = savedEffect.getDuration() - (int) elapsedTicks;
			if (newDuration > 0) {
				player.addPotionEffect(new PotionEffect(savedEffect.getType(), newDuration, savedEffect.getAmplifier(), savedEffect.isAmbient(), savedEffect.hasParticles()));
			}
		}
	}

	public void sendDelayedWallMessage(Player player, Arenas arena) {
		final int delay = arena.getSettings().getWallRemovalDelay();
		player.sendMessage(Component.text("The arena wall will be removed in " + delay + " seconds.", NamedTextColor.GRAY));

		final Player finalPlayer = player;
		final Arenas finalArena = arena;
		new BukkitRunnable() {
			private int ticks = 0;
			private final int totalTicks = delay * 20;

			@Override
			public void run() {
				// Cancel if timer completed, player offline, arena wall removed, or player no longer in arena
				if (ticks >= totalTicks || !finalPlayer.isOnline() || !finalArena.isWallActive() || !finalArena.getPlayers().contains(finalPlayer.getUniqueId())) {
					this.cancel();
					if (finalPlayer.isOnline()) {
						finalPlayer.sendActionBar(Component.empty());
					}
					return;
				}
				int remainingSeconds = (totalTicks - ticks) / 20;
				if (remainingSeconds > 0) {
					finalPlayer.sendActionBar(Component.text("Arena opens in " + remainingSeconds + " seconds", NamedTextColor.AQUA));
				}
				ticks++;
			}
		}.runTaskTimer(plugin, 0L, 1L);
	}
}