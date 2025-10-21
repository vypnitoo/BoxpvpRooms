package com.vypnito.arena.player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class WandListener implements Listener {

	private final SelectionManager selectionManager;

	public WandListener(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		ItemStack itemInHand = player.getInventory().getItemInMainHand();

		if (itemInHand.getType() != Material.WOODEN_AXE) {
			return;
		}

		ItemMeta meta = itemInHand.getItemMeta();
		if (meta == null) {
			return;
		}

		Boolean isWand = meta.getPersistentDataContainer().get(selectionManager.getWandKey(), PersistentDataType.BOOLEAN);
		if (isWand == null || !isWand) {
			return;
		}

		event.setCancelled(true);

		Block clickedBlock = event.getClickedBlock();
		if (clickedBlock == null) {
			return;
		}

		Location clickedLocation = clickedBlock.getLocation();

		if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
			selectionManager.setPos1(player, clickedLocation);
			player.sendMessage(Component.text("Position 1 set to: " + formatLoc(clickedLocation), NamedTextColor.GREEN));
		} else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			selectionManager.setPos2(player, clickedLocation);
			player.sendMessage(Component.text("Position 2 set to: " + formatLoc(clickedLocation), NamedTextColor.GREEN));
		}
	}

	private String formatLoc(Location loc) {
		if (loc == null) return "Not Set";
		return loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
	}
}