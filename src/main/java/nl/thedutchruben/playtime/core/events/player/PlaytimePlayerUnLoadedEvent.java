package nl.thedutchruben.playtime.core.events.player;

import nl.thedutchruben.playtime.core.objects.PlaytimeUser;

/**
 * This event is called when a player is unloaded
 */
public class PlaytimePlayerUnLoadedEvent extends PlaytimePlayerEvent
{
    /**
     * Create a new PlaytimePlayerUnLoadedEvent
     * @param user the user that is unloaded
     * @param async if the event is async
     */
    public PlaytimePlayerUnLoadedEvent(PlaytimeUser user, boolean async) {
        super(user, async);
    }
}
