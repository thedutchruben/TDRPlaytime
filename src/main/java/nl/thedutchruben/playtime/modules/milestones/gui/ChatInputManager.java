package nl.thedutchruben.playtime.modules.milestones.gui;

import nl.thedutchruben.mccore.spigot.listeners.TDRListener;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.objects.Milestone;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

@TDRListener
public class ChatInputManager implements Listener {

    private static ChatInputManager instance;
    private final Map<UUID, ChatInputSession> pendingSessions = new HashMap<>();

    public ChatInputManager() {
        instance = this;
    }
    
    public static void initialize() {
        if (instance == null) {
            new ChatInputManager();
        }
    }

    public static ChatInputManager getInstance() {
        if (instance == null) {
            instance = new ChatInputManager();
        }
        return instance;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (pendingSessions.containsKey(playerId)) {
            event.setCancelled(true);
            
            ChatInputSession session = pendingSessions.get(playerId);
            String input = event.getMessage().trim();

            // Cancel on specific keywords
            if (input.equalsIgnoreCase("cancel") || input.equalsIgnoreCase("exit")) {
                pendingSessions.remove(playerId);
                player.sendMessage(ChatColor.RED + "Input cancelled.");
                
                // Reopen the parent GUI if available
                if (session.parentGui != null) {
                    Bukkit.getScheduler().runTask(Playtime.getPlugin(), () -> 
                        session.parentGui.open(player)
                    );
                }
                return;
            }

            // Process the input
            pendingSessions.remove(playerId);
            
            // Run the callback on the main thread
            Bukkit.getScheduler().runTask(Playtime.getPlugin(), () -> {
                try {
                    session.callback.accept(player, input);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "Error processing input: " + e.getMessage());
                    Playtime.getPlugin().getLogger().warning("Error processing chat input: " + e.getMessage());
                }
            });
        }
    }

    public void requestInput(Player player, String prompt, MilestoneInfoGui parentGui, BiConsumer<Player, String> callback) {
        UUID playerId = player.getUniqueId();
        
        // Cancel any existing session
        if (pendingSessions.containsKey(playerId)) {
            pendingSessions.remove(playerId);
        }
        
        // Create new session
        ChatInputSession session = new ChatInputSession(parentGui, callback);
        pendingSessions.put(playerId, session);
        
        // Send prompt to player
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "=".repeat(50));
        player.sendMessage(ChatColor.GOLD + prompt);
        player.sendMessage(ChatColor.GRAY + "Type 'cancel' or 'exit' to cancel.");
        player.sendMessage(ChatColor.YELLOW + "=".repeat(50));
        player.sendMessage("");
    }

    public void cancelInput(Player player) {
        pendingSessions.remove(player.getUniqueId());
    }

    public boolean hasActiveSession(Player player) {
        return pendingSessions.containsKey(player.getUniqueId());
    }

    private static class ChatInputSession {
        public final MilestoneInfoGui parentGui;
        public final BiConsumer<Player, String> callback;

        public ChatInputSession(MilestoneInfoGui parentGui, BiConsumer<Player, String> callback) {
            this.parentGui = parentGui;
            this.callback = callback;
        }
    }
}