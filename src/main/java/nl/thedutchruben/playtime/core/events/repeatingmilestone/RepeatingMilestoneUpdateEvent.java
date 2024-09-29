package nl.thedutchruben.playtime.core.events.repeatingmilestone;

import nl.thedutchruben.playtime.core.objects.RepeatingMilestone;

public class RepeatingMilestoneUpdateEvent extends RepeatingMilestoneEvent {

    public RepeatingMilestoneUpdateEvent(RepeatingMilestone repeatingMilestone) {
        super(repeatingMilestone);
    }
}