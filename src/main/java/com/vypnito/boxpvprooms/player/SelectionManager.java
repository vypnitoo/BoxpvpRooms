package com.vypnito.boxpvprooms.player;

import com.vypnito.boxpvprooms.BoxpvpRooms;
import com.vypnito.boxpvprooms.enums.RoomType;
import com.vypnito.boxpvprooms.enums.SelectionMode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SelectionManager {

    // Zone selection storage
    private final Map<UUID, Location> pos1 = new HashMap<>();
    private final Map<UUID, Location> pos2 = new HashMap<>();

    // Barrier selection storage
    private final Map<UUID, Location> barrierPos1 = new HashMap<>();
    private final Map<UUID, Location> barrierPos2 = new HashMap<>();

    // Teleport points storage
    private final Map<UUID, Location> teleportPoint1 = new HashMap<>();
    private final Map<UUID, Location> teleportPoint2 = new HashMap<>();
    private final Map<UUID, Location> spectatorSpawn = new HashMap<>();

    // Selection mode tracking
    private final Map<UUID, SelectionMode> selectionModes = new HashMap<>();
    private final Map<UUID, RoomType> wandRoomTypes = new HashMap<>();

    private final NamespacedKey wandKey;
    private final NamespacedKey roomTypeKey;

    public SelectionManager(BoxpvpRooms plugin) {
        this.wandKey = new NamespacedKey(plugin, "boxpvp_selection_wand");
        this.roomTypeKey = new NamespacedKey(plugin, "boxpvp_room_type");
    }

    // Selection mode management
    public void setSelectionMode(Player player, SelectionMode mode) {
        selectionModes.put(player.getUniqueId(), mode);
        player.sendMessage("§aSelection mode changed to: §e" + mode.getDisplayName());
        player.sendMessage("§7" + mode.getDescription());
    }

    public SelectionMode getSelectionMode(Player player) {
        return selectionModes.getOrDefault(player.getUniqueId(), SelectionMode.ZONE);
    }

    // Zone selection methods
    public void setPos1(Player player, Location location) {
        pos1.put(player.getUniqueId(), location);
        player.sendMessage("§aZone position 1 set: §e" + formatLocation(location));
    }

    public void setPos2(Player player, Location location) {
        pos2.put(player.getUniqueId(), location);
        player.sendMessage("§aZone position 2 set: §e" + formatLocation(location));
    }

    public Location getPos1(Player player) {
        return pos1.get(player.getUniqueId());
    }

    public Location getPos2(Player player) {
        return pos2.get(player.getUniqueId());
    }

    // Barrier selection methods
    public void setBarrierPos1(Player player, Location location) {
        barrierPos1.put(player.getUniqueId(), location);
        player.sendMessage("§6Barrier position 1 set: §e" + formatLocation(location));
    }

    public void setBarrierPos2(Player player, Location location) {
        barrierPos2.put(player.getUniqueId(), location);
        player.sendMessage("§6Barrier position 2 set: §e" + formatLocation(location));
    }

    public Location getBarrierPos1(Player player) {
        return barrierPos1.get(player.getUniqueId());
    }

    public Location getBarrierPos2(Player player) {
        return barrierPos2.get(player.getUniqueId());
    }

    // Teleport point methods
    public void setTeleportPoint1(Player player, Location location) {
        teleportPoint1.put(player.getUniqueId(), location);
        player.sendMessage("§bTeleport point 1 set: §e" + formatLocation(location));
    }

    public void setTeleportPoint2(Player player, Location location) {
        teleportPoint2.put(player.getUniqueId(), location);
        player.sendMessage("§bTeleport point 2 set: §e" + formatLocation(location));
    }

    public void setSpectatorSpawn(Player player, Location location) {
        spectatorSpawn.put(player.getUniqueId(), location);
        player.sendMessage("§dSpectator spawn set: §e" + formatLocation(location));
    }

    public Location getTeleportPoint1(Player player) {
        return teleportPoint1.get(player.getUniqueId());
    }

    public Location getTeleportPoint2(Player player) {
        return teleportPoint2.get(player.getUniqueId());
    }

    public Location getSpectatorSpawn(Player player) {
        return spectatorSpawn.get(player.getUniqueId());
    }

    // Utility methods
    public void clearSelection(Player player) {
        UUID uuid = player.getUniqueId();
        pos1.remove(uuid);
        pos2.remove(uuid);
        barrierPos1.remove(uuid);
        barrierPos2.remove(uuid);
        teleportPoint1.remove(uuid);
        teleportPoint2.remove(uuid);
        spectatorSpawn.remove(uuid);
        selectionModes.remove(uuid);
        wandRoomTypes.remove(uuid);
    }

    public boolean hasCompleteSelection(Player player) {
        return getPos1(player) != null && getPos2(player) != null;
    }

    public boolean hasCompleteBarrierSelection(Player player) {
        return getBarrierPos1(player) != null && getBarrierPos2(player) != null;
    }

    public boolean hasCompleteTeleportSelection(Player player) {
        return getTeleportPoint1(player) != null && getTeleportPoint2(player) != null;
    }

    // Wand management
    public void giveWand(Player player, RoomType roomType) {
        wandRoomTypes.put(player.getUniqueId(), roomType);

        ItemStack wand = new ItemStack(Material.WOODEN_AXE);
        ItemMeta meta = wand.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("BoxpvpRooms Selection Wand")
                    .color(NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(Arrays.asList(
                    Component.text("Room Type: " + roomType.getDisplayName())
                            .color(NamedTextColor.YELLOW)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("Mode: " + getSelectionMode(player).getDisplayName())
                            .color(NamedTextColor.AQUA)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("")
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("Left-click: Set position 1")
                            .color(NamedTextColor.GREEN)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("Right-click: Set position 2")
                            .color(NamedTextColor.GREEN)
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("")
                            .decoration(TextDecoration.ITALIC, false),
                    Component.text("Use /rooms mode to change selection mode")
                            .color(NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));

            meta.getPersistentDataContainer().set(wandKey, PersistentDataType.STRING, "boxpvp_wand");
            meta.getPersistentDataContainer().set(roomTypeKey, PersistentDataType.STRING, roomType.name());
            wand.setItemMeta(meta);
        }

        player.getInventory().addItem(wand);
        player.sendMessage("§aReceived BoxpvpRooms selection wand for " + roomType.getDisplayName() + " rooms!");
        player.sendMessage("§7Current mode: §e" + getSelectionMode(player).getDisplayName());
    }

    public void giveWand(Player player) {
        giveWand(player, RoomType.NORMAL);
    }

    public boolean isSelectionWand(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(wandKey, PersistentDataType.STRING);
    }

    public RoomType getWandRoomType(Player player) {
        return wandRoomTypes.getOrDefault(player.getUniqueId(), RoomType.NORMAL);
    }

    public RoomType getWandRoomType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return RoomType.NORMAL;
        }

        ItemMeta meta = item.getItemMeta();
        String roomTypeString = meta.getPersistentDataContainer().get(roomTypeKey, PersistentDataType.STRING);

        if (roomTypeString != null) {
            try {
                return RoomType.valueOf(roomTypeString);
            } catch (IllegalArgumentException e) {
                return RoomType.NORMAL;
            }
        }

        return RoomType.NORMAL;
    }

    // Utility method for location formatting
    private String formatLocation(Location loc) {
        return String.format("%d, %d, %d in %s",
                loc.getBlockX(),
                loc.getBlockY(),
                loc.getBlockZ(),
                loc.getWorld().getName());
    }

    // Selection summary for debugging/info
    public void printSelectionSummary(Player player) {
        UUID uuid = player.getUniqueId();
        SelectionMode mode = getSelectionMode(player);

        player.sendMessage("§8§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage("§6§lSelection Summary");
        player.sendMessage("§7Mode: §e" + mode.getDisplayName());
        player.sendMessage("");

        switch (mode) {
            case ZONE:
                Location p1 = pos1.get(uuid);
                Location p2 = pos2.get(uuid);
                player.sendMessage("§aZone Position 1: §f" + (p1 != null ? formatLocation(p1) : "Not set"));
                player.sendMessage("§aZone Position 2: §f" + (p2 != null ? formatLocation(p2) : "Not set"));
                break;
            case BARRIER:
                Location bp1 = barrierPos1.get(uuid);
                Location bp2 = barrierPos2.get(uuid);
                player.sendMessage("§6Barrier Position 1: §f" + (bp1 != null ? formatLocation(bp1) : "Not set"));
                player.sendMessage("§6Barrier Position 2: §f" + (bp2 != null ? formatLocation(bp2) : "Not set"));
                break;
            case TELEPORT:
                Location tp1 = teleportPoint1.get(uuid);
                Location tp2 = teleportPoint2.get(uuid);
                Location spec = spectatorSpawn.get(uuid);
                player.sendMessage("§bTeleport Point 1: §f" + (tp1 != null ? formatLocation(tp1) : "Not set"));
                player.sendMessage("§bTeleport Point 2: §f" + (tp2 != null ? formatLocation(tp2) : "Not set"));
                player.sendMessage("§dSpectator Spawn: §f" + (spec != null ? formatLocation(spec) : "Not set"));
                break;
        }

        player.sendMessage("§8§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
    }
}