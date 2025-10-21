package nl.thedutchruben.playtime.extentions;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.Settings;
import nl.thedutchruben.playtime.core.objects.Milestone;
import nl.thedutchruben.playtime.core.objects.PlaytimeUser;
import nl.thedutchruben.playtime.core.translations.Messages;
import nl.thedutchruben.playtime.utils.Replacement;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;

public class PlaceholderAPIExtension extends PlaceholderExpansion {

    // Cache for top players to avoid blocking calls
    private List<PlaytimeUser> topPlayersCache = new ArrayList<>();
    private long topPlayersCacheTime = 0;

    // Cache for player ranks
    private final Map<UUID, Integer> rankCache = new ConcurrentHashMap<>();
    private long rankCacheTime = 0;

    @Override
    public String getIdentifier() {
        return "tdrplaytime";
    }

    @Override
    public String getAuthor() {
        return Playtime.getPlugin().getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return Playtime.getPlugin().getDescription().getVersion();
    }

    @Override
    public List<String> getPlaceholders() {
        List<String> placeholders = new ArrayList<>();

        // Player time placeholders
        placeholders.add("%tdrplaytime_time%");
        placeholders.add("%tdrplaytime_time_days_number%");
        placeholders.add("%tdrplaytime_time_hour_number%");
        placeholders.add("%tdrplaytime_time_minutes_number%");
        placeholders.add("%tdrplaytime_time_seconds_number%");

        // Total time placeholders (converted to specific unit)
        placeholders.add("%tdrplaytime_total_seconds%");
        placeholders.add("%tdrplaytime_total_minutes%");
        placeholders.add("%tdrplaytime_total_hours%");
        placeholders.add("%tdrplaytime_total_days%");

        // Top players placeholders
        for (int i = 1; i <= 10; i++) {
            placeholders.add("%tdrplaytime_top_names_" + i + "%");
            placeholders.add("%tdrplaytime_top_time_" + i + "_days%");
            placeholders.add("%tdrplaytime_top_time_" + i + "_hours%");
            placeholders.add("%tdrplaytime_top_time_" + i + "_minutes%");
            placeholders.add("%tdrplaytime_top_time_" + i + "_seconds%");
        }

        // Rank placeholder
        placeholders.add("%tdrplaytime_rank%");

        // AFK placeholders
        placeholders.add("%tdrplaytime_afk_status%");
        placeholders.add("%tdrplaytime_afk_time%");
        placeholders.add("%tdrplaytime_afk_time_days_number%");
        placeholders.add("%tdrplaytime_afk_time_hours_number%");
        placeholders.add("%tdrplaytime_afk_time_minutes_number%");
        placeholders.add("%tdrplaytime_afk_time_seconds_number%");
        placeholders.add("%tdrplaytime_afk_total_seconds%");
        placeholders.add("%tdrplaytime_afk_total_minutes%");
        placeholders.add("%tdrplaytime_afk_total_hours%");
        placeholders.add("%tdrplaytime_afk_total_days%");

        // Active time placeholders
        placeholders.add("%tdrplaytime_active_time%");
        placeholders.add("%tdrplaytime_active_time_days_number%");
        placeholders.add("%tdrplaytime_active_time_hours_number%");
        placeholders.add("%tdrplaytime_active_time_minutes_number%");
        placeholders.add("%tdrplaytime_active_time_seconds_number%");
        placeholders.add("%tdrplaytime_active_total_seconds%");
        placeholders.add("%tdrplaytime_active_total_minutes%");
        placeholders.add("%tdrplaytime_active_total_hours%");
        placeholders.add("%tdrplaytime_active_total_days%");

        // Milestone placeholders
        placeholders.add("%tdrplaytime_next_milestone%");
        placeholders.add("%tdrplaytime_next_milestone_time%");
        placeholders.add("%tdrplaytime_next_milestone_progress%");

        // Offline player support examples
        placeholders.add("%tdrplaytime_<player>_time%");
        placeholders.add("%tdrplaytime_<player>_rank%");

        return placeholders;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        String dummyProofParams = params.toLowerCase().replace('-', '_');

        // Handle offline player placeholders (e.g., %tdrplaytime_PlayerName_time%)
        if (dummyProofParams.contains("_") && !dummyProofParams.startsWith("time_")
                && !dummyProofParams.startsWith("top_") && !dummyProofParams.startsWith("afk_")
                && !dummyProofParams.startsWith("active_") && !dummyProofParams.startsWith("next_")
                && !dummyProofParams.startsWith("rank") && !dummyProofParams.startsWith("total_")) {
            String[] parts = dummyProofParams.split("_", 2);
            if (parts.length == 2) {
                String playerName = parts[0];
                String placeholder = parts[1];
                return handleOfflinePlayerPlaceholder(playerName, placeholder);
            }
        }

        PlaytimeUser user = Playtime.getInstance().getPlaytimeUsers().get(player.getUniqueId());
        if (user == null) {
            return "No data";
        }

        // Basic time placeholders
        if (dummyProofParams.equals("time")) {
            return Messages.PLAYTIME_INFO_OWN.getMessage(
                    new Replacement("%D%", String.valueOf(user.translateTime()[0])),
                    new Replacement("%H%", String.valueOf(user.translateTime()[1])),
                    new Replacement("%M%", String.valueOf(user.translateTime()[2])),
                    new Replacement("%S%", String.valueOf(user.translateTime()[3]))
            );
        }

        // Time component placeholders
        if (dummyProofParams.contains("time_days_number")) {
            return String.valueOf(user.translateTime()[0]);
        } else if (dummyProofParams.contains("time_hour_number")) {
            return String.valueOf(user.translateTime()[1]);
        } else if (dummyProofParams.contains("time_minutes_number")) {
            return String.valueOf(user.translateTime()[2]);
        } else if (dummyProofParams.contains("time_seconds_number")) {
            return String.valueOf(user.translateTime()[3]);
        }

        // Total time placeholders (full conversion)
        if (dummyProofParams.equals("total_seconds")) {
            return String.valueOf((long)(user.getTime() / 1000));
        } else if (dummyProofParams.equals("total_minutes")) {
            return String.valueOf((long)(user.getTime() / 1000 / 60));
        } else if (dummyProofParams.equals("total_hours")) {
            return String.valueOf((long)(user.getTime() / 1000 / 60 / 60));
        } else if (dummyProofParams.equals("total_days")) {
            return String.valueOf((long)(user.getTime() / 1000 / 60 / 60 / 24));
        }

        // Player rank
        if (dummyProofParams.equals("rank")) {
            return String.valueOf(getPlayerRank(player.getUniqueId()));
        }

        // Top players placeholders
        if (dummyProofParams.contains("top_names_")) {
            int placeNumber = parsePlaceNumber(dummyProofParams);
            return getTopUserName(placeNumber);
        }

        if (dummyProofParams.contains("top_time_")) {
            int placeNumber = parsePlaceNumber(dummyProofParams);
            return getTopUserTime(dummyProofParams, placeNumber);
        }

        // AFK status
        if (dummyProofParams.equals("afk_status")) {
            return user.isAfk() ? "AFK" : "Online";
        }

        // AFK time formatted
        if (dummyProofParams.equals("afk_time")) {
            int[] afkTime = user.translateAfkTime();
            return Messages.PLAYER_AFK_TOTAL.getMessage(
                    new Replacement("%player%", player.getName()),
                    new Replacement("%D%", String.valueOf(afkTime[0])),
                    new Replacement("%H%", String.valueOf(afkTime[1])),
                    new Replacement("%M%", String.valueOf(afkTime[2])),
                    new Replacement("%S%", String.valueOf(afkTime[3]))
            );
        }

        // AFK time components
        if (dummyProofParams.contains("afk_time_days_number")) {
            return String.valueOf(user.translateAfkTime()[0]);
        } else if (dummyProofParams.contains("afk_time_hours_number")) {
            return String.valueOf(user.translateAfkTime()[1]);
        } else if (dummyProofParams.contains("afk_time_minutes_number")) {
            return String.valueOf(user.translateAfkTime()[2]);
        } else if (dummyProofParams.contains("afk_time_seconds_number")) {
            return String.valueOf(user.translateAfkTime()[3]);
        }

        // AFK total time placeholders
        if (dummyProofParams.equals("afk_total_seconds")) {
            return String.valueOf((long)(user.getAfkTime() / 1000));
        } else if (dummyProofParams.equals("afk_total_minutes")) {
            return String.valueOf((long)(user.getAfkTime() / 1000 / 60));
        } else if (dummyProofParams.equals("afk_total_hours")) {
            return String.valueOf((long)(user.getAfkTime() / 1000 / 60 / 60));
        } else if (dummyProofParams.equals("afk_total_days")) {
            return String.valueOf((long)(user.getAfkTime() / 1000 / 60 / 60 / 24));
        }

        // Active time formatted
        if (dummyProofParams.equals("active_time")) {
            int[] activeTime = user.translateActiveTime();
            return Messages.PLAYER_ACTIVE_TIME.getMessage(
                    new Replacement("%player%", player.getName()),
                    new Replacement("%D%", String.valueOf(activeTime[0])),
                    new Replacement("%H%", String.valueOf(activeTime[1])),
                    new Replacement("%M%", String.valueOf(activeTime[2])),
                    new Replacement("%S%", String.valueOf(activeTime[3]))
            );
        }

        // Active time components
        if (dummyProofParams.contains("active_time_days_number")) {
            return String.valueOf(user.translateActiveTime()[0]);
        } else if (dummyProofParams.contains("active_time_hours_number")) {
            return String.valueOf(user.translateActiveTime()[1]);
        } else if (dummyProofParams.contains("active_time_minutes_number")) {
            return String.valueOf(user.translateActiveTime()[2]);
        } else if (dummyProofParams.contains("active_time_seconds_number")) {
            return String.valueOf(user.translateActiveTime()[3]);
        }

        // Active total time placeholders
        if (dummyProofParams.equals("active_total_seconds")) {
            return String.valueOf((long)(user.getActivePlaytime() / 1000));
        } else if (dummyProofParams.equals("active_total_minutes")) {
            return String.valueOf((long)(user.getActivePlaytime() / 1000 / 60));
        } else if (dummyProofParams.equals("active_total_hours")) {
            return String.valueOf((long)(user.getActivePlaytime() / 1000 / 60 / 60));
        } else if (dummyProofParams.equals("active_total_days")) {
            return String.valueOf((long)(user.getActivePlaytime() / 1000 / 60 / 60 / 24));
        }

        // Next milestone placeholders
        if (dummyProofParams.equals("next_milestone")) {
            return getNextMilestoneName(user);
        } else if (dummyProofParams.equals("next_milestone_time")) {
            return getNextMilestoneTime(user);
        } else if (dummyProofParams.equals("next_milestone_progress")) {
            return getNextMilestoneProgress(user);
        }

        return super.onPlaceholderRequest(player, params);
    }

