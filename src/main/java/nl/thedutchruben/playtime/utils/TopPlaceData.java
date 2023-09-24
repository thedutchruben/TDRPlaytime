package nl.thedutchruben.playtime.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * This class is used to store the data of the top places
 */
@Getter
@Setter
@AllArgsConstructor
public class TopPlaceData {
    /**
     * The name of the player
     */
    private String name;
    /**
     * The uuid of the player
     */
    private String uuid;
    /**
     * The time the player has played
     */
    private long time;
}