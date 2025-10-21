package com.vypnito.boxpvprooms.placeholders;

import com.vypnito.boxpvprooms.BoxpvpRooms;
import com.vypnito.boxpvprooms.stats.PlayerStats;
import com.vypnito.boxpvprooms.stats.StatisticsManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BoxpvpPlaceholderExpansion extends PlaceholderExpansion {

    private final BoxpvpRooms plugin;
    private final StatisticsManager statsManager;

    public BoxpvpPlaceholderExpansion(BoxpvpRooms plugin, StatisticsManager statsManager) {
        this.plugin = plugin;
        this.statsManager = statsManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "zoneplugin";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // This expansion should persist through PlaceholderAPI reloads
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        // Handle async placeholders by returning cached or default values
        // For real-time accuracy, consider implementing a caching system

        switch (params.toLowerCase()) {
            // Player's individual stats
            case "kills":
                return getPlayerStat(player, "kills");
            case "deaths":
                return getPlayerStat(player, "deaths");
            case "wins":
                return getPlayerStat(player, "wins");
            case "losses":
                return getPlayerStat(player, "losses");
            case "kdr":
                return getPlayerStat(player, "kdr");
            case "winrate":
                return getPlayerStat(player, "winrate");

            // Normal room stats
            case "normal_kills":
                return getPlayerStat(player, "normal_kills");
            case "normal_deaths":
                return getPlayerStat(player, "normal_deaths");
            case "normal_wins":
                return getPlayerStat(player, "normal_wins");
            case "normal_losses":
                return getPlayerStat(player, "normal_losses");

            // Team room stats
            case "team_kills":
                return getPlayerStat(player, "team_kills");
            case "team_deaths":
                return getPlayerStat(player, "team_deaths");
            case "team_wins":
                return getPlayerStat(player, "team_wins");
            case "team_losses":
                return getPlayerStat(player, "team_losses");

            // Clan room stats
            case "clan_kills":
                return getPlayerStat(player, "clan_kills");
            case "clan_deaths":
                return getPlayerStat(player, "clan_deaths");
            case "clan_wins":
                return getPlayerStat(player, "clan_wins");
            case "clan_losses":
                return getPlayerStat(player, "clan_losses");

            // Top player placeholders
            case "maxkills_1":
                return getTopPlayerName("kills", 1);
            case "maxkills_2":
                return getTopPlayerName("kills", 2);
            case "maxkills_3":
                return getTopPlayerName("kills", 3);

            case "top_kills_1":
                return getTopPlayerValue("kills", 1);
            case "top_kills_2":
                return getTopPlayerValue("kills", 2);
            case "top_kills_3":
                return getTopPlayerValue("kills", 3);

            case "top_normal_wins_1":
                return getTopPlayerValue("normal_wins", 1);
            case "top_normal_wins_2":
                return getTopPlayerValue("normal_wins", 2);
            case "top_normal_wins_3":
                return getTopPlayerValue("normal_wins", 3);

            case "top_clan_wins_1":
                return getTopPlayerValue("clan_wins", 1);
            case "top_clan_wins_2":
                return getTopPlayerValue("clan_wins", 2);
            case "top_clan_wins_3":
                return getTopPlayerValue("clan_wins", 3);

            // Player rank placeholders
            case "rank_kills":
                return getPlayerRank(player, "kills");
            case "rank_wins":
                return getPlayerRank(player, "wins");
            case "rank_normal_wins":
                return getPlayerRank(player, "normal_wins");
            case "rank_clan_wins":
                return getPlayerRank(player, "clan_wins");

            default:
                return null; // Placeholder not found
        }
    }

    private String getPlayerStat(OfflinePlayer player, String statType) {
        try {
            CompletableFuture<PlayerStats> future = statsManager.getPlayerStats(player.getUniqueId());
            PlayerStats stats = future.get(); // This might block, consider caching

            return switch (statType.toLowerCase()) {
                case "kills" -> String.valueOf(stats.getTotalKills());
                case "deaths" -> String.valueOf(stats.getTotalDeaths());
                case "wins" -> String.valueOf(stats.getTotalWins());
                case "losses" -> String.valueOf(stats.getTotalLosses());
                case "kdr" -> stats.getFormattedKDR();
                case "winrate" -> stats.getFormattedWinPercentage();
                case "normal_kills" -> String.valueOf(stats.getNormalKills());
                case "normal_deaths" -> String.valueOf(stats.getNormalDeaths());
                case "normal_wins" -> String.valueOf(stats.getNormalWins());
                case "normal_losses" -> String.valueOf(stats.getNormalLosses());
                case "team_kills" -> String.valueOf(stats.getTeamKills());
                case "team_deaths" -> String.valueOf(stats.getTeamDeaths());
                case "team_wins" -> String.valueOf(stats.getTeamWins());
                case "team_losses" -> String.valueOf(stats.getTeamLosses());
                case "clan_kills" -> String.valueOf(stats.getClanKills());
                case "clan_deaths" -> String.valueOf(stats.getClanDeaths());
                case "clan_wins" -> String.valueOf(stats.getClanWins());
                case "clan_losses" -> String.valueOf(stats.getClanLosses());
                default -> "0";
            };
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting player stat " + statType + " for " + player.getName() + ": " + e.getMessage());
            return "0";
        }
    }

    private String getTopPlayerName(String category, int position) {
        try {
            CompletableFuture<List<PlayerStats>> future = statsManager.getTopPlayers(category, position);
            List<PlayerStats> topPlayers = future.get();

            if (topPlayers.size() >= position) {
                return topPlayers.get(position - 1).getPlayerName();
            }
            return "N/A";
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting top player name for " + category + " position " + position + ": " + e.getMessage());
            return "N/A";
        }
    }

    private String getTopPlayerValue(String category, int position) {
        try {
            CompletableFuture<List<PlayerStats>> future = statsManager.getTopPlayers(category, position);
            List<PlayerStats> topPlayers = future.get();

            if (topPlayers.size() >= position) {
                PlayerStats stats = topPlayers.get(position - 1);
                return switch (category.toLowerCase()) {
                    case "kills" -> String.valueOf(stats.getTotalKills());
                    case "deaths" -> String.valueOf(stats.getTotalDeaths());
                    case "wins" -> String.valueOf(stats.getTotalWins());
                    case "losses" -> String.valueOf(stats.getTotalLosses());
                    case "normal_kills" -> String.valueOf(stats.getNormalKills());
                    case "normal_wins" -> String.valueOf(stats.getNormalWins());
                    case "team_kills" -> String.valueOf(stats.getTeamKills());
                    case "team_wins" -> String.valueOf(stats.getTeamWins());
                    case "clan_kills" -> String.valueOf(stats.getClanKills());
                    case "clan_wins" -> String.valueOf(stats.getClanWins());
                    default -> "0";
                };
            }
            return "0";
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting top player value for " + category + " position " + position + ": " + e.getMessage());
            return "0";
        }
    }

    private String getPlayerRank(OfflinePlayer player, String category) {
        try {
            CompletableFuture<Integer> future = statsManager.getPlayerRank(player.getUniqueId(), category);
            Integer rank = future.get();
            return rank != null && rank > 0 ? String.valueOf(rank) : "N/A";
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting player rank for " + category + " for " + player.getName() + ": " + e.getMessage());
            return "N/A";
        }
    }
}