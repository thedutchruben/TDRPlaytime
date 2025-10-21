package nl.thedutchruben.playtime.core.storage.types;

import com.google.gson.Gson;
import nl.thedutchruben.mccore.utils.GsonUtil;
import nl.thedutchruben.mccore.utils.config.FileManager;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.objects.Milestone;
import nl.thedutchruben.playtime.core.objects.PlaytimeHistory;
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
        this.gson = GsonUtil.createGson();
    }

    /**
     * Get the name of the storage type
     *
     * @return The name of the storage type
     */
    @Override
    public String getName() {
        return "Yaml";
    }

    /**
     * Set up the storage such as the database connection
     */
    @Override
    public boolean setup() {

        return true;
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
        return CompletableFuture.supplyAsync(() -> {
            List<Milestone> milestones = new ArrayList<>();
            File[] files = new File(Playtime.getPlugin().getDataFolder(), "milestones/").listFiles();

            if (files == null) {
                return milestones;
            }

            for (final File fileEntry : files) {
                YamlConfiguration config = Playtime.getInstance().getFileManager()
                        .getConfig("milestones/" + fileEntry.getName()).get();
                if (config != null) {
                    milestones.add(this.gson.fromJson(config.getString("data"), Milestone.class));
                }
            }
            return milestones;
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
            Playtime.getInstance().getFileManager().getConfig("milestones/" + milestone.getMilestoneName() + ".yaml")
                    .get().set("data", this.gson.toJson(milestone, Milestone.class));
            Playtime.getInstance().getFileManager().getConfig("milestones/" + milestone.getMilestoneName() + ".yaml")
                    .save();
            return true;
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
            Playtime.getInstance().getFileManager()
                    .getConfig("milestones/" + milestone.getMilestoneName() + ".yaml").file.delete();
            return true;
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
        return saveMilestone(milestone);
    }

    /**
     * Get the repeating milestones
     *
     * @return The list of repeating milestones
     */
    @Override
    public CompletableFuture<List<RepeatingMilestone>> getRepeatingMilestones() {
        return CompletableFuture.supplyAsync(() -> {
            List<RepeatingMilestone> milestones = new ArrayList<>();
            File[] files = new File(Playtime.getPlugin().getDataFolder(), "repeatingmilestones/").listFiles();

            if (files == null) {
                return milestones;
            }

            for (final File fileEntry : files) {
                YamlConfiguration config = Playtime.getInstance().getFileManager()
                        .getConfig("repeatingmilestones/" + fileEntry.getName()).get();
                if (config != null) {
                    milestones.add(this.gson.fromJson(config.getString("data"), RepeatingMilestone.class));
                }
            }
            return milestones;
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
            Playtime.getInstance().getFileManager()
                    .getConfig("repeatingmilestones/" + repeatingMilestone.getMilestoneName() + ".yaml").get()
                    .set("data", this.gson.toJson(repeatingMilestone, RepeatingMilestone.class));
            Playtime.getInstance().getFileManager()
                    .getConfig("repeatingmilestones/" + repeatingMilestone.getMilestoneName() + ".yaml").save();
            return true;
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
            Playtime.getInstance().getFileManager()
                    .getConfig("repeatingmilestones/" + repeatingMilestone.getMilestoneName() + ".yaml").file.delete();
            return true;
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
        return saveRepeatingMilestone(repeatingMilestone);
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
            FileManager.Config config = Playtime.getInstance().getFileManager().getConfig("players/history/" + uuid + ".yaml");
            List<String> history = config.get().getStringList("history");

            history.add("UUID:" + uuid + "|EVENT:" + event + "|TIME:" + time + "|DATE" + new Date());

            config.set("history", history);
            config.save();
            return true;
        });
    }

    @Override
    public CompletableFuture<List<PlaytimeHistory>> getPlaytimeHistory(UUID uuid, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            FileManager.Config config = Playtime.getInstance().getFileManager().getConfig("players/history/" + uuid + ".yaml");
            List<String> history = config.get().getStringList("history");
            List<PlaytimeHistory> playtimeHistories = new ArrayList<>();

            for (String entry : history) {
                String[] parts = entry.split("\\|");
                UUID entryUuid = UUID.fromString(parts[0].split(":")[1]);
                Event event = Event.valueOf(parts[1].split(":")[1]);
                int time = Integer.parseInt(parts[2].split(":")[1]);
                Date date = new Date(parts[3].split(":")[1]);

                playtimeHistories.add(new PlaytimeHistory(0, entryUuid, event, time, date));
            }

            if (limit > 0 && playtimeHistories.size() > limit) {
                return playtimeHistories.subList(0, limit);
            }
            return playtimeHistories;
        });
    }

    @Override
    public CompletableFuture<List<PlaytimeHistory>> getPlaytimeHistoryByName(String name, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            Player player = Bukkit.getPlayer(name);
            if (player == null) {
                return Collections.emptyList();
            }
            UUID uuid = player.getUniqueId();
            return getPlaytimeHistory(uuid, limit).join();
        });
    }

    /**
     * Get all reward cooldowns for a player
     * TODO: Implement database storage for cooldowns
     *
     * @param uuid The UUID of the player
     * @return Map of milestone name to cooldown
     */
    @Override
    public CompletableFuture<java.util.Map<String, nl.thedutchruben.playtime.core.objects.RewardCooldown>> getRewardCooldowns(UUID uuid) {
        return CompletableFuture.completedFuture(new java.util.HashMap<>());
    }

    /**
     * Save a reward cooldown
     * TODO: Implement database storage for cooldowns
     *
     * @param cooldown The cooldown to save
     * @return CompletableFuture that completes when saved
     */
    @Override
    public CompletableFuture<Boolean> saveRewardCooldown(nl.thedutchruben.playtime.core.objects.RewardCooldown cooldown) {
        return CompletableFuture.completedFuture(true);
    }

    /**
     * Delete a reward cooldown
     * TODO: Implement database storage for cooldowns
     *
     * @param uuid The UUID of the player
     * @param milestoneName The milestone name
     * @return CompletableFuture that completes when deleted
     */
    @Override
    public CompletableFuture<Boolean> deleteRewardCooldown(UUID uuid, String milestoneName) {
        return CompletableFuture.completedFuture(true);
    }
}