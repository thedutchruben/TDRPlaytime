package nl.thedutchruben.playtime.core.migration;

import nl.thedutchruben.mccore.utils.config.FileManager;
import nl.thedutchruben.playtime.Playtime;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.logging.Level;

/**
 * Handles migration from old TDRPlaytime 1.x configuration format to 2.0 format
 *
 * Old format:
 * - config.yml: language, settings (update_check, cacheTime, update_checktime, afk)
 * - database.yml: database type, mysql settings, mongodb settings
 *
 * New format:
 * - config.yml: settings (update-check, cache-time, top_10_placeholder_cache_time, afk, playtime_history)
 * - storage.yml: type, mysql settings, mongo settings
 *
 * IMPORTANT: This migration only affects configuration files, not your actual data!
 * - Your player playtime data, milestones, and history remain untouched
 * - The storage type is preserved to ensure data continuity
 * - Only configuration settings are converted to the new format
 */
public class ConfigMigration {

    private final FileManager fileManager;
    private final File dataFolder;

    public ConfigMigration(FileManager fileManager, File dataFolder) {
        this.fileManager = fileManager;
        this.dataFolder = dataFolder;
    }

    /**
     * Check if migration is needed by looking for old database.yml file
     *
     * @return true if old config files exist and migration should run
     */
    public boolean needsMigration() {
        File oldDatabaseYml = new File(dataFolder, "database.yml");
        return oldDatabaseYml.exists();
    }

    /**
     * Perform the migration from old config format to new format
     */
    public void migrate() {
        Playtime.getPlugin().getLogger().log(Level.INFO, "===========================================");
        Playtime.getPlugin().getLogger().log(Level.INFO, "Starting configuration migration from 1.x to 2.0...");
        Playtime.getPlugin().getLogger().log(Level.INFO, "");
        Playtime.getPlugin().getLogger().log(Level.INFO, "NOTE: Your player data is safe!");
        Playtime.getPlugin().getLogger().log(Level.INFO, "Only configuration files are being updated.");
        Playtime.getPlugin().getLogger().log(Level.INFO, "===========================================");

        try {
            // Load old config files
            File oldConfigFile = new File(dataFolder, "config.yml");
            File oldDatabaseFile = new File(dataFolder, "database.yml");

            if (oldConfigFile.exists()) {
                migrateConfigYml(oldConfigFile);
            }

            if (oldDatabaseFile.exists()) {
                migrateDatabaseYml(oldDatabaseFile);
            }

            // Backup old files
            backupOldFiles();

            // Display migration summary
            String finalStorageType = fileManager.getConfig("storage.yml").get().getString("type", "yaml");

            Playtime.getPlugin().getLogger().log(Level.INFO, "===========================================");
            Playtime.getPlugin().getLogger().log(Level.INFO, "Configuration migration completed successfully!");
            Playtime.getPlugin().getLogger().log(Level.INFO, "");
            Playtime.getPlugin().getLogger().log(Level.INFO, "Storage Type: {0}", finalStorageType.toUpperCase());
            Playtime.getPlugin().getLogger().log(Level.INFO, "Your existing data will be preserved.");
            Playtime.getPlugin().getLogger().log(Level.INFO, "");
            Playtime.getPlugin().getLogger().log(Level.INFO, "Old config files backed up:");
            Playtime.getPlugin().getLogger().log(Level.INFO, "  - database.yml -> database.yml.old");
            Playtime.getPlugin().getLogger().log(Level.INFO, "  - config.yml -> config.yml.pre-migration");
            Playtime.getPlugin().getLogger().log(Level.INFO, "===========================================");

        } catch (Exception e) {
            Playtime.getPlugin().getLogger().log(Level.SEVERE, "Failed to migrate configuration!", e);
            Playtime.getPlugin().getLogger().log(Level.WARNING, "Please check your configuration files manually.");
        }
    }

