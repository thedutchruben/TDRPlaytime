package nl.thedutchruben.playtime.core.events.afk;

import nl.thedutchruben.playtime.core.objects.PlaytimeUser;

public class PlayerAFKEvent extends AFKEvent {
    public PlayerAFKEvent(PlaytimeUser user, boolean async) {
        super(user, async);
    }
}