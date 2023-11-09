package nl.thedutchruben.playtime.listeners;

import nl.thedutchruben.mccore.spigot.listeners.TDRListener;
import nl.thedutchruben.playtime.Playtime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

@TDRListener
public class InteractListener implements Listener {
    private final boolean count = Playtime.getInstance().getFileManager().getConfig("config.yml").get()
            .getBoolean("settings.afk.countAfkTime");
    private final boolean interactReset = Playtime.getInstance().getFileManager().getConfig("config.yml").get()
            .getBoolean("settings.afk.events.interactResetAfkTime");

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!count) {
            if (interactReset) {
                Playtime.getInstance().forceSave(event.getPlayer().getUniqueId());
            }
        }
    }
}
