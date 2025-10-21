# SmartArenas Plugin Documentation

## Overview
SmartArenas is a dynamic region management system for Minecraft servers that allows administrators to create configurable PvP arenas with automatic wall generation, player management, and effect systems.

## Plugin Information
- **Name:** SmartArenas
- **Version:** 1.3
- **Author:** Vypnito
- **Minecraft Compatibility:** 1.17+
- **API Version:** 1.17
- **Main Class:** `com.vypnito.arena.Arena`

## Features

### ✨ Dynamic Arena System
- **Automatic Wall Generation:** Creates temporary walls around arenas when players enter
- **Configurable Player Requirements:** Support for 1v1, 2v2, 3v3, and custom XvX formats
- **Smart Wall Removal:** Walls disappear automatically after configurable delay when players leave/die
- **Real-time Countdown:** Action bar display showing arena opening countdown

### 🎯 Advanced Player Management
- **Position Selection:** WorldEdit-style selection tool for defining arena boundaries
- **Player State Tracking:** Saves and restores player effects when entering/leaving arenas
- **Effect Management:** Apply custom potion effects to players in arenas
- **Inventory Management:** Prevents item dropping and block manipulation in arenas

### 🎨 Professional GUI System
- **Arena Type Selection:** Choose from predefined arena types (1v1, 2v2, etc.) or create custom
- **Arena Editor:** Comprehensive GUI for editing arena settings
- **Effects Manager:** Visual interface for adding/removing potion effects
- **Paginated Navigation:** Clean navigation for large lists of effects

### 🌟 Fancy Unicode Text System
- **Configurable Styling:** Toggle between normal text and fancy Unicode (ᴛʜɪѕ ɪѕ ᴇхᴀᴍᴘʟᴇ)
- **Centralized Messages:** All user-facing text is configurable
- **Professional Appearance:** Enhanced visual appeal with small caps Unicode characters

### 🔧 Advanced Configuration
- **Material Replacement:** Define which blocks can be replaced by arena walls
- **Effect Customization:** Configure potion effects with custom amplifiers
- **Message Customization:** Full control over all plugin messages
- **Performance Optimization:** Efficient block handling and memory management

## Commands

### Main Command: `/arena` (Alias: `/ar`)
**Permission:** `smartarenas.admin`

#### Subcommands:
- `/arena create <name>` - Create a new arena
- `/arena delete <name>` - Delete an existing arena
- `/arena edit <name>` - Open arena editor GUI
- `/arena list` - List all configured arenas
- `/arena wand` - Get the selection wand tool
- `/arena reload` - Reload plugin configuration

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `smartarenas.admin` | Allows user to manage arenas | op |

## Configuration

### Default Arena Settings
```yaml
default-arena-settings:
  wall-material: "GLASS"              # Material for arena walls
  allow-block-break: false            # Allow breaking blocks in arena
  allow-block-place: false            # Allow placing blocks in arena
  allow-item-drop: false              # Allow dropping items in arena
  disable-hunger: true                # Disable hunger depletion
  wall-removal-delay-seconds: 30      # Delay before wall removal
  effects: []                         # Default potion effects
  required-players: 2                 # Players needed to activate arena
```

### Message Configuration
```yaml
messages:
  use-fancy-unicode: true             # Enable fancy Unicode text
  arena-sealed: "The arena has been sealed!"
  arena-wall-removed: "The arena wall has been removed."
  arena-wall-removal-countdown: "The arena wall will be removed in {delay} seconds."
  arena-opens-countdown: "Arena opens in {seconds} seconds"
  selection-wand-name: "Arena Selection Wand"
  selection-wand-received: "You have received the selection wand!"
  arena-created: "Arena '{name}' has been created successfully!"
  arena-deleted: "Arena '{name}' has been deleted."
  arena-not-found: "Arena '{name}' not found."
  no-permission: "You don't have permission to use this command."
  player-only: "This command can only be used by players."
  invalid-usage: "Invalid usage. Use: {usage}"
```

