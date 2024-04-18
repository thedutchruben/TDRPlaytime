package nl.thedutchruben.playtime.core.events.player;

import lombok.Getter;
import nl.thedutchruben.playtime.core.objects.PlaytimeUser;

/**
 * This events get's called every time that the playtime gets updated (every 15 seconds)
 */
@Getter
public class AsyncPlaytimePlayerUpdatePlaytimeEvent extends PlaytimePlayerEvent {
    private final float oldPlaytime;
    private final float newPlaytime;

    public AsyncPlaytimePlayerUpdatePlaytimeEvent(PlaytimeUser user, boolean async, float oldPlaytime, float newPlaytime) {
        super(user, async);
        this.oldPlaytime = oldPlaytime;
        this.newPlaytime = newPlaytime;
    }
}

