package nl.thedutchruben.playtime.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayTimeCheckEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();


    /**
     * The default constructor is defined for cleaner code. This constructor
     * assumes the event is synchronous.
     */
    public PlayTimeCheckEvent() {
    }

    /**
     * This constructor is used to explicitly declare an event as synchronous
     * or asynchronous.
     *
     * @param isAsync true indicates the event will fire asynchronously, false
     *                by default from default constructor
     */
    public PlayTimeCheckEvent(boolean isAsync) {
        super(isAsync);
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
