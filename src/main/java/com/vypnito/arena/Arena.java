package com.vypnito.arena;

import com.vypnito.arena.arenas.ArenaManager;
import com.vypnito.arena.gui.GUIManager;
import com.vypnito.arena.message.MessageManager;
import com.vypnito.arena.player.PlayerManager;
import com.vypnito.arena.player.PlayerChatListener;
import com.vypnito.arena.player.SelectionManager;
import com.vypnito.arena.game.GameListener;
import com.vypnito.arena.game.GameManager;
import com.vypnito.arena.player.WandListener;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
public final class Arena extends JavaPlugin {

	private ArenaManager arenaManager;
	private SelectionManager selectionManager;
	private PlayerManager playerManager;
	private GUIManager guiManager;
	private GameManager gameManager;
	private MessageManager messageManager;

	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		this.reloadConfig();

		messageManager = new MessageManager(this);
		arenaManager = new ArenaManager(this);
		selectionManager = new SelectionManager(this);
		playerManager = new PlayerManager();
		guiManager = new GUIManager(this, selectionManager, arenaManager, playerManager);
		gameManager = new GameManager(this, arenaManager);

		Bukkit.getPluginManager().registerEvents(guiManager, this);
		Bukkit.getPluginManager().registerEvents(new PlayerChatListener(this, playerManager, arenaManager, guiManager), this);
		Bukkit.getPluginManager().registerEvents(new GameListener(gameManager), this);
		Bukkit.getPluginManager().registerEvents(new WandListener(selectionManager), this);
		this.getCommand("arena").setExecutor(new ArenaCommand(this, arenaManager, selectionManager, guiManager));
		this.getCommand("arena").setTabCompleter(new ArenaTabCompleter(arenaManager));

		getLogger().info("SmartArenas has been enabled!");
	}

	@Override
	public void onDisable() {
		getLogger().info("SmartArenas has been disabled!");
	}

	public ArenaManager getArenaManager() {
		return arenaManager;
	}

	public SelectionManager getSelectionManager() {
		return selectionManager;
	}

	public PlayerManager getPlayerManager() {
		return playerManager;
	}

	public GUIManager getGuiManager() {
		return guiManager;
	}

	public GameManager getGameManager() {
		return gameManager;
	}

	public MessageManager getMessageManager() {
		return messageManager;
	}
}