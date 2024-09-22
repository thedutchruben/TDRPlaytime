package nl.thedutchruben.playtime.modules.playtime_history.listeners;

import nl.thedutchruben.mccore.spigot.listeners.TDRListener;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.events.player.PlaytimePlayerLoadedEvent;
import nl.thedutchruben.playtime.core.events.player.PlaytimePlayerUnLoadedEvent;
import nl.thedutchruben.playtime.core.storage.Storage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@TDRListener
public class PlayTimePlayerLoadedListener implements Listener {

    @EventHandler
    public void onJoin(PlaytimePlayerLoadedEvent event){
        Playtime.getInstance().getStorage().updatePlaytimeHistory(event.getUser().getUUID(), Storage.Event.JOIN, (int) event.getUser().getTime());
    }

    @EventHandler
    public void onQuit(PlaytimePlayerUnLoadedEvent event){
        Playtime.getInstance().getStorage().updatePlaytimeHistory(event.getUser().getUUID(), Storage.Event.QUIT, (int) event.getUser().getTime());
    }
}