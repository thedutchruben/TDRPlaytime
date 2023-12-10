package nl.thedutchruben.playtime.modules.player.runnables;

import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.objects.PlaytimeUser;

public class UpdatePlayTimeRunnable implements Runnable{

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
        for (PlaytimeUser value : Playtime.getInstance().getPlaytimeUsers().values()) {
            value.updatePlaytime();
        }
    }
}
