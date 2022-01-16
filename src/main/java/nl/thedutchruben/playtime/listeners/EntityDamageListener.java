package nl.thedutchruben.playtime.listeners;


import nl.thedutchruben.mccore.listeners.TDRListener;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@TDRListener
public class EntityDamageListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager().getType() == EntityType.FIREWORK) {
            if (event.getDamager().getScoreboardTags().contains("tdrfirework")) {
                event.setCancelled(true);
            }
        }
    }

}
