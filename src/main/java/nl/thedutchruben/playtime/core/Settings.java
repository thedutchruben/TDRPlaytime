package nl.thedutchruben.playtime.core;

import lombok.Getter;
import nl.thedutchruben.playtime.Playtime;
import org.bukkit.configuration.file.YamlConfiguration;

@Getter
public enum Settings {

    UPDATE_CHECK("settings.update-check", true, 1.0, ConfigFiles.CONFIG),
    CACHE_TIME("settings.cache-time", 5, 1.0, ConfigFiles.CONFIG),
    TOP_10_PLACEHOLDER_CACHE_TIME("settings.top_10_placeholder_cache_time", 600, 1.0, ConfigFiles.CONFIG),
    STORAGE_TYPE("type", "sqllite", 1.0, ConfigFiles.STORAGE),

    STORAGE_MYSQL_HOST("mysql.hostname", "localhost", 1.0, ConfigFiles.STORAGE),
    STORAGE_MYSQL_PORT("mysql.port", 3306, 1.0, ConfigFiles.STORAGE),
    STORAGE_MYSQL_USERNAME("mysql.username", "username", 1.0, ConfigFiles.STORAGE),
    STORAGE_MYSQL_PASSWORD("mysql.password", "password", 1.0, ConfigFiles.STORAGE),
    STORAGE_MYSQL_SSL("mysql.ssl", true, 1.0, ConfigFiles.STORAGE),
    STORAGE_MYSQL_SCHEMA("mysql.schema", "playtime", 1.0, ConfigFiles.STORAGE),
    STORAGE_MYSQL_PREFIX("mysql.prefix", "", 1.0, ConfigFiles.STORAGE),
    STORAGE_MYSQL_POOL("mysql.pool", 20, 1.0, ConfigFiles.STORAGE),
    STORAGE_MYSQL_DRIVER("mysql.driver", "jdbc:mysql://", 1.0, ConfigFiles.STORAGE),

    STORAGE_MONGO_HOST("mongo.hostname", "localhost", 1.0, ConfigFiles.STORAGE),
    STORAGE_MONGO_PORT("mongo.port", 27017, 1.0, ConfigFiles.STORAGE),
    STORAGE_MONGO_USERNAME("mongo.username", "username", 1.0, ConfigFiles.STORAGE),
    STORAGE_MONGO_PASSWORD("mongo.password", "password", 1.0, ConfigFiles.STORAGE),
    STORAGE_MONGO_COLLECTION("mongo.collection", "playtime", 1.0, ConfigFiles.STORAGE),

    AFK_COUNT_TIME("settings.afk.countAfkTime", true, 1.0, ConfigFiles.CONFIG),
    AFK_USE_ESSENTIALS_API("settings.afk.useEssentialsApi", false, 1.0, ConfigFiles.CONFIG),
    AFK_THRESHOLD_MINUTES("settings.afk.thresholdMinutes", 5, 1.0, ConfigFiles.CONFIG),
    AFK_BROADCAST_MESSAGES("settings.afk.broadcastMessages", true, 1.0, ConfigFiles.CONFIG),
    AFK_BROADCAST_TO_ALL("settings.afk.broadcastToAll", false, 1.0, ConfigFiles.CONFIG),
    AFK_EVENTS_CHAT("settings.afk.events.chatResetAfkTime", true, 1.0, ConfigFiles.CONFIG),
    AFK_EVENTS_MOVEMENT("settings.afk.events.movementResetAfkTime", true, 1.0, ConfigFiles.CONFIG),
    AFK_EVENTS_INTERACT("settings.afk.events.interactResetAfkTime", true, 1.0, ConfigFiles.CONFIG),
    AFK_KICK_ENABLED("settings.afk.kick.enabled", false, 1.0, ConfigFiles.CONFIG),
    AFK_KICK_THRESHOLD_MINUTES("settings.afk.kick.thresholdMinutes", 30, 1.0, ConfigFiles.CONFIG),
    AFK_KICK_MESSAGE("settings.afk.kick.message", "&cYou have been kicked for being AFK too long.", 1.0, ConfigFiles.CONFIG),
    ;

    private final String path;
    private final Object defaultValue;
    private final String fileName;
    private final double version;

    Settings(String path, Object defaultValue, double version, ConfigFiles configFile) {
        this.path = path;
        this.defaultValue = defaultValue;
        this.fileName = configFile.getFileName();
        this.version = version;
    }

    /**
     * Set up the default config
     */
    public static void setupDefaults() {
        for (Settings value : Settings.values()) {
            YamlConfiguration yamlConfiguration = Playtime.getInstance().getFileManager().getConfig(value.getFileName()).get();
            if (yamlConfiguration.get(value.getPath()) == null) {
                yamlConfiguration.set(value.getPath(), value.getDefaultValue());
                Playtime.getInstance().getFileManager().getConfig(value.getFileName()).save();
            }
        }
    }

    /**
     * Get the config
     *
     * @param fileName the file name
     * @return the config
     */
    public YamlConfiguration getConfig(String fileName) {
        return Playtime.getInstance().getFileManager().getConfig(fileName).get();
    }

    public Object getValue() {
        return getConfig(this.fileName).get(path, defaultValue);
    }

    public String getValueAsString() {
        return getConfig(this.fileName).getString(path, (String) defaultValue);
    }

    public Boolean getValueAsBoolean() {
        return getConfig(this.fileName).getBoolean(path, (Boolean) defaultValue);
    }

    public Integer getValueAsInteger() {
        return getConfig(this.fileName).getInt(path, (Integer) defaultValue);
    }
}