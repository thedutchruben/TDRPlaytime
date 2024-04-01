package nl.thedutchruben.playtime.modules.player.listeners;


import nl.thedutchruben.mccore.spigot.listeners.TDRListener;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.events.player.PlaytimePlayerSaveEvent;
import nl.thedutchruben.playtime.core.events.player.PlaytimePlayerUnLoadedEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

@TDRListener
public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        Bukkit.getPluginManager().callEvent(new PlaytimePlayerSaveEvent(Playtime.getInstance().getPlaytimeUsers().get(event.getPlayer().getUniqueId())));
        Bukkit.getScheduler().runTaskAsynchronously(Playtime.getPlugin(),() -> {
            Playtime.getInstance().getPlaytimeUsers().get(event.getPlayer().getUniqueId()).updatePlaytime();
            Playtime.getInstance().getStorage().saveUser(Playtime.getInstance().getPlaytimeUsers().get(event.getPlayer().getUniqueId())).thenAcceptAsync(aBoolean -> {
                if(aBoolean){
                    Bukkit.getPluginManager().callEvent(new PlaytimePlayerUnLoadedEvent(Playtime.getInstance().getPlaytimeUsers().get(event.getPlayer().getUniqueId()),true));
                    Playtime.getInstance().getPlaytimeUsers().remove(event.getPlayer().getUniqueId());
                }else{
                    Playtime.getPlugin().getLogger().warning("Could not save the user " + event.getPlayer().getName() + " to the storage");
                    //todo save to file system as backup
                }
            });
        });
    }
}
