package nl.thedutchruben.playtime;

import nl.thedutchruben.playtime.core.DependencyLoader;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main class of the plugin
 * Tdrplaytime is the playtime plugin for tracking the playtime of players and rewarding them!
 *
 * @version 1.0.0
 * @see JavaPlugin
 * @since 1.0.0
 */
public class PlayTimePlugin extends JavaPlugin {
    private Playtime playtime;

    @Override
    public void onLoad() {
        // Load the dependencies of the plugin
        // the dependencies will load by downloading the jar from the maven repository
        // this will keep the download size of the plugin low
        DependencyLoader.load(this);
    }

    @Override
    public void onEnable() {
        playtime = new Playtime(this);
        playtime.onEnable(this);
    }

    @Override
    public void onDisable() {
        if (playtime != null) {
            playtime.onDisable();
        }
    }
}