package nl.thedutchruben.playtime.modules.player.listeners;


import nl.thedutchruben.mccore.spigot.listeners.TDRListener;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.events.player.PlaytimePlayerLoadedEvent;
import nl.thedutchruben.playtime.core.objects.PlaytimeUser;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@TDRListener
public class PlayerJoinListener  implements Listener{

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Playtime.getInstance().getStorage().loadUser(event.getPlayer().getUniqueId()).thenAcceptAsync(playtimeUser -> {
            if(playtimeUser != null){
                Playtime.getInstance().getPlaytimeUsers().put(event.getPlayer().getUniqueId(),playtimeUser);
            }else{
                PlaytimeUser playtimeUser1 = new PlaytimeUser(event.getPlayer().getUniqueId().toString(),event.getPlayer().getName());
                Playtime.getInstance().getStorage().createUser(playtimeUser1);
                Playtime.getInstance().getPlaytimeUsers().put(event.getPlayer().getUniqueId(),playtimeUser1);
            }
            System.out.println(Playtime.getInstance().getPlaytimeUsers().get(event.getPlayer().getUniqueId()).getName());
            Bukkit.getPluginManager().callEvent(new PlaytimePlayerLoadedEvent(Playtime.getInstance().getPlaytimeUsers().get(event.getPlayer().getUniqueId()),true));
        });
    }
}
