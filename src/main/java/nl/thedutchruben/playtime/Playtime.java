package nl.thedutchruben.playtime;

import nl.thedutchruben.playtime.command.PlayTimeCommand;
import nl.thedutchruben.playtime.database.MysqlDatabase;
import nl.thedutchruben.playtime.database.Storage;
import nl.thedutchruben.playtime.database.YamlDatabase;
import nl.thedutchruben.playtime.listeners.PlayerJoinListener;
import nl.thedutchruben.playtime.listeners.PlayerQuitListener;
import nl.thedutchruben.playtime.utils.FileManager;
import nl.thedutchruben.playtime.utils.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Playtime extends JavaPlugin {
    private Map<UUID,Long> playerOnlineTime = new HashMap<>();
    private Map<UUID,Long> lastCheckedTime = new HashMap<>();
    private static Playtime instance;
    private Storage storage;
    private FileManager fileManager = new FileManager(this);

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        Metrics metrics = new Metrics(this, 9404);

        FileManager.Config config = fileManager.getConfig("database.yml");
        FileConfiguration fileConfiguration = config.get();
        fileConfiguration.addDefault("database","yaml");
        fileConfiguration.addDefault("mysql.hostname","localhost");
        fileConfiguration.addDefault("mysql.port",3306);
        fileConfiguration.addDefault("mysql.user","root");
        fileConfiguration.addDefault("mysql.password","password");
        fileConfiguration.addDefault("mysql.database","playtime");
        config.copyDefaults(true).save();
        if(config.get().getString("database").toLowerCase().equalsIgnoreCase("mysql")){
            storage = new MysqlDatabase();
        }else{
            storage = new YamlDatabase();
        }
        config.save();
        storage.setup();
        getCommand("playtime").setExecutor(new PlayTimeCommand());

        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(),this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(),this);

        new UpdateChecker(this, 47894).getVersion(version -> {
            if (!this.getDescription().getVersion().equalsIgnoreCase(version)) {
                Bukkit.getLogger().info("There is a new update available of TDRPlaytime. ");
                Bukkit.getLogger().info("Download it here https://www.spigotmc.org/resources/tdrplaytime.47894/");
            }
        });

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            storage.getPlayTimeByUUID(onlinePlayer.getUniqueId().toString());
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(getInstance(), () -> {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                update(onlinePlayer.getUniqueId());
            }
        },0,20 * 60);

        metrics.addCustomChart(new SimplePie("database_type",() -> config.get().getString("database").toLowerCase()));

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            update(onlinePlayer.getUniqueId());
        }

        storage.stop();

        playerOnlineTime.clear();
        lastCheckedTime.clear();

    }

    public void update(UUID uuid){
        long extraTime = System.currentTimeMillis() - lastCheckedTime.get(uuid);
        lastCheckedTime.replace(uuid,System.currentTimeMillis());
        long newtime = playerOnlineTime.get(uuid) + extraTime;
        playerOnlineTime.replace(uuid,newtime);
        storage.savePlayTime(uuid.toString(),playerOnlineTime.get(uuid));
    }

    public Storage getStorage() {
        return storage;
    }

    public Map<UUID, Long> getLastCheckedTime() {
        return lastCheckedTime;
    }

    public Map<UUID, Long> getPlayerOnlineTime() {
        return playerOnlineTime;
    }

    public static Playtime getInstance() {
        return instance;
    }

    public FileManager getFileManager() {
        return fileManager;
    }
}
