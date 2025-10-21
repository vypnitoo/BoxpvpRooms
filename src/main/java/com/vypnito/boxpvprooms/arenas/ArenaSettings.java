package com.vypnito.boxpvprooms.arenas;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ArenaSettings {
    private Material wallMaterial = Material.GLASS;
    private boolean allowBlockBreak = false;
    private boolean allowBlockPlace = false;
    private boolean allowItemDrop = false;
    private boolean disableHunger = true;
    private int wallRemovalDelay = 30;
    private List<String> effects = new ArrayList<>();
    private int requiredPlayers = 2;

    // Enhanced settings for BoxpvpRooms
    private boolean keepInventory = false;
    private boolean combatLog = true;
    private boolean preventBlocks = true;

    public ArenaSettings() {
    }

    // Wall material
    public Material getWallMaterial() {
        return wallMaterial;
    }

    public void setWallMaterial(Material wallMaterial) {
        this.wallMaterial = wallMaterial;
    }

    public void setWallMaterial(String materialName) {
        try {
            this.wallMaterial = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.wallMaterial = Material.GLASS; // Default fallback
        }
    }

    // Block interactions
    public boolean isAllowBlockBreak() {
        return allowBlockBreak;
    }

    public void setAllowBlockBreak(boolean allowBlockBreak) {
        this.allowBlockBreak = allowBlockBreak;
    }

    public boolean isAllowBlockPlace() {
        return allowBlockPlace;
    }

    public void setAllowBlockPlace(boolean allowBlockPlace) {
        this.allowBlockPlace = allowBlockPlace;
    }

    public boolean isAllowItemDrop() {
        return allowItemDrop;
    }

    public void setAllowItemDrop(boolean allowItemDrop) {
        this.allowItemDrop = allowItemDrop;
    }

    // Hunger system
    public boolean isDisableHunger() {
        return disableHunger;
    }

    public void setDisableHunger(boolean disableHunger) {
        this.disableHunger = disableHunger;
    }

    // Wall removal delay
    public int getWallRemovalDelay() {
        return wallRemovalDelay;
    }

    public void setWallRemovalDelay(int wallRemovalDelay) {
        this.wallRemovalDelay = wallRemovalDelay;
    }

    // Enhanced BoxpvpRooms settings
    public boolean isKeepInventory() {
        return keepInventory;
    }

    public void setKeepInventory(boolean keepInventory) {
        this.keepInventory = keepInventory;
    }

    public boolean isCombatLog() {
        return combatLog;
    }

    public void setCombatLog(boolean combatLog) {
        this.combatLog = combatLog;
    }

    public boolean isPreventBlocks() {
        return preventBlocks;
    }

    public void setPreventBlocks(boolean preventBlocks) {
        this.preventBlocks = preventBlocks;
        // Sync with existing block settings
        this.allowBlockBreak = !preventBlocks;
        this.allowBlockPlace = !preventBlocks;
    }

    // Required players
    public int getRequiredPlayers() {
        return requiredPlayers;
    }

    public void setRequiredPlayers(int requiredPlayers) {
        this.requiredPlayers = Math.max(2, requiredPlayers); // Minimum 2 players
    }

    // Potion effects management
    public List<String> getEffects() {
        return new ArrayList<>(effects);
    }

    public void addEffect(String effectString) {
        String newEffectType = effectString.split(":")[0];
        boolean alreadyHas = effects.stream().anyMatch(e -> e.startsWith(newEffectType + ":"));
        if (!alreadyHas) {
            effects.add(effectString);
        } else {
            effects = effects.stream()
                    .map(e -> e.startsWith(newEffectType + ":") ? effectString : e)
                    .collect(Collectors.toList());
        }
    }

    public void removeEffect(String effectName) {
        effects.removeIf(s -> s.startsWith(effectName.toUpperCase() + ":"));
    }

    public void removeEffect(PotionEffectType type) {
        effects.removeIf(s -> s.startsWith(type.getName() + ":"));
    }

    public void clearEffects() {
        effects.clear();
    }

    public boolean hasEffect(PotionEffectType type) {
        return effects.stream().anyMatch(s -> s.startsWith(type.getName() + ":"));
    }

    public boolean hasEffect(String effectName) {
        return effects.stream().anyMatch(s -> s.startsWith(effectName.toUpperCase() + ":"));
    }

    // Utility methods for effect parsing
    public List<String> getFormattedEffects() {
        return effects.stream()
                .map(this::formatEffectString)
                .collect(Collectors.toList());
    }

    private String formatEffectString(String effectString) {
        String[] parts = effectString.split(":");
        if (parts.length == 2) {
            try {
                String effectName = parts[0];
                int level = Integer.parseInt(parts[1]);
                return effectName + " " + (level + 1); // Display level is 1-based
            } catch (NumberFormatException e) {
                return effectString; // Return as-is if parsing fails
            }
        }
        return effectString;
    }

    // Copy constructor for cloning settings
    public ArenaSettings(ArenaSettings other) {
        this.wallMaterial = other.wallMaterial;
        this.allowBlockBreak = other.allowBlockBreak;
        this.allowBlockPlace = other.allowBlockPlace;
        this.allowItemDrop = other.allowItemDrop;
        this.disableHunger = other.disableHunger;
        this.wallRemovalDelay = other.wallRemovalDelay;
        this.effects = new ArrayList<>(other.effects);
        this.requiredPlayers = other.requiredPlayers;
        this.keepInventory = other.keepInventory;
        this.combatLog = other.combatLog;
        this.preventBlocks = other.preventBlocks;
    }

    @Override
    public String toString() {
        return "ArenaSettings{" +
                "wallMaterial=" + wallMaterial +
                ", allowBlockBreak=" + allowBlockBreak +
                ", allowBlockPlace=" + allowBlockPlace +
                ", allowItemDrop=" + allowItemDrop +
                ", disableHunger=" + disableHunger +
                ", wallRemovalDelay=" + wallRemovalDelay +
                ", effects=" + effects.size() +
                ", requiredPlayers=" + requiredPlayers +
                ", keepInventory=" + keepInventory +
                ", combatLog=" + combatLog +
                ", preventBlocks=" + preventBlocks +
                '}';
    }
}