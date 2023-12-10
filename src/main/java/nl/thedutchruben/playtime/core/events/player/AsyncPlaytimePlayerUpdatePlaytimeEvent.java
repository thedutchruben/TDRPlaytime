package nl.thedutchruben.playtime.core.events.player;

import lombok.Getter;
import nl.thedutchruben.playtime.core.objects.PlaytimeUser;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * This events get's called every time that the playtime gets updated (every 15 seconds)
 */
@Getter
public class AsyncPlaytimePlayerUpdatePlaytimeEvent extends Event {
    private final PlaytimeUser player;
    private final long oldPlaytime;
    private final long newPlaytime;
    private static final HandlerList handlers = new HandlerList();

    public AsyncPlaytimePlayerUpdatePlaytimeEvent(PlaytimeUser player, long oldPlaytime, long newPlaytime) {
        super(true);
        this.player = player;
        this.oldPlaytime = oldPlaytime;
        this.newPlaytime = newPlaytime;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
