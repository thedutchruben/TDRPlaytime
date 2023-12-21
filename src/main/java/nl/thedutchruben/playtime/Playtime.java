package nl.thedutchruben.playtime;

import lombok.Getter;
import nl.thedutchruben.mccore.Mccore;
import nl.thedutchruben.mccore.config.UpdateCheckerConfig;
import nl.thedutchruben.mccore.utils.config.FileManager;
import nl.thedutchruben.playtime.core.Settings;
import nl.thedutchruben.playtime.core.migrations.FourteenToFiveteenMigration;
import nl.thedutchruben.playtime.core.objects.Milestone;
import nl.thedutchruben.playtime.core.objects.PlaytimeUser;
import nl.thedutchruben.playtime.core.objects.RepeatingMilestone;
import nl.thedutchruben.playtime.core.storage.Storage;
import nl.thedutchruben.playtime.core.storage.types.Mongodb;
import nl.thedutchruben.playtime.core.storage.types.Mysql;
import nl.thedutchruben.playtime.core.storage.types.SqlLite;
import nl.thedutchruben.playtime.core.translations.Messages;
import nl.thedutchruben.playtime.extentions.BStatsExtension;
import nl.thedutchruben.playtime.extentions.PlaceholderAPIExtension;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Level;

public class Playtime {

    @Getter
    private static Playtime instance;
    @Getter
    private static JavaPlugin plugin;

    @Getter
    private Mccore mccore;

    @Getter
    private FileManager fileManager;

    @Getter
    public Map<UUID, PlaytimeUser> playtimeUsers = new HashMap<>();
    @Getter
    public Storage storage;
    @Getter
    public List<Milestone> milestones;
    @Getter
    public List<RepeatingMilestone> repeatingMilestones;

    public Playtime(JavaPlugin playTimePlugin) {
        plugin = playTimePlugin;
    }

    public void onEnable(JavaPlugin playTimePlugin){
        instance = this;
        this.fileManager = new FileManager(plugin);

        //set up the storage
        this.storage = getSelectedStorage();
        this.storage.setup();

        // todo check if migration is needed
        if(fileManager.getConfig("config.yml").get().getString("version") == null){
            new FourteenToFiveteenMigration();
        }


        Settings.setupDefaults();

        // Register the mc core
        mccore = new Mccore(plugin, "tdrplaytime", "623a25c0ea9f206b0ba31f3f", Mccore.PluginType.SPIGOT);
        mccore.startUpdateChecker(new UpdateCheckerConfig("tdrplaytime.admin",60));

        // Register the bstats
        new BStatsExtension().startBStats(playTimePlugin);

        // Register the placeholder api
        if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")){
            new PlaceholderAPIExtension().register();
        }

        // Load the messages
        this.storage.getMilestones().thenAccept(milestones -> this.milestones = milestones).join();
        getPlugin().getLogger().log(Level.INFO,"Loaded {} milestones",this.milestones.size());
        this.storage.getRepeatingMilestones().thenAccept(repeatingMilestones -> this.repeatingMilestones = repeatingMilestones).join();
        getPlugin().getLogger().log(Level.INFO,"Loaded {} repeatingmilestones",this.repeatingMilestones.size());
        
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            Playtime.getInstance().getStorage().loadUser(onlinePlayer.getUniqueId()).thenAccept(playtimeUser -> {
                if(playtimeUser != null){
                    Playtime.getInstance().getPlaytimeUsers().put(onlinePlayer.getUniqueId(),playtimeUser);
                }else{
                    PlaytimeUser playtimeUser1 = new PlaytimeUser(onlinePlayer.getUniqueId().toString(),onlinePlayer.getName());
                    Playtime.getInstance().getStorage().createUser(playtimeUser1);
                    Playtime.getInstance().getPlaytimeUsers().put(onlinePlayer.getUniqueId(),playtimeUser1);
                }
            });
        }
    }

    public Storage getSelectedStorage(){
        String value = (String) Settings.STORAGE_TYPE.getValue();
        if (value.equals("mongodb")) {
            return new Mongodb();
        } else if (value.equals("mysql")) {
            return new Mysql();
        }
        return new SqlLite();
    }

    public void onDisable(){
        this.storage.stop();
        this.milestones.clear();
        this.repeatingMilestones.clear();
        Messages.getMessages().clear();
    }

    /**
     * Get the playtime user from the cache or load it from the storage
     * @param uuid The uuid of the player
     * @return The playtime user
     */
    public Optional<PlaytimeUser> getPlaytimeUser(UUID uuid){
        return Optional.of(playtimeUsers.get(uuid));
    }

    /**
     * Get a playtime user by name
     * @param name The name of the player
     * @return The playtime user if exist
     */
    public Optional<PlaytimeUser> getPlaytimeUser(String name){
        return playtimeUsers.values().stream().filter(item -> item.getName().equalsIgnoreCase(name)).findFirst();
    }
}
