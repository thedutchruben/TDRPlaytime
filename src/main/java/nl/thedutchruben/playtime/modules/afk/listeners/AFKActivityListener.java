package nl.thedutchruben.playtime.modules.afk.listeners;

import nl.thedutchruben.mccore.spigot.listeners.TDRListener;
import nl.thedutchruben.playtime.core.Settings;
import nl.thedutchruben.playtime.core.afk.AFKManager;
import nl.thedutchruben.playtime.core.events.player.PlaytimePlayerLoadedEvent;
import nl.thedutchruben.playtime.core.events.player.PlaytimePlayerUnLoadedEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

@TDRListener
public class AFKActivityListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlaytimePlayerLoadedEvent event) {
        if (!Settings.AFK_ENABLED.getValueAsBoolean()) return;
        // Reset activity time when player joins
        AFKManager.getInstance().recordActivity(event.getUser().getBukkitPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlaytimePlayerUnLoadedEvent event) {
        if (!Settings.AFK_ENABLED.getValueAsBoolean()) return;
        // Remove player from AFK tracking when they quit
        AFKManager.getInstance().removePlayer(event.getUser().getUUID());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!Settings.AFK_ENABLED.getValueAsBoolean()) return;
        if (!Settings.AFK_EVENTS_CHAT.getValueAsBoolean()) return;

        // Reset activity on chat
        Player player = event.getPlayer();
        AFKManager.getInstance().recordActivity(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!Settings.AFK_ENABLED.getValueAsBoolean()) return;
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
        if (!Settings.AFK_ENABLED.getValueAsBoolean()) return;
        if (!Settings.AFK_EVENTS_INTERACT.getValueAsBoolean()) return;

        // Reset activity on interaction
        Player player = event.getPlayer();
        AFKManager.getInstance().recordActivity(player);
    }
}