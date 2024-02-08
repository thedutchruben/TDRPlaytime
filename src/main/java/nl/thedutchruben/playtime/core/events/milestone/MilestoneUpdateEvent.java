package nl.thedutchruben.playtime.core.events.milestone;

import nl.thedutchruben.playtime.core.objects.Milestone;

public class MilestoneUpdateEvent extends MilestoneEvent{
    public MilestoneUpdateEvent(Milestone milestone) {
        super(milestone);
    }
}
