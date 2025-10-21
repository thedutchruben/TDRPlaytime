package nl.thedutchruben.playtime.core.objects;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Represents a cooldown record for a reward claim
 */
@Getter
@Setter
public class RewardCooldown {

    /**
     * The UUID of the player
     */
    @SerializedName("player_uuid")
    private String playerUuid;

    /**
     * The name of the milestone/reward
     */
    @SerializedName("milestone_name")
    private String milestoneName;

    /**
     * Type of reward (MILESTONE or REPEATING_MILESTONE)
     */
    @SerializedName("reward_type")
    private String rewardType;

    /**
     * When the reward was last claimed (timestamp in milliseconds)
     */
    @SerializedName("last_claimed")
    private long lastClaimed;

    /**
     * When the reward can be claimed again (timestamp in milliseconds)
     * If 0, there is no cooldown
     */
    @SerializedName("next_available")
    private long nextAvailable;

    public RewardCooldown() {
    }

    public RewardCooldown(UUID playerUuid, String milestoneName, String rewardType, long lastClaimed, long cooldownMillis) {
        this.playerUuid = playerUuid.toString();
        this.milestoneName = milestoneName;
        this.rewardType = rewardType;
        this.lastClaimed = lastClaimed;
        this.nextAvailable = lastClaimed + cooldownMillis;
    }

    /**
     * Check if the cooldown has expired
     *
     * @return true if the reward can be claimed now
     */
    public boolean isAvailable() {
        return System.currentTimeMillis() >= nextAvailable;
    }

    /**
     * Get remaining cooldown time in milliseconds
     *
     * @return milliseconds until reward is available again, or 0 if available
     */
    public long getRemainingTime() {
        long remaining = nextAvailable - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    /**
     * Get remaining cooldown time formatted as a string
     *
     * @return formatted time string (e.g., "2h 30m 15s")
     */
    public String getRemainingTimeFormatted() {
        long remaining = getRemainingTime();
        if (remaining == 0) {
            return "Available now";
        }

        long seconds = remaining / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        seconds %= 60;
        minutes %= 60;
        hours %= 24;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0) sb.append(seconds).append("s");

        return sb.toString().trim();
    }

    /**
     * Get the player UUID
     *
     * @return UUID of the player
     */
    public UUID getPlayerUUID() {
        return UUID.fromString(playerUuid);
    }
}
