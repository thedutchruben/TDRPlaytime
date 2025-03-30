package nl.thedutchruben.playtime.core.objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PlaytimeUserTest {

    private PlaytimeUser playtimeUser;
    private final UUID testUuid = UUID.randomUUID();
    private final String testName = "TestPlayer";

    @BeforeEach
    void setUp() {
        playtimeUser = new PlaytimeUser(testUuid.toString(), testName, 0);
    }

    @Test
    void testGetUUID() {
        assertEquals(testUuid, playtimeUser.getUUID());
    }

    @Test
    void testGetName() {
        assertEquals(testName, playtimeUser.getName());
    }

    @Test
    void testAddPlaytime() {
        // Test adding 1 hour
        playtimeUser.addPlaytime(1, TimeUnit.HOURS);
        assertEquals(TimeUnit.HOURS.toMillis(1), playtimeUser.getTime());

        // Test adding 30 minutes
        playtimeUser.addPlaytime(30, TimeUnit.MINUTES);
        assertEquals(TimeUnit.HOURS.toMillis(1) + TimeUnit.MINUTES.toMillis(30), playtimeUser.getTime());
    }

    @Test
    void testRemovePlaytime() {
        // Add 2 hours first
        playtimeUser.addPlaytime(2, TimeUnit.HOURS);

        // Remove 30 minutes
        playtimeUser.removePlaytime(30, TimeUnit.MINUTES);

        assertEquals(TimeUnit.HOURS.toMillis(2) - TimeUnit.MINUTES.toMillis(30), playtimeUser.getTime());
    }

    @Test
    void testSetPlaytime() {
        playtimeUser.setPlaytime(10000);
        assertEquals(10000, playtimeUser.getTime());
    }

    @Test
    void testTranslateTime() {
        // Set playtime to 1 day, 2 hours, 30 minutes, and 15 seconds
        long playtime =
                TimeUnit.DAYS.toMillis(1) +
                        TimeUnit.HOURS.toMillis(2) +
                        TimeUnit.MINUTES.toMillis(30) +
                        TimeUnit.SECONDS.toMillis(15);

        playtimeUser.setPlaytime(playtime);

        int[] time = playtimeUser.translateTime();

        assertEquals(1, time[0], "Days should be 1");
        assertEquals(2, time[1], "Hours should be 2");
        assertEquals(30, time[2], "Minutes should be 30");
        assertEquals(15, time[3], "Seconds should be 15");
    }
}