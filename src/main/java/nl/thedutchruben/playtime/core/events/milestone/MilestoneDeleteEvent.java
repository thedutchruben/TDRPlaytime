package nl.thedutchruben.playtime.core.events.milestone;

import nl.thedutchruben.playtime.core.objects.Milestone;

public class MilestoneDeleteEvent extends MilestoneEvent{
    public MilestoneDeleteEvent(Milestone milestone) {
        super(milestone);
    }
}
