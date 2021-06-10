package nl.thedutchruben.playtime.database;

import nl.thedutchruben.playtime.milestone.Milestone;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class Storage {

    /**
     * Setup the storage such as the database connection
     */
    public abstract void setup();

    /**
     * Stops the storage such things as the database connection
     */
    public abstract void stop();

    /**
     *
     * @param uuid The {@link UUID} of the {@link org.bukkit.entity.Player}
     * @return A {@link CompletableFuture}
     */
    public abstract CompletableFuture<Long> getPlayTimeByUUID(String uuid);

    public abstract CompletableFuture<Long> getPlayTimeByName(String name);

    public abstract CompletableFuture<Void> savePlayTime(String uuid,long playtime);

    public abstract CompletableFuture< Map<String,Long>> getTopTenList();

    public abstract CompletableFuture<Void> createMilestone(Milestone milestone);

    public abstract CompletableFuture<Void> saveMileStone(Milestone milestone);

    public abstract CompletableFuture<List<Milestone>> getMilestones();

    public abstract CompletableFuture<Void> reset(String name);
}
