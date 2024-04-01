package nl.thedutchruben.playtime.core.events.player;

import lombok.Getter;
import nl.thedutchruben.playtime.core.objects.PlaytimeUser;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class PlaytimePlayerLoadedEvent extends PlaytimePlayerEvent {

    public PlaytimePlayerLoadedEvent(PlaytimeUser user, boolean async) {
        super(user, async);
    }
}
