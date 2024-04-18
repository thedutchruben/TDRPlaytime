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



    public PlaytimeUser(String uuid, String name, long time) {
        this.uuid = uuid;
        this.name = name;
        this.time = time;
        this.lastChecked = System.currentTimeMillis();
    }

    public PlaytimeUser(String uuid, String name) {
        this(uuid,name,0);
    }

    public void updatePlaytime(){
        time = time + (System.currentTimeMillis() - lastChecked);
        Bukkit.getScheduler().runTaskAsynchronously(Playtime.getPlugin(),() -> Bukkit.getPluginManager().callEvent(new AsyncPlaytimePlayerUpdatePlaytimeEvent(this,true,time - (System.currentTimeMillis() - lastChecked),time)));
        lastChecked = System.currentTimeMillis();
    }

    public UUID getUUID(){
        return UUID.fromString(this.uuid);
    }

    public void addPlaytime(long time, TimeUnit timeUnit){
        this.time = this.time + timeUnit.toMillis(time);
        lastChecked = System.currentTimeMillis();
    }


    public CompletableFuture<Boolean> save(){
        return Playtime.getInstance().getStorage().saveUser(this);
    }

    public void removePlaytime(long time, TimeUnit timeUnit){
        this.time = this.time - timeUnit.toMillis(time);
        lastChecked = System.currentTimeMillis();
    }

    public void setPlaytime(float time){
        this.time = time;
    }


    public Player getBukkitPlayer(){
        return Bukkit.getPlayer(getUUID());
    }

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