    /**
     * Migrate settings from old config.yml to new config.yml
     */
    private void migrateConfigYml(File oldConfigFile) {
        Playtime.getPlugin().getLogger().log(Level.INFO, "Migrating config.yml settings...");

        YamlConfiguration oldConfig = YamlConfiguration.loadConfiguration(oldConfigFile);
        YamlConfiguration newConfig = fileManager.getConfig("config.yml").get();

        // Migrate language setting - save for reference in comments
        String language = oldConfig.getString("language", "en_GB");
        newConfig.setComments("settings",
            java.util.Collections.singletonList("Old language setting was: " + language + " (now managed via Messages system)"));

        // Migrate update check setting
        if (oldConfig.contains("settings.update_check")) {
            boolean updateCheck = oldConfig.getBoolean("settings.update_check", true);
            newConfig.set("settings.update-check", updateCheck);
            Playtime.getPlugin().getLogger().log(Level.INFO, "  - Migrated update_check: {0}", updateCheck);
        }

        // Migrate cache time setting
        if (oldConfig.contains("settings.cacheTime")) {
            int cacheTime = oldConfig.getInt("settings.cacheTime", 5);
            newConfig.set("settings.cache-time", cacheTime);
            Playtime.getPlugin().getLogger().log(Level.INFO, "  - Migrated cacheTime: {0}", cacheTime);
        }

        // Note: settings.update_checktime is removed in new version
        if (oldConfig.contains("settings.update_checktime")) {
            Playtime.getPlugin().getLogger().log(Level.INFO, "  - Skipped update_checktime (no longer used in 2.0)");
        }

        // Migrate AFK settings
        migrateAFKSettings(oldConfig, newConfig);

        fileManager.getConfig("config.yml").save();
        Playtime.getPlugin().getLogger().log(Level.INFO, "Config.yml migration completed");
    }

    /**
     * Migrate AFK settings from old to new config
     */
    private void migrateAFKSettings(YamlConfiguration oldConfig, YamlConfiguration newConfig) {
        Playtime.getPlugin().getLogger().log(Level.INFO, "  Migrating AFK settings...");

        // Migrate countAfkTime
        if (oldConfig.contains("settings.afk.countAfkTime")) {
            boolean countAfkTime = oldConfig.getBoolean("settings.afk.countAfkTime", true);
            newConfig.set("settings.afk.countAfkTime", countAfkTime);
            Playtime.getPlugin().getLogger().log(Level.INFO, "    - Migrated countAfkTime: {0}", countAfkTime);
        }

        // Migrate useEssentialsApi
        if (oldConfig.contains("settings.afk.useEssentialsApi")) {
            boolean useEssentialsApi = oldConfig.getBoolean("settings.afk.useEssentialsApi", false);
            newConfig.set("settings.afk.useEssentialsApi", useEssentialsApi);
            Playtime.getPlugin().getLogger().log(Level.INFO, "    - Migrated useEssentialsApi: {0}", useEssentialsApi);
        }

        // Migrate event settings
        if (oldConfig.contains("settings.afk.events.chatResetAfkTime")) {
            boolean chatReset = oldConfig.getBoolean("settings.afk.events.chatResetAfkTime", true);
            newConfig.set("settings.afk.events.chatResetAfkTime", chatReset);
            Playtime.getPlugin().getLogger().log(Level.INFO, "    - Migrated chatResetAfkTime: {0}", chatReset);
        }

        if (oldConfig.contains("settings.afk.events.inventoryClickResetAfkTime")) {
            // Note: inventoryClickResetAfkTime is not in new config, but we'll note it
            Playtime.getPlugin().getLogger().log(Level.INFO, "    - Skipped inventoryClickResetAfkTime (not in 2.0)");
        }

        if (oldConfig.contains("settings.afk.events.interactResetAfkTime")) {
            boolean interactReset = oldConfig.getBoolean("settings.afk.events.interactResetAfkTime", true);
            newConfig.set("settings.afk.events.interactResetAfkTime", interactReset);
            Playtime.getPlugin().getLogger().log(Level.INFO, "    - Migrated interactResetAfkTime: {0}", interactReset);
        }

        // Note: New AFK settings in 2.0 will use defaults (thresholdMinutes, broadcastMessages, kick settings, etc.)
        Playtime.getPlugin().getLogger().log(Level.INFO, "    - New AFK settings (thresholdMinutes, broadcastMessages, kick, movement) will use defaults");
    }

