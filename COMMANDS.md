# TDRPlaytime Commands

This document provides detailed information about all commands available in the TDRPlaytime plugin.

## Playtime Commands

### Basic Playtime Commands
| Command | Description | Permission | Usage |
|---------|-------------|------------|-------|
| `/playtime` | View your own playtime | `playtime.playtime` | `/playtime` |
| `/playtime <player>` | View another player's playtime | `playtime.playtime.other` | `/playtime Steve` |
| `/playtime top [amount]` | View top players by playtime | `playtime.playtime.top` | `/playtime top 5` |

### Playtime Administration
| Command | Description | Permission | Usage |
|---------|-------------|------------|-------|
| `/playtime reset <player>` | Reset a player's playtime | `playtime.playtime.reset` | `/playtime reset Steve` |
| `/playtime add <player> <time>` | Add playtime to a player | `playtime.playtime.add` | `/playtime add Steve 2h30m` |
| `/playtime remove <player> <time>` | Remove playtime from a player | `playtime.playtime.remove` | `/playtime remove Steve 1h` |
| `/playtime pluginInfo` | Display plugin information | `playtime.playtime.pluginInfo` | `/playtime pluginInfo` |

Time format examples:
- `1d` - 1 day
- `2h` - 2 hours
- `30m` - 30 minutes
- `45s` - 45 seconds
- `1d2h3m4s` - 1 day, 2 hours, 3 minutes, 4 seconds

## Milestone Commands

| Command | Description | Permission | Usage |
|---------|-------------|------------|-------|
| `/milestone list` | List all milestones | `playtime.milestone.list` | `/milestone list` |
| `/milestone info <name>` | Get info about a milestone | `playtime.milestone.info` | `/milestone info gold` |
| `/milestone create <name> <time>` | Create a new milestone | `playtime.milestone.create` | `/milestone create diamond 24h` |
| `/milestone delete <name>` | Delete a milestone | `playtime.milestone.delete` | `/milestone delete bronze` |
| `/milestone test <name>` | Test the rewards of a milestone | `playtime.milestone.test` | `/milestone test gold` |

### Milestone Reward Management
| Command | Description | Permission | Usage |
|---------|-------------|------------|-------|
| `/milestone addItemToMilestone <name>` | Add the item in your hand to a milestone | `playtime.milestone.addItemToMilestone` | `/milestone addItemToMilestone gold` |
| `/milestone addCommand <name> <command>` | Add a command to a milestone | `playtime.milestone.addCommand` | `/milestone addCommand gold give %playername% diamond 1` |
| `/milestone removeCommand <name> <command>` | Remove a command from a milestone | `playtime.milestone.removeCommand` | `/milestone removeCommand gold give %playername% diamond 1` |
| `/milestone addMessage <name> <message>` | Add a message to a milestone | `playtime.milestone.addMessage` | `/milestone addMessage gold &6Congratulations on reaching gold status!` |
| `/milestone removeMessage <name> <message>` | Remove a message from a milestone | `playtime.milestone.removeMessage` | `/milestone removeMessage gold &6Congratulations on reaching gold status!` |

### Milestone Firework Management
| Command | Description | Permission | Usage |
|---------|-------------|------------|-------|
| `/milestone togglefirework <name>` | Toggle firework effects for a milestone | `playtime.milestone.togglefirework` | `/milestone togglefirework gold` |
| `/milestone setfireworkamount <name> <amount>` | Set the number of fireworks | `playtime.milestone.setfireworkamount` | `/milestone setfireworkamount gold 5` |
| `/milestone setfireworkdelay <name> <seconds>` | Set delay between fireworks | `playtime.milestone.setfireworkdelay` | `/milestone setfireworkdelay gold 1` |

## Repeating Milestone Commands

Repeating milestones work similarly to regular milestones but are awarded repeatedly when a player reaches intervals of the specified playtime.

| Command | Description | Permission | Usage |
|---------|-------------|------------|-------|
| `/repeatingmilestone list` | List all repeating milestones | `playtime.repeatingmilestone.list` | `/repeatingmilestone list` |
| `/repeatingmilestone info <name>` | Get info about a repeating milestone | `playtime.repeatingmilestone.info` | `/repeatingmilestone info hourly` |
| `/repeatingmilestone create <name> <time>` | Create a new repeating milestone | `playtime.repeatingmilestone.create` | `/repeatingmilestone create hourly 1h` |
| `/repeatingmilestone delete <name>` | Delete a repeating milestone | `playtime.repeatingmilestone.delete` | `/repeatingmilestone delete daily` |
| `/repeatingmilestone test <name>` | Test the rewards of a repeating milestone | `playtime.repeatingmilestone.test` | `/repeatingmilestone test hourly` |

### Repeating Milestone Reward Management
The same commands as regular milestones, but using `/repeatingmilestone` instead of `/milestone`:

- `/repeatingmilestone addItemToMilestone <name>`
- `/repeatingmilestone addCommand <name> <command>`
- `/repeatingmilestone removeCommand <name> <command>`
- `/repeatingmilestone addMessage <name> <message>`
- `/repeatingmilestone removeMessage <name> <message>`
- `/repeatingmilestone togglefirework <name>`
- `/repeatingmilestone setfireworkamount <name> <amount>`
- `/repeatingmilestone setfireworkdelay <name> <seconds>`

## AFK Commands

| Command | Description | Permission | Usage |
|---------|-------------|------------|-------|
| `/afk` | Toggle your AFK status | `playtime.afk` | `/afk` |
| `/afk status [player]` | Check if a player is AFK (or yourself) | `playtime.afk.status` | `/afk status` or `/afk status Steve` |
| `/afk active [player]` | Check a player's active playtime | `playtime.afk.active` | `/afk active` or `/afk active Steve` |

## Command Variables

In milestone commands, you can use these variables:
- `%playername%` or `%player_name%` - The player's name
- `%playeruuid%` or `%player_uuid%` - The player's UUID