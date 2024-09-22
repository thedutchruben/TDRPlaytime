package nl.thedutchruben.playtime.core.events.repeatingmilestone;

import nl.thedutchruben.playtime.core.objects.RepeatingMilestone;

/**
 * Event that is called when a RepeatingMilestone is created
 */
public class RepeatingMilestoneCreateEvent extends RepeatingMilestoneEvent {

    /**
     * Create a new RepeatingMilestoneCreateEvent
     * @param repeatingMilestone The RepeatingMilestone that is created
     */
    public RepeatingMilestoneCreateEvent(RepeatingMilestone repeatingMilestone) {
        super(repeatingMilestone);
    }
}
