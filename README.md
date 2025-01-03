## Warning: 2.0 is in development and is not stable. Use at your own risk.
### 2.0 Checklist:
- [x] New database implementation
- [ ] Playtime history system
- [ ] New implementation for placeholders
- [ ] Updated system for rewards
- [ ] Migration testing from 1.* to 2.0

# Playtime Plugin

![GitHub Release](https://img.shields.io/github/v/release/thedutchruben/tdrplaytime?sort=date&label=Latest%20release)
![GitHub Release](https://img.shields.io/github/v/release/thedutchruben/tdrplaytime?include_prereleases&sort=date&label=Latest%20pre-release)

### Downloads
![Spiget Downloads](https://img.shields.io/spiget/downloads/47894?label=Spigot&labelColor=Spigot&link=https%3A%2F%2Fwww.spigotmc.org%2Fresources%2Ftdr-playtime-rewards-mysql.47894%2F)
![CurseForge Downloads](https://img.shields.io/curseforge/dt/279491?label=Curse%20Forge)
![GitHub Downloads](https://img.shields.io/github/downloads/thedutchruben/tdrplaytime/total?label=GitHub)
![Hangar Downloads](https://img.shields.io/hangar/dt/tdrplaytime?label=Hangar&link=https%3A%2F%2Fhangar.papermc.io%2FTheDutchRuben%2FTDRPlaytime)
![Modrinth Downloads](https://img.shields.io/modrinth/dt/t9QEZM17?label=Modrinth&link=https%3A%2F%2Fmodrinth.com%2Fplugin%2Ftdr-playtime)

## Overview
TDRPlaytime is a Minecraft plugin that tracks player playtime and rewards players with milestones. The plugin is built using Java and Maven.

## Features
- Track player playtime
- Reward players with items, commands, and messages upon reaching milestones
- Customizable repeating milestones
- Firework shows for milestone celebrations

## Installation
1. Clone the repository:
    ```sh
    git clone https://github.com/thedutchruben/TDRPlaytime.git
    ```
2. Navigate to the project directory:
    ```sh
    cd TDRPlaytime
    ```
3. Build the project using Maven:
    ```sh
    mvn clean install
    ```
4. Place the generated `TDRPlaytime.jar` file in your Minecraft server's `plugins` directory.

## Usage
1. Start your Minecraft server.
2. Configure the plugin by editing the `config.yml` file in the `plugins/TDRPlaytime` directory.
