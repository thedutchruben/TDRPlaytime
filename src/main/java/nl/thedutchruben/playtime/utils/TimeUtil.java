package nl.thedutchruben.playtime.utils;

import java.util.concurrent.TimeUnit;

/**
 * Utility class for handling time conversions and formatting
 */
public class TimeUtil {

    /**
     * Format milliseconds into a human-readable string showing days, hours, minutes, and seconds
     *
     * @param millis Time in milliseconds
     * @return Formatted time string
     */
    public static String getFormattedTime(long millis) {
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder builder = new StringBuilder();

        if (days > 0) {
            builder.append(days).append("d ");
        }
        if (hours > 0 || days > 0) {
            builder.append(hours).append("h ");
        }
        if (minutes > 0 || hours > 0 || days > 0) {
            builder.append(minutes).append("m ");
        }
        builder.append(seconds).append("s");

        return builder.toString();
    }
}
