package nl.thedutchruben.playtime.core.events.milestone;

import nl.thedutchruben.playtime.core.objects.Milestone;

/**
 * Event that is called when a milestone is created
 */
public class MilestoneCreateEvent extends MilestoneEvent {

    /**
     * Create a new MilestoneCreateEvent
     * @param milestone The milestone that is created
     */
    public MilestoneCreateEvent(Milestone milestone) {
        super(milestone);
    }
}