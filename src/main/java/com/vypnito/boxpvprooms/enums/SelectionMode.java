package com.vypnito.boxpvprooms.enums;

public enum SelectionMode {
    ZONE("Zone", "Select the main arena area"),
    BARRIER("Barrier", "Select barrier placement area"),
    TELEPORT("Teleport", "Select teleport points");

    private final String displayName;
    private final String description;

    SelectionMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static SelectionMode fromString(String str) {
        for (SelectionMode mode : SelectionMode.values()) {
            if (mode.name().equalsIgnoreCase(str) || mode.displayName.equalsIgnoreCase(str)) {
                return mode;
            }
        }
        return ZONE; // Default fallback
    }
}