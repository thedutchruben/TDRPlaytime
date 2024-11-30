package nl.thedutchruben.playtime.modules.player.runnables;

import nl.thedutchruben.mccore.spigot.runnables.ASyncRepeatingTask;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.objects.PlaytimeUser;

import java.util.logging.Level;

/**
 * Save the playtime of the players every 5 minutes
 */
@ASyncRepeatingTask(repeatTime = 6000, startTime = 6000)
public class SavePlayTimeRunnable implements Runnable {

    /**
     * When an object implementing interface {@code Runnable} is used
     * to create a thread, starting the thread causes the object's
     * {@code run} method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method {@code run} is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        if (Playtime.getInstance().getPlaytimeUsers().isEmpty()) return;
        Playtime.getPlugin().getLogger().log(Level.INFO, "Saving playtime...");
        for (PlaytimeUser value : Playtime.getInstance().getPlaytimeUsers().values()) {
            Playtime.getInstance().getStorage().saveUser(value);
        }
        Playtime.getPlugin().getLogger().log(Level.INFO, "Saved playtime of {0} players", Playtime.getInstance().getPlaytimeUsers().size());
    }
}