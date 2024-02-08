package nl.thedutchruben.playtime.modules.milestones.listeners;

import nl.thedutchruben.mccore.spigot.listeners.TDRListener;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.events.player.AsyncPlaytimePlayerUpdatePlaytimeEvent;
import nl.thedutchruben.playtime.core.objects.RepeatingMilestone;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@TDRListener
public class UpdatePlaytimeListener implements Listener {

    @EventHandler
    public void updatePlaytime(AsyncPlaytimePlayerUpdatePlaytimeEvent event){
        if(!Playtime.getInstance().getMilestones().isEmpty()){
            Playtime.getInstance().getMilestones().forEach(milestone -> {
                if(milestone.getOnlineTime() <= event.getNewPlaytime() && milestone.getOnlineTime() > event.getOldPlaytime()){
                    milestone.apply(event.getPlayer().getBukkitPlayer());
                }
            });
        }

        if(!Playtime.getInstance().getRepeatingMilestones().isEmpty()){
            for (float i = event.getOldPlaytime(); i < event.getNewPlaytime(); i++) {
                if (i > 0) {
                    for (RepeatingMilestone repeatingMilestone : Playtime.getInstance().getRepeatingMilestones()) {
                        if (i % (repeatingMilestone.getOnlineTime() * 1000) == 1) {
                            repeatingMilestone.apply(event.getPlayer().getBukkitPlayer());
                        }
                    }
                }
            }
        }

    }
}
