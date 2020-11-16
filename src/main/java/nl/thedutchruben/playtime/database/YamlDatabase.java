package nl.thedutchruben.playtime.database;

import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.utils.FileManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class YamlDatabase extends Storage{
    @Override
    public void setup() {
    }

    @Override
    public void stop() {
    }

    @Override
    public long getPlayTimeByUUID(String uuid) {
        return Playtime.getInstance().getFileManager().getConfig("players/" + uuid + ".yaml").get().getLong("onlinetime",0);
    }

    @Override
    public long getPlayTimeByName(String name) {
        return Playtime.getInstance().getFileManager().getConfig("players/" + Bukkit.getOfflinePlayer(name).getUniqueId().toString() + ".yaml").get().getLong("onlinetime",0);
    }

    @Override
    public void savePlayTime(String uuid, long playtime) {
        Playtime.getInstance().getFileManager().getConfig("players/" + uuid + ".yaml").get().set("onlinetime",playtime);
        Playtime.getInstance().getFileManager().getConfig("players/" + uuid + ".yaml").save();
    }

    @Override
    public Map<String, Long> getTopTenList() {
        Map<String,Long> hashMap = new HashMap<>();
        for (final File fileEntry : Objects.requireNonNull(new File(Playtime.getInstance().getDataFolder(), "players/").listFiles())) {
            YamlConfiguration config = Playtime.getInstance().getFileManager().getConfig("players/" + fileEntry.getName().replace(".yaml","") + ".yaml").get();
            if(config != null){
                if(config.contains("onlinetime")){
                    hashMap.put(Bukkit.getPlayer
                                    (UUID.fromString(fileEntry.getName().replace(".yaml",""))).getName(),
                            config.getLong("onlinetime"));
                }
            }
        }
        return hashMap;
    }

    @Override
    public void reset(String uuid) {
        Playtime.getInstance().getFileManager().getConfig("players/" + uuid + ".yaml").get().set("onlinetime",0);
        Playtime.getInstance().getFileManager().getConfig("players/" + uuid + ".yaml").save();
    }
}
