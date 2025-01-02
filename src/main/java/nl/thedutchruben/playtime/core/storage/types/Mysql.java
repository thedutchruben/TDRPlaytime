package nl.thedutchruben.playtime.core.storage.types;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.Settings;
import nl.thedutchruben.playtime.core.objects.Milestone;
import nl.thedutchruben.playtime.core.objects.PlaytimeUser;
import nl.thedutchruben.playtime.core.objects.RepeatingMilestone;
import nl.thedutchruben.playtime.core.storage.SqlStatements;
import nl.thedutchruben.playtime.core.storage.Storage;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * The mysql storage
 */
public class Mysql extends Storage {
    private HikariDataSource ds;
    private Connection connection;

    private String tablePrefix = "";

    /**
     * Get the name of the storage type
     *
     * @return The name of the storage type
     */
    @Override
    public String getName() {
        return "mysql";
    }

    /**
     * Setup the storage such as the database connection
     */
    @Override
    public boolean setup() {
        this.tablePrefix = Settings.STORAGE_MYSQL_PREFIX.getValueAsString();
        HikariConfig config = getHikariConfig();
        ds = new HikariDataSource(config);

        try {
            this.connection = ds.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        for (String statement : SqlStatements.getStatements(Settings.STORAGE_MYSQL_PREFIX.getValueAsString(), true)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            } catch (SQLException sqlException) {
                Playtime.getPlugin().getLogger().severe("Error while creating table in database: " + sqlException.getMessage());
            }
        }
        return ds.isRunning();
    }

    private static HikariConfig getHikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(Settings.STORAGE_MYSQL_DRIVER.getValueAsString() + Settings.STORAGE_MYSQL_HOST.getValueAsString() + ":" + Settings.STORAGE_MYSQL_PORT.getValueAsInteger() + "/" + Settings.STORAGE_MYSQL_SCHEMA.getValueAsString());
        config.setConnectionTestQuery("SELECT 1");
        config.setUsername(Settings.STORAGE_MYSQL_USERNAME.getValueAsString());
        config.setPassword(Settings.STORAGE_MYSQL_PASSWORD.getValueAsString());
        config.setMaximumPoolSize(Settings.STORAGE_MYSQL_POOL.getValueAsInteger());

        config.setPoolName("PlaytimePool");
        config.addDataSourceProperty("useSSl", (Settings.STORAGE_MYSQL_SSL.getValueAsBoolean()));
        config.setThreadFactory(r -> {
            Thread thread = new Thread(r);
            thread.setName("Playtime-Database-Thread-" + thread.getId());
            return thread;
        });

        return config;
    }

    /**
     * Get the table name with the prefix
     *
     * @param name The name of the table
     * @return The table name with the prefix
     */
    public String getTableName(String name) {
        return "`" + this.tablePrefix + name + "`";
    }

    /**
     * Stops the storage such things as the database connection
     */
    @Override
    public void stop() {
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
                    .prepareStatement("SELECT * FROM " + getTableName("playtime") + " WHERE `uuid` = ?")) {
                preparedStatement.setString(1, uuid.toString());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return new PlaytimeUser(uuid.toString(), resultSet.getString("name"), resultSet.getLong("time"));
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
                    .prepareStatement("SELECT * FROM " + getTableName("playtime") + " WHERE `name` = ?")) {
                preparedStatement.setString(1, name);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return new PlaytimeUser(resultSet.getString("uuid"), resultSet.getString("name"), resultSet.getLong("time"));
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
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement("UPDATE " + getTableName("playtime") + " SET `name` = ?, `time` = ? WHERE `uuid` = ?")) {
                preparedStatement.setString(1, playtimeUser.getName());
                preparedStatement.setFloat(2, playtimeUser.getTime());
                preparedStatement.setString(3, playtimeUser.getUUID().toString());
                preparedStatement.executeUpdate();
                return true;
            } catch (SQLException sqlException) {
                Playtime.getPlugin().getLogger().severe("Error while saving user to database: " + sqlException.getMessage());
            }
            return false;
        });
    }

    /**
     * Create the user
     *
     * @param playtimeUser
     * @return
     */
    @Override
    public CompletableFuture<Boolean> createUser(PlaytimeUser playtimeUser) {
        return CompletableFuture.supplyAsync(() -> {
            try (PreparedStatement preparedStatement = connection
                    .prepareStatement("INSERT INTO " + getTableName("playtime") + " (`uuid`, `name`, `time`) VALUES (?, ?, ?)")) {
                preparedStatement.setString(1, playtimeUser.getUUID().toString());
                preparedStatement.setString(2, playtimeUser.getName());
                preparedStatement.setFloat(3, playtimeUser.getTime());
                preparedStatement.executeUpdate();
                return true;
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
                    .prepareStatement("SELECT * FROM " + getTableName("playtime") + " ORDER BY `time` DESC LIMIT ? OFFSET ?")) {
                preparedStatement.setInt(1, amount);
                preparedStatement.setInt(2, skip);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    List<PlaytimeUser> playtimeUsers = new ArrayList<>();
                    while (resultSet.next()) {
                        playtimeUsers.add(new PlaytimeUser(resultSet.getString("uuid"), resultSet.getString("name"), resultSet.getLong("time")));
                    }
                    return playtimeUsers;
                }
            } catch (SQLException sqlException) {
                Playtime.getPlugin().getLogger().severe("Error while getting top users from database: " + sqlException.getMessage());
            }
            return null;
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
                    .prepareStatement("SELECT * FROM " + getTableName("playtime") + " ORDER BY `time` DESC LIMIT 1 OFFSET ?")) {
                preparedStatement.setInt(1, place);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return new PlaytimeUser(resultSet.getString("uuid"), resultSet.getString("name"), resultSet.getLong("time"));
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
                    .prepareStatement("SELECT * FROM " + getTableName("milestones"))) {
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
            return null;
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
                    .prepareStatement("INSERT INTO " + getTableName("milestones") + "(`name`, `data`) VALUES (?,?)")) {
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
                    .prepareStatement("DELETE FROM " + getTableName("milestones") + " WHERE `name`=?")) {
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
                    .prepareStatement("UPDATE " + getTableName("milestones") + " SET `data`=? WHERE `name`=?")) {
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
                    .prepareStatement("SELECT * FROM " + getTableName("repeating_milestones"))) {
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
            return null;
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
                    .prepareStatement("INSERT INTO " + getTableName("repeating_milestones") + "`(`name`, `data`) VALUES (?,?)")) {
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
                    .prepareStatement("DELETE FROM " + getTableName("repeating_milestones") + " WHERE `name`=?")) {
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
                    .prepareStatement("UPDATE " + getTableName("repeating_milestones") + " SET `data`=? WHERE `name`=?")) {
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
                    "INSERT INTO " + getTableName("playtime_history") + " (`uuid`, `time`, `date`) VALUES (?, ?, ?)")) {
                preparedStatement.setString(1, uuid.toString());
                preparedStatement.setInt(2, time);
                preparedStatement.setDate(4, new Date(new java.util.Date().getTime()));
                preparedStatement.executeUpdate();
                return true;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
