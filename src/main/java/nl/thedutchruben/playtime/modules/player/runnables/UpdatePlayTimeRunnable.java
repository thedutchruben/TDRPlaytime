package nl.thedutchruben.playtime.modules.player.runnables;

import nl.thedutchruben.mccore.spigot.runnables.ASyncRepeatingTask;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.Settings;
import nl.thedutchruben.playtime.core.afk.AFKManager;
import nl.thedutchruben.playtime.core.objects.PlaytimeUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Every 15 seconds the playtime will update for scoreboards and for getting the milestones
 */
@ASyncRepeatingTask(repeatTime = 300, startTime = 300)
public class UpdatePlayTimeRunnable implements Runnable {

    /**
     * When an object implementing interface {@code Runnable} is used
     * to create a thread, starting the thread causes the object's
     * {@code run} method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method {@code run} is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        // Update AFK statuses first
        AFKManager.getInstance().checkAllPlayers();

        // Check if any players need to be kicked for being AFK too long
        if (Settings.AFK_KICK_ENABLED.getValueAsBoolean()) {
            long kickThresholdMillis = Settings.AFK_KICK_THRESHOLD_MINUTES.getValueAsInteger() * 60 * 1000;
            String kickMessage = Settings.AFK_KICK_MESSAGE.getValueAsString();

            for (PlaytimeUser playtimeUser : Playtime.getInstance().getPlaytimeUsers().values()) {
                if (playtimeUser.isAfk()) {
                    long afkDuration = System.currentTimeMillis() - playtimeUser.getAfkSince();
                    if (afkDuration >= kickThresholdMillis) {
                        Player player = playtimeUser.getBukkitPlayer();
                        if (player != null && !player.hasPermission("playtime.afk.kickexempt")) {
                            Bukkit.getScheduler().runTask(Playtime.getPlugin(), () ->
                                    player.kickPlayer(kickMessage)
                            );
                        }
                    }
                }
            }
        }

        // Update playtimes
        for (PlaytimeUser playtimeUser : Playtime.getInstance().getPlaytimeUsers().values()) {
            playtimeUser.updatePlaytime();
        }
    }
}