package com.vypnito.boxpvprooms.game;

import com.vypnito.boxpvprooms.BoxpvpRooms;
import com.vypnito.boxpvprooms.arenas.Arenas;
import com.vypnito.boxpvprooms.arenas.ArenaManager;
import com.vypnito.arena.player.PlayerState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.*;

public class PvPArenaListener implements Listener {
    private final BoxpvpRooms plugin;
    private final ArenaManager arenaManager;
    private final Map<String, Set<UUID>> players = new HashMap<>();
    private final Map<String, Long> matches = new HashMap<>();
    private final Map<UUID, String> playerLoc = new HashMap<>();
    private final Map<String, BukkitRunnable> tasks = new HashMap<>();
    private final Map<UUID, Collection<PotionEffect>> effects = new HashMap<>();

    public PvPArenaListener(BoxpvpRooms plugin) {
        this.plugin = plugin;
        this.arenaManager = plugin.getArenaManager();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        Location from = event.getFrom();

        if (to == null || (to.getBlockX() == from.getBlockX() &&
                          to.getBlockY() == from.getBlockY() &&
                          to.getBlockZ() == from.getBlockZ())) {
            return;
        }

        Arenas arena = arenaManager.findArenaByRegion(to);
        String currentArena = playerLoc.get(player.getUniqueId());

        if (arena != null && !arena.getName().equals(currentArena)) {
            onPlayerEnterArena(player, arena);
        } else if (arena == null && currentArena != null) {
            onPlayerLeaveArena(player, currentArena);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        PlayerMoveEvent moveEvent = new PlayerMoveEvent(event.getPlayer(), event.getFrom(), event.getTo());
        onPlayerMove(moveEvent);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String arenaName = playerLoc.get(player.getUniqueId());
        if (arenaName != null) {
            onPlayerLeaveArena(player, arenaName);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String arenaName = playerLoc.get(player.getUniqueId());

        if (arenaName != null) {
            Player killer = player.getKiller();

            if (killer != null && playerLoc.containsKey(killer.getUniqueId())) {
                handleMatchEnd(arenaName, killer, player);
            } else {
                handleMatchEnd(arenaName, null, player);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }

        Player victim = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();

        String victimArena = playerLoc.get(victim.getUniqueId());
        String attackerArena = playerLoc.get(attacker.getUniqueId());

        if (victimArena == null || !victimArena.equals(attackerArena)) {
            event.setCancelled(true);
            return;
        }

        if (!matches.containsKey(victimArena)) {
            event.setCancelled(true);
            attacker.sendMessage("§cMatch hasn't started yet! Wait for more players.");
        }
    }

    private void onPlayerEnterArena(Player player, Arenas arena) {
        String arenaName = arena.getName();

        players.computeIfAbsent(arenaName, k -> new HashSet<>()).add(player.getUniqueId());
        playerLoc.put(player.getUniqueId(), arenaName);

        plugin.getPlayerManager().setPlayerState(player, PlayerState.IN_MATCH);

        Set<UUID> playersInArena = players.get(arenaName);
        int requiredPlayers = arena.getSettings().getRequiredPlayers();

        player.sendMessage("§aYou entered arena: §e" + arenaName);
        player.sendMessage("§7Players: §b" + playersInArena.size() + "§7/§b" + requiredPlayers);

        if (playersInArena.size() >= requiredPlayers) {
            startMatch(arena);
        } else {
            player.sendMessage("§eWaiting for " + (requiredPlayers - playersInArena.size()) + " more players...");
        }
    }

    private void onPlayerLeaveArena(Player player, String arenaName) {
        Set<UUID> playersInArena = players.get(arenaName);
        if (playersInArena != null) {
            playersInArena.remove(player.getUniqueId());

            if (playersInArena.isEmpty()) {
                players.remove(arenaName);
                matches.remove(arenaName);

                BukkitRunnable openingTask = tasks.remove(arenaName);
                if (openingTask != null) {
                    openingTask.cancel();
                }

                Arenas arena = arenaManager.getArena(arenaName);
                if (arena != null && arena.isWallActive()) {
                    arena.removeBoundaryWall();
                }
            }
        }

        playerLoc.remove(player.getUniqueId());
        plugin.getPlayerManager().setPlayerState(player, PlayerState.IDLE);

        player.sendMessage("§7You left the arena.");
    }

    private void startMatch(Arenas arena) {
        String arenaName = arena.getName();
        Set<UUID> playersInArena = players.get(arenaName);

        if (playersInArena == null) return;

        arena.createBoundaryWall();

        matches.put(arenaName, System.currentTimeMillis());

        for (UUID playerId : playersInArena) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.sendMessage("§a§l✦ MATCH STARTED ✦");
                player.sendMessage("§eFight! Last player standing wins!");
                player.sendTitle("§a§lFIGHT!", "§eLast player standing wins!", 10, 40, 10);

                applyArenaEffects(player, arena);
            }
        }

        plugin.getLogger().info("Match started in arena: " + arenaName + " with " + playersInArena.size() + " players");
    }

    private void handleMatchEnd(String arenaName, Player winner, Player loser) {
        Set<UUID> playersInArena = players.get(arenaName);
        if (playersInArena == null) return;

        playersInArena.remove(loser.getUniqueId());
        playerLoc.remove(loser.getUniqueId());

        if (playersInArena.size() <= 1) {
            endMatch(arenaName, winner, loser);
        } else {
            for (UUID playerId : playersInArena) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null) {
                    player.sendMessage("§c" + loser.getName() + " §7has been eliminated!");
                    player.sendMessage("§e" + playersInArena.size() + " players remaining.");
                }
            }
        }
    }

    private void endMatch(String arenaName, Player winner, Player loser) {
        Arenas arena = arenaManager.getArena(arenaName);
        if (arena == null) return;

        Long startTime = matches.remove(arenaName);
        long duration = startTime != null ? (System.currentTimeMillis() - startTime) / 1000 : 0;

        String winnerName = winner != null ? winner.getName() : "Nobody";

        Set<UUID> finalPlayers = new HashSet<>(players.getOrDefault(arenaName, new HashSet<>()));
        for (UUID playerId : finalPlayers) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.sendMessage("§8§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                player.sendMessage("§6§l⚔ MATCH ENDED ⚔");
                player.sendMessage("");
                player.sendMessage("§eWinner: §a§l" + winnerName);
                player.sendMessage("§eMatch Duration: §b" + duration + "s");
                player.sendMessage("");
                player.sendMessage("§7Arena will open in 30 seconds...");
                player.sendMessage("§8§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

                if (player.equals(winner)) {
                    player.sendTitle("§a§lVICTORY!", "§eYou won the match!", 10, 60, 10);
                } else {
                    player.sendTitle("§c§lDEFEAT", "§eBetter luck next time!", 10, 60, 10);
                }
            }
        }

        if (winner != null && loser != null) {
            plugin.getStatisticsManager().recordMatch(arena, winner, loser, duration);
        }

        scheduleArenaOpening(arena, 30);

        plugin.getLogger().info("Match ended in arena: " + arenaName + ". Winner: " + winnerName + " (Duration: " + duration + "s)");
    }

    private void scheduleArenaOpening(Arenas arena, int delaySeconds) {
        String arenaName = arena.getName();

        BukkitRunnable existingTask = tasks.get(arenaName);
        if (existingTask != null) {
            existingTask.cancel();
        }

        BukkitRunnable openingTask = new BukkitRunnable() {
            int countdown = delaySeconds;

            @Override
            public void run() {
                Set<UUID> playersInArena = players.get(arenaName);

                if (countdown > 0) {
                    if (playersInArena != null) {
                        for (UUID playerId : playersInArena) {
                            Player player = Bukkit.getPlayer(playerId);
                            if (player != null) {
                                player.sendActionBar("§eArena opening in: §c" + countdown + "s");
                            }
                        }
                    }
                    countdown--;
                } else {
                    arena.removeBoundaryWall();

                    players.remove(arenaName);
                    matches.remove(arenaName);

                    if (playersInArena != null) {
                        for (UUID playerId : playersInArena) {
                            Player player = Bukkit.getPlayer(playerId);
                            if (player != null) {
                                playerLoc.remove(playerId);
                                plugin.getPlayerManager().setPlayerState(player, PlayerState.IDLE);
                                player.sendMessage("§a§lArena opened! You can now leave safely.");

                                restorePlayerEffects(player);
                            }
                        }
                    }

                    tasks.remove(arenaName);
                    this.cancel();

                    plugin.getLogger().info("Arena opened: " + arenaName);
                }
            }
        };

        tasks.put(arenaName, openingTask);
        openingTask.runTaskTimer(plugin, 0L, 20L);
    }

    public boolean isPlayerInMatch(Player player) {
        return playerLoc.containsKey(player.getUniqueId());
    }

    public String getPlayerArena(Player player) {
        return playerLoc.get(player.getUniqueId());
    }

    public Set<UUID> getPlayersInArena(String arenaName) {
        return players.getOrDefault(arenaName, new HashSet<>());
    }

    public boolean isMatchActive(String arenaName) {
        return matches.containsKey(arenaName);
    }

    private void applyArenaEffects(Player player, Arenas arena) {
        effects.put(player.getUniqueId(), new ArrayList<>(player.getActivePotionEffects()));

        if (arena.getSettings().getEffects() != null) {
            for (String effectString : arena.getSettings().getEffects()) {
                try {
                    String[] parts = effectString.split(":");
                    if (parts.length >= 1) {
                        PotionEffectType effectType = PotionEffectType.getByName(parts[0].toUpperCase());
                        if (effectType != null) {
                            int amplifier = parts.length >= 2 ? Integer.parseInt(parts[1]) - 1 : 0;
                            int duration = Integer.MAX_VALUE;
                            player.addPotionEffect(new PotionEffect(effectType, duration, amplifier));
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Invalid effect format: " + effectString);
                }
            }
        }
    }

    private void restorePlayerEffects(Player player) {
        Collection<PotionEffect> originalEffects = effects.remove(player.getUniqueId());
        if (originalEffects != null) {
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            for (PotionEffect effect : originalEffects) {
                player.addPotionEffect(effect);
            }
        }
    }
}