    /**
     * Migrate settings from old database.yml to new storage.yml
     */
    private void migrateDatabaseYml(File oldDatabaseFile) {
        Playtime.getPlugin().getLogger().log(Level.INFO, "Migrating database.yml to storage.yml...");

        YamlConfiguration oldDatabase = YamlConfiguration.loadConfiguration(oldDatabaseFile);
        YamlConfiguration newStorage = fileManager.getConfig("storage.yml").get();

        // Migrate database type
        String oldType = oldDatabase.getString("database", "yaml");
        String newType = migrateStorageType(oldType);
        newStorage.set("type", newType);

        if (oldType.equalsIgnoreCase(newType)) {
            Playtime.getPlugin().getLogger().log(Level.INFO, "  - Storage type: {0} (preserved)", oldType);
        } else {
            Playtime.getPlugin().getLogger().log(Level.INFO, "  - Migrated storage type: {0} -> {1}", new Object[]{oldType, newType});
        }

        // Migrate MySQL settings
        if (oldDatabase.contains("mysql")) {
            migrateMySQLSettings(oldDatabase, newStorage);
        }

        // Migrate MongoDB settings
        if (oldDatabase.contains("mongodb")) {
            migrateMongoDBSettings(oldDatabase, newStorage);
        }

        fileManager.getConfig("storage.yml").save();
        Playtime.getPlugin().getLogger().log(Level.INFO, "Storage.yml migration completed");
    }

    /**
     * Convert old storage type names to new format
     * Keeps users on their original database type to prevent data loss
     */
    private String migrateStorageType(String oldType) {
        switch (oldType.toLowerCase()) {
            case "yaml":
            case "yml":
                // Keep users on YAML if they were using it
                Playtime.getPlugin().getLogger().log(Level.INFO, "  NOTE: YAML storage is not recommended for large servers. Consider migrating to SQLite, MySQL, or MongoDB.");
                return "yaml";
            case "mysql":
                return "mysql";
            case "mongodb":
            case "mongo":
                return "mongodb";
            case "redis":
                Playtime.getPlugin().getLogger().log(Level.SEVERE, "Redis storage type is not yet implemented in 2.0!");
                Playtime.getPlugin().getLogger().log(Level.SEVERE, "Please manually configure a different storage type in storage.yml");
                Playtime.getPlugin().getLogger().log(Level.SEVERE, "Defaulting to YAML temporarily - you MUST reconfigure storage before restarting!");
                return "yaml";
            default:
                Playtime.getPlugin().getLogger().log(Level.WARNING, "Unknown storage type: {0}", oldType);
                Playtime.getPlugin().getLogger().log(Level.WARNING, "Defaulting to YAML - please configure storage.yml manually!");
                return "yaml";
        }
    }

    /**
     * Migrate MySQL settings from old to new format
     */
    private void migrateMySQLSettings(YamlConfiguration oldDatabase, YamlConfiguration newStorage) {
        Playtime.getPlugin().getLogger().log(Level.INFO, "  Migrating MySQL settings...");

        // hostname
        if (oldDatabase.contains("mysql.hostname")) {
            String hostname = oldDatabase.getString("mysql.hostname", "localhost");
            newStorage.set("mysql.hostname", hostname);
            Playtime.getPlugin().getLogger().log(Level.INFO, "    - Migrated hostname: {0}", hostname);
        }

        // port
        if (oldDatabase.contains("mysql.port")) {
            int port = oldDatabase.getInt("mysql.port", 3306);
            newStorage.set("mysql.port", port);
            Playtime.getPlugin().getLogger().log(Level.INFO, "    - Migrated port: {0}", port);
        }

        // user -> username
        if (oldDatabase.contains("mysql.user")) {
            String username = oldDatabase.getString("mysql.user", "username");
            newStorage.set("mysql.username", username);
            Playtime.getPlugin().getLogger().log(Level.INFO, "    - Migrated user -> username: {0}", username);
        }

        // password
        if (oldDatabase.contains("mysql.password")) {
            String password = oldDatabase.getString("mysql.password", "password");
            newStorage.set("mysql.password", password);
            Playtime.getPlugin().getLogger().log(Level.INFO, "    - Migrated password: ****");
        }

        // database -> schema
        if (oldDatabase.contains("mysql.database")) {
            String database = oldDatabase.getString("mysql.database", "playtime");
            newStorage.set("mysql.schema", database);
            Playtime.getPlugin().getLogger().log(Level.INFO, "    - Migrated database -> schema: {0}", database);
        }

        // ssl
        if (oldDatabase.contains("mysql.ssl")) {
            String sslString = oldDatabase.getString("mysql.ssl", "false");
            boolean ssl = Boolean.parseBoolean(sslString);
            newStorage.set("mysql.ssl", ssl);
            Playtime.getPlugin().getLogger().log(Level.INFO, "    - Migrated ssl: {0}", ssl);
        }

        // table_prefix -> prefix
        if (oldDatabase.contains("mysql.table_prefix")) {
            String prefix = oldDatabase.getString("mysql.table_prefix", "");
            newStorage.set("mysql.prefix", prefix);
            Playtime.getPlugin().getLogger().log(Level.INFO, "    - Migrated table_prefix -> prefix: {0}",
                prefix.isEmpty() ? "(empty)" : prefix);
        }

        // New settings in 2.0 will use defaults (pool, driver)
        Playtime.getPlugin().getLogger().log(Level.INFO, "    - New MySQL settings (pool, driver) will use defaults");
    }

