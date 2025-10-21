package nl.thedutchruben.playtime.core.managers;

import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.objects.RewardCooldown;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages reward cooldowns for players
 */
public class RewardCooldownManager {

    // In-memory cache of cooldowns: UUID -> MilestoneName -> Cooldown
    private final Map<UUID, Map<String, RewardCooldown>> cooldownCache;

    public RewardCooldownManager() {
        this.cooldownCache = new ConcurrentHashMap<>();
    }

    /**
     * Check if a player can claim a reward
     *
     * @param playerUuid The player's UUID
     * @param milestoneName The milestone name
     * @return true if the player can claim the reward
     */
    public boolean canClaim(UUID playerUuid, String milestoneName) {
        Map<String, RewardCooldown> playerCooldowns = cooldownCache.get(playerUuid);
        if (playerCooldowns == null) {
            return true;
        }

        RewardCooldown cooldown = playerCooldowns.get(milestoneName);
        return cooldown == null || cooldown.isAvailable();
    }

    /**
     * Set a cooldown for a player
     *
     * @param playerUuid The player's UUID
     * @param milestoneName The milestone name
     * @param rewardType The type of reward
     * @param cooldownMillis The cooldown duration in milliseconds
     */
    public void setCooldown(UUID playerUuid, String milestoneName, String rewardType, long cooldownMillis) {
        if (cooldownMillis <= 0) {
            return; // No cooldown
        }

        long now = System.currentTimeMillis();
        RewardCooldown cooldown = new RewardCooldown(playerUuid, milestoneName, rewardType, now, cooldownMillis);

        cooldownCache.computeIfAbsent(playerUuid, k -> new ConcurrentHashMap<>())
                .put(milestoneName, cooldown);

        // Async save to storage
        saveCooldownAsync(cooldown);
    }

    /**
     * Get the remaining cooldown for a reward
     *
     * @param playerUuid The player's UUID
     * @param milestoneName The milestone name
     * @return remaining time in milliseconds, or 0 if no cooldown
     */
    public long getRemainingCooldown(UUID playerUuid, String milestoneName) {
        Map<String, RewardCooldown> playerCooldowns = cooldownCache.get(playerUuid);
        if (playerCooldowns == null) {
            return 0;
        }

        RewardCooldown cooldown = playerCooldowns.get(milestoneName);
        return cooldown != null ? cooldown.getRemainingTime() : 0;
    }

    /**
     * Get a formatted cooldown string
     *
     * @param playerUuid The player's UUID
     * @param milestoneName The milestone name
     * @return formatted cooldown string
     */
    public String getFormattedCooldown(UUID playerUuid, String milestoneName) {
        Map<String, RewardCooldown> playerCooldowns = cooldownCache.get(playerUuid);
        if (playerCooldowns == null) {
            return "Available now";
        }

        RewardCooldown cooldown = playerCooldowns.get(milestoneName);
        return cooldown != null ? cooldown.getRemainingTimeFormatted() : "Available now";
    }

    /**
     * Load cooldowns for a player from storage
     *
     * @param playerUuid The player's UUID
     * @return CompletableFuture that completes when cooldowns are loaded
     */
    public CompletableFuture<Void> loadCooldowns(UUID playerUuid) {
        return CompletableFuture.runAsync(() -> {
            // Load from storage
            CompletableFuture<Map<String, RewardCooldown>> future =
                    Playtime.getInstance().getStorage().getRewardCooldowns(playerUuid);

            try {
                Map<String, RewardCooldown> cooldowns = future.get();
                if (cooldowns != null && !cooldowns.isEmpty()) {
                    // Remove expired cooldowns
                    cooldowns.entrySet().removeIf(entry -> entry.getValue().isAvailable());

                    if (!cooldowns.isEmpty()) {
                        cooldownCache.put(playerUuid, new ConcurrentHashMap<>(cooldowns));
                    }
                }
            } catch (Exception e) {
                Playtime.getPlugin().getLogger().warning("Failed to load cooldowns for player " + playerUuid + ": " + e.getMessage());
            }
        });
    }

    /**
     * Save a cooldown to storage asynchronously
     *
     * @param cooldown The cooldown to save
     */
    private void saveCooldownAsync(RewardCooldown cooldown) {
        CompletableFuture.runAsync(() -> {
            try {
                Playtime.getInstance().getStorage().saveRewardCooldown(cooldown).get();
            } catch (Exception e) {
                Playtime.getPlugin().getLogger().warning("Failed to save cooldown: " + e.getMessage());
            }
        });
    }

    /**
     * Clear all cooldowns for a player
     *
     * @param playerUuid The player's UUID
     */
    public void clearCooldowns(UUID playerUuid) {
        cooldownCache.remove(playerUuid);
    }

    /**
     * Clear a specific cooldown for a player
     *
     * @param playerUuid The player's UUID
     * @param milestoneName The milestone name
     */
    public void clearCooldown(UUID playerUuid, String milestoneName) {
        Map<String, RewardCooldown> playerCooldowns = cooldownCache.get(playerUuid);
        if (playerCooldowns != null) {
            playerCooldowns.remove(milestoneName);

            // Also remove from storage
            CompletableFuture.runAsync(() -> {
                try {
                    Playtime.getInstance().getStorage().deleteRewardCooldown(playerUuid, milestoneName).get();
                } catch (Exception e) {
                    Playtime.getPlugin().getLogger().warning("Failed to delete cooldown: " + e.getMessage());
                }
            });
        }
    }

    /**
     * Cleanup expired cooldowns from cache
     */
    public void cleanupExpiredCooldowns() {
        cooldownCache.values().forEach(playerCooldowns ->
            playerCooldowns.entrySet().removeIf(entry -> entry.getValue().isAvailable())
        );

        // Remove empty player entries
        cooldownCache.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
}
