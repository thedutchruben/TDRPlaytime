package nl.thedutchruben.playtime.core.objects;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.events.player.AsyncPlaytimePlayerUpdatePlaytimeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Getter
public class PlaytimeUser {

    @SerializedName("_id")
    public String uuid;
    public String name;
    public float time;
    private transient long lastChecked;

    /**
     * Constructs a PlaytimeUser with the specified UUID, name, and playtime.
     *
     * @param uuid The UUID of the user.
     * @param name The name of the user.
     * @param time The initial playtime of the user in milliseconds.
     */
    public PlaytimeUser(String uuid, String name, long time) {
        this.uuid = uuid;
        this.name = name;
        this.time = time;
        this.lastChecked = System.currentTimeMillis();
    }

    /**
     * Constructs a PlaytimeUser with the specified UUID and name.
     * The initial playtime is set to 0.
     *
     * @param uuid The UUID of the user.
     * @param name The name of the user.
     */
    public PlaytimeUser(String uuid, String name) {
        this(uuid, name, 0);
    }

    /**
     * Loads a PlaytimeUser asynchronously based on the specified UUID.
     *
     * @param uuid The UUID of the user to load.
     * @return A CompletableFuture that completes with the loaded PlaytimeUser.
     */
    public static CompletableFuture<PlaytimeUser> loadUser(UUID uuid) {
        return Playtime.getInstance().getStorage().loadUser(uuid);
    }

    /**
     * Updates the playtime of the user based on the time elapsed since the last check.
     * Fires an AsyncPlaytimePlayerUpdatePlaytimeEvent asynchronously.
     */
    public void updatePlaytime() {
        float oldTime = time;
        time = time + (System.currentTimeMillis() - lastChecked);
        Bukkit.getScheduler().runTaskAsynchronously(Playtime.getPlugin(),
                () -> Bukkit.getPluginManager().callEvent(new AsyncPlaytimePlayerUpdatePlaytimeEvent(this, true, oldTime, time)));
        lastChecked = System.currentTimeMillis();
    }

    /**
     * Returns the UUID of the user.
     *
     * @return The UUID of the user.
     */
    public UUID getUUID() {
        return UUID.fromString(this.uuid);
    }

    /**
     * Adds the specified amount of playtime to the user's total playtime.
     *
     * @param time     The amount of time to add.
     * @param timeUnit The unit of time.
     */
    public void addPlaytime(long time, TimeUnit timeUnit) {
        this.time = this.time + timeUnit.toMillis(time);
        lastChecked = System.currentTimeMillis();
    }

    /**
     * Saves the user's playtime data asynchronously.
     *
     * @return A CompletableFuture that completes when the save operation is done.
     */
    public CompletableFuture<Boolean> save() {
        return Playtime.getInstance().getStorage().saveUser(this);
    }

    /**
     * Removes the specified amount of playtime from the user's total playtime.
     *
     * @param time     The amount of time to remove.
     * @param timeUnit The unit of time.
     */
    public void removePlaytime(long time, TimeUnit timeUnit) {
        this.time = this.time - timeUnit.toMillis(time);
        lastChecked = System.currentTimeMillis();
    }

    /**
     * Sets the user's total playtime to the specified value.
     *
     * @param time The new total playtime in milliseconds.
     */
    public void setPlaytime(float time) {
        this.time = time;
    }

    /**
     * Returns the Bukkit Player object associated with the user.
     *
     * @return The Bukkit Player object.
     */
    public Player getBukkitPlayer() {
        return Bukkit.getPlayer(getUUID());
    }

    /**
     * Translates the user's total playtime into an array of days, hours, minutes, and seconds.
     *
     * @return An array containing the days, hours, minutes, and seconds of playtime.
     */
    public int[] translateTime() {
        float tempTime = this.time;
        tempTime = tempTime / 1000;
        int days = (int) (tempTime / 86400);
        tempTime = tempTime - days * 86400L;
        int hours = (int) (tempTime / 3600);
        tempTime = tempTime - hours * 3600L;
        int minutes = (int) (tempTime / 60);
        tempTime = tempTime - minutes * 60L;
        int seconds = (int) tempTime;
        return new int[]{days, hours, minutes, seconds};
    }
}
