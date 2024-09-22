package nl.thedutchruben.playtime.core.events.player;

import lombok.Getter;
import nl.thedutchruben.playtime.core.objects.PlaytimeUser;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This is the base class for all the player events
 */
@Getter
public abstract class PlaytimePlayerEvent extends Event {
    private PlaytimeUser user;
    private static final HandlerList handlers = new HandlerList();

    /**
     * Create a new PlaytimePlayerEvent
     * @param user The user
     * @param async If the event is async
     */
    public PlaytimePlayerEvent(PlaytimeUser user,boolean async) {
        super(async);
        this.user = user;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
