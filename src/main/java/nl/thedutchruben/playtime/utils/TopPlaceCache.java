package nl.thedutchruben.playtime.utils;

import lombok.AllArgsConstructor;
import nl.thedutchruben.mccore.global.caching.CachingObject;
import nl.thedutchruben.playtime.Playtime;

import java.util.Date;

@AllArgsConstructor
public class TopPlaceCache extends CachingObject {
    private String key;
    private Date createDate = new Date();
    private String name, uuid;
    private Long time;

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Boolean isPersistent() {
        return false;
    }

    @Override
    public Date getCreateDate() {
        return createDate;
    }

    @Override
    public Date getExpireDate() {
        int minuteCache = Playtime.getInstance().getFileManager().getConfig("config.yml").get()
                .getInt("settings.cacheTime",5);
        return new Date(createDate.getTime() + (minuteCache * 60000L));
    }

    @Override
    public Object getData() {
        return new TopPlaceData(name, uuid, time);
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public Long getTime() {
        return time;
    }
}
