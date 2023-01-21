package nl.thedutchruben.playtime.listeners;

import nl.thedutchruben.mccore.spigot.listeners.TDRListener;
import nl.thedutchruben.playtime.Playtime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

@TDRListener
public class OnChatListener implements Listener {
    private boolean count = Playtime.getInstance().getFileManager().getConfig("config.yml").get()
            .getBoolean("settings.afk.countAfkTime");
    private boolean chatReset = Playtime.getInstance().getFileManager().getConfig("config.yml").get()
            .getBoolean("settings.afk.events.chatResetAfkTime");

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!count) {
            if (chatReset) {
                Playtime.getInstance().forceSave(event.getPlayer().getUniqueId());
            }
        }
    }

    @EventHandler
    public void onChat(PlayerCommandPreprocessEvent event) {
        if (!count) {
            if (chatReset) {
                Playtime.getInstance().forceSave(event.getPlayer().getUniqueId());
            }
        }
    }
}
