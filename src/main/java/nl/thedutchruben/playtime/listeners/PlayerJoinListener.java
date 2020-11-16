package nl.thedutchruben.playtime.listeners;

import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.utils.UpdateChecker;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerJoinListener implements Listener {
    private List<UUID> reported = new ArrayList<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        long onlineTime = Playtime.getInstance().getStorage().getPlayTimeByUUID(event.getPlayer().getUniqueId().toString());
        Playtime.getInstance().getPlayerOnlineTime().put(event.getPlayer().getUniqueId(),onlineTime);
        Playtime.getInstance().getLastCheckedTime().put(event.getPlayer().getUniqueId(),System.currentTimeMillis());

        if(event.getPlayer().isOp()){
            if(!reported.contains(event.getPlayer().getUniqueId())) {
                new UpdateChecker(Playtime.getInstance(), 47894).getVersion(version -> {
                    if (!Playtime.getInstance().getDescription().getVersion().equalsIgnoreCase(version)) {
                        event.getPlayer().sendMessage(ChatColor.GOLD + "There is a new update available of TDRPlaytime.");
                        event.getPlayer().sendMessage(ChatColor.GOLD + "Download it here https://www.spigotmc.org/resources/tdrplaytime.47894/");
                    }
                });
                reported.add(event.getPlayer().getUniqueId());
            }
        }
    }
}
