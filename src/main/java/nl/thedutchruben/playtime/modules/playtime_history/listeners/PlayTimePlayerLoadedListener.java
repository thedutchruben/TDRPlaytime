package nl.thedutchruben.playtime.modules.playtime_history.listeners;

import nl.thedutchruben.mccore.spigot.listeners.TDRListener;
import nl.thedutchruben.playtime.core.events.player.PlaytimePlayerLoadedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@TDRListener
public class PlayTimePlayerLoadedListener implements Listener {

    @EventHandler
    public void onJoin(PlaytimePlayerLoadedEvent event){

    }

}
