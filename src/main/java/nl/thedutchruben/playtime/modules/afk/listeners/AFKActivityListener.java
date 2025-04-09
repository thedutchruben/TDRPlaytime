package nl.thedutchruben.playtime.modules.afk.listeners;

import nl.thedutchruben.mccore.spigot.listeners.TDRListener;
import nl.thedutchruben.playtime.core.Settings;
import nl.thedutchruben.playtime.core.afk.AFKManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@TDRListener
public class AFKActivityListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Reset activity time when player joins
        AFKManager.getInstance().recordActivity(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Remove player from AFK tracking when they quit
        AFKManager.getInstance().removePlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!Settings.AFK_EVENTS_CHAT.getValueAsBoolean()) return;

        // Reset activity on chat
        Player player = event.getPlayer();
        AFKManager.getInstance().recordActivity(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!Settings.AFK_EVENTS_MOVEMENT.getValueAsBoolean()) return;

        // Only count as movement if position changes, not just head rotation
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        // Reset activity on movement
        Player player = event.getPlayer();
        AFKManager.getInstance().recordActivity(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!Settings.AFK_EVENTS_INTERACT.getValueAsBoolean()) return;

        // Reset activity on interaction
        Player player = event.getPlayer();
        AFKManager.getInstance().recordActivity(player);
    }
}