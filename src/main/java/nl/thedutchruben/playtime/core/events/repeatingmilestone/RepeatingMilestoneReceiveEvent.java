package nl.thedutchruben.playtime.core.events.repeatingmilestone;

import nl.thedutchruben.playtime.core.objects.PlaytimeUser;
import nl.thedutchruben.playtime.core.objects.RepeatingMilestone;

/**
 * Event that is called when a RepeatingMilestone is received
 */
public class RepeatingMilestoneReceiveEvent extends RepeatingMilestoneEvent {

    private final PlaytimeUser playtimeUser;

    /**
     * Create a new RepeatingMilestoneReceiveEvent
     * @param repeatingMilestone The RepeatingMilestone that is received
     * @param playtimeUser The user that received the RepeatingMilestone
     */
    public RepeatingMilestoneReceiveEvent(RepeatingMilestone repeatingMilestone, PlaytimeUser playtimeUser) {
        super(repeatingMilestone);

        this.playtimeUser = playtimeUser;
    }

    public PlaytimeUser getPlaytimeUser() {
        return playtimeUser;
    }
}