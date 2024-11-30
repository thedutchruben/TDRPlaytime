package nl.thedutchruben.playtime.core.events.milestone;

import nl.thedutchruben.playtime.core.objects.Milestone;

/**
 * Event that is called when a milestone is deleted
 */
public class MilestoneDeleteEvent extends MilestoneEvent {

    /**
     * Create a new MilestoneDeleteEvent
     * @param milestone The milestone that is deleted
     */
    public MilestoneDeleteEvent(Milestone milestone) {
        super(milestone);
    }
}