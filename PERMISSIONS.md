# TDRPlaytime Permissions

This document lists all permissions available in the TDRPlaytime plugin. Assigning these permissions to players or groups allows them to use specific commands and features.

## Playtime Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `playtime.playtime` | Access to basic playtime command to check own playtime | `true` |
| `playtime.playtime.other` | Check another player's playtime | `op` |
| `playtime.playtime.reset` | Reset a player's playtime | `op` |
| `playtime.playtime.add` | Add playtime to a player | `op` |
| `playtime.playtime.remove` | Remove playtime from a player | `op` |
| `playtime.playtime.top` | View the top players by playtime | `op` |
| `playtime.playtime.pluginInfo` | Access plugin information | `op` |

## Milestone Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `playtime.milestone` | Access to milestone commands | `op` |
| `playtime.milestone.list` | List all milestones | `op` |
| `playtime.milestone.info` | Get information about a milestone | `op` |
| `playtime.milestone.create` | Create a new milestone | `op` |
| `playtime.milestone.delete` | Delete a milestone | `op` |
| `playtime.milestone.test` | Test milestone rewards on yourself | `op` |
| `playtime.milestone.addItemToMilestone` | Add held item to a milestone | `op` |
| `playtime.milestone.addCommand` | Add a command to a milestone | `op` |
| `playtime.milestone.removeCommand` | Remove a command from a milestone | `op` |
| `playtime.milestone.addMessage` | Add a message to a milestone | `op` |
| `playtime.milestone.removeMessage` | Remove a message from a milestone | `op` |
| `playtime.milestone.togglefirework` | Toggle firework effects for a milestone | `op` |
| `playtime.milestone.setfireworkamount` | Set the amount of fireworks for a milestone | `op` |
| `playtime.milestone.setfireworkdelay` | Set the delay between fireworks | `op` |

## Repeating Milestone Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `playtime.repeatingmilestone` | Access to repeating milestone commands | `op` |
| `playtime.repeatingmilestone.list` | List all repeating milestones | `op` |
| `playtime.repeatingmilestone.info` | Get information about a repeating milestone | `op` |
| `playtime.repeatingmilestone.create` | Create a new repeating milestone | `op` |
| `playtime.repeatingmilestone.delete` | Delete a repeating milestone | `op` |
| `playtime.repeatingmilestone.test` | Test repeating milestone rewards on yourself | `op` |
| `playtime.repeatingmilestone.addItemToMilestone` | Add held item to a repeating milestone | `op` |
| `playtime.repeatingmilestone.addCommand` | Add a command to a repeating milestone | `op` |
| `playtime.repeatingmilestone.removeCommand` | Remove a command from a repeating milestone | `op` |
| `playtime.repeatingmilestone.addMessage` | Add a message to a repeating milestone | `op` |
| `playtime.repeatingmilestone.removeMessage` | Remove a message from a repeating milestone | `op` |
| `playtime.repeatingmilestone.togglefirework` | Toggle firework effects for a repeating milestone | `op` |
| `playtime.repeatingmilestone.setfireworkamount` | Set the amount of fireworks for a repeating milestone | `op` |
| `playtime.repeatingmilestone.setfireworkdelay` | Set the delay between fireworks | `op` |

## AFK Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `playtime.afk` | Toggle AFK status | `true` |
| `playtime.afk.status` | Check player AFK status | `true` |
| `playtime.afk.active` | Check player active playtime | `true` |
| `playtime.afk.kickexempt` | Exempt from being kicked when AFK | `op` |

## Permission Examples

### Example 1: Basic User Permissions
```yaml
permissions:
  exampleuser:
    playtime.playtime: true
    playtime.afk: true
    playtime.afk.status: true
    playtime.afk.active: true
```

### Example 2: Moderator Permissions
```yaml
permissions:
  moderator:
    playtime.playtime: true
    playtime.playtime.other: true
    playtime.playtime.top: true
    playtime.afk: true
    playtime.afk.status: true 
    playtime.afk.active: true
    playtime.afk.kickexempt: true
```

### Example 3: Admin Permissions
```yaml
permissions:
  admin:
    playtime.playtime.*: true
    playtime.milestone.*: true
    playtime.repeatingmilestone.*: true
    playtime.afk.*: true
```

## Using Permission Plugins

You can use any permission plugin that supports Bukkit permissions, such as LuckPerms, PermissionsEx, or GroupManager. Here's an example using LuckPerms:

```
/lp user Steve permission set playtime.playtime true
/lp group moderator permission set playtime.playtime.other true
/lp group admin permission set playtime.milestone true
```