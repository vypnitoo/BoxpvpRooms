package com.vypnito.boxpvprooms.enums;

public enum RoomType {
    NORMAL("Normal", "Traditional 1v1 rooms"),
    TEAM("Team", "Team battles with auto-assignment"),
    CLAN("Clan", "Clan vs clan battles");

    private final String displayName;
    private final String description;

    RoomType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static RoomType fromString(String str) {
        for (RoomType type : RoomType.values()) {
            if (type.name().equalsIgnoreCase(str) || type.displayName.equalsIgnoreCase(str)) {
                return type;
            }
        }
        return NORMAL; // Default fallback
    }
}