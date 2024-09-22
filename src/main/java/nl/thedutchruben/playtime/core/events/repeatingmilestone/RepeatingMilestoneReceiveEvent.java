package nl.thedutchruben.playtime.core.events.repeatingmilestone;

import nl.thedutchruben.playtime.core.objects.PlaytimeUser;
import nl.thedutchruben.playtime.core.objects.RepeatingMilestone;

public class RepeatingMilestoneReceiveEvent extends RepeatingMilestoneEvent {
    private PlaytimeUser playtimeUser;
    public RepeatingMilestoneReceiveEvent(RepeatingMilestone repeatingMilestone, PlaytimeUser playtimeUser) {
        super(repeatingMilestone);

        this.playtimeUser = playtimeUser;
    }

    public PlaytimeUser getPlaytimeUser() {
        return playtimeUser;
    }
}
