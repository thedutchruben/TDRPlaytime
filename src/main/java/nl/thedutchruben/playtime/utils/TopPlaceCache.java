package nl.thedutchruben.playtime.utils;

import lombok.AllArgsConstructor;
import nl.thedutchruben.mccore.global.caching.CachingObject;

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
        return null;
    }

    @Override
    public Object getData() {
        return new TopPlaceData(name, uuid, time);
    }

}
