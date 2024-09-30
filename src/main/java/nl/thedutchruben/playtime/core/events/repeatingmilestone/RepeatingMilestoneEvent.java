package nl.thedutchruben.playtime.core.events.repeatingmilestone;

import lombok.Getter;
import nl.thedutchruben.playtime.core.objects.RepeatingMilestone;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Base for the repeatingMilestone events
 */
public abstract class RepeatingMilestoneEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    private RepeatingMilestone repeatingMilestone;


    public RepeatingMilestoneEvent(RepeatingMilestone repeatingMilestone) {
        this.repeatingMilestone = repeatingMilestone;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}