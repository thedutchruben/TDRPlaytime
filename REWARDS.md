# TDRPlaytime Enhanced Reward System

This document describes the enhanced reward system available in TDRPlaytime 2.0+, including conditions, cooldowns, and permission-based rewards.

## Table of Contents
- [Overview](#overview)
- [Milestone Types](#milestone-types)
- [Reward Conditions](#reward-conditions)
- [Cooldowns](#cooldowns)
- [Permission-Based Rewards](#permission-based-rewards)
- [Configuration Examples](#configuration-examples)
- [Advanced Use Cases](#advanced-use-cases)

## Overview

The enhanced reward system in TDRPlaytime 2.0 provides advanced features for controlling when and how rewards are granted to players:

- **Conditional Rewards**: Set specific conditions that must be met before a reward is granted
- **Cooldowns**: Prevent players from claiming the same reward too frequently
- **Permission-Based**: Require specific permissions to receive rewards
- **Time-Based**: Restrict rewards to specific days or hours
- **World-Based**: Only grant rewards in specific worlds

## Milestone Types

### Regular Milestones

One-time rewards granted when a player reaches a specific playtime threshold.

**Example:**
```
/milestone create FirstHour 3600
```

### Repeating Milestones

Rewards that are granted repeatedly at regular playtime intervals.

**Example:**
```
/repeatingmilestone create HourlyReward 3600
```

## Reward Conditions

Conditions allow you to control when rewards are granted based on various criteria.

### Available Conditions

#### Permission Requirements

**Required Permissions** - Player must have at least one of the listed permissions:
```json
{
  "conditions": {
    "required_permissions": ["vip.tier1", "vip.tier2"]
  }
}
```

**Denied Permissions** - Player must NOT have any of the listed permissions:
```json
{
  "conditions": {
    "denied_permissions": ["banned.from.rewards"]
  }
}
```

#### World-Based Conditions

**Allowed Worlds** - Only grant rewards when player is in specific worlds:
```json
{
  "conditions": {
    "allowed_worlds": ["survival", "creative"]
  }
}
```

**Denied Worlds** - Never grant rewards in specific worlds:
```json
{
  "conditions": {
    "denied_worlds": ["spawn", "lobby"]
  }
}
```

#### Playtime-Based Conditions

**Minimum Active Playtime** - Require a minimum amount of active (non-AFK) time:
```json
{
  "conditions": {
    "min_active_playtime": 7200000
  }
}
```
_Note: Time is in milliseconds (7200000 = 2 hours)_

**Maximum AFK Time** - Limit how much AFK time a player can have:
```json
{
  "conditions": {
    "max_afk_time": 1800000
  }
}
```
_Note: Time is in milliseconds (1800000 = 30 minutes)_

#### Time-Based Conditions

**Allowed Days** - Only grant rewards on specific days of the week:
```json
{
  "conditions": {
    "allowed_days": ["SATURDAY", "SUNDAY"]
  }
}
```
_Valid values: MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY_

**Allowed Hours** - Only grant rewards during specific hours (0-23):
```json
{
  "conditions": {
    "allowed_hours": [18, 19, 20, 21, 22]
  }
}
```
_Example above: Only between 6 PM and 11 PM_

## Cooldowns

Cooldowns prevent players from claiming the same reward too frequently.

### Configuration

**For Regular Milestones:**
```json
{
  "cooldown_millis": 86400000
}
```
_Example: 24-hour cooldown (86400000 ms = 24 hours)_

**For Repeating Milestones:**
```json
{
  "cooldown_millis": 3600000
}
```
_Note: For repeating milestones, cooldown is in addition to the repeat interval_

### How Cooldowns Work

1. Player claims a reward
2. Cooldown timer starts
3. Player cannot claim the same reward until cooldown expires
4. Cooldown is tracked per-player, per-milestone

### Time Conversions

- 1 second = 1,000 milliseconds
- 1 minute = 60,000 milliseconds
- 1 hour = 3,600,000 milliseconds
- 1 day = 86,400,000 milliseconds
- 1 week = 604,800,000 milliseconds

## Permission-Based Rewards

Simple permission check for receiving a reward.

### Configuration

```json
{
  "required_permission": "tdrplaytime.vip.rewards"
}
```

### Difference from Condition Permissions

- `required_permission`: Simple single permission check
- `conditions.required_permissions`: Advanced - player needs ONE of multiple permissions
- `conditions.denied_permissions`: Advanced - player must NOT have ANY of these permissions

## Configuration Examples

### Example 1: VIP Weekend Reward

A milestone that only VIP players can claim on weekends:

```json
{
  "milestoneName": "VIPWeekendBonus",
  "onlineTime": 3600,
  "required_permission": "tdrplaytime.vip",
  "conditions": {
    "allowed_days": ["SATURDAY", "SUNDAY"]
  },
  "commands": [
    "give %playername% diamond 5"
  ],
  "messages": [
    "&6&lVIP Weekend Bonus!",
    "&eYou received 5 diamonds for playing on the weekend!"
  ],
  "firework_show": true,
  "firework_show_amount": 3,
  "cooldown_millis": 86400000
}
```

### Example 2: Active Player Reward

Reward for players who are actually active (not AFK):

```json
{
  "milestoneName": "ActivePlayer10Hours",
  "onlineTime": 36000,
  "conditions": {
    "min_active_playtime": 32400000,
    "max_afk_time": 3600000
  },
  "commands": [
    "give %playername% emerald 10",
    "eco give %playername% 1000"
  ],
  "messages": [
    "&a&lActive Player Reward!",
    "&7Thanks for being an active player!",
    "&e+10 Emeralds & $1000"
  ]
}
```

### Example 3: Survival World Only

Reward only available in the survival world:

```json
{
  "milestoneName": "SurvivalExpert",
  "onlineTime": 72000,
  "conditions": {
    "allowed_worlds": ["survival", "survival_nether", "survival_the_end"]
  },
  "item_stacks": [
    {
      "type": "DIAMOND_SWORD",
      "amount": 1,
      "meta": {
        "display-name": "Survival Expert Sword",
        "enchants": {
          "DAMAGE_ALL": 5,
          "DURABILITY": 3
        }
      }
    }
  ],
  "messages": [
    "&6&lSurvival Expert!",
    "&eYou've mastered survival mode!"
  ]
}
```

### Example 4: Prime Time Reward

Reward only available during peak server hours:

```json
{
  "milestoneName": "PrimeTimePlayer",
  "onlineTime": 7200,
  "conditions": {
    "allowed_hours": [18, 19, 20, 21, 22, 23],
    "denied_worlds": ["lobby", "spawn"]
  },
  "cooldown_millis": 21600000,
  "commands": [
    "give %playername% diamond 3",
    "give %playername% gold_ingot 10"
  ],
  "messages": [
    "&e&lPrime Time Bonus!",
    "&7Thanks for playing during peak hours!",
    "&a+3 Diamonds & 10 Gold Ingots"
  ],
  "firework_show": true
}
```

### Example 5: No-AFK Challenge

Reward for players who haven't been AFK at all:

```json
{
  "milestoneName": "NoAFKChallenge",
  "onlineTime": 18000,
  "conditions": {
    "max_afk_time": 0
  },
  "commands": [
    "give %playername% nether_star 1",
    "eco give %playername% 5000"
  ],
  "messages": [
    "&d&lNo-AFK Achievement!",
    "&75 hours of pure activity!",
    "&e+1 Nether Star & $5000"
  ],
  "firework_show": true,
  "firework_show_amount": 5,
  "firework_show_seconds_between_firework": 1
}
```

### Example 6: Repeating Hourly Reward with Conditions

```json
{
  "milestoneName": "HourlyActiveBonus",
  "onlineTime": 3600,
  "conditions": {
    "denied_worlds": ["spawn", "lobby"],
    "min_active_playtime": 3000000
  },
  "cooldown_millis": 1800000,
  "commands": [
    "give %playername% gold_nugget 5"
  ],
  "messages": [
    "&6+5 Gold Nuggets for another active hour!"
  ]
}
```

## Advanced Use Cases

### Multi-Tier Reward System

Create different rewards based on player rank:

**Bronze Tier:**
```json
{
  "milestoneName": "Bronze10Hours",
  "onlineTime": 36000,
  "required_permission": "tdrplaytime.bronze",
  "conditions": {
    "denied_permissions": ["tdrplaytime.silver", "tdrplaytime.gold", "tdrplaytime.platinum"]
  },
  "commands": ["give %playername% iron_ingot 10"]
}
```

**Silver Tier:**
```json
{
  "milestoneName": "Silver10Hours",
  "onlineTime": 36000,
  "required_permission": "tdrplaytime.silver",
  "conditions": {
    "denied_permissions": ["tdrplaytime.gold", "tdrplaytime.platinum"]
  },
  "commands": ["give %playername% gold_ingot 10"]
}
```

### Weekend Event Rewards

Create special rewards for weekend events:

```json
{
  "milestoneName": "WeekendEvent",
  "onlineTime": 7200,
  "conditions": {
    "allowed_days": ["SATURDAY", "SUNDAY"],
    "allowed_hours": [12, 13, 14, 15, 16, 17, 18, 19, 20],
    "allowed_worlds": ["event_world"]
  },
  "cooldown_millis": 86400000,
  "commands": [
    "give %playername% diamond 10",
    "give %playername% emerald 5"
  ],
  "messages": [
    "&d&lWeekend Event Reward!",
    "&eThank you for participating!"
  ],
  "firework_show": true,
  "firework_show_amount": 10,
  "firework_show_seconds_between_firework": 2
}
```

### Anti-AFK System

Reward players who actively play:

```json
{
  "milestoneName": "ActivePlayer",
  "onlineTime": 14400,
  "conditions": {
    "max_afk_time": 1800000,
    "min_active_playtime": 12600000
  },
  "commands": [
    "give %playername% diamond 5",
    "eco give %playername% 2500"
  ],
  "messages": [
    "&a&lActive Player Bonus!",
    "&7You've been actively playing!",
    "&e+5 Diamonds & $2500"
  ]
}
```

## Managing Rewards

### Viewing Current Rewards

```
/milestone list
/repeatingmilestone list
```

### Adding Conditions (Manual Database Edit)

Currently, conditions must be added by editing the milestone data in your database. The structure follows the JSON format shown in the examples above.

**For MySQL/SQLite:**
Update the milestone record and set the `conditions` field with a JSON string.

**For MongoDB:**
Update the document's `conditions` field with the JSON object.

**For YAML:**
Edit the YAML file and add the conditions section.

### Clearing Cooldowns

Server administrators can manually clear cooldowns by removing entries from the cooldown cache. This requires server restart or database manipulation.

## Troubleshooting

### Reward Not Granted

**Check the following:**

1. **Permission**: Does the player have the required permission?
2. **Cooldown**: Is the player on cooldown for this reward?
3. **Conditions**: Review all conditions to ensure they're met
4. **World**: Is the player in an allowed world?
5. **Time**: Is it the right day/hour for the reward?
6. **AFK Status**: Does the player meet AFK time requirements?

### Debugging Tips

1. Check server console for any error messages
2. Verify condition syntax in database/config
3. Test with simplified conditions first
4. Ensure cooldown times are in milliseconds
5. Verify permission nodes are correct

## Best Practices

1. **Test Thoroughly**: Always test rewards on a staging server first
2. **Clear Conditions**: Document what each reward requires
3. **Reasonable Cooldowns**: Don't make cooldowns too long or too short
4. **Balance**: Ensure rewards are balanced and fair
5. **Performance**: Avoid too many complex conditions on frequently-triggered rewards
6. **Player Communication**: Inform players about special reward conditions

## Future Enhancements

The following features are planned for future versions:

- GUI for managing conditions
- More condition types (biome-based, weather-based, etc.)
- Reward groups and categories
- Cooldown reset commands
- Condition testing commands
- Reward history tracking
- Player-specific reward multipliers

## Version Information

These features are available in TDRPlaytime version 2.0.0-BETA and above.

**Note**: Database storage for cooldowns is currently in-memory only. Full database persistence will be added in a future update.
