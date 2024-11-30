package nl.thedutchruben.playtime.core.events.milestone;

import nl.thedutchruben.playtime.core.objects.Milestone;

/**
 * Event that is called when a milestone is updated
 */
public class MilestoneUpdateEvent extends MilestoneEvent {

    /**
     * Create a new MilestoneUpdateEvent
     * @param milestone The milestone that is updated
     */
    public MilestoneUpdateEvent(Milestone milestone) {
        super(milestone);
    }
}