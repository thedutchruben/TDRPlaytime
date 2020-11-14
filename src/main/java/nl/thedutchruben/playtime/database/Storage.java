package nl.thedutchruben.playtime.database;

import java.util.Map;

public abstract class Storage {

    public abstract void setup();

    public abstract void stop();

    public abstract long getPlayTimeByUUID(String uuid);

    public abstract long getPlayTimeByName(String name);

    public abstract void savePlayTime(String uuid,long playtime);

    public abstract Map<String,Long> getTopTenList();

    public abstract void reset(String uuid);
}
