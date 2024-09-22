package nl.thedutchruben.playtime.core.events.repeatingmilestone;

import nl.thedutchruben.playtime.core.objects.RepeatingMilestone;

public class RepeatingMilestoneDeleteEvent extends RepeatingMilestoneEvent {
    public RepeatingMilestoneDeleteEvent(RepeatingMilestone repeatingMilestone) {
        super(repeatingMilestone);
    }
}
