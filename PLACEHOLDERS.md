# TDRPlaytime Placeholders

This document lists all available PlaceholderAPI placeholders for TDRPlaytime version 2.0+.

## Table of Contents
- [Player Time Placeholders](#player-time-placeholders)
- [Total Time Placeholders](#total-time-placeholders)
- [Top Players Placeholders](#top-players-placeholders)
- [Player Rank Placeholder](#player-rank-placeholder)
- [AFK Placeholders](#afk-placeholders)
- [Active Time Placeholders](#active-time-placeholders)
- [Milestone Placeholders](#milestone-placeholders)
- [Offline Player Placeholders](#offline-player-placeholders)

## Player Time Placeholders

Basic placeholders showing the player's total playtime.

| Placeholder | Description | Example Output |
|------------|-------------|----------------|
| `%tdrplaytime_time%` | Full formatted time | `5 days, 3 hours, 45 minutes, 12 seconds` |
| `%tdrplaytime_time_days_number%` | Days component only | `5` |
| `%tdrplaytime_time_hour_number%` | Hours component only (0-23) | `3` |
| `%tdrplaytime_time_minutes_number%` | Minutes component only (0-59) | `45` |
| `%tdrplaytime_time_seconds_number%` | Seconds component only (0-59) | `12` |

## Total Time Placeholders

Placeholders showing total time converted to a single unit.

| Placeholder | Description | Example Output |
|------------|-------------|----------------|
| `%tdrplaytime_total_seconds%` | Total playtime in seconds | `453912` |
| `%tdrplaytime_total_minutes%` | Total playtime in minutes | `7565` |
| `%tdrplaytime_total_hours%` | Total playtime in hours | `126` |
| `%tdrplaytime_total_days%` | Total playtime in days | `5` |

## Top Players Placeholders

Placeholders for displaying the top 10 players by playtime. Replace `X` with a number from 1 to 10.

### Player Names
| Placeholder | Description | Example Output |
|------------|-------------|----------------|
| `%tdrplaytime_top_names_X%` | Name of player at rank X | `Steve` |

### Player Times
| Placeholder | Description | Example Output |
|------------|-------------|----------------|
| `%tdrplaytime_top_time_X_days%` | Days for rank X player | `10` |
| `%tdrplaytime_top_time_X_hours%` | Hours for rank X player | `5` |
| `%tdrplaytime_top_time_X_minutes%` | Minutes for rank X player | `30` |
| `%tdrplaytime_top_time_X_seconds%` | Seconds for rank X player | `45` |

**Examples:**
- `%tdrplaytime_top_names_1%` - Name of #1 player
- `%tdrplaytime_top_time_1_days%` - Days played by #1 player
- `%tdrplaytime_top_names_5%` - Name of #5 player

**Note:** Top player data is cached for performance. Cache duration is configurable via `settings.top_10_placeholder_cache_time` in config.yml (default: 600 seconds).

## Player Rank Placeholder

Shows the player's current position in the playtime leaderboard.

| Placeholder | Description | Example Output |
|------------|-------------|----------------|
| `%tdrplaytime_rank%` | Player's leaderboard position | `42` |

**Note:** Rank data is cached. Cache duration matches the top players cache setting.

## AFK Placeholders

Placeholders related to AFK (Away From Keyboard) time tracking.

### AFK Status
| Placeholder | Description | Example Output |
|------------|-------------|----------------|
| `%tdrplaytime_afk_status%` | Current AFK status | `AFK` or `Online` |

### AFK Time (Formatted)
| Placeholder | Description | Example Output |
|------------|-------------|----------------|
| `%tdrplaytime_afk_time%` | Full formatted AFK time | `2 days, 1 hour, 15 minutes, 30 seconds` |
| `%tdrplaytime_afk_time_days_number%` | Days component only | `2` |
| `%tdrplaytime_afk_time_hours_number%` | Hours component only | `1` |
| `%tdrplaytime_afk_time_minutes_number%` | Minutes component only | `15` |
| `%tdrplaytime_afk_time_seconds_number%` | Seconds component only | `30` |

### AFK Total Time
| Placeholder | Description | Example Output |
|------------|-------------|----------------|
| `%tdrplaytime_afk_total_seconds%` | Total AFK time in seconds | `176130` |
| `%tdrplaytime_afk_total_minutes%` | Total AFK time in minutes | `2935` |
| `%tdrplaytime_afk_total_hours%` | Total AFK time in hours | `48` |
| `%tdrplaytime_afk_total_days%` | Total AFK time in days | `2` |

## Active Time Placeholders

Placeholders showing active playtime (total time minus AFK time).

### Active Time (Formatted)
| Placeholder | Description | Example Output |
|------------|-------------|----------------|
| `%tdrplaytime_active_time%` | Full formatted active time | `3 days, 2 hours, 30 minutes, 42 seconds` |
| `%tdrplaytime_active_time_days_number%` | Days component only | `3` |
| `%tdrplaytime_active_time_hours_number%` | Hours component only | `2` |
| `%tdrplaytime_active_time_minutes_number%` | Minutes component only | `30` |
| `%tdrplaytime_active_time_seconds_number%` | Seconds component only | `42` |

### Active Total Time
| Placeholder | Description | Example Output |
|------------|-------------|----------------|
| `%tdrplaytime_active_total_seconds%` | Total active time in seconds | `268242` |
| `%tdrplaytime_active_total_minutes%` | Total active time in minutes | `4470` |
| `%tdrplaytime_active_total_hours%` | Total active time in hours | `74` |
| `%tdrplaytime_active_total_days%` | Total active time in days | `3` |

## Milestone Placeholders

Placeholders showing information about the player's next milestone.

| Placeholder | Description | Example Output |
|------------|-------------|----------------|
| `%tdrplaytime_next_milestone%` | Name of next milestone | `Diamond Reward` |
| `%tdrplaytime_next_milestone_time%` | Time remaining until next milestone | `5h 30m 15s` |
| `%tdrplaytime_next_milestone_progress%` | Progress percentage to next milestone | `67.5%` |

**Note:** If no milestones are configured or all milestones have been reached:
- `%tdrplaytime_next_milestone%` returns `None`
- `%tdrplaytime_next_milestone_time%` returns `0h 0m 0s`
- `%tdrplaytime_next_milestone_progress%` returns `100%`

## Offline Player Placeholders

Get playtime information for offline players by replacing `<player>` with the player's name.

| Placeholder Pattern | Description | Example |
|------------|-------------|----------------|
| `%tdrplaytime_<player>_time%` | Total playtime of specified player | `%tdrplaytime_Steve_time%` |
| `%tdrplaytime_<player>_rank%` | Rank of specified player | `%tdrplaytime_Steve_rank%` |
| `%tdrplaytime_<player>_total_hours%` | Total hours of specified player | `%tdrplaytime_Steve_total_hours%` |

**Examples:**
- `%tdrplaytime_Steve_time%` - Shows Steve's total playtime
- `%tdrplaytime_Alex_rank%` - Shows Alex's leaderboard position
- `%tdrplaytime_Notch_total_hours%` - Shows Notch's total hours played

**Note:** Offline player placeholders use async data loading. If data is not immediately available, they will return "No data".

## Configuration

### Cache Settings

Top player and rank placeholders use caching to improve performance. You can configure the cache duration in `config.yml`:

```yaml
settings:
  top_10_placeholder_cache_time: 600  # Cache duration in seconds (default: 10 minutes)
```

**Recommendation:** For high-traffic servers, consider increasing this value to reduce database queries. For servers where real-time accuracy is critical, decrease it.

## Integration Examples

### Scoreboards (Using DeluxeScoreboard or similar)

```yaml
lines:
  - "&6Your Playtime:"
  - "&e%tdrplaytime_time%"
  - ""
  - "&6Your Rank: &e#%tdrplaytime_rank%"
  - "&6Next Milestone: &e%tdrplaytime_next_milestone%"
  - "&6Progress: &e%tdrplaytime_next_milestone_progress%"
```

### Chat (Using ChatFormat plugins)

```yaml
format: "&7[Rank #%tdrplaytime_rank%&7] %player%: %message%"
```

### Tab List (Using TAB plugin)

```yaml
tabprefix: "&6[%tdrplaytime_total_hours%h] "
```

### Signs

Players can create signs with placeholders that will update automatically:
```
Line 1: Top Player:
Line 2: %tdrplaytime_top_names_1%
Line 3: Playtime:
Line 4: %tdrplaytime_top_time_1_days%d %tdrplaytime_top_time_1_hours%h
```

## Performance Considerations

1. **Caching:** Top player and rank placeholders are cached to reduce database load
2. **Async Operations:** All database queries are performed asynchronously to prevent server lag
3. **Offline Players:** Offline player placeholders may have a slight delay on first request
4. **Cache Duration:** Adjust `top_10_placeholder_cache_time` based on your server's needs

## Troubleshooting

### Placeholder shows as plain text
- Ensure PlaceholderAPI is installed
- Ensure TDRPlaytime is loaded
- Run `/papi reload` to reload placeholders

### Placeholder shows "No data" or "Error"
- Check server console for errors
- Verify database connection is working
- For offline players, ensure the player exists in the database
- Check that the player name is spelled correctly (case-sensitive)

### Rank shows as "0"
- Rank data may still be calculating
- Wait for cache to update (default: 10 minutes)
- Verify player has playtime recorded

### Top players not updating
- Top player data is cached (default: 10 minutes)
- Wait for cache to expire or restart the server
- Adjust `top_10_placeholder_cache_time` in config.yml for more frequent updates

## Version Information

These placeholders are available in TDRPlaytime version 2.0.0-BETA and above.

For older versions (1.x), please refer to the plugin's Spigot page or legacy documentation.
