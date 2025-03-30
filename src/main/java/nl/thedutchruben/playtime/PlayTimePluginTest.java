package nl.thedutchruben.playtime;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import nl.thedutchruben.playtime.core.objects.PlaytimeUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class PlayTimePluginTest {

    private ServerMock server;
    private PlayTimePlugin plugin;

    @BeforeEach
    void setUp() {
        // Start the mock server
        server = MockBukkit.mock();

        // Load the plugin
        plugin = MockBukkit.load(PlayTimePlugin.class);
    }

    @AfterEach
    void tearDown() {
        // Stop the mock server
        MockBukkit.unmock();
    }

    @Test
    void testPluginEnables() {
        assertTrue(plugin.isEnabled());
    }

    @Test
    void testPlayerJoin() {
        // Add a player to the server which should trigger the join event
        PlayerMock player = server.addPlayer();

        // Give the async tasks time to complete
        server.getScheduler().performTicks(100);

        // The player should have been added to Playtime's user map
        PlaytimeUser user = Playtime.getInstance().getPlaytimeUsers().get(player.getUniqueId());
        assertNotNull(user);
        assertEquals(player.getName(), user.getName());
        assertEquals(player.getUniqueId().toString(), user.getUUID().toString());
    }

    @Test
    void testPlaytimeCommand() {
        // Add a player
        PlayerMock player = server.addPlayer();
        server.getScheduler().performTicks(100);

        // Get the player's playtime user and set some playtime
        PlaytimeUser user = Playtime.getInstance().getPlaytimeUsers().get(player.getUniqueId());
        user.addPlaytime(2, TimeUnit.HOURS);

        // Execute the command
        player.performCommand("playtime");

        // Player should receive a message containing their playtime
        player.assertSaid("[PlayTime] Your playtime is 0 day(s) 2 hour(s) 0 minute(s) 0 second(s)");
    }

    @Test
    void testMilestoneCommand() {
        // Add a player with OP permissions
        PlayerMock player = server.addPlayer();
        player.setOp(true);
        server.getScheduler().performTicks(100);

        // Create a milestone
        player.performCommand("milestone create TestMilestone 3600");
        player.assertSaid("The milestone is created!");

        // List milestones
        player.performCommand("milestone list");
        player.assertSaid("TestMilestone Time: Days: 0 Hours: 1 ,Minute's: 0 ,Seconds's: 0");
    }

    @Test
    void testPlaytimeIncreases() {
        // Add a player
        PlayerMock player = server.addPlayer();
        server.getScheduler().performTicks(100);

        // Get the initial playtime
        PlaytimeUser user = Playtime.getInstance().getPlaytimeUsers().get(player.getUniqueId());
        float initialPlaytime = user.getTime();

        // Trigger the update playtime runnable
        server.getScheduler().performTicks(300);

        // Playtime should have increased
        float newPlaytime = user.getTime();
        assertTrue(newPlaytime > initialPlaytime);
    }
}