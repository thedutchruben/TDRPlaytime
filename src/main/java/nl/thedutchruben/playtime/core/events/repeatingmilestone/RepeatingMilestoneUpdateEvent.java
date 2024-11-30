package nl.thedutchruben.playtime.core.events.repeatingmilestone;

import nl.thedutchruben.playtime.core.objects.RepeatingMilestone;

/**
 * Event that is called when a RepeatingMilestone is updated
 */
public class RepeatingMilestoneUpdateEvent extends RepeatingMilestoneEvent {

    /**
     * Create a new RepeatingMilestoneUpdateEvent
     * @param repeatingMilestone The RepeatingMilestone that is updated
     */
    public RepeatingMilestoneUpdateEvent(RepeatingMilestone repeatingMilestone) {
        super(repeatingMilestone);
    }
}