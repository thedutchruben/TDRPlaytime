package nl.thedutchruben.playtime.core.storage.types;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.Settings;
import nl.thedutchruben.playtime.core.objects.Milestone;
import nl.thedutchruben.playtime.core.objects.PlaytimeHistory;
import nl.thedutchruben.playtime.core.objects.PlaytimeUser;
import nl.thedutchruben.playtime.core.objects.RepeatingMilestone;
import nl.thedutchruben.playtime.core.storage.SqlStatements;
import nl.thedutchruben.playtime.core.storage.Storage;
import nl.thedutchruben.playtime.core.storage.migrations.MigrationManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SqlLite extends Storage {
    private HikariDataSource ds;
    private Connection connection;

    /**
     * Get the name of the storage type
     *
     * @return The name of the storage type
     */
    @Override
    public String getName() {
        return "sqllite";
    }

    /**
     * Setup the storage such as the database connection
     */
    @Override
    public boolean setup() {
        HikariConfig config = getHikariConfig();
        ds = new HikariDataSource(config);

        try {
            this.connection = ds.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Create base tables
        for (String statement : SqlStatements.getStatements(Settings.STORAGE_MYSQL_PREFIX.getValueAsString(), false)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            } catch (SQLException sqlException) {
                Playtime.getPlugin().getLogger().severe("Error while creating table in database: " + sqlException.getMessage());
            }
        }

        // Run migrations
        MigrationManager migrationManager = new MigrationManager(connection, false);
        migrationManager.runMigrations();

        return true;
    }

    private static HikariConfig getHikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + Playtime.getPlugin().getDataFolder().getAbsolutePath() + "/playtime.db");
        config.setConnectionTestQuery("SELECT 1");
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSwlLimit", "2048");
        config.setPoolName("PlaytimePool");
        config.setIdleTimeout(10000);
        config.setMaxLifetime(30000);
        config.setValidationTimeout(30000);
        config.setMaximumPoolSize(100);
        config.setMinimumIdle(10);
        config.setAllowPoolSuspension(false);
        config.setThreadFactory(r -> {
            Thread thread = new Thread(r);
            thread.setName("Playtime-Database-Thread-" + thread.getId());
            return thread;
        });
        return config;
    }

    /**
     * Stops the storage such things as the database connection
     */
    @Override
    public void stop() {
        try {
            this.connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        ds.close();
    }

    /**
     * Load the user from the storage
     *
     * @param uuid The uuid of the player
     * @return The playtime user
     */
    @Override
    public CompletableFuture<PlaytimeUser> loadUser(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement("SELECT * FROM `playtime` WHERE `uuid` = ?")) {
                preparedStatement.setString(1, uuid.toString());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        PlaytimeUser user = new PlaytimeUser(uuid.toString(), resultSet.getString("name"), resultSet.getLong("time"));

                        // Load AFK data if available
                        try {
                            // Check if the column exists before trying to access it
                            resultSet.getLong("afk_time");
                            user.addAfkTime(resultSet.getLong("afk_time"));

                            // Load last activity time if available
                            try {
                                resultSet.getLong("last_activity");
                                user.setLastActivity(resultSet.getLong("last_activity"));
                            } catch (SQLException e) {
                                // Column doesn't exist yet, set default value
                                user.setLastActivity(System.currentTimeMillis());
                            }
                        } catch (SQLException e) {
                            // AFk columns don't exist yet, set default values
                            user.addAfkTime(0);
                            user.setLastActivity(System.currentTimeMillis());
                        }

                        return user;
                    }
                }
            } catch (SQLException sqlException) {
                Playtime.getPlugin().getLogger().severe("Error while loading user from database: " + sqlException.getMessage());
            }
            return null;
        });
    }

    /**
     * Load user loaded by name
     *
     * @param name
     * @return
     */
    @Override
    public CompletableFuture<PlaytimeUser> loadUserByName(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement("SELECT * FROM `playtime` WHERE `name` = ?")) {
                preparedStatement.setString(1, name);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        PlaytimeUser user = new PlaytimeUser(resultSet.getString("uuid"), resultSet.getString("name"), resultSet.getLong("time"));

                        // Load AFK data if available
                        try {
                            // Check if the column exists before trying to access it
                            resultSet.getLong("afk_time");
                            user.addAfkTime(resultSet.getLong("afk_time"));

                            // Load last activity time if available
                            try {
                                resultSet.getLong("last_activity");
                                user.setLastActivity(resultSet.getLong("last_activity"));
                            } catch (SQLException e) {
                                // Column doesn't exist yet, set default value
                                user.setLastActivity(System.currentTimeMillis());
                            }
                        } catch (SQLException e) {
                            // AFK columns don't exist yet, set default values
                            user.addAfkTime(0);
                            user.setLastActivity(System.currentTimeMillis());
                        }

                        return user;
                    }
                }
            } catch (SQLException sqlException) {
                Playtime.getPlugin().getLogger().severe("Error while loading user from database: " + sqlException.getMessage());
            }
            return null;
        });
    }

    /**
     * Save the user to the storage
     *
     * @param playtimeUser The playtime user
     * @return If the user is saved
     */
    @Override
    public CompletableFuture<Boolean> saveUser(PlaytimeUser playtimeUser) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // First try with AFK columns
                try (PreparedStatement preparedStatement = connection
                        .prepareStatement("UPDATE `playtime` SET `name` = ?, `time` = ?, `afk_time` = ?, `last_activity` = ? WHERE `uuid` = ?")) {
                    preparedStatement.setString(1, playtimeUser.getName());
                    preparedStatement.setFloat(2, playtimeUser.getTime());
                    preparedStatement.setFloat(3, playtimeUser.getAfkTime());
                    preparedStatement.setLong(4, playtimeUser.getLastActivity());
                    preparedStatement.setString(5, playtimeUser.getUUID().toString());
                    preparedStatement.executeUpdate();
                    return true;
                } catch (SQLException e) {
                    // If that fails, the AFK columns might not exist yet, try without them
                    try (PreparedStatement preparedStatement = connection
                            .prepareStatement("UPDATE `playtime` SET `name` = ?, `time` = ? WHERE `uuid` = ?")) {
                        preparedStatement.setString(1, playtimeUser.getName());
                        preparedStatement.setFloat(2, playtimeUser.getTime());
                        preparedStatement.setString(3, playtimeUser.getUUID().toString());
                        preparedStatement.executeUpdate();
                        return true;
                    }
                }
            } catch (SQLException sqlException) {
                Playtime.getPlugin().getLogger().severe("Error while saving user to database: " + sqlException.getMessage());
            }
            return false;
        });
    }

    /**
     * Create the user
     *
     */
    @Override
    public CompletableFuture<Boolean> createUser(PlaytimeUser playtimeUser) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // First try with AFK columns
                try (PreparedStatement preparedStatement = connection
                        .prepareStatement("INSERT INTO `playtime` (`uuid`, `name`, `time`, `afk_time`, `last_activity`) VALUES (?, ?, ?, ?, ?)")) {
                    preparedStatement.setString(1, playtimeUser.getUUID().toString());
                    preparedStatement.setString(2, playtimeUser.getName());
                    preparedStatement.setFloat(3, playtimeUser.getTime());
                    preparedStatement.setFloat(4, playtimeUser.getAfkTime());
                    preparedStatement.setLong(5, playtimeUser.getLastActivity());
                    preparedStatement.executeUpdate();
                    return true;
                } catch (SQLException e) {
                    // If that fails, the AFK columns might not exist yet, try without them
                    try (PreparedStatement preparedStatement = connection
                            .prepareStatement("INSERT INTO `playtime` (`uuid`, `name`, `time`) VALUES (?, ?, ?)")) {
                        preparedStatement.setString(1, playtimeUser.getUUID().toString());
                        preparedStatement.setString(2, playtimeUser.getName());
                        preparedStatement.setFloat(3, playtimeUser.getTime());
                        preparedStatement.executeUpdate();
                        return true;
                    }
                }
            } catch (SQLException sqlException) {
                Playtime.getPlugin().getLogger().severe("Error while creating user in database: " + sqlException.getMessage());
            }
            return false;
        });
    }

    /**
     * Get the top users
     *
     * @param amount The amount of users
     * @param skip   The amount of users to skip
     * @return The list of users
     */
    @Override
    public CompletableFuture<List<PlaytimeUser>> getTopUsers(int amount, int skip) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement("SELECT * FROM `playtime` ORDER BY `time` DESC LIMIT ? OFFSET ?")) {
                preparedStatement.setInt(1, amount);
                preparedStatement.setInt(2, skip);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    List<PlaytimeUser> playtimeUsers = new ArrayList<>();
                    while (resultSet.next()) {
                        PlaytimeUser user = new PlaytimeUser(resultSet.getString("uuid"), resultSet.getString("name"), resultSet.getLong("time"));

                        // Load AFK data if available
                        try {
                            // Check if the column exists before trying to access it
                            resultSet.getLong("afk_time");
                            user.addAfkTime(resultSet.getLong("afk_time"));

                            // Load last activity time if available
                            try {
                                resultSet.getLong("last_activity");
                                user.setLastActivity(resultSet.getLong("last_activity"));
                            } catch (SQLException e) {
                                // Column doesn't exist yet, set default value
                                user.setLastActivity(System.currentTimeMillis());
                            }
                        } catch (SQLException e) {
                            // AFK columns don't exist yet, set default values
                            user.addAfkTime(0);
                            user.setLastActivity(System.currentTimeMillis());
                        }

                        playtimeUsers.add(user);
                    }
                    return playtimeUsers;
                }
            } catch (SQLException sqlException) {
                Playtime.getPlugin().getLogger().severe("Error while getting top users from database: " + sqlException.getMessage());
            }
            return new ArrayList<>();
        });
    }

    /**
     * Get the top user
     *
     * @param place The place of the user
     * @return The user
     */
    @Override
    public CompletableFuture<PlaytimeUser> getTopUser(int place) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement("SELECT * FROM `playtime` ORDER BY `time` DESC LIMIT 1 OFFSET ?")) {
                preparedStatement.setInt(1, place);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        PlaytimeUser user = new PlaytimeUser(resultSet.getString("uuid"), resultSet.getString("name"), resultSet.getLong("time"));

                        // Load AFK data if available
                        try {
                            // Check if the column exists before trying to access it
                            resultSet.getLong("afk_time");
                            user.addAfkTime(resultSet.getLong("afk_time"));

                            // Load last activity time if available
                            try {
                                resultSet.getLong("last_activity");
                                user.setLastActivity(resultSet.getLong("last_activity"));
                            } catch (SQLException e) {
                                // Column doesn't exist yet, set default value
                                user.setLastActivity(System.currentTimeMillis());
                            }
                        } catch (SQLException e) {
                            // AFK columns don't exist yet, set default values
                            user.addAfkTime(0);
                            user.setLastActivity(System.currentTimeMillis());
                        }

                        return user;
                    }
                }
            } catch (SQLException sqlException) {
                Playtime.getPlugin().getLogger().severe("Error while getting top user from database: " + sqlException.getMessage());
            }
            return null;
        });
    }

    /**
     * Get the milestones
     *
     * @return The list of milestones
     */
    @Override
    public CompletableFuture<List<Milestone>> getMilestones() {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement("SELECT * FROM `milestones`")) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    List<Milestone> milestones = new ArrayList<>();
                    while (resultSet.next()) {
                        milestones.add(getGson().fromJson(resultSet.getString("data"), Milestone.class));
                    }
                    return milestones;
                }
            } catch (SQLException sqlException) {
                Playtime.getPlugin().getLogger().severe("Error while getting milestones from database: " + sqlException.getMessage());
            }
            return new ArrayList<>();
        });
    }

    /**
     * Save the milestone
     *
     * @param milestone The milestone to save
     * @return If the milestone is saved
     */
    @Override
    public CompletableFuture<Boolean> saveMilestone(Milestone milestone) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement("INSERT INTO `milestones`(`name`, `data`) VALUES (?,?)")) {
                preparedStatement.setString(1, milestone.getMilestoneName());
                preparedStatement.setString(2, getGson().toJson(milestone));
                preparedStatement.executeUpdate();
                return true;
            } catch (SQLException sqlException) {
                Playtime.getPlugin().getLogger().severe("Error while saving milestone to database: " + sqlException.getMessage());
            }
            return false;
        });
    }

    /**
     * Delete the milestone
     *
     * @param milestone The milestone to delete
     * @return If the milestone is deleted
     */
    @Override
    public CompletableFuture<Boolean> deleteMilestone(Milestone milestone) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement("DELETE FROM `milestones` WHERE `name`=?")) {
                preparedStatement.setString(1, milestone.getMilestoneName());
                preparedStatement.executeUpdate();
                return true;
            } catch (SQLException sqlException) {
                Playtime.getPlugin().getLogger().severe("Error while deleting milestone from database: " + sqlException.getMessage());
            }
            return false;
        });
    }

    /**
     * Update the milestone
     *
     * @param milestone The milestone to update
     * @return If the milestone is updated
     */
    @Override
    public CompletableFuture<Boolean> updateMilestone(Milestone milestone) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement("UPDATE `milestones` SET `data`=? WHERE `name`=?")) {
                preparedStatement.setString(1, getGson().toJson(milestone));
                preparedStatement.setString(2, milestone.getMilestoneName());
                preparedStatement.executeUpdate();
                return true;
            } catch (SQLException sqlException) {
                Playtime.getPlugin().getLogger().severe("Error while updating milestone to database: " + sqlException.getMessage());
            }
            return false;
        });
    }

    /**
     * Get the repeating milestones
     *
     * @return The list of repeating milestones
     */
    @Override
    public CompletableFuture<List<RepeatingMilestone>> getRepeatingMilestones() {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement("SELECT * FROM `repeating_milestones`")) {
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    List<RepeatingMilestone> repeatingMilestones = new ArrayList<>();
                    while (resultSet.next()) {
                        repeatingMilestones.add(getGson().fromJson(resultSet.getString("data"), RepeatingMilestone.class));
                    }
                    return repeatingMilestones;
                }
            } catch (SQLException sqlException) {
                Playtime.getPlugin().getLogger().severe("Error while getting repeating milestones from database: " + sqlException.getMessage());
            }
            return new ArrayList<>();
        });
    }

    /**
     * Save the repeating milestone
     *
     * @param repeatingMilestone The repeating milestone to save
     * @return If the repeating milestone is saved
     */
    @Override
    public CompletableFuture<Boolean> saveRepeatingMilestone(RepeatingMilestone repeatingMilestone) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement("INSERT INTO `repeating_milestones`(`name`, `data`) VALUES (?,?)")) {
                preparedStatement.setString(1, repeatingMilestone.getMilestoneName());
                preparedStatement.setString(2, getGson().toJson(repeatingMilestone));
                preparedStatement.executeUpdate();
                return true;
            } catch (SQLException sqlException) {
                Playtime.getPlugin().getLogger().severe("Error while saving repeating milestone to database: " + sqlException.getMessage());
            }
            return false;
        });
    }

    /**
     * Delete the repeating milestone
     *
     * @param repeatingMilestone The repeating milestone to delete
     * @return If the repeating milestone is deleted
     */
    @Override
    public CompletableFuture<Boolean> deleteRepeatingMilestone(RepeatingMilestone repeatingMilestone) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement("DELETE FROM `repeating_milestones` WHERE `name`=?")) {
                preparedStatement.setString(1, repeatingMilestone.getMilestoneName());
                preparedStatement.executeUpdate();
                return true;
            } catch (SQLException sqlException) {
                Playtime.getPlugin().getLogger().severe("Error while deleting repeating milestone from database: " + sqlException.getMessage());
            }
            return false;
        });
    }

    /**
     * Update the repeating milestone
     *
     * @param repeatingMilestone The repeating milestone to update
     * @return If the repeating milestone is updated
     */
    @Override
    public CompletableFuture<Boolean> updateRepeatingMilestone(RepeatingMilestone repeatingMilestone) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement("UPDATE `repeating_milestones` SET `data`=? WHERE `name`=?")) {
                preparedStatement.setString(1, getGson().toJson(repeatingMilestone));
                preparedStatement.setString(2, repeatingMilestone.getMilestoneName());
                preparedStatement.executeUpdate();
                return true;
            } catch (SQLException sqlException) {
                Playtime.getPlugin().getLogger().severe("Error while updating repeating milestone to database: " + sqlException.getMessage());
            }
            return false;
        });
    }

    /**
     * @param uuid
     * @param event
     * @param time
     * @return
     */
    @Override
    public CompletableFuture<Boolean> addPlaytimeHistory(UUID uuid, Event event, int time) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO playtime_history (`uuid`, `time`, `event`, `date`) VALUES (?, ?, ?, ?)")) {
                preparedStatement.setString(1, uuid.toString());
                preparedStatement.setInt(2, time);
                preparedStatement.setString(3, event.toString());
                preparedStatement.setDate(4, new Date(new java.util.Date().getTime()));
                preparedStatement.executeUpdate();
                return true;
            } catch (SQLException e) {
                Playtime.getPlugin().getLogger().severe("Error while adding playtime history: " + e.getMessage());
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<List<PlaytimeHistory>> getPlaytimeHistory(UUID uuid, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            List<PlaytimeHistory> playtimeHistories = new ArrayList<>();
            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT * FROM playtime_history WHERE uuid = ? ORDER BY date DESC LIMIT ?")) {
                preparedStatement.setString(1, uuid.toString());
                preparedStatement.setInt(2, limit);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        PlaytimeHistory history = new PlaytimeHistory(
                                resultSet.getInt("id"),
                                UUID.fromString(resultSet.getString("uuid")),
                                Event.valueOf(resultSet.getString("event")),
                                resultSet.getLong("time"),
                                resultSet.getDate("date")
                        );
                        playtimeHistories.add(history);
                    }
                }
            } catch (SQLException e) {
                Playtime.getPlugin().getLogger().severe("Error while getting playtime history: " + e.getMessage());
            }
            return playtimeHistories;
        });
    }

    @Override
    public CompletableFuture<List<PlaytimeHistory>> getPlaytimeHistoryByName(String name, int limit) {
    return CompletableFuture.supplyAsync(() -> {
            List<PlaytimeHistory> playtimeHistories = new ArrayList<>();
            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT ph.* FROM playtime_history ph JOIN playtime p ON ph.uuid = p.uuid WHERE p.name = ? ORDER BY ph.date DESC LIMIT ?")) {
                preparedStatement.setString(1, name);
                preparedStatement.setInt(2, limit);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        PlaytimeHistory history = new PlaytimeHistory(
                                resultSet.getInt("id"),
                                UUID.fromString(resultSet.getString("uuid")),
                                Event.valueOf(resultSet.getString("event")),
                                resultSet.getLong("time"),
                                resultSet.getDate("date")
                        );
                        playtimeHistories.add(history);
                    }
                }
            } catch (SQLException e) {
                Playtime.getPlugin().getLogger().severe("Error while getting playtime history by name: " + e.getMessage());
            }
            return playtimeHistories;
        });
    }
}