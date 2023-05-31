package nl.thedutchruben.playtime.utils;

/**
 * @author Ruben
 * @version 1.0
 * <p>
 * This class is used to replace a string with a string.
 * </p>
 * @since 1.0
 */
public class Replacement {
    /**
     * The string to replace.
     */
    private final String from;

    /**
     * The string to replace with.
     */
    private final String to;

    /**
     * @param from The string to replace.
     * @param to   The string to replace with.
     */
    public Replacement(String from, String to) {
        this.from = from;
        this.to = to;
    }

    /**
     * @return The string to replace.
     */
    public String getFrom() {
        return from;
    }

    /**
     * @return The string to replace with.
     */
    public String getTo() {
        return to;
    }
}
