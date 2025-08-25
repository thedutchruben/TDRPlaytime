# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Development Commands

### Maven Commands
- `mvn clean compile` - Compile the project
- `mvn clean package` - Build the plugin JAR file (outputs to `target/TDRPlaytime-{version}.jar`)
- `mvn test` - Run JUnit 5 tests
- `mvn test -Dtest=ClassName#methodName` - Run a specific test method
- `mvn jacoco:report` - Generate code coverage reports (available at `target/site/jacoco/index.html`)
- `mvn javadoc:javadoc` - Generate JavaDoc documentation (outputs to `target/site/`)
- `mvn source:jar` - Generate source JAR file

### Testing
- Tests use JUnit 5 with Mockito and MockBukkit for Bukkit API mocking
- Test files follow the pattern `**/*Test.java`
- Coverage reports are generated with JaCoCo
- Maven Surefire configured to run tests with parallel execution disabled

## Project Architecture

### Core Plugin Structure
- **Main Plugin Class**: `PlayTimePlugin.java` - Standard Bukkit plugin entry point that delegates to `Playtime.java`
- **Core Manager**: `Playtime.java` - Central manager class handling plugin lifecycle, storage, and caching
- **Dependency Loading**: Uses libby-bukkit for runtime dependency management to keep JAR size small

### Package Structure
- `core/` - Core functionality and configuration
  - `afk/` - AFK detection and management system
  - `events/` - Custom plugin events for milestones, AFK, and players
  - `objects/` - Data models (PlaytimeUser, Milestone, RepeatingMilestone, PlaytimeHistory)
  - `storage/` - Abstract storage layer with implementations for MySQL, SQLite, MongoDB, YAML
  - `translations/` - Message management system
- `extensions/` - Plugin integrations (BStats, PlaceholderAPI)
- `modules/` - Feature modules organized by functionality
  - `afk/` - AFK commands and listeners
  - `milestones/` - Milestone commands, listeners, and GUI implementations
  - `player/` - Player commands, listeners, and runnables
  - `playtime_history/` - Playtime history tracking
- `utils/` - Utility classes for time formatting and text replacement

### Storage System
- Abstract `Storage` base class defines the interface
- Four storage implementations: MySQL, SQLite, MongoDB, YAML
- All storage operations use CompletableFuture for async handling
- Storage type configured in `storage.yml` with `type` field

### Milestone System
- Two types: Regular milestones (one-time rewards) and Repeating milestones (recurring rewards)
- Milestones support multiple reward types: commands, items, messages, fireworks
- Event-driven system for milestone achievements

### AFK System
- Configurable AFK detection based on player activity (chat, movement, interaction)
- Optional integration with Essentials plugin
- Supports AFK kicking and time exclusion from playtime tracking

## Configuration Files
- `config.yml` - Main plugin configuration (AFK settings, cache times, etc.)
- `storage.yml` - Database connection configuration
- `translations.yml` - All plugin messages and translations
- Plugin uses custom Settings enum for type-safe configuration access

## Key Dependencies
- **MCCore**: Custom framework (nl.thedutchruben.mccore) for common Minecraft plugin functionality
- **Spigot API**: 1.21.1 target version
- **Lombok**: Used extensively for getters/setters
- **PlaceholderAPI**: Optional integration for placeholder support
- **Libby**: Runtime dependency loader
- **HikariCP**: Database connection pooling (for MySQL)

## Development Notes
- Plugin is currently version 2.0.0-BETA with significant refactoring from 1.x
- Uses Java 11 target with annotation processing for Lombok
- Extensive use of async patterns with CompletableFuture
- Plugin supports hot-reload of milestones and configuration changes
- All user data is cached in memory for performance
- Maven Shade plugin relocates bstats to avoid conflicts
- Dependencies loaded at runtime via libby-bukkit to keep JAR size minimal