### Replaceable Materials
The plugin includes a comprehensive list of materials that can be safely replaced by arena walls:
- Air variants (AIR, CAVE_AIR, VOID_AIR)
- Fluids (WATER, LAVA)
- Plants (TALL_GRASS, SEAGRASS, VINE, etc.)
- Crops (WHEAT, CARROTS, POTATOES, etc.)
- Decorative blocks (TORCH, LEVER, BUTTON, etc.)

## Technical Details

### Architecture
The plugin follows a modular architecture with separate managers for different responsibilities:

- **ArenaManager:** Handles arena creation, deletion, and persistence
- **PlayerManager:** Manages player states and arena assignments
- **SelectionManager:** Handles WorldEdit-style region selection
- **GameManager:** Controls arena gameplay mechanics and effects
- **GUIManager:** Manages all graphical user interfaces
- **MessageManager:** Centralized message handling with Unicode support

### Modern API Usage
- **Registry API:** Uses modern Bukkit Registry for potion effects (1.17+ compatible)
- **Adventure API:** Modern text components for better formatting
- **PersistentDataContainer:** Reliable data storage on items
- **Thread Safety:** Proper async/sync handling for chat events

### Performance Optimizations
- **Efficient Block Handling:** Stores only replaceable blocks for restoration
- **Memory Management:** Automatic cleanup of player data
- **Resource Management:** Proper cancellation of timers and tasks
- **Stream Optimization:** Efficient collection processing

## Installation

1. Download the plugin JAR file
2. Place it in your server's `plugins` folder
3. Start/restart your server
4. Configure the plugin in `plugins/SmartArenas/config.yml`
5. Use `/arena wand` to get the selection tool
6. Create your first arena with `/arena create <name>`

## Usage Guide

### Creating an Arena
1. Get the selection wand: `/arena wand`
2. Select two corners by clicking blocks (like WorldEdit)
3. Create the arena: `/arena create MyArena`
4. Choose arena type in the GUI (1v1, 2v2, custom, etc.)
5. Configure settings in the arena editor

### Configuring Arena Effects
1. Open arena editor: `/arena edit MyArena`
2. Click the "Manage Effects" option
3. Browse available effects in the paginated GUI
4. Click effects to add/remove them
5. Enter amplifier levels when prompted

### Customizing Messages
1. Edit `config.yml` in the plugin folder
2. Modify messages under the `messages:` section
3. Toggle fancy Unicode with `use-fancy-unicode: true/false`
4. Use placeholders like `{name}`, `{delay}`, `{seconds}` in messages
5. Reload with `/arena reload`

## Code Quality Features

### Modern Java Practices
- **Records:** For immutable data structures
- **Stream API:** Efficient data processing
- **Lambda Expressions:** Clean functional programming
- **Proper Exception Handling:** Comprehensive error management

### Security & Safety
- **Null Safety:** Comprehensive null checking
- **Input Validation:** Proper validation of user inputs
- **Thread Safety:** Async-safe operations
- **Resource Management:** Automatic cleanup of resources

### Maintainability
- **Clean Architecture:** Separation of concerns
- **Comprehensive Documentation:** Well-documented code
- **Error Handling:** Graceful failure handling
- **Modular Design:** Easy to extend and modify

## Recent Improvements (Version 1.3)

### 🧹 Code Cleanup
- Removed 367 lines of unused code
- Deleted 4 redundant GUI files
- Cleaned up deprecated implementations

### 🔧 Modernization
- Updated to 1.17+ compatible APIs
- Fixed deprecated `PotionEffectType.getByName()` usage
- Improved thread safety for async operations
- Added proper null safety checks

### ✨ New Features
- **Fancy Unicode Text System:** Configurable fancy text styling
- **MessageManager:** Centralized message handling
- **Enhanced Configuration:** Comprehensive message customization
- **Improved Error Handling:** Better user feedback and debugging

### 🐛 Bug Fixes
- Fixed resource leaks in countdown timers
- Resolved async thread safety issues
- Improved effect identification reliability
- Enhanced GUI navigation stability

## Support

For issues, suggestions, or contributions, please refer to the plugin's repository or contact the author.

---

**Last Updated:** $(Get-Date -Format "MMMM dd, yyyy")
**Plugin Version:** 1.3
**Documentation Version:** 1.0