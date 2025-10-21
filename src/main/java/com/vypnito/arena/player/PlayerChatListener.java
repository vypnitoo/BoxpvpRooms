package com.vypnito.arena.player;

import com.vypnito.arena.Arena;
import com.vypnito.arena.arenas.Arenas;
import com.vypnito.arena.arenas.ArenaManager;
import com.vypnito.arena.gui.GUIManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PlayerChatListener implements Listener {
	private final Arena plugin;
	private final PlayerManager playerManager;
	private final ArenaManager arenaManager;
	private final GUIManager guiManager;

	private static final Pattern X_V_X_PATTERN = Pattern.compile("(\\d+)v(\\d+)", Pattern.CASE_INSENSITIVE);

	public PlayerChatListener(Arena plugin, PlayerManager playerManager, ArenaManager arenaManager, GUIManager guiManager) {
		this.plugin = plugin;
		this.playerManager = playerManager;
		this.arenaManager = arenaManager;
		this.guiManager = guiManager;
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		PlayerState state = playerManager.getPlayerState(player);
		if (state == null) return;

		event.setCancelled(true);
		String message = event.getMessage();
		Arenas arena = state.getArena();

		if (message.equalsIgnoreCase("cancel")) {
			playerManager.clearPlayerState(player);
			if (state.getArenaNameForCreation() != null) {
				Bukkit.getScheduler().runTask(plugin, () -> guiManager.openArenaTypeSelectionGUI(player, state.getArenaNameForCreation()));
			} else if (arena != null) {
				Bukkit.getScheduler().runTask(plugin, () -> guiManager.openEditGUI(player, arena));
			} else {
				player.sendMessage(Component.text("Action cancelled, but unable to return to previous GUI.", NamedTextColor.RED));
			}
			player.sendMessage(Component.text("Action cancelled.", NamedTextColor.RED));
			return;
		}

		if (state.getInputType() != null) {
			handleChatInput(player, arena, message, state);
		} else if (state.getEffectType() != null) {
			handleAddEffect(player, arena, message, state.getEffectType());
		}
	}

	private void handleChatInput(Player player, Arenas arena, String message, PlayerState state) {
		if (state.getInputType() == PlayerState.InputType.CUSTOM_REQUIRED_PLAYERS) {
			handleCustomRequiredPlayersInput(player, message, state);
			return;
		}

		switch(state.getInputType()) {
			case WALL_MATERIAL:
				try {
					Material newMaterial = Material.valueOf(message.toUpperCase());
					arena.getSettings().setWallMaterial(newMaterial);
					player.sendMessage(Component.text("Wall material updated to " + newMaterial.name(), NamedTextColor.GREEN));
				} catch (IllegalArgumentException e) {
					player.sendMessage(Component.text("Invalid material name. Please try again.", NamedTextColor.RED));
					return;
				}
				break;
			case DELAY:
				try {
					int newDelay = Integer.parseInt(message);
					if (newDelay < 0) {
						player.sendMessage(Component.text("Delay cannot be negative. Please enter a positive number.", NamedTextColor.RED));
						return;
					} else {
						arena.getSettings().setWallRemovalDelay(newDelay);
						player.sendMessage(Component.text("Wall removal delay updated to " + newDelay + "s.", NamedTextColor.GREEN));
					}
				} catch (NumberFormatException e) {
					player.sendMessage(Component.text("That is not a valid number. Please enter a number.", NamedTextColor.RED));
					return;
				}
				break;
		}

		// Run arena save and GUI operations on main thread for thread safety
		Bukkit.getScheduler().runTask(plugin, () -> {
			arenaManager.saveArena(arena);
			playerManager.clearPlayerState(player);
			guiManager.openEditGUI(player, arena);
		});
	}

	private void handleCustomRequiredPlayersInput(Player player, String message, PlayerState state) {
		Matcher matcher = X_V_X_PATTERN.matcher(message);
		if (matcher.matches()) {
			try {
				int players1 = Integer.parseInt(matcher.group(1));
				int players2 = Integer.parseInt(matcher.group(2));
				int totalPlayers = players1 + players2;

				if (players1 <= 0 || players2 <= 0) {
					player.sendMessage(Component.text("Player counts must be positive. Example: '5v5'.", NamedTextColor.RED));
					return;
				}
				if (totalPlayers > 64) {
					player.sendMessage(Component.text("Total players (" + totalPlayers + ") is too high. Max 32v32 (64 total).", NamedTextColor.RED));
					return;
				}

				state.setSelectedRequiredPlayers(totalPlayers);
				playerManager.clearPlayerState(player);
				playerManager.setPlayerState(player, state);

				player.sendMessage(Component.text("Arena type set to " + players1 + "v" + players2 + " (" + totalPlayers + " players).", NamedTextColor.GREEN));

				Bukkit.getScheduler().runTask(plugin, () -> guiManager.openSetupGUI(player, state.getArenaNameForCreation()));
			} catch (NumberFormatException e) {
				player.sendMessage(Component.text("Invalid numbers in XvX format. Example: '5v5'.", NamedTextColor.RED));
			}
		} else {
			player.sendMessage(Component.text("Invalid format. Please use 'XvX' (e.g., '5v5').", NamedTextColor.RED));
		}
	}


	private void handleAddEffect(Player player, Arenas arena, String message, PotionEffectType effectType) {
		try {
			int amplifier = Integer.parseInt(message);
			if (amplifier < 0 || amplifier > 255) {
				player.sendMessage(Component.text("Amplifier must be between 0 and 255. Please try again.", NamedTextColor.RED));
				return;
			}

			String effectString = effectType.getName() + ":" + amplifier;
			arena.getSettings().addEffect(effectString);

			player.sendMessage(Component.text("Effect '" + formatEffectName(effectType.getName()) + " " + (amplifier + 1) + "' added!", NamedTextColor.GREEN));

			// Run arena save and GUI operations on main thread for thread safety
			Bukkit.getScheduler().runTask(plugin, () -> {
				arenaManager.saveArena(arena);
				playerManager.clearPlayerState(player);
				guiManager.openEffectsGUI(player, arena, 0);
			});
		} catch (NumberFormatException e) {
			player.sendMessage(Component.text("That is not a valid number. Please enter a number.", NamedTextColor.RED));
		}
	}

	private String formatEffectName(String name) {
		return Arrays.stream(name.split("_"))
				.map(w -> w.substring(0, 1).toUpperCase() + w.substring(1).toLowerCase())
				.collect(Collectors.joining(" "));
	}
}