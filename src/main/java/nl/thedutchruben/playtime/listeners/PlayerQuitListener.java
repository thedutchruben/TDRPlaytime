package nl.thedutchruben.playtime.listeners;

import nl.thedutchruben.playtime.Playtime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        Playtime.getInstance().update(event.getPlayer().getUniqueId());
        Playtime.getInstance().getPlayerOnlineTime().remove(event.getPlayer().getUniqueId());
        Playtime.getInstance().getLastCheckedTime().remove(event.getPlayer().getUniqueId());
    }
}
