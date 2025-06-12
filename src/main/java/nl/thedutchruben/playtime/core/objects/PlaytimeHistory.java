package nl.thedutchruben.playtime.core.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nl.thedutchruben.playtime.core.storage.Storage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Represents a single playtime history entry
 */
@Getter
@AllArgsConstructor
public class PlaytimeHistory {
    private final int id;
    private final UUID uuid;
    private final Storage.Event event;
    private final long time;
    private final Date date;

    /**
     * Get the formatted date string based on the provided format
     *
     * @param format The date format pattern
     * @return The formatted date string
     */
    public String getFormattedDate(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }
}
