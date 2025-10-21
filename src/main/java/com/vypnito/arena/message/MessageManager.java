package com.vypnito.arena.message;

import com.vypnito.arena.Arena;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class MessageManager {
    private final Arena plugin;
    private final Map<String, String> messages = new HashMap<>();
    private boolean useFancyUnicode = true;

    // Simplified fancy text - disabled due to encoding issues
    private static final Map<Character, Character> UNICODE_MAP = new HashMap<>();
    // Unicode mapping disabled to prevent compilation issues

    public MessageManager(Arena plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    private void loadMessages() {
        FileConfiguration config = plugin.getConfig();
        useFancyUnicode = config.getBoolean("messages.use-fancy-unicode", true);

        // Load all message keys with defaults
        messages.put("arena-sealed", config.getString("messages.arena-sealed", "The arena has been sealed!"));
        messages.put("arena-wall-removed", config.getString("messages.arena-wall-removed", "The arena wall has been removed."));
        messages.put("arena-wall-removal-countdown", config.getString("messages.arena-wall-removal-countdown", "The arena wall will be removed in {delay} seconds."));
        messages.put("arena-opens-countdown", config.getString("messages.arena-opens-countdown", "Arena opens in {seconds} seconds"));
        messages.put("selection-wand-name", config.getString("messages.selection-wand-name", "Arena Selection Wand"));
        messages.put("selection-wand-received", config.getString("messages.selection-wand-received", "You have received the selection wand!"));
        messages.put("arena-created", config.getString("messages.arena-created", "Arena '{name}' has been created successfully!"));
        messages.put("arena-deleted", config.getString("messages.arena-deleted", "Arena '{name}' has been deleted."));
        messages.put("arena-not-found", config.getString("messages.arena-not-found", "Arena '{name}' not found."));
        messages.put("no-permission", config.getString("messages.no-permission", "You don't have permission to use this command."));
        messages.put("player-only", config.getString("messages.player-only", "This command can only be used by players."));
        messages.put("invalid-usage", config.getString("messages.invalid-usage", "Invalid usage. Use: {usage}"));
    }

    /**
     * Get a message by key with optional placeholders
     */
    public String getMessage(String key, String... placeholders) {
        String message = messages.getOrDefault(key, key);

        // Replace placeholders
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
            }
        }

        return useFancyUnicode ? convertToFancyUnicode(message) : message;
    }

    /**
     * Get a formatted Component message
     */
    public Component getComponent(String key, NamedTextColor color, String... placeholders) {
        return Component.text(getMessage(key, placeholders), color);
    }

    /**
     * Get a formatted Component message with default color
     */
    public Component getComponent(String key, String... placeholders) {
        return Component.text(getMessage(key, placeholders));
    }

    /**
     * Convert regular text to fancy Unicode small caps (DISABLED)
     */
    private String convertToFancyUnicode(String text) {
        // Fancy unicode disabled to prevent encoding issues
        return text;
    }

    /**
     * Reload messages from config
     */
    public void reload() {
        messages.clear();
        loadMessages();
    }

    /**
     * Check if fancy Unicode is enabled
     */
    public boolean isFancyUnicodeEnabled() {
        return useFancyUnicode;
    }
}