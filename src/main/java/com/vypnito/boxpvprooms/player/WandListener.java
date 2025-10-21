package com.vypnito.boxpvprooms.player;

import com.vypnito.boxpvprooms.BoxpvpRooms;
import com.vypnito.arena.player.PlayerState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

public class WandListener implements Listener {

    private final BoxpvpRooms plugin;
    private final SelectionManager selectionManager;
    private final NamespacedKey wandKey;

    public WandListener(BoxpvpRooms plugin) {
        this.plugin = plugin;
        this.selectionManager = plugin.getSelectionManager();
        this.wandKey = new NamespacedKey(plugin, "arena_wand");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() != Material.WOODEN_AXE) {
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(wandKey, PersistentDataType.STRING)) {
            return;
        }

        event.setCancelled(true);

        Location clickedLocation = null;
        if (event.getClickedBlock() != null) {
            clickedLocation = event.getClickedBlock().getLocation();
        }

        if (clickedLocation == null) {
            player.sendMessage("§cPlease click on a block!");
            return;
        }

        PlayerState state = plugin.getPlayerManager().getPlayerState(player);

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            selectionManager.setPos1(player, clickedLocation);
            plugin.getPlayerManager().setPlayerState(player, PlayerState.SELECTING_POS1);

            player.sendMessage("§aPosition 1 set: §e" + formatLocation(clickedLocation));

            Location pos2 = selectionManager.getPos2(player);
            if (pos2 != null) {
                int blocks = calculateBlocks(clickedLocation, pos2);
                player.sendMessage("§7Area selected: §b" + blocks + " §7blocks");
            }

        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            selectionManager.setPos2(player, clickedLocation);
            plugin.getPlayerManager().setPlayerState(player, PlayerState.SELECTING_POS2);

            player.sendMessage("§aPosition 2 set: §e" + formatLocation(clickedLocation));

            Location pos1 = selectionManager.getPos1(player);
            if (pos1 != null) {
                int blocks = calculateBlocks(pos1, clickedLocation);
                player.sendMessage("§7Area selected: §b" + blocks + " §7blocks");

                if (state == PlayerState.CREATING_ARENA) {
                    player.sendMessage("§aBoth positions set! Use §e/rooms save §ato create the arena.");
                }
            }
        }
    }

    private String formatLocation(Location loc) {
        return loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();
    }

    private int calculateBlocks(Location pos1, Location pos2) {
        int dx = Math.abs(pos1.getBlockX() - pos2.getBlockX()) + 1;
        int dy = Math.abs(pos1.getBlockY() - pos2.getBlockY()) + 1;
        int dz = Math.abs(pos1.getBlockZ() - pos2.getBlockZ()) + 1;
        return dx * dy * dz;
    }
}