package nl.thedutchruben.playtime.listeners;

import nl.thedutchruben.mccore.spigot.listeners.TDRListener;
import nl.thedutchruben.playtime.Playtime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

@TDRListener
public class InventoryClickListener implements Listener {
    private final boolean count = Playtime.getInstance().getFileManager().getConfig("config.yml").get()
            .getBoolean("settings.afk.countAfkTime");
    private final boolean clickReset = Playtime.getInstance().getFileManager().getConfig("config.yml").get()
            .getBoolean("settings.afk.events.inventoryClickResetAfkTime");

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!count) {
            if (clickReset) {
                Playtime.getInstance().forceSave(event.getWhoClicked().getUniqueId());
            }
        }
    }
}
