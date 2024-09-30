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
        // Check and apply milestones that are not repeating
        Playtime.getInstance().getMilestones().stream()
                .filter(milestone -> milestone.getOnlineTime() <= event.getNewPlaytime() && milestone.getOnlineTime() > event.getOldPlaytime())
                .forEach(milestone -> milestone.apply(event.getUser().getBukkitPlayer()));

        // Check and apply repeating milestones
        for (float i = event.getOldPlaytime(); i < event.getNewPlaytime(); i++) {
            if (i > 0) {
                float finalI = i;
                Playtime.getInstance().getRepeatingMilestones().stream()
                        .filter(repeatingMilestone -> finalI % (repeatingMilestone.getOnlineTime() * 1000) == 1)
                        .forEach(repeatingMilestone -> repeatingMilestone.apply(event.getUser().getBukkitPlayer()));
            }
        }
    }
}