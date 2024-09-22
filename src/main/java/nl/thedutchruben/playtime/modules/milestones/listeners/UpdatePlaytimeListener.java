package nl.thedutchruben.playtime.modules.milestones.listeners;

import nl.thedutchruben.mccore.spigot.listeners.TDRListener;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.events.player.AsyncPlaytimePlayerUpdatePlaytimeEvent;
import nl.thedutchruben.playtime.core.objects.RepeatingMilestone;
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
        if (!Playtime.getInstance().getMilestones().isEmpty()) {
            Playtime.getInstance().getMilestones().forEach(milestone -> {
                if (milestone.getOnlineTime() <= event.getNewPlaytime() && milestone.getOnlineTime() > event.getOldPlaytime()) {
                    milestone.apply(event.getUser().getBukkitPlayer());
                }
            });
        }

        // Check and apply repeating milestones
        if (!Playtime.getInstance().getRepeatingMilestones().isEmpty()) {
            for (float i = event.getOldPlaytime(); i < event.getNewPlaytime(); i++) {
                if (i > 0) {
                    for (RepeatingMilestone repeatingMilestone : Playtime.getInstance().getRepeatingMilestones()) {
                        if (i % (repeatingMilestone.getOnlineTime() * 1000) == 1) {
                            repeatingMilestone.apply(event.getUser().getBukkitPlayer());
                        }
                    }
                }
            }
        }
    }
}