package nl.thedutchruben.playtime.database;

import nl.thedutchruben.playtime.milestone.Milestone;
import nl.thedutchruben.playtime.milestone.RepeatingMilestone;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public abstract class Storage {

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

    public CompletableFuture<Void> savePlayTime(UUID uuid, long playtime){
        return savePlayTime(uuid.toString(),playtime);
    }

    public abstract CompletableFuture<Map<String, Long>> getTopTenList();

    public abstract long getTotalPlayTime();

    public abstract int getTotalPlayers();

    public abstract String getTopPlace(int place);

    public abstract CompletableFuture<Void> createMilestone(Milestone milestone);

    public abstract CompletableFuture<Void> saveMileStone(Milestone milestone);

    public abstract CompletableFuture<List<Milestone>> getMilestones();

    public abstract CompletableFuture<Void> createRepeatingMilestone(RepeatingMilestone milestone);

    public abstract CompletableFuture<Void> saveRepeatingMileStone(RepeatingMilestone milestone);

    public abstract CompletableFuture<List<RepeatingMilestone>> getRepeatingMilestones();

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
