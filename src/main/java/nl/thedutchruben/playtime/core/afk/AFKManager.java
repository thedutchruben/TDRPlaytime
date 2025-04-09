package nl.thedutchruben.playtime.core.afk;

import lombok.Getter;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.Settings;
import nl.thedutchruben.playtime.core.events.afk.PlayerAFKEvent;
import nl.thedutchruben.playtime.core.events.afk.PlayerReturnFromAFKEvent;
import nl.thedutchruben.playtime.core.objects.PlaytimeUser;
import nl.thedutchruben.playtime.core.translations.Messages;
import nl.thedutchruben.playtime.utils.Replacement;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AFKManager {

    @Getter
    private static AFKManager instance;
    private final Map<UUID, Long> lastActivity = new HashMap<>();
    private final long afkThresholdMillis;
    private final boolean useEssentialsAPI;
    private final boolean countAfkTime;

    public AFKManager() {
        instance = this;
        this.afkThresholdMillis = TimeUnit.MINUTES.toMillis(Settings.AFK_THRESHOLD_MINUTES.getValueAsInteger());
        this.useEssentialsAPI = Settings.AFK_USE_ESSENTIALS_API.getValueAsBoolean();
        this.countAfkTime = Settings.AFK_COUNT_TIME.getValueAsBoolean();

        // Initialize lastActivity for all online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            lastActivity.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    /**
     * Records player activity and resets AFK status if needed
     * @param player The player who performed an activity
     */
    public void recordActivity(Player player) {
        UUID uuid = player.getUniqueId();
        lastActivity.put(uuid, System.currentTimeMillis());

        PlaytimeUser user = Playtime.getInstance().getPlaytimeUser(uuid).orElse(null);
        if (user != null && user.isAfk()) {
            setPlayerNotAFK(user);
        }
    }

    /**
     * Checks if a player is AFK based on their last activity
     * @param uuid The player's UUID
     * @return true if the player is AFK, false otherwise
     */
    public boolean isAFK(UUID uuid) {
        if (!lastActivity.containsKey(uuid)) return false;

        // If using Essentials, check if the player is marked as AFK in Essentials
        if (useEssentialsAPI && Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                com.earth2me.essentials.Essentials essentials = (com.earth2me.essentials.Essentials)
                        Bukkit.getPluginManager().getPlugin("Essentials");
                return essentials.getUser(player).isAfk();
            }
        }

        // Otherwise, check based on last activity
        long timeSinceLastActivity = System.currentTimeMillis() - lastActivity.get(uuid);
        return timeSinceLastActivity >= afkThresholdMillis;
    }

    /**
     * Sets a player as AFK
     * @param user The PlaytimeUser to mark as AFK
     */
    public void setPlayerAFK(PlaytimeUser user) {
        if (user.isAfk()) return;

        user.setAfk(true);
        user.setAfkSince(System.currentTimeMillis());

        Player player = user.getBukkitPlayer();
        if (player != null) {
            // Fire AFK event
            PlayerAFKEvent event = new PlayerAFKEvent(user, false);
            Bukkit.getPluginManager().callEvent(event);

            // Broadcast message if enabled
            if (Settings.AFK_BROADCAST_MESSAGES.getValueAsBoolean()) {
                String message = Messages.PLAYER_NOW_AFK.getMessage(
                        new Replacement("%player%", player.getName())
                );
                if (Settings.AFK_BROADCAST_TO_ALL.getValueAsBoolean()) {
                    Bukkit.broadcastMessage(message);
                } else {
                    player.sendMessage(message);
                }
            }
        }
    }

    /**
     * Sets a player as not AFK
     * @param user The PlaytimeUser to mark as not AFK
     */
    public void setPlayerNotAFK(PlaytimeUser user) {
        if (!user.isAfk()) return;

        long afkDuration = System.currentTimeMillis() - user.getAfkSince();
        user.setAfk(false);
        user.addAfkTime(afkDuration);

        Player player = user.getBukkitPlayer();
        if (player != null) {
            // Fire return from AFK event
            PlayerReturnFromAFKEvent event = new PlayerReturnFromAFKEvent(user, false, afkDuration);
            Bukkit.getPluginManager().callEvent(event);

            // Broadcast message if enabled
            if (Settings.AFK_BROADCAST_MESSAGES.getValueAsBoolean()) {
                String message = Messages.PLAYER_NO_LONGER_AFK.getMessage(
                        new Replacement("%player%", player.getName())
                );
                if (Settings.AFK_BROADCAST_TO_ALL.getValueAsBoolean()) {
                    Bukkit.broadcastMessage(message);
                } else {
                    player.sendMessage(message);
                }
            }
        }
    }

    /**
     * Checks all online players and updates their AFK status
     */
    public void checkAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            PlaytimeUser user = Playtime.getInstance().getPlaytimeUser(uuid).orElse(null);

            if (user == null) continue;

            boolean shouldBeAfk = isAFK(uuid);
            if (shouldBeAfk && !user.isAfk()) {
                setPlayerAFK(user);
            } else if (!shouldBeAfk && user.isAfk()) {
                setPlayerNotAFK(user);
            }
        }
    }

    /**
     * Check if the system should count AFK time toward playtime
     * @return true if AFK time should be counted, false otherwise
     */
    public boolean shouldCountAfkTime() {
        return countAfkTime;
    }

    /**
     * Removes a player from the activity tracking
     * @param uuid The UUID of the player to remove
     */
    public void removePlayer(UUID uuid) {
        lastActivity.remove(uuid);
    }
}