package nl.thedutchruben.playtime.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import lombok.SneakyThrows;
import nl.thedutchruben.mccore.utils.config.FileManager;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.milestone.Milestone;
import nl.thedutchruben.playtime.milestone.RepeatingMilestone;
import nl.thedutchruben.playtime.utils.ConfigurationSerializableAdapter;
import nl.thedutchruben.playtime.utils.ItemStackAdapter;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class MongoDatabase extends Storage {
    private MongoClient mongoClient;
    private com.mongodb.client.MongoDatabase database;

    private Gson gson;

    /**
     * Get the name of the storage type
     *
     * @return The name of the storage type
     */
    @Override
    public String getName() {
        return "MongoDB";
    }

    /**
     * Setup the storage such as the database connection
     */
    @SneakyThrows
    @Override
    public boolean setup() {
        YamlConfiguration databaseConfig =  Playtime.getInstance().getFileManager().getConfig("database.yml").get();
        // build the connection string from config values
        String connectionString = "mongodb://";
        if (!Objects.equals(databaseConfig.getString("mongodb.user"), "") && databaseConfig.getString("mongodb.user") != null && !Playtime.getPluginInstance().getConfig().getString("mongodb.user").isEmpty()) {
            connectionString += databaseConfig.getString("mongodb.user") + ":" + databaseConfig.getString("mongodb.password") + "@";
        }
        connectionString += databaseConfig.getString("mongodb.hostname") + ":" + databaseConfig.getInt("mongodb.port");
        connectionString += "/" + databaseConfig.getString("mongodb.collection");
        this.mongoClient = MongoClients.create(connectionString);
        //Check if the database is valid
        this.database = this.mongoClient.getDatabase(databaseConfig.getString("mongodb.collection"));

        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .setObjectToNumberStrategy(JsonReader::nextInt)
                .registerTypeHierarchyAdapter(ConfigurationSerializable.class, new ConfigurationSerializableAdapter())
                .registerTypeHierarchyAdapter(ItemStack.class, new ItemStackAdapter())
                .create();

        return this.mongoClient != null && this.database != null;
    }

    /**
     * Stops the storage such things as the database connection
     */
    @Override
    public void stop() {
        this.mongoClient.close();
    }

    /**
     * @param uuid The {@link java.util.UUID} of the {@link Player}
     * @return A {@link CompletableFuture} with the time of the
     * {@link Player}
     */
    @Override
    public CompletableFuture<Long> getPlayTimeByUUID(String uuid) {
        return CompletableFuture.supplyAsync(() -> {
            Document player = this.database.getCollection("playtime").find(new Document("uuid", uuid)).first();
            if(player == null){
                return 0L;
            }
            return player.get("playtime", Long.class);
        });
    }

    /**
     * @param name The name as an {@link String} of the
     *             {@link Player}
     * @return A {@link CompletableFuture} with the time of the
     * {@link Player}
     */
    @Override
    public CompletableFuture<Long> getPlayTimeByName(String name) {
        return CompletableFuture.supplyAsync(() -> {
            Document player = this.database.getCollection("playtime").find(new Document("name", name)).first();
            if(player == null){
                return 0L;
            }
            return player.get("playtime", Long.class);
        });
    }

    /**
     * Save the players online time
     *
     * @param uuid     The {@link java.util.UUID} of the {@link Player}
     * @param playtime the players online time
     * @return Empty CompletableFuture
     */
    @Override
    public CompletableFuture<Boolean> savePlayTime(String uuid, long playtime) {
        return CompletableFuture.supplyAsync(() -> {
            Document player = this.database.getCollection("playtime").find(new Document("uuid", uuid)).first();
            if(player == null){
                this.database.getCollection("playtime").insertOne(new Document("uuid",uuid).append("name", Bukkit.getPlayer(UUID.fromString(uuid)).getName()).append("playtime",playtime));
            }else{
                this.database.getCollection("playtime").updateOne(new Document("uuid",uuid),new Document("$set",new Document("playtime",playtime).append("name", Bukkit.getPlayer(UUID.fromString(uuid)).getName())));
            }
            return true;
        });
    }

    /**
     * Get the top 10 list
     *
     * @return A {@link CompletableFuture} with a {@link Map} with the {@link UUID}
     * and the time of the {@link Player}
     */
    @Override
    public CompletableFuture<Map<String, Long>> getTopTenList() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Long> topTen = new HashMap<>();
            this.database.getCollection("playtime").find().sort(new Document("playtime", -1)).limit(10).forEach(document -> {
                topTen.put(document.getString("uuid"), document.getLong("playtime"));
            });

            return topTen;
        });

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
        return (int) this.database.getCollection("playtime").countDocuments();
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
        return this.database.getCollection("playtime").find().sort(new Document("playtime", -1)).skip(place).first().getString("uuid");
    }

    /**
     * Create a milestone
     *
     * @param milestone The {@link Milestone} to create
     * @return Empty CompletableFuture
     */
    @Override
    public CompletableFuture<Boolean> createMilestone(Milestone milestone) {
        return CompletableFuture.supplyAsync(() -> {
            Document document = Document.parse(gson.toJson(milestone, Milestone.class));
            InsertOneResult insertOneResult = this.database.getCollection("milestones").insertOne(document);
            return insertOneResult.wasAcknowledged();
        });
    }

    /**
     * Save a milestone
     *
     * @param milestone The {@link Milestone} to save
     * @return Empty CompletableFuture
     */
    @Override
    public CompletableFuture<Boolean> saveMileStone(Milestone milestone) {
        return CompletableFuture.supplyAsync(() -> {
            Document document = Document.parse(gson.toJson(milestone, Milestone.class));
            System.out.println(document.toJson());
            UpdateResult insertOneResult = this.database.getCollection("milestones").updateOne(new Document("_id", milestone.getMilestoneName()), new Document("$set", document));
            return insertOneResult.wasAcknowledged();
        });
    }

    /**
     * Remove a milestone
     *
     * @param milestone The {@link Milestone} to remove
     * @return Empty CompletableFuture
     */
    @Override
    public CompletableFuture<Boolean> removeMileStone(Milestone milestone) {
        return CompletableFuture.supplyAsync(() -> {
            DeleteResult insertOneResult = this.database.getCollection("milestones").deleteOne(new Document("_id", milestone.getMilestoneName()));
            return insertOneResult.wasAcknowledged();
        });
    }

    /**
     * Get all milestones
     *
     * @return A {@link CompletableFuture} with a {@link List} of {@link Milestone}
     */
    @Override
    public CompletableFuture<List<Milestone>> getMilestones() {
        return CompletableFuture.supplyAsync(() -> this.database.getCollection("milestones").find().map(document -> gson.fromJson(document.toJson(),Milestone.class)).into(new ArrayList<>()));
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
        return CompletableFuture.supplyAsync(() -> this.database.getCollection("repeatingmilestones").find().map(document -> gson.fromJson(document.toJson(),RepeatingMilestone.class)).into(new ArrayList<>()));
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
