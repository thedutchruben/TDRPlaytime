package nl.thedutchruben.playtime.database;

public abstract class Storage {

    public abstract void setup();

    public abstract void stop();

    public abstract Long getPlayTimeByUUID(String uuid);

    public abstract Long getPlayTimeByName(String name);

    public abstract void savePlayTime(String uuid);

}
