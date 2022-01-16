package nl.thedutchruben.playtime.listeners;

import lombok.SneakyThrows;
import nl.thedutchruben.mccore.listeners.TDRListener;
import nl.thedutchruben.playtime.Playtime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@TDRListener
public class PlayerJoinListener implements Listener {

    @SneakyThrows
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Playtime.getInstance().getStorage().getPlayTimeByUUID(event.getPlayer().getUniqueId().toString()).whenCompleteAsync((aLong, throwable) -> {
            Playtime.getInstance().getPlayerOnlineTime().put(event.getPlayer().getUniqueId(), aLong);
            Playtime.getInstance().getLastCheckedTime().put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        });

    }

}
