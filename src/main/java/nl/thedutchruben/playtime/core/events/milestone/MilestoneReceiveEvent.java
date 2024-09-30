package nl.thedutchruben.playtime.core.events.milestone;

import nl.thedutchruben.playtime.core.objects.Milestone;
import nl.thedutchruben.playtime.core.objects.PlaytimeUser;

public class MilestoneReceiveEvent extends MilestoneEvent {
    private PlaytimeUser playtimeUser;

    public MilestoneReceiveEvent(Milestone milestone, PlaytimeUser playtimeUser) {
        super(milestone);

        this.playtimeUser = playtimeUser;
    }

    public PlaytimeUser getPlaytimeUser() {
        return playtimeUser;
    }
}