    /**
     * Parse the place number from a placeholder parameter
     */
    private int parsePlaceNumber(String params) {
        try {
            String[] parts = params.split("_");
            return Math.max(Integer.parseInt(parts[parts.length - 1]), 1);
        } catch (NumberFormatException e) {
            Bukkit.getLogger().log(Level.WARNING, "Wrong number format in placeholder: " + params);
            return 1;
        }
    }

    /**
     * Update the top players cache if it's expired
     */
    private void updateTopPlayersCache() {
        long cacheTime = Settings.TOP_10_PLACEHOLDER_CACHE_TIME.getValueAsInteger() * 1000L;
        if (System.currentTimeMillis() - topPlayersCacheTime > cacheTime) {
            try {
                CompletableFuture<List<PlaytimeUser>> future = Playtime.getInstance().getStorage().getTopUsers(10, 0);
                future.thenAccept(users -> {
                    topPlayersCache = users;
                    topPlayersCacheTime = System.currentTimeMillis();
                }).exceptionally(ex -> {
                    Bukkit.getLogger().log(Level.WARNING, "Failed to update top players cache", ex);
                    return null;
                });
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "Failed to initiate top players cache update", e);
            }
        }
    }

    /**
     * Get top user name with caching
     */
    private String getTopUserName(int placeNumber) {
        updateTopPlayersCache();

        if (placeNumber <= 0 || placeNumber > topPlayersCache.size()) {
            return "No player";
        }

        try {
            PlaytimeUser user = topPlayersCache.get(placeNumber - 1);
            return user != null ? user.getName() : "No player";
        } catch (IndexOutOfBoundsException e) {
            return "No player";
        }
    }

    /**
     * Get top user time with caching
     */
    private String getTopUserTime(String params, int placeNumber) {
        updateTopPlayersCache();

        if (placeNumber <= 0 || placeNumber > topPlayersCache.size()) {
            return "0";
        }

        try {
            PlaytimeUser playtimeUser = topPlayersCache.get(placeNumber - 1);
            if (playtimeUser == null) {
                return "0";
            }

            // Check if player is online and get fresh data
            if (Bukkit.getPlayer(playtimeUser.getUUID()) != null) {
                PlaytimeUser onlineUser = Playtime.getInstance().getPlaytimeUsers().get(playtimeUser.getUUID());
                if (onlineUser != null) {
                    playtimeUser = onlineUser;
                }
            }

            if (params.endsWith("_days")) {
                return String.valueOf(playtimeUser.translateTime()[0]);
            } else if (params.endsWith("_hours")) {
                return String.valueOf(playtimeUser.translateTime()[1]);
            } else if (params.endsWith("_minutes")) {
                return String.valueOf(playtimeUser.translateTime()[2]);
            } else if (params.endsWith("_seconds")) {
                return String.valueOf(playtimeUser.translateTime()[3]);
            } else {
                return String.valueOf((long) playtimeUser.getTime());
            }
        } catch (IndexOutOfBoundsException e) {
            return "0";
        }
    }

    /**
     * Get player rank (position in leaderboard)
     */
    private int getPlayerRank(UUID uuid) {
        // Update rank cache if expired
        long cacheTime = Settings.TOP_10_PLACEHOLDER_CACHE_TIME.getValueAsInteger() * 1000L;
        if (System.currentTimeMillis() - rankCacheTime > cacheTime) {
            rankCache.clear();
            rankCacheTime = System.currentTimeMillis();
        }

        // Check cache first
        if (rankCache.containsKey(uuid)) {
            return rankCache.get(uuid);
        }

        // Calculate rank
        try {
            PlaytimeUser targetUser = Playtime.getInstance().getPlaytimeUsers().get(uuid);
            if (targetUser == null) {
                return 0;
            }

            // Get top users and find position
            CompletableFuture<List<PlaytimeUser>> future = Playtime.getInstance().getStorage().getTopUsers(100, 0);
            future.thenAccept(users -> {
                for (int i = 0; i < users.size(); i++) {
                    if (users.get(i).getUUID().equals(uuid)) {
                        rankCache.put(uuid, i + 1);
                        return;
                    }
                }
                rankCache.put(uuid, users.size() + 1);
            }).exceptionally(ex -> {
                Bukkit.getLogger().log(Level.WARNING, "Failed to calculate player rank", ex);
                return null;
            });

            // Return cached value or 0 if not calculated yet
            return rankCache.getOrDefault(uuid, 0);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Error calculating player rank", e);
            return 0;
        }
    }

    /**
     * Handle offline player placeholders
     */
    private String handleOfflinePlayerPlaceholder(String playerName, String placeholder) {
        try {
            CompletableFuture<PlaytimeUser> future = Playtime.getInstance().getStorage().loadUserByName(playerName);
            PlaytimeUser user = future.getNow(null);

            if (user == null) {
                return "No data";
            }

            // Handle different placeholder types
            if (placeholder.equals("time")) {
                return Messages.PLAYTIME_INFO_OWN.getMessage(
                        new Replacement("%D%", String.valueOf(user.translateTime()[0])),
                        new Replacement("%H%", String.valueOf(user.translateTime()[1])),
                        new Replacement("%M%", String.valueOf(user.translateTime()[2])),
                        new Replacement("%S%", String.valueOf(user.translateTime()[3]))
                );
            } else if (placeholder.equals("rank")) {
                return String.valueOf(getPlayerRank(user.getUUID()));
            } else if (placeholder.equals("total_hours")) {
                return String.valueOf((long)(user.getTime() / 1000 / 60 / 60));
            }

            return "Unknown placeholder";
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Error handling offline player placeholder", e);
            return "Error";
        }
    }

    /**
     * Get next milestone name
     */
    private String getNextMilestoneName(PlaytimeUser user) {
        try {
            CompletableFuture<List<Milestone>> future = Playtime.getInstance().getStorage().getMilestones();
            List<Milestone> milestones = future.getNow(new ArrayList<>());

            if (milestones.isEmpty()) {
                return "None";
            }

            // Find next milestone
            Milestone nextMilestone = null;
            for (Milestone milestone : milestones) {
                if (milestone.getTime() > user.getTime()) {
                    if (nextMilestone == null || milestone.getTime() < nextMilestone.getTime()) {
                        nextMilestone = milestone;
                    }
                }
            }

            return nextMilestone != null ? nextMilestone.getName() : "None";
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Error getting next milestone name", e);
            return "Error";
        }
    }

    /**
     * Get next milestone time remaining
     */
    private String getNextMilestoneTime(PlaytimeUser user) {
        try {
            CompletableFuture<List<Milestone>> future = Playtime.getInstance().getStorage().getMilestones();
            List<Milestone> milestones = future.getNow(new ArrayList<>());

            if (milestones.isEmpty()) {
                return "0h 0m 0s";
            }

            // Find next milestone
            Milestone nextMilestone = null;
            for (Milestone milestone : milestones) {
                if (milestone.getTime() > user.getTime()) {
                    if (nextMilestone == null || milestone.getTime() < nextMilestone.getTime()) {
                        nextMilestone = milestone;
                    }
                }
            }

            if (nextMilestone == null) {
                return "0h 0m 0s";
            }

            long remainingTime = (long) (nextMilestone.getTime() - user.getTime()) / 1000;
            long hours = remainingTime / 3600;
            long minutes = (remainingTime % 3600) / 60;
            long seconds = remainingTime % 60;

            return hours + "h " + minutes + "m " + seconds + "s";
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Error getting next milestone time", e);
            return "Error";
        }
    }

    /**
     * Get next milestone progress percentage
     */
    private String getNextMilestoneProgress(PlaytimeUser user) {
        try {
            CompletableFuture<List<Milestone>> future = Playtime.getInstance().getStorage().getMilestones();
            List<Milestone> milestones = future.getNow(new ArrayList<>());

            if (milestones.isEmpty()) {
                return "100%";
            }

            // Find next milestone
            Milestone nextMilestone = null;
            Milestone previousMilestone = null;
            for (Milestone milestone : milestones) {
                if (milestone.getTime() <= user.getTime()) {
                    if (previousMilestone == null || milestone.getTime() > previousMilestone.getTime()) {
                        previousMilestone = milestone;
                    }
                } else {
                    if (nextMilestone == null || milestone.getTime() < nextMilestone.getTime()) {
                        nextMilestone = milestone;
                    }
                }
            }

            if (nextMilestone == null) {
                return "100%";
            }

            long previousTime = previousMilestone != null ? (long) previousMilestone.getTime() : 0;
            long nextTime = (long) nextMilestone.getTime();
            long currentTime = (long) user.getTime();

            double progress = ((double) (currentTime - previousTime) / (nextTime - previousTime)) * 100;
            return String.format("%.1f%%", Math.min(progress, 100.0));
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Error getting next milestone progress", e);
            return "Error";
        }
    }
}