package nl.thedutchruben.playtime.core.events.player;

import nl.thedutchruben.playtime.core.objects.PlaytimeUser;

public class PlaytimePlayerUnLoadedEvent extends PlaytimePlayerEvent
{
    public PlaytimePlayerUnLoadedEvent(PlaytimeUser user, boolean async) {
        super(user, async);
    }
}
