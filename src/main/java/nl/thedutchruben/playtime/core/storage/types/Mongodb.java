package nl.thedutchruben.playtime.core.storage.types;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.Settings;
import nl.thedutchruben.playtime.core.objects.Milestone;
import nl.thedutchruben.playtime.core.objects.PlaytimeHistory;
import nl.thedutchruben.playtime.core.objects.PlaytimeUser;
import nl.thedutchruben.playtime.core.objects.RepeatingMilestone;
import nl.thedutchruben.playtime.core.storage.Storage;
import org.bson.Document;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * The mongodb storage
 */
public class Mongodb extends Storage {
    private MongoClient mongoClient;
    private com.mongodb.client.MongoDatabase database;

    /**
     * Get the name of the storage type
     *
     * @return The name of the storage type
     */
    @Override
    public String getName() {
        return "mongodb";
    }

    /**
     * Setup the storage such as the database connection
     */
    @Override
    public boolean setup() {
        // build the connection string from config values
        String connectionString = "mongodb://";
        if (!Objects.equals(Settings.STORAGE_MONGO_USERNAME.getValueAsString(), "")) {
            connectionString += Settings.STORAGE_MONGO_USERNAME.getValueAsString() + ":" + Settings.STORAGE_MONGO_PASSWORD.getValueAsString() + "@";
        }
        connectionString += Settings.STORAGE_MONGO_HOST.getValueAsString() + ":" + Settings.STORAGE_MONGO_PORT.getValueAsInteger();
        connectionString += "/" + Settings.STORAGE_MONGO_COLLECTION.getValueAsString();
        this.mongoClient = MongoClients.create(connectionString);
        //Check if the database is valid
        this.database = this.mongoClient.getDatabase(Settings.STORAGE_MONGO_COLLECTION.getValueAsString());

        return this.mongoClient != null;
    }

    /**
     * Stops the storage such things as the database connection
     */
    @Override
    public void stop() {
        this.mongoClient.close();
    }

