package nl.thedutchruben.playtime.core.events.milestone;

import nl.thedutchruben.playtime.core.objects.Milestone;
import nl.thedutchruben.playtime.core.objects.PlaytimeUser;

/**
 * Event that is called when a milestone is received
 */
public class MilestoneReceiveEvent extends MilestoneEvent {
    private final PlaytimeUser playtimeUser;

    /**
     * Create a new MilestoneReceiveEvent
     * @param milestone The milestone that is received
     * @param playtimeUser The user that received the milestone
     */
    public MilestoneReceiveEvent(Milestone milestone, PlaytimeUser playtimeUser) {
        super(milestone);

        this.playtimeUser = playtimeUser;
    }

    /**
     * Get the user that received the milestone
     * @see PlaytimeUser
     * @return The user that received the milestone
     */
    public PlaytimeUser getPlaytimeUser() {
        return playtimeUser;
    }
}