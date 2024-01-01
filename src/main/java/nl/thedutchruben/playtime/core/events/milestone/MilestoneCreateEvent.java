package nl.thedutchruben.playtime.core.events.milestone;

import nl.thedutchruben.playtime.core.objects.Milestone;

public class MilestoneCreateEvent extends MilestoneEvent{
    public MilestoneCreateEvent(Milestone milestone) {
        super(milestone);
    }
}
