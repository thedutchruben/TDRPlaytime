# TDRPlaytime Plugin

![GitHub Release](https://img.shields.io/github/v/release/thedutchruben/tdrplaytime?sort=date&label=Latest%20release)
![GitHub Release](https://img.shields.io/github/v/release/thedutchruben/tdrplaytime?include_prereleases&sort=date&label=Latest%20pre-release)

### Downloads
![Spiget Downloads](https://img.shields.io/spiget/downloads/47894?label=Spigot&labelColor=Spigot&link=https%3A%2F%2Fwww.spigotmc.org%2Fresources%2Ftdr-playtime-rewards-mysql.47894%2F)
![CurseForge Downloads](https://img.shields.io/curseforge/dt/279491?label=Curse%20Forge)
![GitHub Downloads](https://img.shields.io/github/downloads/thedutchruben/tdrplaytime/total?label=GitHub)
![Hangar Downloads](https://img.shields.io/hangar/dt/tdrplaytime?label=Hangar&link=https%3A%2F%2Fhangar.papermc.io%2FTheDutchRuben%2FTDRPlaytime)
![Modrinth Downloads](https://img.shields.io/modrinth/dt/t9QEZM17?label=Modrinth&link=https%3A%2F%2Fmodrinth.com%2Fplugin%2Ftdr-playtime)

## Overview
TDRPlaytime is a comprehensive Minecraft plugin that tracks player playtime and rewards players with customizable milestones. The plugin offers flexible storage options, an AFK system, and extensive configuration possibilities.

## Features
- **Playtime Tracking**: Accurately track and store player playtime
- **Multiple Database Support**: Choose from MySQL, SQLite, MongoDB, or YAML storage
- **Enhanced Milestone System**: Create custom milestones based on playtime
  - Reward players with items, commands, and custom messages
  - Configure optional firework celebrations for milestones
  - **Conditional Rewards**: Set conditions that must be met (permissions, worlds, time-based)
  - **Cooldown System**: Prevent reward spamming with configurable cooldowns
  - **Permission-Based Rewards**: Require specific permissions to receive rewards
  - **Time-Restricted Rewards**: Limit rewards to specific days or hours
  - **World-Based Rewards**: Only grant rewards in specific worlds
  - **AFK/Activity Requirements**: Require minimum active time or maximum AFK time
- **Repeating Milestones**: Set up rewards that repeat at regular playtime intervals (with all condition features)
- **Advanced AFK System**:
  - Detect when players are AFK based on activity
  - Configure whether AFK time should count toward playtime
  - Optional integration with Essentials AFK system
  - AFK kicking functionality with configurable messages
- **PlaceholderAPI Integration**: Enhanced placeholder support with caching and new placeholder types
- **Playtime History**: Track when players join and leave

## Documentation
- [Setup Guide](SETUP.md) - Installation and configuration instructions
- [Commands](COMMANDS.md) - List of all available commands
- [Permissions](PERMISSIONS.md) - Permission nodes and explanations
- [Placeholders](PLACEHOLDERS.md) - Complete PlaceholderAPI placeholder reference
- [Enhanced Rewards](REWARDS.md) - Advanced reward system with conditions and cooldowns

## Installation
1. Download the latest version from one of the platforms above
2. Place the JAR file in your server's `plugins` directory
3. Start or restart your server
4. Configure the plugin by editing the generated configuration files
5. See the [Setup Guide](SETUP.md) for detailed configuration instructions

## Version 2.0 Development Status
Version 2.0 brings significant improvements and new features:

- ✅ New database implementation
- ✅ Advanced AFK system
- ✅ Playtime history system
- ✅ Improved placeholders with caching and new placeholder types
- ✅ Enhanced reward system with conditions, cooldowns, and advanced features
- ⏳ Migration from 1.x to 2.0 (in testing)

**Note**: Version 2.0 is currently in development and may contain bugs or unfinished features. Use in production environments at your own risk.

## Support and Contributions
- Report issues on [GitHub](https://github.com/thedutchruben/tdrplaytime/issues)
- Fork the repository and submit pull requests

