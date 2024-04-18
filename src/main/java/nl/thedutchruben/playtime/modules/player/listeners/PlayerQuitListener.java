package nl.thedutchruben.playtime.modules.player.listeners;


import com.google.gson.Gson;
import nl.thedutchruben.mccore.spigot.listeners.TDRListener;
import nl.thedutchruben.mccore.utils.config.FileManager;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.events.player.PlaytimePlayerSaveEvent;
import nl.thedutchruben.playtime.core.events.player.PlaytimePlayerUnLoadedEvent;
import nl.thedutchruben.playtime.core.objects.PlaytimeUser;
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
            PlaytimeUser user = Playtime.getInstance().getPlaytimeUsers().get(event.getPlayer().getUniqueId());
            user.updatePlaytime();
            Playtime.getInstance().getStorage().saveUser(Playtime.getInstance().getPlaytimeUsers().get(event.getPlayer().getUniqueId())).thenAcceptAsync(aBoolean -> {
                if(aBoolean){
                    Bukkit.getPluginManager().callEvent(new PlaytimePlayerUnLoadedEvent(Playtime.getInstance().getPlaytimeUsers().get(event.getPlayer().getUniqueId()),true));
                    Playtime.getInstance().getPlaytimeUsers().remove(event.getPlayer().getUniqueId());
                }else{
                    Playtime.getPlugin().getLogger().warning("Could not save the user " + event.getPlayer().getName() + " to the storage");
                    FileManager.Config config = Playtime.getInstance().getFileManager().getConfig("recover/" + event.getPlayer().getUniqueId() + ".yml");
                    config.set("data",new Gson().toJson(user));
                    config.save();
                }
            });
        });
    }
}
