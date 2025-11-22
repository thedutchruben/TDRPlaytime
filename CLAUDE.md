# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TDRPlaytime is a Spigot/Paper Minecraft plugin (Java 11) that tracks player playtime and rewards players through a milestone system. It supports multiple storage backends, features an AFK detection system, and integrates with PlaceholderAPI.

**Current Version:** 2.0.0-BETA (active development)

## Build & Development Commands

### Building
```bash
mvn clean package
```
Built JAR: `target/TDRPlaytime-${version}.jar`

### Testing
```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=ClassName#methodName

# Run with coverage report
mvn test jacoco:report
# Coverage report: target/site/jacoco/index.html
```

### Documentation
```bash
# Generate JavaDoc
mvn javadoc:javadoc
# Output: target/site/
```

### Installing Locally
```bash
mvn clean install
```

## Architecture

### Core Plugin Structure

**Lifecycle Flow:**
1. `PlayTimePlugin.onLoad()` → Downloads dependencies via libby (HikariCP, MongoDB, SQLite)
2. `PlayTimePlugin.onEnable()` → Initializes `Playtime` class
3. `Playtime.onEnable()` → Sets up storage, loads milestones, registers commands/listeners

**Main Classes:**
- `PlayTimePlugin`: Spigot JavaPlugin entry point
- `Playtime`: Core singleton managing plugin state, storage, and user cache
- `AFKManager`: Singleton managing AFK detection and status updates

### Storage System

**Abstract Pattern:** All storage implementations extend `Storage` abstract class with CompletableFuture-based async operations.

**Supported Types:**
- `Mysql`: MySQL/MariaDB via HikariCP connection pool
- `SqlLite`: SQLite embedded database
- `Mongodb`: MongoDB driver
- `Yaml`: File-based (not recommended for production)

**Key Points:**
- Storage type selected in `Playtime.getSelectedStorage()` based on config
- All user data cached in `Playtime.playtimeUsers` Map
- Milestones and repeating milestones loaded at startup into lists

### Module Structure

Code organized into modules under `modules/` package:

- **afk/**: AFK detection system (commands, listeners)
- **milestones/**: Milestone and repeating milestone management (commands, listeners for checking/awarding)
- **player/**: Player tracking (join/quit listeners, playtime update/save runnables, playtime command)
- **playtime_history/**: Join/quit history tracking (in development)

### Core Domain Objects

**PlaytimeUser:**
- Stores: UUID, name, total time (ms), AFK time (ms), AFK status, last activity timestamp
- Methods: `updatePlaytime()` (checks AFK status), `translateTime()` (converts ms to [days, hours, mins, secs])
- Uses Gson serialization annotations for storage persistence

**Milestone:**
- One-time rewards at specific playtime thresholds
- Can contain: commands, items, messages, fireworks

**RepeatingMilestone:**
- Recurring rewards at regular intervals (e.g., every hour)
- Same reward structure as Milestone

### AFK System

**AFKManager Behavior:**
- Tracks last activity timestamp per player
- Configurable threshold (default: 5 minutes)
- Optional Essentials integration (checks Essentials AFK status)
- Player activity resets timer (chat, movement, interactions)
- AFK time counted separately; optionally excluded from playtime
- Fires custom events: `PlayerAFKEvent`, `PlayerReturnFromAFKEvent`

**Activity Listeners:** `AFKActivityListener` monitors player events to record activity

### Event System

Custom events in `core/events/`:
- **afk/**: AFK status changes
- **milestone/**: Milestone CRUD and receive events
- **player/**: User load/unload/save, playtime updates
- **repeatingmilestone/**: Repeating milestone CRUD and receive events

Events follow Bukkit event patterns and can be listened to by other plugins.

### Scheduled Tasks

**Runnables:**
- `UpdatePlayTimeRunnable`: Periodically calls `updatePlaytime()` on all online users
- `SavePlayTimeRunnable`: Periodically saves all user data to storage

Both scheduled in module initialization.

### Configuration

**Files (managed by FileManager from mccore library):**
- `Settings`: Enum-based config access via mccore
- `Messages`: Translation system for all plugin messages
- `ConfigFiles`: Constants for config file paths

Configuration uses the mccore library's config system.

### External Integrations

**PlaceholderAPI:**
- `PlaceholderAPIExtension`: Registers placeholders for playtime data
- Provides top player, individual player, AFK status placeholders

**bStats:**
- `BStatsExtension`: Metrics collection

**Essentials (optional):**
- AFKManager can use Essentials AFK API if enabled in config

## Important Patterns

1. **CompletableFuture Usage:** All storage operations are async; use `.thenAccept()`, `.join()`, or `.get()` appropriately
2. **Singleton Access:** `Playtime.getInstance()`, `AFKManager.getInstance()`
3. **User Cache:** Always check `Playtime.getPlaytimeUsers()` map before loading from storage
4. **Event Firing:** Schedule events on main thread via `Bukkit.getScheduler().runTask()` when called from async context
5. **Time Storage:** All time values stored as float milliseconds internally
6. **Lombok Usage:** Core objects use Lombok `@Getter` for field access

## Testing Notes

- Tests use JUnit 5, Mockito, and MockBukkit
- Test classes: `MilestoneTest.java`, `PlaytimeUserTest.java`
- Bukkit environment must be mocked for most tests
- Storage implementations should be tested with actual database connections where feasible

## Dependencies

**Key Libraries:**
- Spigot API 1.21.1 (provided)
- Lombok 1.18.38 (compile-time)
- HikariCP 6.2.1 (runtime downloaded)
- MongoDB driver 5.2.1 (runtime downloaded)
- SQLite JDBC 3.46.1.2 (runtime downloaded)
- mccore 1.6.0 (provided - custom framework by author)
- PlaceholderAPI 2.11.6 (optional)
- Essentials 2.20.1 (optional)

Dependencies downloaded at runtime via libby except provided ones.

## Common Development Scenarios

### Adding a New Storage Type
1. Create new class in `core/storage/types/` extending `Storage`
2. Implement all abstract methods with async CompletableFuture returns
3. Add case to `Playtime.getSelectedStorage()` switch statement
4. Document new storage type in SETUP.md

### Adding a New Milestone Reward Type
1. Add field to `Milestone`/`RepeatingMilestone` objects
2. Update storage implementations to persist new field
3. Add command in `MileStoneCommand`/`RepeatingMilestoneCommand` to configure
4. Update `UpdatePlaytimeListener` to check and award new reward type

### Adding New PlaceholderAPI Placeholders
1. Add parsing logic in `PlaceholderAPIExtension.onRequest()`
2. Use existing pattern: parse identifier, fetch user data, return formatted string
3. Document in SETUP.md placeholder section

## Version 2.0 Development Status

Current focus areas:
- Database implementation ✅
- AFK system ✅
- Playtime history system ✅
- Improved placeholders (in progress)
- Enhanced reward system (in progress)
- Migration from 1.x to 2.0 (testing)

Version 2.0 may contain bugs or incomplete features.
