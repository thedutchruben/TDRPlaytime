package nl.thedutchruben.playtime.core.events.player;

import lombok.Getter;
import nl.thedutchruben.playtime.core.objects.PlaytimeUser;


@Getter
public class PlaytimePlayerSaveEvent extends PlaytimePlayerEvent {

    public PlaytimePlayerSaveEvent(PlaytimeUser user, boolean async) {
        super(user, async);
    }
}
