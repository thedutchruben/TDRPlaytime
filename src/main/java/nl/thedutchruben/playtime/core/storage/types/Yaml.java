package nl.thedutchruben.playtime.core.storage.types;

import com.google.gson.Gson;
import nl.thedutchruben.mccore.utils.config.FileManager;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.objects.Milestone;
import nl.thedutchruben.playtime.core.objects.PlaytimeUser;
import nl.thedutchruben.playtime.core.objects.RepeatingMilestone;
import nl.thedutchruben.playtime.core.storage.Storage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Yaml extends Storage {
    private final Gson gson;

    public Yaml() {
        this.gson = new Gson();
    }

    /**
     * Get the name of the storage type
     *
     * @return The name of the storage type
     */
    @Override
    public String getName() {
        return "";
    }

    /**
     * Set up the storage such as the database connection
     */
    @Override
    public boolean setup() {
        return false;
    }

    /**
     * Stops the storage such things as the database connection
     */
    @Override
    public void stop() {

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
            long playtime = Playtime.getInstance().getFileManager().getConfig("players/" + uuid + ".yaml").get().getLong("onlinetime", 0);
            return new PlaytimeUser(uuid.toString(), Objects.requireNonNull(Bukkit.getPlayer(uuid)).getName(), playtime);
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
            Player player = Bukkit.getPlayer(name);
            if (player != null) {
                long playtime = Playtime.getInstance().getFileManager().getConfig("players/" + player.getUniqueId() + ".yaml").get().getLong("onlinetime", 0);
                return new PlaytimeUser(player.getUniqueId().toString(), player.getName(), playtime);
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
            FileManager.Config config = Playtime.getInstance().getFileManager().getConfig("players/" + playtimeUser.getUUID().toString() + ".yaml");
            config.set("onlinetime", playtimeUser.getTime());
            config.save();
            return true;
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
            FileManager.Config config = Playtime.getInstance().getFileManager().getConfig("players/" + playtimeUser.getUUID().toString() + ".yaml");
            config.set("onlinetime", playtimeUser.getTime());
            config.save();
            return true;
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
            List<PlaytimeUser> data = getTopUsersData().join();
            if (data.size() < amount + skip) {
                return data;
            }

            return data.subList(skip, amount + skip);
        });
    }

    public CompletableFuture<List<PlaytimeUser>> getTopUsersData() {
        return CompletableFuture.supplyAsync(() -> {
            List<PlaytimeUser> playtimeUsers = new ArrayList<>();
            File folder = new File(Playtime.getPlugin().getDataFolder(), "players");
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
                    String uuid = file.getName().replace(".yaml", "");
                    long time = yamlConfiguration.getLong("onlinetime", 0);
                    playtimeUsers.add(new PlaytimeUser(uuid, Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName(), time));
                }
            }
            playtimeUsers.sort((o1, o2) -> {
                if (o1.getTime() > o2.getTime()) {
                    return -1;
                } else if (o1.getTime() < o2.getTime()) {
                    return 1;
                }
                return 0;
            });
            return playtimeUsers;
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
            List<PlaytimeUser> data = getTopUsersData().join();
            if (data.size() < place) {
                return null;
            }
            return data.get(place - 1);
        });
    }

    /**
     * Get the milestones
     *
     * @return The list of milestones
     */
    @Override
    public CompletableFuture<List<Milestone>> getMilestones() {
        return null;
    }

    /**
     * Save the milestone
     *
     * @param milestone The milestone to save
     * @return If the milestone is saved
     */
    @Override
    public CompletableFuture<Boolean> saveMilestone(Milestone milestone) {
        return null;
    }

    /**
     * Delete the milestone
     *
     * @param milestone The milestone to delete
     * @return If the milestone is deleted
     */
    @Override
    public CompletableFuture<Boolean> deleteMilestone(Milestone milestone) {
        return null;
    }

    /**
     * Update the milestone
     *
     * @param milestone The milestone to update
     * @return If the milestone is updated
     */
    @Override
    public CompletableFuture<Boolean> updateMilestone(Milestone milestone) {
        return null;
    }

    /**
     * Get the repeating milestones
     *
     * @return The list of repeating milestones
     */
    @Override
    public CompletableFuture<List<RepeatingMilestone>> getRepeatingMilestones() {
        return null;
    }

    /**
     * Save the repeating milestone
     *
     * @param repeatingMilestone The repeating milestone to save
     * @return If the repeating milestone is saved
     */
    @Override
    public CompletableFuture<Boolean> saveRepeatingMilestone(RepeatingMilestone repeatingMilestone) {
        return null;
    }

    /**
     * Delete the repeating milestone
     *
     * @param repeatingMilestone The repeating milestone to delete
     * @return If the repeating milestone is deleted
     */
    @Override
    public CompletableFuture<Boolean> deleteRepeatingMilestone(RepeatingMilestone repeatingMilestone) {
        return null;
    }

    /**
     * Update the repeating milestone
     *
     * @param repeatingMilestone The repeating milestone to update
     * @return If the repeating milestone is updated
     */
    @Override
    public CompletableFuture<Boolean> updateRepeatingMilestone(RepeatingMilestone repeatingMilestone) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> updatePlaytimeHistory(UUID uuid, Event event, int time) {
        return null;
    }
}
