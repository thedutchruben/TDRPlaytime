package nl.thedutchruben.playtime.database;

import nl.thedutchruben.playtime.milestone.Milestone;
import nl.thedutchruben.playtime.milestone.RepeatingMilestone;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public abstract class Storage {

    /**
     * Get the name of the storage type
     * @return The name of the storage type
     */
    public abstract String getName();

    /**
     * Setup the storage such as the database connection
     */
    public abstract boolean setup();

    /**
     * Stops the storage such things as the database connection
     */
    public abstract void stop();

    /**
     * @param uuid The {@link UUID} of the {@link org.bukkit.entity.Player}
     * @return A {@link CompletableFuture} with the time of the {@link org.bukkit.entity.Player}
     */
    public abstract CompletableFuture<Long> getPlayTimeByUUID(String uuid);

    /**
     * @param name The name as an {@link String} of the {@link org.bukkit.entity.Player}
     * @return A {@link CompletableFuture} with the time of the {@link org.bukkit.entity.Player}
     */
    public abstract CompletableFuture<Long> getPlayTimeByName(String name);

    /**
     * Save the players online time
     * @param uuid The {@link UUID} of the {@link org.bukkit.entity.Player}
     * @param playtime the players online time
     * @return Empty CompletableFuture
     */
    public abstract CompletableFuture<Void> savePlayTime(String uuid, long playtime);

    /**
     * Save the players online time
     * @param uuid The {@link UUID} of the {@link org.bukkit.entity.Player}
     * @param playtime the players online time
     * @return Empty CompletableFuture
     */
    public CompletableFuture<Void> savePlayTime(UUID uuid, long playtime){
        return savePlayTime(uuid.toString(),playtime);
    }

    /**
     * Get the top 10 list
     * @return A {@link CompletableFuture} with a {@link Map} with the {@link UUID} and the time of the {@link org.bukkit.entity.Player}
     */
    public abstract CompletableFuture<Map<String, Long>> getTopTenList();

    /**
     * Get the total playtime of all players
     * @return A {@link CompletableFuture} with the total playtime of all players
     */
    public abstract long getTotalPlayTime();

    /**
     * Get the total registered players
     * @return The total registered players
     */
    public abstract int getTotalPlayers();

    /**
     * Get the name of a top place
     * @param place The place
     * @return The name of the top place
     */
    public abstract String getTopPlace(int place);

    /**
     * Get the time of a top place
     * @param place The place of the player
     * @return The time of the top place
     */
    public abstract String getTopPlaceTime(int place);

    /**
     * Create a milestone
     * @param milestone The {@link Milestone} to create
     * @return Empty CompletableFuture
     */
    public abstract CompletableFuture<Void> createMilestone(Milestone milestone);

    /**
     * Save a milestone
     * @param milestone The {@link Milestone} to save
     * @return Empty CompletableFuture
     */
    public abstract CompletableFuture<Void> saveMileStone(Milestone milestone);

    /**
     * Get all milestones
     * @return A {@link CompletableFuture} with a {@link List} of {@link Milestone}
     */
    public abstract CompletableFuture<List<Milestone>> getMilestones();

    /**
     * Create a repeating milestone
     * @param milestone The {@link RepeatingMilestone} to create
     * @return Empty CompletableFuture
     */
    public abstract CompletableFuture<Void> createRepeatingMilestone(RepeatingMilestone milestone);

    /**
     * Save a repeating milestone
     * @param milestone The {@link RepeatingMilestone} to save
     * @return Empty CompletableFuture
     */
    public abstract CompletableFuture<Void> saveRepeatingMileStone(RepeatingMilestone milestone);

    /**
     * Get all repeating milestones
     * @return A {@link CompletableFuture} with a {@link List} of {@link RepeatingMilestone}
     */
    public abstract CompletableFuture<List<RepeatingMilestone>> getRepeatingMilestones();

    /**
     * Reset the playtime of a player
     * @param name The name of the {@link org.bukkit.entity.Player}
     * @return Empty CompletableFuture
     */
    public abstract CompletableFuture<Void> reset(String name);


    public Set<String> sortHashMapByValues(
            Map<String, Long> passedMap) {
        List<String> mapKeys = new ArrayList<>(passedMap.keySet());
        List<Long> mapValues = new ArrayList<>(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);
        Collections.reverse(mapValues);
        Collections.reverse(mapKeys);

        LinkedHashMap<String, Long> sortedMap =
                new LinkedHashMap<>();

        Iterator<Long> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            long val = valueIt.next();
            Iterator<String> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                String key = keyIt.next();
                Long comp1 = passedMap.get(key);

                if (comp1.equals(val)) {
                    keyIt.remove();
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap.keySet();
    }

}
