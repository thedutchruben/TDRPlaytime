package nl.thedutchruben.playtime.core.events.milestone;

import lombok.Getter;
import nl.thedutchruben.playtime.core.objects.Milestone;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Base for the milestone events
 */
public abstract class MilestoneEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    private Milestone milestone;

    public MilestoneEvent(Milestone milestone) {
        this.milestone = milestone;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}