package nl.thedutchruben.playtime.core.objects;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents conditions that must be met for a reward to be granted
 */
@Getter
@Setter
public class RewardCondition {

    /**
     * List of permissions the player must have (any of them)
     */
    @SerializedName("required_permissions")
    private List<String> requiredPermissions;

    /**
     * List of permissions the player must NOT have (none of them)
     */
    @SerializedName("denied_permissions")
    private List<String> deniedPermissions;

    /**
     * List of worlds where the reward can be given
     */
    @SerializedName("allowed_worlds")
    private List<String> allowedWorlds;

    /**
     * List of worlds where the reward cannot be given
     */
    @SerializedName("denied_worlds")
    private List<String> deniedWorlds;

    /**
     * Minimum active playtime required (in milliseconds)
     */
    @SerializedName("min_active_playtime")
    private Long minActivePlaytime;

    /**
     * Maximum AFK time allowed (in milliseconds)
     */
    @SerializedName("max_afk_time")
    private Long maxAfkTime;

    /**
     * Minimum rank position required (1 = top player)
     */
    @SerializedName("min_rank")
    private Integer minRank;

    /**
     * Maximum rank position allowed
     */
    @SerializedName("max_rank")
    private Integer maxRank;

    /**
     * Only give reward during specific days of week (MONDAY, TUESDAY, etc.)
     */
    @SerializedName("allowed_days")
    private List<String> allowedDays;

    /**
     * Only give reward during specific hours (0-23)
     */
    @SerializedName("allowed_hours")
    private List<Integer> allowedHours;

    /**
     * Check if all conditions are met for the player
     *
     * @param player The player to check
     * @param playtimeUser The player's playtime data
     * @return true if all conditions are met, false otherwise
     */
    public boolean check(Player player, PlaytimeUser playtimeUser) {
        // Check required permissions (player must have at least one)
        if (requiredPermissions != null && !requiredPermissions.isEmpty()) {
            boolean hasPermission = false;
            for (String permission : requiredPermissions) {
                if (player.hasPermission(permission)) {
                    hasPermission = true;
                    break;
                }
            }
            if (!hasPermission) {
                return false;
            }
        }

        // Check denied permissions (player must not have any)
        if (deniedPermissions != null && !deniedPermissions.isEmpty()) {
            for (String permission : deniedPermissions) {
                if (player.hasPermission(permission)) {
                    return false;
                }
            }
        }

        // Check allowed worlds
        if (allowedWorlds != null && !allowedWorlds.isEmpty()) {
            String playerWorld = player.getWorld().getName();
            if (!allowedWorlds.contains(playerWorld)) {
                return false;
            }
        }

        // Check denied worlds
        if (deniedWorlds != null && !deniedWorlds.isEmpty()) {
            String playerWorld = player.getWorld().getName();
            if (deniedWorlds.contains(playerWorld)) {
                return false;
            }
        }

        // Check minimum active playtime
        if (minActivePlaytime != null) {
            if (playtimeUser.getActivePlaytime() < minActivePlaytime) {
                return false;
            }
        }

        // Check maximum AFK time
        if (maxAfkTime != null) {
            if (playtimeUser.getAfkTime() > maxAfkTime) {
                return false;
            }
        }

        // Check allowed days of week
        if (allowedDays != null && !allowedDays.isEmpty()) {
            DayOfWeek currentDay = LocalDateTime.now().getDayOfWeek();
            String currentDayString = currentDay.toString();
            if (!allowedDays.contains(currentDayString)) {
                return false;
            }
        }

        // Check allowed hours
        if (allowedHours != null && !allowedHours.isEmpty()) {
            int currentHour = LocalDateTime.now().getHour();
            if (!allowedHours.contains(currentHour)) {
                return false;
            }
        }

        // All conditions met
        return true;
    }

    /**
     * Check if any conditions are configured
     *
     * @return true if at least one condition is set
     */
    public boolean hasConditions() {
        return (requiredPermissions != null && !requiredPermissions.isEmpty()) ||
                (deniedPermissions != null && !deniedPermissions.isEmpty()) ||
                (allowedWorlds != null && !allowedWorlds.isEmpty()) ||
                (deniedWorlds != null && !deniedWorlds.isEmpty()) ||
                minActivePlaytime != null ||
                maxAfkTime != null ||
                minRank != null ||
                maxRank != null ||
                (allowedDays != null && !allowedDays.isEmpty()) ||
                (allowedHours != null && !allowedHours.isEmpty());
    }

    /**
     * Add a required permission
     */
    public void addRequiredPermission(String permission) {
        if (requiredPermissions == null) {
            requiredPermissions = new ArrayList<>();
        }
        requiredPermissions.add(permission);
    }

    /**
     * Add a denied permission
     */
    public void addDeniedPermission(String permission) {
        if (deniedPermissions == null) {
            deniedPermissions = new ArrayList<>();
        }
        deniedPermissions.add(permission);
    }

    /**
     * Add an allowed world
     */
    public void addAllowedWorld(String world) {
        if (allowedWorlds == null) {
            allowedWorlds = new ArrayList<>();
        }
        allowedWorlds.add(world);
    }

    /**
     * Add a denied world
     */
    public void addDeniedWorld(String world) {
        if (deniedWorlds == null) {
            deniedWorlds = new ArrayList<>();
        }
        deniedWorlds.add(world);
    }

    /**
     * Add an allowed day
     */
    public void addAllowedDay(DayOfWeek day) {
        if (allowedDays == null) {
            allowedDays = new ArrayList<>();
        }
        allowedDays.add(day.toString());
    }

    /**
     * Add an allowed hour
     */
    public void addAllowedHour(int hour) {
        if (allowedHours == null) {
            allowedHours = new ArrayList<>();
        }
        if (hour >= 0 && hour <= 23) {
            allowedHours.add(hour);
        }
    }

    /**
     * Get a description of this condition for display
     */
    public String getDescription() {
        StringBuilder sb = new StringBuilder();

        if (requiredPermissions != null && !requiredPermissions.isEmpty()) {
            sb.append("Required permission: ").append(String.join(" OR ", requiredPermissions)).append("\n");
        }

        if (deniedPermissions != null && !deniedPermissions.isEmpty()) {
            sb.append("Cannot have permission: ").append(String.join(", ", deniedPermissions)).append("\n");
        }

        if (allowedWorlds != null && !allowedWorlds.isEmpty()) {
            sb.append("Only in worlds: ").append(String.join(", ", allowedWorlds)).append("\n");
        }

        if (deniedWorlds != null && !deniedWorlds.isEmpty()) {
            sb.append("Not in worlds: ").append(String.join(", ", deniedWorlds)).append("\n");
        }

        if (minActivePlaytime != null) {
            sb.append("Minimum active time: ").append(minActivePlaytime / 1000 / 60).append(" minutes\n");
        }

        if (maxAfkTime != null) {
            sb.append("Maximum AFK time: ").append(maxAfkTime / 1000 / 60).append(" minutes\n");
        }

        if (allowedDays != null && !allowedDays.isEmpty()) {
            sb.append("Only on: ").append(String.join(", ", allowedDays)).append("\n");
        }

        if (allowedHours != null && !allowedHours.isEmpty()) {
            sb.append("Only during hours: ").append(allowedHours.toString()).append("\n");
        }

        return sb.toString().trim();
    }
}
