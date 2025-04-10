# TDRPlaytime Setup Guide

This guide provides comprehensive instructions for setting up and configuring the TDRPlaytime plugin.

## Installation

1. Download the latest version of TDRPlaytime from one of the following sources:
   - [Spigot](https://www.spigotmc.org/resources/tdr-playtime-rewards-mysql.47894/)
   - [Curse Forge](https://www.curseforge.com/minecraft/bukkit-plugins/tdr-playtime)
   - [GitHub](https://github.com/thedutchruben/tdrplaytime/releases)
   - [Hangar](https://hangar.papermc.io/TheDutchRuben/TDRPlaytime)
   - [Modrinth](https://modrinth.com/plugin/tdr-playtime)

2. Place the downloaded JAR file in your server's `plugins` directory.

3. Start or restart your server.

4. Once the server has started, the plugin will generate default configuration files in the `plugins/TDRPlaytime` directory.

## Configuration Files

TDRPlaytime uses several configuration files:

- `config.yml` - Main configuration file
- `storage.yml` - Database configuration
- `translations.yml` - Messages and translations
- Players are stored under `/players/`
- Milestones are stored under `/milestones/`
- Repeating milestones are stored under `/repeatingmilestones/`

## Database Configuration

TDRPlaytime supports multiple database types. You can configure your preferred storage method in the `storage.yml` file.

### Storage Types

#### SQLite (Default)
```yaml
type: sqllite
```
No additional configuration required. Data will be stored in a file called `playtime.db` in the plugin folder.

#### MySQL
```yaml
type: mysql
mysql:
  hostname: localhost
  port: 3306
  username: your_username
  password: your_password
  ssl: true
  schema: playtime
  prefix: ''
  pool: 20
  driver: jdbc:mysql://
```

#### MongoDB
```yaml
type: mongodb
mongo:
  hostname: localhost
  port: 27017
  username: your_username
  password: your_password
  collection: playtime
```

#### YAML (Not recommended for production)
```yaml
type: yaml
```
Using YAML storage is not recommended for servers with many players as it can cause performance issues.

## Main Configuration

The `config.yml` file contains general settings for the plugin. Here are the key configuration options:

### General Settings
```yaml
settings:
  update-check: true  # Check for plugin updates
  cache-time: 5       # Cache time in minutes
  top_10_placeholder_cache_time: 600  # Cache time for top 10 players placeholder in seconds
```

### AFK System Configuration
```yaml
settings:
  afk:
    countAfkTime: true      # Whether AFK time should count toward playtime
    useEssentialsApi: false # Use Essentials AFK detection if available
    thresholdMinutes: 5     # Time until player is considered AFK
    broadcastMessages: true # Broadcast AFK status changes
    broadcastToAll: false   # Broadcast to all players (true) or only to the affected player (false)
    
    # Activity types that reset AFK timer
    events:
      chatResetAfkTime: true
      movementResetAfkTime: true
      interactResetAfkTime: true
    
    # AFK Kick settings
    kick:
      enabled: false
      thresholdMinutes: 30
      message: "&cYou have been kicked for being AFK too long."
```

## Translations

You can customize all plugin messages in the `translations.yml` file. Here are some examples:

```yaml
command:
  playtime:
    time_message: "&8[&6PlayTime&8] &7Your playtime is &6%D% &7day(s) &6%H% &7hour(s) &6%M% &7minute(s) &6%S% &7second(s)"
    user_time_message: "&8[&6PlayTime&8] &7%NAME% 's playtime is &6%D% &7day(s) &6%H% &7hour(s) &6%M% &7minute(s) &6%S% &7second(s)"
    reset_time_confirm: "&cUser time reset!"

afk:
  player_now_afk: "&8[&6PlayTime&8] &7%player% is now AFK"
  player_no_longer_afk: "&8[&6PlayTime&8] &7%player% is no longer AFK"
```

## Creating Milestones

Milestones can be created using the in-game commands. Here's a step-by-step example:

1. Create a basic milestone:
   ```
   /milestone create diamond 24h
   ```

2. Add a command reward:
   ```
   /milestone addCommand diamond give %playername% diamond 5
   ```

3. Add a message:
   ```
   /milestone addMessage diamond &bCongratulations! You've played for 24 hours and earned the Diamond rank!
   ```

4. Add item rewards (hold the item you want to give):
   ```
   /milestone addItemToMilestone diamond
   ```

5. Enable fireworks:
   ```
   /milestone togglefirework diamond
   /milestone setfireworkamount diamond 5
   /milestone setfireworkdelay diamond 1
   ```

## Creating Repeating Milestones

Repeating milestones are similar to regular milestones but are awarded repeatedly at specified intervals:

1. Create a repeating milestone:
   ```
   /repeatingmilestone create hourly 1h
   ```

2. Configure it using the same commands as regular milestones, just with the `repeatingmilestone` command:
   ```
   /repeatingmilestone addCommand hourly give %playername% cookie 1
   /repeatingmilestone addMessage hourly &aYou've played another hour! Have a cookie!
   ```

## PlaceholderAPI Integration

TDRPlaytime integrates with PlaceholderAPI to provide placeholders that can be used in other plugins. Available placeholders include:

- `%tdrplaytime_time%` - Formatted playtime
- `%tdrplaytime_time_days_number%` - Days of playtime
- `%tdrplaytime_time_hour_number%` - Hours of playtime
- `%tdrplaytime_time_minutes_number%` - Minutes of playtime
- `%tdrplaytime_time_seconds_number%` - Seconds of playtime
- `%tdrplaytime_top_names_1%` - Name of the player with most playtime
- `%tdrplaytime_top_time_1_days%` - Days of playtime for the top player
- `%tdrplaytime_afk_status%` - Whether the player is AFK
- `%tdrplaytime_afk_time%` - How long the player has been AFK
- `%tdrplaytime_active_time%` - Active playtime (excluding AFK time)

## Permissions

For a full list of permissions, see the [PERMISSIONS.md](PERMISSIONS.md) file.

## Troubleshooting

### Common Issues

#### Players not getting rewards
- Check if the milestone time is set correctly
- Make sure the player has enough playtime
- Check console for any error messages when the milestone should be triggered

#### Database connection issues
- Verify your database credentials
- Make sure the database exists and is accessible
- Check if the database user has the necessary permissions

#### AFK system not working correctly
- Ensure the AFK threshold is set appropriately 
- Check if any other plugins might be interfering with player activity detection
- If using Essentials integration, verify that Essentials is correctly installed

#### Performance issues
- If using YAML storage, consider switching to SQLite or MySQL
- Adjust the cache-time settings if necessary
- Check server performance during milestone awards

For more help, you can:
- Report issues on [GitHub](https://github.com/thedutchruben/tdrplaytime/issues)
- Contact the plugin author via Spigot or Discord