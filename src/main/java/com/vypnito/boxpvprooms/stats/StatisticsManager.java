package com.vypnito.boxpvprooms.stats;

import com.vypnito.boxpvprooms.BoxpvpRooms;
import com.vypnito.boxpvprooms.enums.RoomType;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class StatisticsManager {

    private final BoxpvpRooms plugin;
    private Connection connection;
    private final String databasePath;

    public StatisticsManager(BoxpvpRooms plugin) {
        this.plugin = plugin;
        this.databasePath = plugin.getDataFolder().getAbsolutePath() + File.separator + "database.db";
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);

            createTables();

            plugin.getLogger().info("Statistics database initialized successfully!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize statistics database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        String createPlayerStats = """
            CREATE TABLE IF NOT EXISTS player_stats (
                uuid TEXT PRIMARY KEY,
                player_name TEXT NOT NULL,
                total_kills INTEGER DEFAULT 0,
                total_deaths INTEGER DEFAULT 0,
                total_wins INTEGER DEFAULT 0,
                total_losses INTEGER DEFAULT 0,
                normal_kills INTEGER DEFAULT 0,
                normal_deaths INTEGER DEFAULT 0,
                normal_wins INTEGER DEFAULT 0,
                normal_losses INTEGER DEFAULT 0,
                team_kills INTEGER DEFAULT 0,
                team_deaths INTEGER DEFAULT 0,
                team_wins INTEGER DEFAULT 0,
                team_losses INTEGER DEFAULT 0,
                clan_kills INTEGER DEFAULT 0,
                clan_deaths INTEGER DEFAULT 0,
                clan_wins INTEGER DEFAULT 0,
                clan_losses INTEGER DEFAULT 0,
                last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        String createMatchHistory = """
            CREATE TABLE IF NOT EXISTS match_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                arena_name TEXT NOT NULL,
                room_type TEXT NOT NULL,
                winner_uuid TEXT,
                loser_uuid TEXT,
                winner_name TEXT,
                loser_name TEXT,
                match_duration INTEGER,
                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createPlayerStats);
            stmt.execute(createMatchHistory);
        }
    }

    public CompletableFuture<PlayerStats> getPlayerStats(UUID playerUUID) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "SELECT * FROM player_stats WHERE uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, playerUUID.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return new PlayerStats(
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("player_name"),
                        rs.getInt("total_kills"),
                        rs.getInt("total_deaths"),
                        rs.getInt("total_wins"),
                        rs.getInt("total_losses"),
                        rs.getInt("normal_kills"),
                        rs.getInt("normal_deaths"),
                        rs.getInt("normal_wins"),
                        rs.getInt("normal_losses"),
                        rs.getInt("team_kills"),
                        rs.getInt("team_deaths"),
                        rs.getInt("team_wins"),
                        rs.getInt("team_losses"),
                        rs.getInt("clan_kills"),
                        rs.getInt("clan_deaths"),
                        rs.getInt("clan_wins"),
                        rs.getInt("clan_losses")
                    );
                } else {
                    return new PlayerStats(playerUUID, Bukkit.getOfflinePlayer(playerUUID).getName());
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Error getting player stats: " + e.getMessage());
                return new PlayerStats(playerUUID, Bukkit.getOfflinePlayer(playerUUID).getName());
            }
        });
    }

    public void updatePlayerStats(UUID playerUUID, String playerName, RoomType roomType, boolean won, boolean kill) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                insertOrUpdatePlayer(playerUUID, playerName);
                StringBuilder updateQuery = new StringBuilder("UPDATE player_stats SET ");
                updateQuery.append("total_kills = total_kills + ?, ");
                updateQuery.append("total_deaths = total_deaths + ?, ");
                updateQuery.append("total_wins = total_wins + ?, ");
                updateQuery.append("total_losses = total_losses + ?, ");

                switch (roomType) {
                    case NORMAL:
                        updateQuery.append("normal_kills = normal_kills + ?, ");
                        updateQuery.append("normal_deaths = normal_deaths + ?, ");
                        updateQuery.append("normal_wins = normal_wins + ?, ");
                        updateQuery.append("normal_losses = normal_losses + ?, ");
                        break;
                    case TEAM:
                        updateQuery.append("team_kills = team_kills + ?, ");
                        updateQuery.append("team_deaths = team_deaths + ?, ");
                        updateQuery.append("team_wins = team_wins + ?, ");
                        updateQuery.append("team_losses = team_losses + ?, ");
                        break;
                    case CLAN:
                        updateQuery.append("clan_kills = clan_kills + ?, ");
                        updateQuery.append("clan_deaths = clan_deaths + ?, ");
                        updateQuery.append("clan_wins = clan_wins + ?, ");
                        updateQuery.append("clan_losses = clan_losses + ?, ");
                        break;
                }

                updateQuery.append("last_updated = CURRENT_TIMESTAMP WHERE uuid = ?");

                try (PreparedStatement stmt = connection.prepareStatement(updateQuery.toString())) {
                    int paramIndex = 1;

                    stmt.setInt(paramIndex++, kill ? 1 : 0);
                    stmt.setInt(paramIndex++, kill ? 0 : 1);
                    stmt.setInt(paramIndex++, won ? 1 : 0);
                    stmt.setInt(paramIndex++, won ? 0 : 1);
                    stmt.setInt(paramIndex++, kill ? 1 : 0);
                    stmt.setInt(paramIndex++, kill ? 0 : 1);
                    stmt.setInt(paramIndex++, won ? 1 : 0);
                    stmt.setInt(paramIndex++, won ? 0 : 1);

                    stmt.setString(paramIndex, playerUUID.toString());

                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Error updating player stats: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void insertOrUpdatePlayer(UUID playerUUID, String playerName) throws SQLException {
        String insertQuery = """
            INSERT OR IGNORE INTO player_stats (uuid, player_name)
            VALUES (?, ?)
        """;

        String updateNameQuery = """
            UPDATE player_stats SET player_name = ? WHERE uuid = ?
        """;

        try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
             PreparedStatement updateStmt = connection.prepareStatement(updateNameQuery)) {

            insertStmt.setString(1, playerUUID.toString());
            insertStmt.setString(2, playerName);
            insertStmt.executeUpdate();

            updateStmt.setString(1, playerName);
            updateStmt.setString(2, playerUUID.toString());
            updateStmt.executeUpdate();
        }
    }

    public void recordMatch(String arenaName, RoomType roomType, UUID winnerUUID, UUID loserUUID,
                          String winnerName, String loserName, long duration) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String insertQuery = """
                INSERT INTO match_history (arena_name, room_type, winner_uuid, loser_uuid,
                                         winner_name, loser_name, match_duration)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

            try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
                stmt.setString(1, arenaName);
                stmt.setString(2, roomType.name());
                stmt.setString(3, winnerUUID.toString());
                stmt.setString(4, loserUUID.toString());
                stmt.setString(5, winnerName);
                stmt.setString(6, loserName);
                stmt.setLong(7, duration);

                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Error recording match: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public CompletableFuture<List<PlayerStats>> getTopPlayers(String category, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            List<PlayerStats> topPlayers = new ArrayList<>();

            String orderBy = switch (category.toLowerCase()) {
                case "kills" -> "total_kills";
                case "deaths" -> "total_deaths";
                case "wins" -> "total_wins";
                case "losses" -> "total_losses";
                case "normal_kills" -> "normal_kills";
                case "normal_wins" -> "normal_wins";
                case "team_kills" -> "team_kills";
                case "team_wins" -> "team_wins";
                case "clan_kills" -> "clan_kills";
                case "clan_wins" -> "clan_wins";
                case "kdr" -> "(CASE WHEN total_deaths = 0 THEN total_kills ELSE CAST(total_kills AS REAL) / total_deaths END)";
                default -> "total_kills";
            };

            String query = "SELECT * FROM player_stats ORDER BY " + orderBy + " DESC LIMIT ?";

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, limit);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    PlayerStats stats = new PlayerStats(
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("player_name"),
                        rs.getInt("total_kills"),
                        rs.getInt("total_deaths"),
                        rs.getInt("total_wins"),
                        rs.getInt("total_losses"),
                        rs.getInt("normal_kills"),
                        rs.getInt("normal_deaths"),
                        rs.getInt("normal_wins"),
                        rs.getInt("normal_losses"),
                        rs.getInt("team_kills"),
                        rs.getInt("team_deaths"),
                        rs.getInt("team_wins"),
                        rs.getInt("team_losses"),
                        rs.getInt("clan_kills"),
                        rs.getInt("clan_deaths"),
                        rs.getInt("clan_wins"),
                        rs.getInt("clan_losses")
                    );
                    topPlayers.add(stats);
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Error getting top players: " + e.getMessage());
                e.printStackTrace();
            }

            return topPlayers;
        });
    }

    public CompletableFuture<Integer> getPlayerRank(UUID playerUUID, String category) {
        return CompletableFuture.supplyAsync(() -> {
            String orderBy = switch (category.toLowerCase()) {
                case "kills" -> "total_kills";
                case "deaths" -> "total_deaths";
                case "wins" -> "total_wins";
                case "losses" -> "total_losses";
                case "normal_kills" -> "normal_kills";
                case "normal_wins" -> "normal_wins";
                case "team_kills" -> "team_kills";
                case "team_wins" -> "team_wins";
                case "clan_kills" -> "clan_kills";
                case "clan_wins" -> "clan_wins";
                default -> "total_kills";
            };

            String query = """
                SELECT COUNT(*) + 1 as rank FROM player_stats
                WHERE %s > (SELECT %s FROM player_stats WHERE uuid = ?)
            """.formatted(orderBy, orderBy);

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, playerUUID.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return rs.getInt("rank");
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Error getting player rank: " + e.getMessage());
                e.printStackTrace();
            }

            return -1;
        });
    }

    public void closeDatabase() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Statistics database connection closed.");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error closing database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void recordMatch(com.vypnito.boxpvprooms.arenas.Arenas arena, org.bukkit.entity.Player winner, org.bukkit.entity.Player loser, long duration) {
        if (arena == null || winner == null || loser == null) return;

        recordMatch(arena.getName(), arena.getRoomType(), winner.getUniqueId(), loser.getUniqueId(),
                   winner.getName(), loser.getName(), duration);

        updatePlayerStats(winner.getUniqueId(), winner.getName(), arena.getRoomType(), true, true);
        updatePlayerStats(loser.getUniqueId(), loser.getName(), arena.getRoomType(), false, false);
    }
}