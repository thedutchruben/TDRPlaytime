package nl.thedutchruben.playtime.modules.milestones.listeners;

import nl.thedutchruben.mccore.spigot.listeners.TDRListener;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.events.player.AsyncPlaytimePlayerUpdatePlaytimeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Listener class that handles the update of player playtime milestones.
 */
@TDRListener
public class UpdatePlaytimeListener implements Listener {

    /**
     * Event handler for updating player playtime.
     *
     * @param event The event that contains the old and new playtime of the player.
     */
    @EventHandler
    public void updatePlaytime(AsyncPlaytimePlayerUpdatePlaytimeEvent event) {
        Playtime.getInstance().getMilestones().stream()
                .filter(milestone -> milestone.getOnlineTimeInMilliseconds() <= event.getNewPlaytime()
                        && milestone.getOnlineTimeInMilliseconds() > event.getOldPlaytime())
                .forEach(milestone -> milestone.apply(event.getUser().getBukkitPlayer()));

        // Check and apply repeating milestones
        // For each repeating milestone, check if we crossed an interval boundary
        Playtime.getInstance().getRepeatingMilestones().stream()
                .filter(repeatingMilestone -> repeatingMilestone.getOnlineTimeInMilliseconds() > 0)
                .forEach(repeatingMilestone -> {
                    long interval = repeatingMilestone.getOnlineTimeInMilliseconds();

                    // Calculate how many times this milestone should have been awarded
                    long oldCount = (long) (event.getOldPlaytime() / interval);
                    long newCount = (long) (event.getNewPlaytime() / interval);

                    // If the count increased, award the milestone for each time crossed
                    long timesToAward = newCount - oldCount;
                    for (int i = 0; i < timesToAward; i++) {
                        repeatingMilestone.apply(event.getUser().getBukkitPlayer());
                    }
                });
    }
}