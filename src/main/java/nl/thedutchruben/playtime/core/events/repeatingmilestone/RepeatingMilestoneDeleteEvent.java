package nl.thedutchruben.playtime.core.events.repeatingmilestone;

import nl.thedutchruben.playtime.core.objects.RepeatingMilestone;

/**
 * Event that is called when a RepeatingMilestone is deleted
 */
public class RepeatingMilestoneDeleteEvent extends RepeatingMilestoneEvent {

    /**
     * Create a new RepeatingMilestoneDeleteEvent
     *
     * @param repeatingMilestone The RepeatingMilestone that is deleted
     */
    public RepeatingMilestoneDeleteEvent(RepeatingMilestone repeatingMilestone) {
        super(repeatingMilestone);
    }
}