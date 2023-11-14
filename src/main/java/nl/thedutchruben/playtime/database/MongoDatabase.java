package nl.thedutchruben.playtime.database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.SneakyThrows;
import nl.thedutchruben.mccore.utils.JarLoader;
import nl.thedutchruben.mccore.utils.config.FileManager;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.milestone.Milestone;
import nl.thedutchruben.playtime.milestone.RepeatingMilestone;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class MongoDatabase extends Storage {


    /**
     * Get the name of the storage type
     *
     * @return The name of the storage type
     */
    @Override
    public String getName() {
        return null;
    }

    /**
     * Setup the storage such as the database connection
     */
    @SneakyThrows
    @Override
    public boolean setup() {
        // download file
    // LOOKK AT THIS https://github.com/LuckPerms/LuckPerms/blob/master/common/loader-utils/src/main/java/me/lucko/luckperms/common/loader/JarInJarClassLoader.java#L48
        Playtime.getInstance().getLogger().log(Level.INFO,"Downloading mongodb driver...");
        URL website = new URL("https://repo1.maven.org/maven2/org/mongodb/mongo-java-driver/3.12.14/mongo-java-driver-3.12.14.jar");
        InputStream in = website.openStream();
        String fileName = Playtime.getInstance().getDataFolder().getAbsolutePath() + "/libs/" + "mongo-java-driver-3.12.14.jar";
        Files.copy(in, Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);
        Playtime.getInstance().getLogger().log(Level.INFO,"Mongodb driver downloaded");

//        new JarLoader().load(new File(fileName), MongoClients.class);
        MongoClient mongoClient = MongoClients.create("mongodb://localhost");

        return true;
    }

    /**
     * Stops the storage such things as the database connection
     */
    @Override
    public void stop() {

    }

    /**
     * @param uuid The {@link UUID} of the {@link Player}
     * @return A {@link CompletableFuture} with the time of the
     * {@link Player}
     */
    @Override
    public CompletableFuture<Long> getPlayTimeByUUID(String uuid) {
        return null;
    }

    /**
     * @param name The name as an {@link String} of the
     *             {@link Player}
     * @return A {@link CompletableFuture} with the time of the
     * {@link Player}
     */
    @Override
    public CompletableFuture<Long> getPlayTimeByName(String name) {
        return null;
    }

    /**
     * Save the players online time
     *
     * @param uuid     The {@link UUID} of the {@link Player}
     * @param playtime the players online time
     * @return Empty CompletableFuture
     */
    @Override
    public CompletableFuture<Boolean> savePlayTime(String uuid, long playtime) {
        return null;
    }

    /**
     * Get the top 10 list
     *
     * @return A {@link CompletableFuture} with a {@link Map} with the {@link UUID}
     * and the time of the {@link Player}
     */
    @Override
    public CompletableFuture<Map<String, Long>> getTopTenList() {
        return null;
    }

    /**
     * Get the total playtime of all players
     *
     * @return A {@link CompletableFuture} with the total playtime of all players
     */
    @Override
    public long getTotalPlayTime() {
        return 0;
    }

    /**
     * Get the total registered players
     *
     * @return The total registered players
     */
    @Override
    public int getTotalPlayers() {
        return 0;
    }

    /**
     * Get the name of a top place
     *
     * @param place The place
     * @return The name of the top place
     */
    @Override
    public String getTopPlace(int place) {
        return null;
    }

    /**
     * Get the time of a top place
     *
     * @param place The place of the player
     * @return The time of the top place
     */
    @Override
    public String getTopPlaceTime(int place) {
        return null;
    }

    /**
     * Create a milestone
     *
     * @param milestone The {@link Milestone} to create
     * @return Empty CompletableFuture
     */
    @Override
    public CompletableFuture<Boolean> createMilestone(Milestone milestone) {
        return null;
    }

    /**
     * Save a milestone
     *
     * @param milestone The {@link Milestone} to save
     * @return Empty CompletableFuture
     */
    @Override
    public CompletableFuture<Boolean> saveMileStone(Milestone milestone) {
        return null;
    }

    /**
     * Remove a milestone
     *
     * @param milestone The {@link Milestone} to remove
     * @return Empty CompletableFuture
     */
    @Override
    public CompletableFuture<Boolean> removeMileStone(Milestone milestone) {
        return null;
    }

    /**
     * Get all milestones
     *
     * @return A {@link CompletableFuture} with a {@link List} of {@link Milestone}
     */
    @Override
    public CompletableFuture<List<Milestone>> getMilestones() {
        return null;
    }

    /**
     * Create a repeating milestone
     *
     * @param milestone The {@link RepeatingMilestone} to create
     * @return Empty CompletableFuture
     */
    @Override
    public CompletableFuture<Boolean> createRepeatingMilestone(RepeatingMilestone milestone) {
        return null;
    }

    /**
     * Save a repeating milestone
     *
     * @param milestone The {@link RepeatingMilestone} to save
     * @return Empty CompletableFuture
     */
    @Override
    public CompletableFuture<Boolean> saveRepeatingMileStone(RepeatingMilestone milestone) {
        return null;
    }

    /**
     * Remove a repeating milestone
     *
     * @param milestone The {@link RepeatingMilestone} to remove
     * @return Empty CompletableFuture
     */
    @Override
    public CompletableFuture<Boolean> removeRepeatingMileStone(RepeatingMilestone milestone) {
        return null;
    }

    /**
     * Get all repeating milestones
     *
     * @return A {@link CompletableFuture} with a {@link List} of
     * {@link RepeatingMilestone}
     */
    @Override
    public CompletableFuture<List<RepeatingMilestone>> getRepeatingMilestones() {
        return null;
    }

    /**
     * Reset the playtime of a player
     *
     * @param name The name of the {@link Player}
     * @return Empty CompletableFuture
     */
    @Override
    public CompletableFuture<Boolean> reset(String name) {
        return null;
    }
}
