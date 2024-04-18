package nl.thedutchruben.playtime.core.events.milestone;

import lombok.Getter;
import nl.thedutchruben.playtime.core.objects.Milestone;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Base for the milestone events
 */
public abstract class MilestoneEvent extends Event {
    @Getter
    private Milestone milestone;
    private static final HandlerList handlers = new HandlerList();


    public MilestoneEvent(Milestone milestone) {
        this.milestone = milestone;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
