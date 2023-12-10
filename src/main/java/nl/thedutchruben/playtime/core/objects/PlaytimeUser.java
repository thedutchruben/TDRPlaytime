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
    public long time;
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

    public void addPlaytime(long time){
        this.time = this.time + time;
    }

    public void removePlaytime(long time){
        this.time = this.time - time;
    }

    public void setPlaytime(long time){
        this.time = time;
    }


    public Player getBukkitPlayer(){
        return Bukkit.getPlayer(getUuid());
    }

}
