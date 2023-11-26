package nl.thedutchruben.playtime.utils;

import lombok.AllArgsConstructor;
import nl.thedutchruben.mccore.global.caching.CachingObject;
import nl.thedutchruben.playtime.Playtime;

import java.util.Date;

/**
 * This class is used to store the data of the top places
 */
@AllArgsConstructor
public class TopPlaceCache extends CachingObject {
    /**
     * The key of the cache
     */
    private String key;
    /**
     * The date when the cache is created
     */
    private Date createDate = new Date();
    /**
     * The name and uuid of the player
     */
    private String name, uuid;
    /**
     * The time of the player
     */
    private Long time;

    /**
     * This method is used to get the key of the cache
     *
     * @return the key of the cache
     */
    @Override
    public String getKey() {
        return key;
    }

    /**
     * This method is used to check if the cache is persistent
     *
     * @return if the cache is persistent
     */
    @Override
    public Boolean isPersistent() {
        return false;
    }

    /**
     * This method is used to get the date when the cache is created
     *
     * @return the date when the cache is created
     */
    @Override
    public Date getCreateDate() {
        return createDate;
    }

    /**
     * This method is used to get the date when the cache is expired
     *
     * @return the date when the cache is expired
     */
    @Override
    public Date getExpireDate() {
        int minuteCache = Playtime.getInstance().getFileManager().getConfig("config.yml").get()
                .getInt("settings.cacheTime", 5);
        return new Date(createDate.getTime() + (minuteCache * 60000L));
    }

    /**
     * This method is used to get the data of the cache
     *
     * @return the data of the cache
     */
    @Override
    public Object getData() {
        return new TopPlaceData(name, uuid, time);
    }

    /**
     * This method is used to get the uuid of the player
     *
     * @return the uuid of the player
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * This method is used to get the name of the player
     *
     * @return the name of the player
     */
    public String getName() {
        return name;
    }

    /**
     * This method is used to get the time of the player
     *
     * @return the time of the player
     */
    public Long getTime() {
        return time;
    }
}
