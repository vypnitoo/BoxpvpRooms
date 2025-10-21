package com.vypnito.arena;

import com.vypnito.arena.arenas.ArenaManager;
import com.vypnito.arena.arenas.Arenas;
import com.vypnito.arena.gui.GUIManager;
import com.vypnito.arena.player.SelectionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
public class ArenaCommand implements CommandExecutor {

	private final Arena plugin;
	private final ArenaManager arenaManager;
	private final SelectionManager selectionManager;
	private final GUIManager guiManager;

	public ArenaCommand(Arena plugin, ArenaManager arenaManager, SelectionManager selectionManager, GUIManager guiManager) {
		this.plugin = plugin;
		this.arenaManager = arenaManager;
		this.selectionManager = selectionManager;
		this.guiManager = guiManager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
			return true;
		}

		if (!player.hasPermission("smartarenas.admin")) {
			player.sendMessage(Component.text("You don't have permission to use this command.", NamedTextColor.RED));
			return true;
		}

		if (args.length == 0) {
			player.sendMessage(Component.text("Usage: /arena <wand|create|delete|reload|edit>", NamedTextColor.YELLOW));
			return true;
		}

		switch (args[0].toLowerCase()) {
			case "wand":
				selectionManager.giveWand(player);
				break;
			case "create":
				if (args.length < 2) {
					player.sendMessage(Component.text("Usage: /arena create <name>", NamedTextColor.YELLOW));
					return true;
				}
				String arenaName = args[1];
				guiManager.openArenaTypeSelectionGUI(player, arenaName);
				break;
			case "delete":
				if (args.length < 2) {
					player.sendMessage(Component.text("Usage: /arena delete <name>", NamedTextColor.YELLOW));
					return true;
				}
				arenaManager.deleteArena(args[1]);
				break;
			case "reload":
				plugin.reloadConfig();
				arenaManager.loadReplaceableMaterials();
				player.sendMessage(Component.text("SmartArenas configuration reloaded.", NamedTextColor.GREEN));
				break;
			case "edit":
				if (args.length < 2) {
					player.sendMessage(Component.text("Usage: /arena edit <name>", NamedTextColor.YELLOW));
					return true;
				}
				Arenas arenaToEdit = arenaManager.getArena(args[1]);
				if (arenaToEdit == null) {
					player.sendMessage(Component.text("Arena '" + args[1] + "' not found.", NamedTextColor.RED));
					return true;
				}
				guiManager.openEditGUI(player, arenaToEdit);
				break;
			default:
				player.sendMessage(Component.text("Unknown subcommand. Usage: /arena <wand|create|delete|reload|edit>", NamedTextColor.YELLOW));
				break;
		}
		return true;
	}
}