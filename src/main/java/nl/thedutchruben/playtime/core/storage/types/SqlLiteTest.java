package nl.thedutchruben.playtime.core.storage.types;

import nl.thedutchruben.playtime.core.objects.Milestone;
import nl.thedutchruben.playtime.core.objects.PlaytimeUser;
import nl.thedutchruben.playtime.core.objects.RepeatingMilestone;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SqlLiteTest {

    private SqlLite sqlLite;
    private final UUID testUuid = UUID.randomUUID();
    private final String testName = "TestPlayer";

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // Set system property to use temp directory for database
        System.setProperty("playtime.dbfile", tempDir.resolve("playtime.db").toString());

        sqlLite = new SqlLite();
        sqlLite.setup();
    }

    @AfterEach
    void tearDown() {
        sqlLite.stop();
        System.clearProperty("playtime.dbfile");
    }

    @Test
    void testCreateAndLoadUser() throws ExecutionException, InterruptedException {
        // Create a user
        PlaytimeUser user = new PlaytimeUser(testUuid.toString(), testName, 3600000); // 1 hour playtime
        assertTrue(sqlLite.createUser(user).get());

        // Load the user and verify
        PlaytimeUser loadedUser = sqlLite.loadUser(testUuid).get();
        assertNotNull(loadedUser);
        assertEquals(testUuid.toString(), loadedUser.getUUID().toString());
        assertEquals(testName, loadedUser.getName());
        assertEquals(3600000, loadedUser.getTime());
    }

    @Test
    void testSaveUser() throws ExecutionException, InterruptedException {
        // Create a user
        PlaytimeUser user = new PlaytimeUser(testUuid.toString(), testName, 0);
        assertTrue(sqlLite.createUser(user).get());

        // Update playtime
        user.setPlaytime(7200000); // 2 hours
        assertTrue(sqlLite.saveUser(user).get());

        // Load and verify
        PlaytimeUser loadedUser = sqlLite.loadUser(testUuid).get();
        assertEquals(7200000, loadedUser.getTime());
    }

    @Test
    void testCreateAndGetMilestone() throws ExecutionException, InterruptedException {
        // Create a milestone
        Milestone milestone = new Milestone();
        milestone.setMilestoneName("TestMilestone");
        milestone.setOnlineTime(3600); // 1 hour
        milestone.addCommand("give %playername% diamond 1");
        milestone.addMessage("Congratulations!");

        assertTrue(sqlLite.saveMilestone(milestone).get());

        // Get all milestones
        List<Milestone> milestones = sqlLite.getMilestones().get();
        assertEquals(1, milestones.size());

        Milestone loadedMilestone = milestones.get(0);
        assertEquals("TestMilestone", loadedMilestone.getMilestoneName());
        assertEquals(3600, loadedMilestone.getOnlineTime());
        assertEquals(1, loadedMilestone.getCommands().size());
        assertEquals(1, loadedMilestone.getMessages().size());
    }

    @Test
    void testDeleteMilestone() throws ExecutionException, InterruptedException {
        // Create a milestone
        Milestone milestone = new Milestone();
        milestone.setMilestoneName("DeleteTest");
        milestone.setOnlineTime(3600);

        assertTrue(sqlLite.saveMilestone(milestone).get());

        // Delete the milestone
        assertTrue(sqlLite.deleteMilestone(milestone).get());

        // Verify it's gone
        List<Milestone> milestones = sqlLite.getMilestones().get();
        assertEquals(0, milestones.size());
    }

    @Test
    void testGetTopUsers() throws ExecutionException, InterruptedException {
        // Create multiple users with different playtimes
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID uuid3 = UUID.randomUUID();

        PlaytimeUser user1 = new PlaytimeUser(uuid1.toString(), "Player1", 10000);
        PlaytimeUser user2 = new PlaytimeUser(uuid2.toString(), "Player2", 20000);
        PlaytimeUser user3 = new PlaytimeUser(uuid3.toString(), "Player3", 30000);

        sqlLite.createUser(user1).get();
        sqlLite.createUser(user2).get();
        sqlLite.createUser(user3).get();

        // Get top 2 users
        List<PlaytimeUser> topUsers = sqlLite.getTopUsers(2, 0).get();
        assertEquals(2, topUsers.size());

        // Verify they're in order
        assertEquals(user3.getName(), topUsers.get(0).getName());
        assertEquals(user2.getName(), topUsers.get(1).getName());
    }
}