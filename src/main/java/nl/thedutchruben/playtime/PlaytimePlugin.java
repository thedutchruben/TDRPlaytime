package nl.thedutchruben.playtime;

import org.bukkit.plugin.java.JavaPlugin;

public final class PlaytimePlugin extends JavaPlugin {
    private Playtime playtime;

    @Override
    public void onLoad() {
        DependencyLoader.load(this);
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        // Load dependencies
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