    /**
     * Migrate MongoDB settings from old to new format
     */
    private void migrateMongoDBSettings(YamlConfiguration oldDatabase, YamlConfiguration newStorage) {
        Playtime.getPlugin().getLogger().log(Level.INFO, "  Migrating MongoDB settings...");

        // hostname
        if (oldDatabase.contains("mongodb.hostname")) {
            String hostname = oldDatabase.getString("mongodb.hostname", "localhost");
            newStorage.set("mongo.hostname", hostname);
            Playtime.getPlugin().getLogger().log(Level.INFO, "    - Migrated hostname: {0}", hostname);
        }

        // port
        if (oldDatabase.contains("mongodb.port")) {
            int port = oldDatabase.getInt("mongodb.port", 27017);
            newStorage.set("mongo.port", port);
            Playtime.getPlugin().getLogger().log(Level.INFO, "    - Migrated port: {0}", port);
        }

        // user -> username
        if (oldDatabase.contains("mongodb.user")) {
            String username = oldDatabase.getString("mongodb.user", "username");
            newStorage.set("mongo.username", username);
            Playtime.getPlugin().getLogger().log(Level.INFO, "    - Migrated user -> username: {0}", username);
        }

        // password
        if (oldDatabase.contains("mongodb.password")) {
            String password = oldDatabase.getString("mongodb.password", "password");
            newStorage.set("mongo.password", password);
            Playtime.getPlugin().getLogger().log(Level.INFO, "    - Migrated password: ****");
        }

        // collection
        if (oldDatabase.contains("mongodb.collection")) {
            String collection = oldDatabase.getString("mongodb.collection", "playtime");
            newStorage.set("mongo.collection", collection);
            Playtime.getPlugin().getLogger().log(Level.INFO, "    - Migrated collection: {0}", collection);
        }
    }

    /**
     * Backup old configuration files by renaming them with .old extension
     */
    private void backupOldFiles() {
        Playtime.getPlugin().getLogger().log(Level.INFO, "Backing up old configuration files...");

        File oldDatabaseYml = new File(dataFolder, "database.yml");
        if (oldDatabaseYml.exists()) {
            File backup = new File(dataFolder, "database.yml.old");
            if (backup.exists()) {
                backup.delete();
            }
            if (oldDatabaseYml.renameTo(backup)) {
                Playtime.getPlugin().getLogger().log(Level.INFO, "  - Backed up database.yml -> database.yml.old");
            }
        }

        // Optionally backup old config.yml as well
        File oldConfigYml = new File(dataFolder, "config.yml.pre-migration");
        File currentConfig = new File(dataFolder, "config.yml");
        if (currentConfig.exists() && !oldConfigYml.exists()) {
            try {
                java.nio.file.Files.copy(currentConfig.toPath(), oldConfigYml.toPath());
                Playtime.getPlugin().getLogger().log(Level.INFO, "  - Backed up config.yml -> config.yml.pre-migration");
            } catch (Exception e) {
                Playtime.getPlugin().getLogger().log(Level.WARNING, "Could not backup config.yml", e);
            }
        }
    }
}
