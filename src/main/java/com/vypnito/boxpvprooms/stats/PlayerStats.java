package com.vypnito.boxpvprooms.stats;

import java.util.UUID;

public class PlayerStats {
    private final UUID uuid;
    private String playerName;

    // Total stats
    private int totalKills;
    private int totalDeaths;
    private int totalWins;
    private int totalLosses;

    // Normal room stats
    private int normalKills;
    private int normalDeaths;
    private int normalWins;
    private int normalLosses;

    // Team room stats
    private int teamKills;
    private int teamDeaths;
    private int teamWins;
    private int teamLosses;

    // Clan room stats
    private int clanKills;
    private int clanDeaths;
    private int clanWins;
    private int clanLosses;

    // Constructor for new player (empty stats)
    public PlayerStats(UUID uuid, String playerName) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.totalKills = 0;
        this.totalDeaths = 0;
        this.totalWins = 0;
        this.totalLosses = 0;
        this.normalKills = 0;
        this.normalDeaths = 0;
        this.normalWins = 0;
        this.normalLosses = 0;
        this.teamKills = 0;
        this.teamDeaths = 0;
        this.teamWins = 0;
        this.teamLosses = 0;
        this.clanKills = 0;
        this.clanDeaths = 0;
        this.clanWins = 0;
        this.clanLosses = 0;
    }

    // Constructor for existing player (full stats)
    public PlayerStats(UUID uuid, String playerName, int totalKills, int totalDeaths, int totalWins, int totalLosses,
                      int normalKills, int normalDeaths, int normalWins, int normalLosses,
                      int teamKills, int teamDeaths, int teamWins, int teamLosses,
                      int clanKills, int clanDeaths, int clanWins, int clanLosses) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.totalKills = totalKills;
        this.totalDeaths = totalDeaths;
        this.totalWins = totalWins;
        this.totalLosses = totalLosses;
        this.normalKills = normalKills;
        this.normalDeaths = normalDeaths;
        this.normalWins = normalWins;
        this.normalLosses = normalLosses;
        this.teamKills = teamKills;
        this.teamDeaths = teamDeaths;
        this.teamWins = teamWins;
        this.teamLosses = teamLosses;
        this.clanKills = clanKills;
        this.clanDeaths = clanDeaths;
        this.clanWins = clanWins;
        this.clanLosses = clanLosses;
    }

    // Getters
    public UUID getUuid() { return uuid; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    // Total stats getters
    public int getTotalKills() { return totalKills; }
    public int getTotalDeaths() { return totalDeaths; }
    public int getTotalWins() { return totalWins; }
    public int getTotalLosses() { return totalLosses; }

    // Normal room stats getters
    public int getNormalKills() { return normalKills; }
    public int getNormalDeaths() { return normalDeaths; }
    public int getNormalWins() { return normalWins; }
    public int getNormalLosses() { return normalLosses; }

    // Team room stats getters
    public int getTeamKills() { return teamKills; }
    public int getTeamDeaths() { return teamDeaths; }
    public int getTeamWins() { return teamWins; }
    public int getTeamLosses() { return teamLosses; }

    // Clan room stats getters
    public int getClanKills() { return clanKills; }
    public int getClanDeaths() { return clanDeaths; }
    public int getClanWins() { return clanWins; }
    public int getClanLosses() { return clanLosses; }

    // Calculated stats
    public double getTotalKDR() {
        return totalDeaths == 0 ? totalKills : (double) totalKills / totalDeaths;
    }

    public double getTotalWLR() {
        return totalLosses == 0 ? totalWins : (double) totalWins / totalLosses;
    }

    public double getNormalKDR() {
        return normalDeaths == 0 ? normalKills : (double) normalKills / normalDeaths;
    }

    public double getTeamKDR() {
        return teamDeaths == 0 ? teamKills : (double) teamKills / teamDeaths;
    }

    public double getClanKDR() {
        return clanDeaths == 0 ? clanKills : (double) clanKills / clanDeaths;
    }

    public int getTotalMatches() {
        return totalWins + totalLosses;
    }

    public double getWinPercentage() {
        int totalMatches = getTotalMatches();
        return totalMatches == 0 ? 0.0 : (double) totalWins / totalMatches * 100.0;
    }

    // Utility methods for formatting
    public String getFormattedKDR() {
        return String.format("%.2f", getTotalKDR());
    }

    public String getFormattedWinPercentage() {
        return String.format("%.1f%%", getWinPercentage());
    }

    @Override
    public String toString() {
        return "PlayerStats{" +
                "uuid=" + uuid +
                ", playerName='" + playerName + '\'' +
                ", totalKills=" + totalKills +
                ", totalDeaths=" + totalDeaths +
                ", totalWins=" + totalWins +
                ", totalLosses=" + totalLosses +
                ", KDR=" + getFormattedKDR() +
                ", WinRate=" + getFormattedWinPercentage() +
                '}';
    }
}