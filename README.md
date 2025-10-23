# BoxPvPRoomsArenas

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20+-brightgreen.svg)](https://minecraft.net)
[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://openjdk.java.net/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Version](https://img.shields.io/badge/Version-1.0-purple.svg)](https://github.com/vypnitoo/BoxpvpRooms/releases)
A dynamic arena management system for Minecraft servers that creates configurable PvP arenas with automatic wall generation, player management, and advanced effect systems.

## ✨ Features

- 🏟️ **Dynamic Arena Creation** - Create arenas with WorldEdit-style selection tools
- 🧱 **Automatic Wall Generation** - Temporary walls appear when players enter arenas
- ⚡ **Smart Player Management** - Configurable player requirements (1v1, 2v2, custom XvX)
- 🎭 **Effect System** - Apply custom potion effects to arena players
- 🎨 **Professional GUI** - Intuitive graphical interfaces for all management tasks
- ✨ **Fancy Unicode Text** - Configurable fancy text styling (ᴛʜɪѕ ɪѕ ᴇхᴀᴍᴘʟᴇ)
- 🔧 **Highly Configurable** - Extensive configuration options for all features
- 🚀 **Modern & Optimized** - Built with modern Minecraft APIs and best practices

## 📋 Requirements

- **Minecraft Server:** 1.20 or higher
- **Java:** 21 or higher
- **Server Software:** Spigot, Paper, or compatible forks

## 🚀 Installation

1. **Download** the latest release from the [Releases](https://github.com/vypnitoo/BoxpvpRooms/releases) page
2. **Place** the `SmartArenas.jar` file in your server's `plugins` folder
3. **Start/Restart** your server to generate the configuration files
4. **Configure** the plugin in `plugins/SmartArenas/config.yml` as needed
5. **Ready to use!** Type `/arena wand` to get started

## 🎮 Quick Start

### Creating Your First Arena

1. **Get the selection tool:**
   ```
   /arena wand
   ```

2. **Select two corners** by clicking blocks (like WorldEdit)

3. **Create the arena:**
   ```
   /arena create MyFirstArena
   ```

4. **Choose arena type** in the GUI (1v1, 2v2, custom, etc.)

5. **Configure settings** using the arena editor

### Basic Usage

```bash
# Create a new arena
/arena create <name>

# Edit an existing arena
/arena edit <name>

# Delete an arena
/arena delete <name>

# List all arenas
/arena list

# Get selection wand
/arena wand

# Reload configuration
/arena reload
```

## 🔑 Commands & Permissions

### Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/arena create <name>` | Create a new arena | `smartarenas.admin` |
| `/arena delete <name>` | Delete an existing arena | `smartarenas.admin` |
| `/arena edit <name>` | Open arena editor GUI | `smartarenas.admin` |
| `/arena list` | List all configured arenas | `smartarenas.admin` |
| `/arena wand` | Get the selection wand tool | `smartarenas.admin` |
| `/arena reload` | Reload plugin configuration | `smartarenas.admin` |

**Alias:** `/ar` can be used instead of `/arena`

### Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `smartarenas.admin` | Full access to all arena management features | `op` |

## ⚙️ Configuration

### Basic Configuration

```yaml
# Default settings for new arenas
default-arena-settings:
  wall-material: "GLASS"              # Material for arena walls
  allow-block-break: false            # Allow breaking blocks
  allow-block-place: false            # Allow placing blocks
  allow-item-drop: false              # Allow dropping items
  disable-hunger: true                # Disable hunger depletion
  wall-removal-delay-seconds: 30      # Wall removal delay
  effects: []                         # Default potion effects
  required-players: 2                 # Players needed to activate

# Message customization with fancy Unicode support
messages:
  use-fancy-unicode: true             # Enable fancy Unicode text
  arena-sealed: "The arena has been sealed!"
  arena-wall-removed: "The arena wall has been removed."
  # ... more messages
```

### Arena Types

- **1v1** - Two players required
- **2v2** - Four players required
- **3v3** - Six players required
- **Custom** - Define your own XvX format (e.g., 5v5, 10v10)

### Effect System

Add potion effects to arena players:

```yaml
effects:
  - "SPEED:1"           # Speed II
  - "STRENGTH:0"        # Strength I
  - "JUMP_BOOST:2"      # Jump Boost III
```

### Fancy Unicode Text

Toggle between normal and fancy Unicode text:

```yaml
messages:
  use-fancy-unicode: true   # ᴛʜɪѕ ɪѕ ғᴀɴᴄʏ ᴛᴇхᴛ
  # use-fancy-unicode: false   # This is normal text
```

## 🎯 Arena Mechanics

### Wall System
- **Automatic Generation:** Walls appear when enough players enter
- **Smart Materials:** Only replaces air, plants, and other safe blocks
- **Restoration:** Original blocks are restored when walls disappear
- **Configurable Delay:** Set custom removal delays per arena

### Player Management
- **Effect Preservation:** Player effects are saved and restored
- **State Tracking:** Monitors player locations and arena status
- **Flexible Requirements:** Support for any XvX format

### GUI Features
- **Arena Type Selection:** Visual selection of arena configurations
- **Effect Manager:** Browse and configure potion effects
- **Settings Editor:** Comprehensive arena customization
- **Paginated Navigation:** Smooth browsing of large lists

## 🔧 Advanced Features

### Replaceable Materials

The plugin safely replaces these materials with arena walls:

- **Air variants:** `AIR`, `CAVE_AIR`, `VOID_AIR`
- **Fluids:** `WATER`, `LAVA`
- **Plants:** `TALL_GRASS`, `SEAGRASS`, `VINE`, `WHEAT`, `CARROTS`
- **Decoratives:** `TORCH`, `LEVER`, `BUTTON`, `FLOWER_POT`
- **And many more...**

### Message Placeholders

Use placeholders in custom messages:

- `{name}` - Arena name
- `{delay}` - Countdown delay
- `{seconds}` - Remaining seconds
- `{usage}` - Command usage

## 🛠️ Development

### Building from Source

```bash
git clone https://github.com/vypnitoo/BoxpvpRooms.git
cd BoxpvpRooms
./gradlew build
```

### API Usage

SmartArenas provides a clean API for developers:

```java
// Get the plugin instance
Arena plugin = (Arena) Bukkit.getPluginManager().getPlugin("SmartArenas");

// Access managers
ArenaManager arenaManager = plugin.getArenaManager();
PlayerManager playerManager = plugin.getPlayerManager();
MessageManager messageManager = plugin.getMessageManager();

// Create an arena programmatically
arenaManager.createArena("TestArena", pos1, pos2, 2);
```

## 📊 Performance

- **Optimized Block Handling:** Efficient storage and restoration
- **Memory Management:** Automatic cleanup of player data
- **Thread Safety:** Proper async/sync handling
- **Modern APIs:** Uses latest Minecraft Registry API for compatibility

## 🤝 Contributing

We welcome contributions! Please feel free to:

1. 🐛 **Report bugs** by opening an issue
2. 💡 **Suggest features** through feature requests
3. 🔧 **Submit pull requests** with improvements
4. 📖 **Improve documentation**

### Development Guidelines

- Follow Java naming conventions
- Maintain compatibility with Minecraft 1.17+
- Include comprehensive JavaDoc comments
- Write unit tests for new features
- Ensure thread safety for all operations

## 🆘 Support

Need help? Here are your options:

- 📖 **Documentation:** Check this README and the [Wiki]([https://github.com/YourUsername/SmartArenas/wiki](https://discord.gg/cX2Vc7cbJr))
- 🐛 **Bug Reports:** Open an [Issue]([https://github.com/YourUsername/SmartArenas/issues](https://discord.gg/cX2Vc7cbJr))
- 💬 **Discord:** Join our [Discord Server]([https://discord.gg/YourServer](https://discord.gg/cX2Vc7cbJr))

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Bukkit/Spigot Team** - For the amazing server platform
- **Adventure API** - For modern text components
- **Paper Team** - For performance improvements and modern APIs
- **Community** - For feedback, suggestions, and contributions

---

<div align="center">

**Made with ❤️ for the Minecraft community**

[⭐ Star this repo](https://github.com/YourUsername/SmartArenas) • [🐛 Report Bug](https://github.com/YourUsername/SmartArenas/issues) • [💡 Request Feature](https://github.com/YourUsername/SmartArenas/issues)

</div>
