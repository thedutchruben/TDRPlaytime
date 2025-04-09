package nl.thedutchruben.playtime.core.events.afk;

import lombok.Getter;
import nl.thedutchruben.playtime.core.objects.PlaytimeUser;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Base class for AFK events
 */
public abstract class AFKEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    protected PlaytimeUser user;

    public AFKEvent(PlaytimeUser user, boolean async) {
        super(async);
        this.user = user;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}