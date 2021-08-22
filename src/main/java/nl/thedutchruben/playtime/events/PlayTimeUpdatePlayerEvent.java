package nl.thedutchruben.playtime.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayTimeUpdatePlayerEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final long oldTime;
    private final long newTime;


    /**
     * The default constructor is defined for cleaner code. This constructor
     * assumes the event is synchronous.
     */
    public PlayTimeUpdatePlayerEvent(Player player, long oldTime, long newTime) {
        super(true);
        this.player = player;
        this.oldTime = oldTime;
        this.newTime = newTime;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public long getNewTime() {
        return newTime;
    }

    public long getOldTime() {
        return oldTime;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
