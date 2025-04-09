package nl.thedutchruben.playtime.core.events.afk;

import lombok.Getter;
import nl.thedutchruben.playtime.core.objects.PlaytimeUser;

public class PlayerReturnFromAFKEvent extends AFKEvent {
    @Getter
    private final long afkDuration;

    public PlayerReturnFromAFKEvent(PlaytimeUser user, boolean async, long afkDuration) {
        super(user, async);
        this.afkDuration = afkDuration;
    }
}