    /**
     * Load the user from the storage
     *
     * @param uuid The uuid of the player
     * @return The playtime user
     */
    @Override
    public CompletableFuture<PlaytimeUser> loadUser(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            Document document = this.database.getCollection("playtime").find(new Document("uuid", uuid.toString())).first();
            if (document == null) {
                return null;
            }
            return new PlaytimeUser(document.getString("uuid"), document.getString("name"), document.getLong("playtime"));
        });
    }

    /**
     * Load user loaded by name
     *
     * @param name
     * @return
     */
    @Override
    public CompletableFuture<PlaytimeUser> loadUserByName(String name) {
        return CompletableFuture.supplyAsync(() -> {
            Document document = this.database.getCollection("playtime").find(new Document("name", name)).first();
            if (document == null) {
                return null;
            }
            return new PlaytimeUser(document.getString("uuid"), document.getString("name"), document.getLong("playtime"));
        });
    }

    /**
     * Save the user to the storage
     *
     * @param playtimeUser The playtime user
     * @return If the user is saved
     */
    @Override
    public CompletableFuture<Boolean> saveUser(PlaytimeUser playtimeUser) {
        return CompletableFuture.supplyAsync(() -> {
            Document document = new Document("uuid", playtimeUser.getUUID().toString())
                    .append("name", playtimeUser.getName())
                    .append("playtime", playtimeUser.getTime());
            UpdateResult updateResult = this.database.getCollection("playtime").updateOne(new Document("uuid", playtimeUser.getUUID().toString()), new Document("$set", document));
            return updateResult.wasAcknowledged();
        });
    }


    /**
     * Create the user
     *
     * @param playtimeUser
     * @return
     */
    @Override
    public CompletableFuture<Boolean> createUser(PlaytimeUser playtimeUser) {
        return CompletableFuture.supplyAsync(() -> {
            Document document = new Document("uuid", playtimeUser.getUUID().toString())
                    .append("name", playtimeUser.getName())
                    .append("playtime", playtimeUser.getTime());
            InsertOneResult result = this.database.getCollection("playtime").insertOne(document);
            return result.wasAcknowledged();
        });
    }

    /**
     * Get the top users
     *
     * @param amount The amount of users
     * @param skip   The amount of users to skip
     * @return The list of users
     */
    @Override
    public CompletableFuture<List<PlaytimeUser>> getTopUsers(int amount, int skip) {
        return CompletableFuture.supplyAsync(() -> {
            List<PlaytimeUser> users = new ArrayList<>();
            this.database.getCollection("playtime").find().sort(new Document("playtime", -1)).skip(skip).limit(amount).forEach(document -> {
                users.add(new PlaytimeUser(document.getString("uuid"), document.getString("name"), document.getLong("playtime")));
            });
            return users;
        });
    }

    /**
     * Get the top user
     *
     * @param place The place of the user
     * @return The user
     */
    @Override
    public CompletableFuture<PlaytimeUser> getTopUser(int place) {
        return CompletableFuture.supplyAsync(() -> {
            Document document = this.database.getCollection("playtime").find().sort(new Document("playtime", -1)).skip(place).limit(1).first();
            if (document == null) {
                return null;
            }
            return new PlaytimeUser(document.getString("uuid"), document.getString("name"), document.getLong("playtime"));
        });
    }

    /**
     * Get the milestones
     *
     * @return The list of milestones
     */
    @Override
    public CompletableFuture<List<Milestone>> getMilestones() {
        return CompletableFuture.supplyAsync(() -> this.database.getCollection("milestones").find().map(document -> getGson().fromJson(document.toJson(), Milestone.class)).into(new ArrayList<>()));
    }

    /**
     * Save the milestone
     *
     * @param milestone The milestone to save
     * @return If the milestone is saved
     */
    @Override
    public CompletableFuture<Boolean> saveMilestone(Milestone milestone) {
        return CompletableFuture.supplyAsync(() -> {
            Document document = Document.parse(getGson().toJson(milestone));
            InsertOneResult result = this.database.getCollection("milestones").insertOne(document);
            return result.wasAcknowledged();
        });
    }

    /**
     * Delete the milestone
     *
     * @param milestone The milestone to delete
     * @return If the milestone is deleted
     */
    @Override
    public CompletableFuture<Boolean> deleteMilestone(Milestone milestone) {
        return CompletableFuture.supplyAsync(() -> {
            Document document = Document.parse(getGson().toJson(milestone));
            DeleteResult deleteResult = this.database.getCollection("milestones").deleteOne(document);
            return deleteResult.wasAcknowledged();
        });
    }

    /**
     * Update the milestone
     *
     * @param milestone The milestone to update
     * @return If the milestone is updated
     */
    @Override
    public CompletableFuture<Boolean> updateMilestone(Milestone milestone) {
        return CompletableFuture.supplyAsync(() -> {
            Document document = Document.parse(getGson().toJson(milestone, Milestone.class));
            UpdateResult insertOneResult = this.database.getCollection("milestones").updateOne(new Document("_id", milestone.getMilestoneName()), new Document("$set", document));
            return insertOneResult.wasAcknowledged();
        });
    }

    /**
     * Get the repeating milestones
     *
     * @return The list of repeating milestones
     */
    @Override
    public CompletableFuture<List<RepeatingMilestone>> getRepeatingMilestones() {
        return CompletableFuture.supplyAsync(() -> this.database.getCollection("repeatingmilestones").find().map(document -> getGson().fromJson(document.toJson(), RepeatingMilestone.class)).into(new ArrayList<>()));
    }

    /**
     * Save the repeating milestone
     *
     * @param repeatingMilestone The repeating milestone to save
     * @return If the repeating milestone is saved
     */
    @Override
    public CompletableFuture<Boolean> saveRepeatingMilestone(RepeatingMilestone repeatingMilestone) {
        return CompletableFuture.supplyAsync(() -> {
            Document document = Document.parse(getGson().toJson(repeatingMilestone));
            InsertOneResult result = this.database.getCollection("repeatingmilestones").insertOne(document);
            return result.wasAcknowledged();
        });
    }

    /**
     * Delete the repeating milestone
     *
     * @param repeatingMilestone The repeating milestone to delete
     * @return If the repeating milestone is deleted
     */
    @Override
    public CompletableFuture<Boolean> deleteRepeatingMilestone(RepeatingMilestone repeatingMilestone) {
        return CompletableFuture.supplyAsync(() -> {
            Document document = Document.parse(getGson().toJson(repeatingMilestone));
            DeleteResult deleteResult = this.database.getCollection("repeatingmilestones").deleteOne(document);
            return deleteResult.wasAcknowledged();
        });
    }

    /**
     * Update the repeating milestone
     *
     * @param repeatingMilestone The repeating milestone to update
     * @return If the repeating milestone is updated
     */
    @Override
    public CompletableFuture<Boolean> updateRepeatingMilestone(RepeatingMilestone repeatingMilestone) {
        return CompletableFuture.supplyAsync(() -> {
            Document document = Document.parse(getGson().toJson(repeatingMilestone, RepeatingMilestone.class));
            UpdateResult insertOneResult = this.database.getCollection("repeatingmilestones").updateOne(new Document("_id", repeatingMilestone.getMilestoneName()), new Document("$set", document));
            return insertOneResult.wasAcknowledged();
        });
    }

    /**
     * @param uuid
     * @param event
     * @param time
     * @return
     */
    @Override
    public CompletableFuture<Boolean> addPlaytimeHistory(UUID uuid, Event event, int time) {
        return CompletableFuture.supplyAsync(() -> {
            InsertOneResult result = this.database.getCollection("playtimeHistory")
                    .insertOne(
                            new Document("uuid", uuid)
                                    .append("date", new Date())
                                    .append("event", event)
                                    .append("time", time)
                    );
            return result.wasAcknowledged();
        });
    }

    @Override
    public CompletableFuture<List<PlaytimeHistory>> getPlaytimeHistory(UUID uuid, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            List<PlaytimeHistory> history = new ArrayList<>();
            Document query = new Document("uuid", uuid.toString());
            this.database.getCollection("playtime_history").find(query).sort(new Document("date", -1)).limit(limit).forEach(document -> {
                PlaytimeHistory playtimeHistory = getGson().fromJson(document.toJson(), PlaytimeHistory.class);
                history.add(playtimeHistory);
            });
            return history;
        });
    }

    @Override
    public CompletableFuture<List<PlaytimeHistory>> getPlaytimeHistoryByName(String name, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            List<PlaytimeHistory> history = new ArrayList<>();
            Document query = new Document("name", name);
            this.database.getCollection("playtime_history").find(query).sort(new Document("date", -1)).limit(limit).forEach(document -> {
                PlaytimeHistory playtimeHistory = getGson().fromJson(document.toJson(), PlaytimeHistory.class);
                history.add(playtimeHistory);
            });
            return history;
        });
    }

    /**
     * Check if a playtime record exists for the specified player and date
     *
     * @param uuid The UUID of the player
     * @param date The date to check
     * @return If the record exists
     */
    private boolean playtimeRecordExists(UUID uuid, java.sql.Date date) {
        Document query = new Document("uuid", uuid.toString()).append("date", date);
        return this.database.getCollection("playtime_history").find(query).first() != null;
    }
}
