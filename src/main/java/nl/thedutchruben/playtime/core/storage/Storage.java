package nl.thedutchruben.playtime.core.storage;

import com.google.gson.Gson;
import nl.thedutchruben.mccore.utils.GsonUtil;
import nl.thedutchruben.playtime.core.objects.Milestone;
import nl.thedutchruben.playtime.core.objects.PlaytimeHistory;
import nl.thedutchruben.playtime.core.objects.PlaytimeUser;
import nl.thedutchruben.playtime.core.objects.RepeatingMilestone;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class Storage {
    private final Gson gson;

    public Storage() {
        this.gson =  GsonUtil.createGson();
    }

    /**
     * Get the name of the storage type
     *
     * @return The name of the storage type
     */
    public abstract String getName();

    /**
     * Set up the storage such as the database connection
     */
    public abstract boolean setup();

    /**
     * Stops the storage such things as the database connection
     */
    public abstract void stop();

    /**
     * Load the user from the storage
     *
     * @param uuid The uuid of the player
     * @return The playtime user
     */

    public abstract CompletableFuture<PlaytimeUser> loadUser(UUID uuid);

    /**
     * Load user loaded by name
     *
     * @param name
     * @return
     */
    public abstract CompletableFuture<PlaytimeUser> loadUserByName(String name);

    /**
     * Save the user to the storage
     *
     * @param playtimeUser The playtime user
     * @return If the user is saved
     */
    public abstract CompletableFuture<Boolean> saveUser(PlaytimeUser playtimeUser);

    /**
     * Create the user
     *
     * @param playtimeUser
     * @return
     */
    public abstract CompletableFuture<Boolean> createUser(PlaytimeUser playtimeUser);

    /**
     * Get the top users
     *
     * @param amount The amount of users
     * @param skip   The amount of users to skip
     * @return The list of users
     */
    public abstract CompletableFuture<List<PlaytimeUser>> getTopUsers(int amount, int skip);

    /**
     * Get the top user
     *
     * @param place The place of the user
     * @return The user
     */
    public abstract CompletableFuture<PlaytimeUser> getTopUser(int place);

    /**
     * Get the milestones
     *
     * @return The list of milestones
     */
    public abstract CompletableFuture<List<Milestone>> getMilestones();

    /**
     * Save the milestone
     *
     * @param milestone The milestone to save
     * @return If the milestone is saved
     */
    public abstract CompletableFuture<Boolean> saveMilestone(Milestone milestone);

    /**
     * Delete the milestone
     *
     * @param milestone The milestone to delete
     * @return If the milestone is deleted
     */
    public abstract CompletableFuture<Boolean> deleteMilestone(Milestone milestone);

    /**
     * Update the milestone
     *
     * @param milestone The milestone to update
     * @return If the milestone is updated
     */
    public abstract CompletableFuture<Boolean> updateMilestone(Milestone milestone);

    /**
     * Get the repeating milestones
     *
     * @return The list of repeating milestones
     */
    public abstract CompletableFuture<List<RepeatingMilestone>> getRepeatingMilestones();

    /**
     * Save the repeating milestone
     *
     * @param repeatingMilestone The repeating milestone to save
     * @return If the repeating milestone is saved
     */
    public abstract CompletableFuture<Boolean> saveRepeatingMilestone(RepeatingMilestone repeatingMilestone);

    /**
     * Delete the repeating milestone
     *
     * @param repeatingMilestone The repeating milestone to delete
     * @return If the repeating milestone is deleted
     */
    public abstract CompletableFuture<Boolean> deleteRepeatingMilestone(RepeatingMilestone repeatingMilestone);

    /**
     * Update the repeating milestone
     *
     * @param repeatingMilestone The repeating milestone to update
     * @return If the repeating milestone is updated
     */
    public abstract CompletableFuture<Boolean> updateRepeatingMilestone(RepeatingMilestone repeatingMilestone);

    /**
     *
     * @param uuid
     * @param event
     * @param time
     * @return
     */
    public abstract CompletableFuture<Boolean> addPlaytimeHistory(UUID uuid, Event event, int time);

    /**
     * Get playtime history for a user
     *
     * @param uuid The uuid of the player
     * @param limit Maximum number of entries to return
     * @return List of playtime history entries
     */
    public abstract CompletableFuture<List<PlaytimeHistory>> getPlaytimeHistory(UUID uuid, int limit);

    /**
     * Get playtime history for a user by name
     *
     * @param name The name of the player
     * @param limit Maximum number of entries to return
     * @return List of playtime history entries
     */
    public abstract CompletableFuture<List<PlaytimeHistory>> getPlaytimeHistoryByName(String name, int limit);

    public Gson getGson() {
        return gson;
    }

    public enum Event {
        JOIN,
        QUIT
    }
}
