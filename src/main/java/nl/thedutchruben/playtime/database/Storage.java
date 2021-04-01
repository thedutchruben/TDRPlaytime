package nl.thedutchruben.playtime.database;

import nl.thedutchruben.playtime.milestone.Milestone;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class Storage {

    public abstract void setup();

    public abstract void stop();

    public abstract CompletableFuture<Long> getPlayTimeByUUID(String uuid);

    public abstract CompletableFuture<Long> getPlayTimeByName(String name);

    public abstract CompletableFuture<Void> savePlayTime(String uuid,long playtime);

    public abstract CompletableFuture< Map<String,Long>> getTopTenList();

    public abstract CompletableFuture<Void> createMilestone(Milestone milestone);

    public abstract CompletableFuture<Void> saveMileStone(Milestone milestone);

    public abstract CompletableFuture<List<Milestone>> getMilestones();

    public abstract CompletableFuture<Void> reset(String name);
}
