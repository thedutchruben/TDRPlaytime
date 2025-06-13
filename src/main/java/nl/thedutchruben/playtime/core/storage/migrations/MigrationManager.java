package nl.thedutchruben.playtime.core.storage.migrations;

import lombok.Getter;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.Settings;

import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

/**
 * Handles database migrations to update the schema when needed
 */
public class MigrationManager {

    private final List<Migration> migrations = new ArrayList<>();
    private final String tablePrefix;
    private Connection connection;
    private boolean isMySql;

    /**
     * Constructor
     * @param connection The database connection
     * @param isMySql Whether the database is MySQL (false means SQLite)
     */
    public MigrationManager(Connection connection, boolean isMySql) {
        this.connection = connection;
        this.isMySql = isMySql;
        this.tablePrefix = Settings.STORAGE_MYSQL_PREFIX.getValueAsString();

        // Register migrations
        registerMigrations();
    }

    /**
     * Register all migrations
     */
    private void registerMigrations() {
        // Add all migrations here
        migrations.add(new Migration_001_AddAFKColumns(tablePrefix, isMySql));
    }

    /**
     * Run all pending migrations
     */
    public void runMigrations() {
        try {
            // Create migrations table if it doesn't exist
            createMigrationsTable();

            // Get the current version
            int currentVersion = getCurrentVersion();
            Playtime.getPlugin().getLogger().log(Level.INFO, "Current database schema version: " + currentVersion);

            // Sort migrations by version
            migrations.sort(Comparator.comparingInt(Migration::getVersion));

            // Run all migrations that are newer than current version
            int migrationsRun = 0;
            for (Migration migration : migrations) {
                if (migration.getVersion() > currentVersion) {
                    Playtime.getPlugin().getLogger().log(Level.INFO, "Running migration " + migration.getVersion() + ": " + migration.getDescription());

                    // Run the migration
                    migration.migrate(connection);

                    // Update the version
                    updateVersion(migration.getVersion());

                    migrationsRun++;
                }
            }

            if (migrationsRun > 0) {
                Playtime.getPlugin().getLogger().log(Level.INFO, "Successfully ran " + migrationsRun + " migrations. New database schema version: " + migrations.get(migrations.size() - 1).getVersion());
            } else {
                Playtime.getPlugin().getLogger().log(Level.INFO, "Database schema is up to date.");
            }

        } catch (SQLException e) {
            Playtime.getPlugin().getLogger().log(Level.SEVERE, "Error running migrations: " + e.getMessage(), e);
        }
    }

    /**
     * Create the migrations table if it doesn't exist
     */
    private void createMigrationsTable() throws SQLException {
        String tableName = tablePrefix + "playtime_migrations";

        String createTable = isMySql
                ? "CREATE TABLE IF NOT EXISTS `" + tableName + "` (" +
                "  `version` INT NOT NULL," +
                "  `executed_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  PRIMARY KEY (`version`)" +
                ")"
                : "CREATE TABLE IF NOT EXISTS `" + tableName + "` (" +
                "  `version` INTEGER PRIMARY KEY," +
                "  `executed_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTable);
        }
    }

    /**
     * Get the current schema version
     * @return The current version, or 0 if no migrations have been run
     */
    private int getCurrentVersion() throws SQLException {
        String tableName = tablePrefix + "playtime_migrations";

        String query = "SELECT MAX(version) as version FROM `" + tableName + "`";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                int version = rs.getInt("version");
                if (rs.wasNull()) {
                    return 0;
                }
                return version;
            }

            return 0;
        }
    }

    /**
     * Update the schema version
     * @param version The new version
     */
    private void updateVersion(int version) throws SQLException {
        String tableName = tablePrefix + "playtime_migrations";

        String insert = "INSERT INTO `" + tableName + "` (version) VALUES (?)";

        try (PreparedStatement stmt = connection.prepareStatement(insert)) {
            stmt.setInt(1, version);
            stmt.executeUpdate();
        }
    }

    /**
     * Base class for migrations
     */
    @Getter
    public static abstract class Migration {
        protected final String tablePrefix;
        protected final boolean isMySql;

        public Migration(String tablePrefix, boolean isMySql) {
            this.tablePrefix = tablePrefix;
            this.isMySql = isMySql;
        }

        /**
         * Get the migration version
         * @return The version number
         */
        public abstract int getVersion();

        /**
         * Get the migration description
         * @return A description of what the migration does
         */
        public abstract String getDescription();

        /**
         * Run the migration
         * @param connection The database connection
         * @throws SQLException If an error occurs
         */
        public abstract void migrate(Connection connection) throws SQLException;
    }

    /**
     * Migration to add AFK columns
     */
    public static class Migration_001_AddAFKColumns extends Migration {

        public Migration_001_AddAFKColumns(String tablePrefix, boolean isMySql) {
            super(tablePrefix, isMySql);
        }

        @Override
        public int getVersion() {
            return 1;
        }

        @Override
        public String getDescription() {
            return "Add AFK tracking columns";
        }

        @Override
        public void migrate(Connection connection) throws SQLException {
            String tableName = tablePrefix + "playtime";

            // Check if the column already exists
            String checkColumn = isMySql
                    ? "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = '" + tableName + "' AND COLUMN_NAME = 'afk_time'"
                    : "PRAGMA table_info(" + tableName + ")";

            boolean columnExists = false;
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(checkColumn)) {

                if (isMySql) {
                    if (rs.next()) {
                        columnExists = rs.getInt(1) > 0;
                    }
                } else {
                    while (rs.next()) {
                        if ("afk_time".equalsIgnoreCase(rs.getString("name"))) {
                            columnExists = true;
                            break;
                        }
                    }
                }
            }

            // Add columns if they don't exist
            if (!columnExists) {
                String[] alterStatements = {
                        "ALTER TABLE `" + tableName + "` ADD COLUMN `afk_time` BIGINT DEFAULT 0",
                        "ALTER TABLE `" + tableName + "` ADD COLUMN `last_activity` BIGINT DEFAULT 0",
                        "ALTER TABLE `" + tableName + "` ADD COLUMN `afk_settings` TEXT DEFAULT NULL"
                };

                for (String alter : alterStatements) {
                    try (Statement stmt = connection.createStatement()) {
                        stmt.execute(alter);
                    }
                }
            }
        }
    }
}