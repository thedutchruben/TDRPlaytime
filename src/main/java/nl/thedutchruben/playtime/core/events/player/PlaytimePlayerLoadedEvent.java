package nl.thedutchruben.playtime.core.events.player;

import nl.thedutchruben.playtime.core.objects.PlaytimeUser;

/**
 * Event that is called when a player is loaded
 */
public class PlaytimePlayerLoadedEvent extends PlaytimePlayerEvent {

    /**
     * Create a new PlaytimePlayerLoadedEvent
     * @param user The user that is loaded
     * @param async If the event is async
     */
    public PlaytimePlayerLoadedEvent(PlaytimeUser user, boolean async) {
        super(user, async);
    }
}