package nl.thedutchruben.playtime.core.events.player;

import nl.thedutchruben.playtime.core.objects.PlaytimeUser;

public class PlaytimePlayerLoadedEvent extends PlaytimePlayerEvent {

    public PlaytimePlayerLoadedEvent(PlaytimeUser user, boolean async) {
        super(user, async);
    }
}