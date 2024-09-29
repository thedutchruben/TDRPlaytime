package nl.thedutchruben.playtime.core.events.player;

import nl.thedutchruben.playtime.core.objects.PlaytimeUser;

/**
 * This event is called when a player is saved
 */
public class PlaytimePlayerSaveEvent extends PlaytimePlayerEvent {

    /**
     * Create a new PlaytimePlayerSaveEvent
     * @param user the user that is saved
     * @param async if the event is async
     */
    public PlaytimePlayerSaveEvent(PlaytimeUser user, boolean async) {
        super(user, async);
    }
}