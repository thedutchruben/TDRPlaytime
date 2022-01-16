package nl.thedutchruben.playtime.listeners;

import nl.thedutchruben.mccore.listeners.TDRListener;
import nl.thedutchruben.playtime.Playtime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

@TDRListener
public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Playtime.getInstance().update(event.getPlayer().getUniqueId(), true);
        Playtime.getInstance().getPlayerOnlineTime().remove(event.getPlayer().getUniqueId());
        Playtime.getInstance().getLastCheckedTime().remove(event.getPlayer().getUniqueId());
    }
}
