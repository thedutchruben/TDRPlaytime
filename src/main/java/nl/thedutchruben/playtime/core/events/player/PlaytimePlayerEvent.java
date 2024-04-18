package nl.thedutchruben.playtime.core.events.player;

import lombok.Getter;
import nl.thedutchruben.playtime.core.objects.PlaytimeUser;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public abstract class PlaytimePlayerEvent extends Event {
    private PlaytimeUser user;
    private static final HandlerList handlers = new HandlerList();


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
