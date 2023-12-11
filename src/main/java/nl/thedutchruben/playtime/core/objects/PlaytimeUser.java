package nl.thedutchruben.playtime.core.objects;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import nl.thedutchruben.playtime.core.events.player.AsyncPlaytimePlayerUpdatePlaytimeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
        Bukkit.getPluginManager().callEvent(new AsyncPlaytimePlayerUpdatePlaytimeEvent(this,time - (System.currentTimeMillis() - lastChecked),time));
        lastChecked = System.currentTimeMillis();
    }

    public void addPlaytime(float time){
        this.time = this.time + time;
    }

    public void removePlaytime(float time){
        this.time = this.time - time;
    }

    public void setPlaytime(float time){
        this.time = time;
    }


    public Player getBukkitPlayer(){
        return Bukkit.getPlayer(getUuid());
    }

    public int[] translateTime() {
        float tempTime = this.time;
        tempTime = tempTime / 1000;
        int days = (int) (tempTime / 86400);
        tempTime = tempTime - days * 86400L;
        int hours = (int) (tempTime / 3600);
        tempTime = tempTime - hours * 3600L;
        int minutes = (int) (time / 60);
        tempTime = tempTime - minutes * 60L;
        int seconds = (int) tempTime;
        return new int[]{days, hours, minutes, seconds};
    }
}
