package nl.thedutchruben.playtime.core;


import lombok.Getter;
import nl.thedutchruben.playtime.Playtime;
import org.bukkit.configuration.file.YamlConfiguration;

@Getter
public enum Settings {
    UPDATE_CHECK("settings.update-check",true,1.0,"config.yml"),
    LANGUAGE("language","en_GB",1.0,"config.yml"),
    CACHE_TIME("settings.cache-time",5,1.0,"config.yml"),
    AFK_COUNT_TIME("settings.afk.countAfkTime",true,1.0,"config.yml"),
    AFK_USE_ESSENTIALS_API("settings.afk.useEssentialsApi",false,1.0,"config.yml"),
    AFK_EVENTS_CHAT("settings.afk.events.chatResetAfkTime",true,1.0,"config.yml"),
    TOP_10_PLACEHOLDER_CACHE_TIME("settings.top_10_placeholdler_cache_time",600,1.0,"config.yml"),
    STORAGE_TYPE("type","sqllite",1.0,"storage.yml");

    private final String path;
    private final Object defaultValue;
    private final String fileName;


    public YamlConfiguration getConfig(String fileName){
        return Playtime.getInstance().getFileManager().getConfig(fileName).get();
    }

    Settings(String path, Object defaultValue,double version,String fileName) {
        this.path = path;
        this.defaultValue = defaultValue;
        this.fileName = fileName;
    }



    public Object getValue(){
        return getConfig(this.fileName).get(path,defaultValue);
    }

    public Object getValueAsString(){
        return getConfig(this.fileName).getString(path,(String)defaultValue);
    }
}
