package nl.thedutchruben.playtime.core.objects;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import nl.thedutchruben.playtime.Playtime;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MilestoneTest {

    private Milestone milestone;

    @Mock
    private Player mockPlayer;

    @Mock
    private PlayerInventory mockInventory;

    @Mock
    private Playtime mockPlaytime;

    @BeforeEach
    void setUp() {
        milestone = new Milestone();
        milestone.setMilestoneName("TestMilestone");
        milestone.setOnlineTime(3600); // 1 hour in seconds

        // Setup mock player
        when(mockPlayer.getInventory()).thenReturn(mockInventory);
        when(mockPlayer.getName()).thenReturn("TestPlayer");
        when(mockPlayer.getUniqueId()).thenReturn(UUID.randomUUID());
    }

    @Test
    void testGetOnlineTimeInMilliseconds() {
        assertEquals(3600 * 1000, milestone.getOnlineTimeInMilliseconds());
    }

    @Test
    void testAddCommand() {
        milestone.addCommand("give %playername% diamond 1");
        assertEquals(1, milestone.getCommands().size());
        assertEquals("give %playername% diamond 1", milestone.getCommands().get(0));
    }

    @Test
    void testAddMessage() {
        milestone.addMessage("Congratulations on reaching the milestone!");
        assertEquals(1, milestone.getMessages().size());
        assertEquals("Congratulations on reaching the milestone!", milestone.getMessages().get(0));
    }

    @Test
    void testRemoveCommand() {
        String command = "give %playername% diamond 1";
        milestone.addCommand(command);
        assertEquals(1, milestone.getCommands().size());

        milestone.removeCommand(command);
        assertEquals(0, milestone.getCommands().size());
    }

    @Test
    void testRemoveMessage() {
        String message = "Congratulations on reaching the milestone!";
        milestone.addMessage(message);
        assertEquals(1, milestone.getMessages().size());

        milestone.removeMessage(message);
        assertEquals(0, milestone.getMessages().size());
    }

    @Test
    void testFireworkSettings() {
        assertFalse(milestone.isFireworkShow());

        milestone.setFireworkShow(true);
        assertTrue(milestone.isFireworkShow());

        milestone.setFireworkShowAmount(5);
        assertEquals(5, milestone.getFireworkShowAmount());

        milestone.setFireworkShowSecondsBetween(2);
        assertEquals(2, milestone.getFireworkShowSecondsBetween());
    }
}