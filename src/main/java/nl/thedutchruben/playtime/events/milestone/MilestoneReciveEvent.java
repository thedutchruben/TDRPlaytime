package nl.thedutchruben.playtime.events.milestone;

import nl.thedutchruben.playtime.milestone.Milestone;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MilestoneReciveEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private Player player;
    private Milestone milestone;

    /**
     * The default constructor is defined for cleaner code. This constructor
     * assumes the event is synchronous.
     */
    public MilestoneReciveEvent(Player player, Milestone milestone) {
        this.player = player;
        this.milestone = milestone;
    }

    /**
     * This constructor is used to explicitly declare an event as synchronous
     * or asynchronous.
     *
     * @param isAsync true indicates the event will fire asynchronously, false
     *                by default from default constructor
     */
    public MilestoneReciveEvent(boolean isAsync) {
        super(isAsync);
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public Milestone getMilestone() {
        return milestone;
    }

    public Player getPlayer() {
        return player;
    }
}
