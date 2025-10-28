package nl.thedutchruben.playtime;

import lombok.Getter;
import nl.thedutchruben.mccore.Mccore;
import nl.thedutchruben.mccore.config.UpdateCheckerConfig;
import nl.thedutchruben.mccore.spigot.commands.CommandRegistry;
import nl.thedutchruben.mccore.utils.config.FileManager;
import nl.thedutchruben.playtime.core.Settings;
import nl.thedutchruben.playtime.core.afk.AFKManager;
import nl.thedutchruben.playtime.core.objects.Milestone;
import nl.thedutchruben.playtime.core.objects.PlaytimeUser;
import nl.thedutchruben.playtime.core.objects.RepeatingMilestone;
import nl.thedutchruben.playtime.core.storage.Storage;
import nl.thedutchruben.playtime.core.storage.exceptions.StorageTypeNotFoundException;
import nl.thedutchruben.playtime.core.storage.types.Mongodb;
import nl.thedutchruben.playtime.core.storage.types.Mysql;
import nl.thedutchruben.playtime.core.storage.types.SqlLite;
import nl.thedutchruben.playtime.core.storage.types.Yaml;
import nl.thedutchruben.playtime.core.migration.ConfigMigration;
import nl.thedutchruben.playtime.core.translations.Messages;
import nl.thedutchruben.playtime.extentions.BStatsExtension;
import nl.thedutchruben.playtime.extentions.PlaceholderAPIExtension;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Tdrplaytime is the playtime plugin you need to track the time of your players
 * and reward them for beeing online
 */
public class Playtime {

    /**
     * Instance of playtime
     */
    @Getter
    private static Playtime instance;

    /**
     * Instance of the JavaPlugin
     */
    @Getter
    private static JavaPlugin plugin;
    /**
     * Cache of the PlaytimeUsers
     */
    @Getter
    public Map<UUID, PlaytimeUser> playtimeUsers = new HashMap<>();
    /**
     * The selected storage method
     */
    @Getter
    public Storage storage;
    /**
     * Cache of the Milestones
     */
    @Getter
    public List<Milestone> milestones;
    /**
     * Cache of the RepeatingMilestones
     */
    @Getter
    public List<RepeatingMilestone> repeatingMilestones;
    /**
     * Instance of the core
     */
    @Getter
    private Mccore mccore;
    /**
     * Instance of the FileManager
     */
    @Getter
    private FileManager fileManager;

    @Getter
    private AFKManager afkManager;

    public Playtime(JavaPlugin playTimePlugin) {
        plugin = playTimePlugin;
    }

    public void onEnable(JavaPlugin playTimePlugin) {
        instance = this;
        this.fileManager = new FileManager(plugin);

        // Check and perform migration from 1.x to 2.0 if needed
        ConfigMigration configMigration = new ConfigMigration(fileManager, plugin.getDataFolder());
        if (configMigration.needsMigration()) {
            configMigration.migrate();
        }

        Settings.setupDefaults();
        Messages.setupDefaults();

        //set up the storage
        this.storage = getSelectedStorage();
        this.storage.setup();

        this.afkManager = new AFKManager();

        // Register the mc core
        mccore = new Mccore(plugin, "tdrplaytime", "623a25c0ea9f206b0ba31f3f", Mccore.PluginType.SPIGOT);
        if (Settings.UPDATE_CHECK.getValueAsBoolean()) {
            mccore.startUpdateChecker(new UpdateCheckerConfig("tdrplaytime.admin", 60));
        }

        mccore.registerTabCompletions();
        // Register the bstats
        new BStatsExtension().startBStats(playTimePlugin);

        // Register the placeholder api
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderAPIExtension().register();
        }

        // Load the messages
        this.storage.getMilestones().thenAccept(milestones -> this.milestones = milestones).join();
        getPlugin().getLogger().log(Level.INFO, "Loaded {0} milestones", Optional.of(this.milestones.size()));
        this.storage.getRepeatingMilestones().thenAccept(repeatingMilestones -> this.repeatingMilestones = repeatingMilestones).join();
        getPlugin().getLogger().log(Level.INFO, "Loaded {0} repeatingmilestones", Optional.of(this.repeatingMilestones.size()));

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            UUID playerUUID = onlinePlayer.getUniqueId();
            Playtime.getInstance().getStorage().loadUser(playerUUID).thenAccept(playtimeUser -> {
                if (playtimeUser == null) {
                    playtimeUser = new PlaytimeUser(playerUUID.toString(), onlinePlayer.getName());
                    Playtime.getInstance().getStorage().createUser(playtimeUser);
                }
                Playtime.getInstance().getPlaytimeUsers().put(playerUUID, playtimeUser);
            });
        }

        CommandRegistry.getTabCompletable().put("milestone", commandSender ->
                this.milestones.stream().map(Milestone::getMilestoneName)
                        .collect(Collectors.toSet())
        );

        CommandRegistry.getTabCompletable().put("repeatingMilestone", commandSender ->
                this.repeatingMilestones.stream().map(RepeatingMilestone::getMilestoneName)
                        .collect(Collectors.toSet())
        );
    }

    public Storage getSelectedStorage() {
        String storageType = Settings.STORAGE_TYPE.getValueAsString().toLowerCase();
        switch (storageType) {
            case "mongodb":
            case "mongo":
                return new Mongodb();
            case "mysql":
            case "sql":
            case "mariadb":
                return new Mysql();
            case "yaml":
            case "yml":
                getPlugin().getLogger().log(Level.WARNING, "Yaml storage is not recommended. If you have a lot of players it can cause lag. Please use sqlLite, mysql or mongodb");
                return new Yaml();
            case "sqlite":
            case "sqllite":
                return new SqlLite();
            case "postgresql":
                throw new StorageTypeNotFoundException("Postgresql is not supported yet");
            case "h2":
                throw new StorageTypeNotFoundException("H2 is not supported yet");
            default:
                throw new StorageTypeNotFoundException("Storage type " + storageType + " not found");
        }
    }

    public void onDisable() {

        for (PlaytimeUser playtimeUser : playtimeUsers.values()) {
            storage.saveUser(playtimeUser);
        }

        this.storage.stop();
        this.milestones.clear();
        this.repeatingMilestones.clear();
        Messages.getMessages().clear();
    }

    /**
     * Get the playtime user from the cache or load it from the storage
     *
     * @param uuid The uuid of the player
     * @return The playtime user
     */
    public Optional<PlaytimeUser> getPlaytimeUser(UUID uuid) {
        return Optional.of(playtimeUsers.get(uuid));
    }

    /**
     * Get a playtime user by name
     *
     * @param name The name of the player
     * @return The playtime user if exist
     */
    public Optional<PlaytimeUser> getPlaytimeUser(String name) {
        return playtimeUsers.values().stream().filter(item -> item.getName().equalsIgnoreCase(name)).findFirst();
    }
}