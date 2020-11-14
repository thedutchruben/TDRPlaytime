package nl.thedutchruben.playtime.listeners;

import nl.thedutchruben.playtime.Playtime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        long onlineTime = Playtime.getInstance().getStorage().getPlayTimeByUUID(event.getPlayer().getUniqueId().toString());
        Playtime.getInstance().getPlayerOnlineTime().put(event.getPlayer().getUniqueId(),onlineTime);
        Playtime.getInstance().getLastCheckedTime().put(event.getPlayer().getUniqueId(),System.currentTimeMillis());
    }
}
