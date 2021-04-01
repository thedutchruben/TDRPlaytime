package nl.thedutchruben.playtime;

import lombok.SneakyThrows;
import nl.thedutchruben.playtime.command.MilestoneCommand;
import nl.thedutchruben.playtime.command.PlayTimeCommand;
import nl.thedutchruben.playtime.database.LanguageNotFoundException;
import nl.thedutchruben.playtime.database.MysqlDatabase;
import nl.thedutchruben.playtime.database.Storage;
import nl.thedutchruben.playtime.database.YamlDatabase;
import nl.thedutchruben.playtime.extentions.PlaceholderAPIExpansion;
import nl.thedutchruben.playtime.listeners.PlayerJoinListener;
import nl.thedutchruben.playtime.listeners.PlayerQuitListener;
import nl.thedutchruben.playtime.milestone.Milestone;
import nl.thedutchruben.playtime.utils.FileManager;
import nl.thedutchruben.playtime.utils.UpdateChecker;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public final class Playtime extends JavaPlugin {
    private Map<UUID,Long> playerOnlineTime = new HashMap<>();
    private Map<UUID,Long> lastCheckedTime = new HashMap<>();
    private Map<Long, Milestone> milestoneMap = new HashMap<>();
    private Map<String,String> keyMessageMap = new HashMap<>();

    private static Playtime instance;
    private Storage storage;
    private FileManager fileManager = new FileManager(this);
    private FileManager.Config langFile;
    @SneakyThrows
    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        Metrics metrics = new Metrics(this, 9404);

        FileManager.Config config = fileManager.getConfig("config.yml");
        FileConfiguration configfileConfiguration = config.get();
        configfileConfiguration.options().header("TDR Playtime Plugin " +
                "\nhttps://www.spigotmc.org/resources/tdrplaytime.47894/ \n" +
                "Change the language to one of the other files default it has nl_NL.yml and en_GB.yml, " +
                "you can create your own language file");
        configfileConfiguration.addDefault("language","en_GB");
        config.copyDefaults(true).save();

        FileManager.Config database = fileManager.getConfig("database.yml");
        FileConfiguration fileConfiguration = database.get();
        fileConfiguration.options().header("TDR Playtime Plugin Database\n" +
                "You can use the following database types : yaml/mysql");
        fileConfiguration.addDefault("database","yaml");
        fileConfiguration.addDefault("mysql.hostname","localhost");
        fileConfiguration.addDefault("mysql.port",3306);
        fileConfiguration.addDefault("mysql.user","root");
        fileConfiguration.addDefault("mysql.password","password");
        fileConfiguration.addDefault("mysql.database","playtime");
        database.copyDefaults(true).save();

        if(Objects.requireNonNull(database.get().getString("database")).equalsIgnoreCase("mysql")){
            storage = new MysqlDatabase();
        }else{
            storage = new YamlDatabase();
        }
        config.save();
        database.save();
        storage.setup();
        getCommand("playtime").setExecutor(new PlayTimeCommand());
        getCommand("playtime").setTabCompleter(new PlayTimeCommand());
        getCommand("milestone").setExecutor(new MilestoneCommand());
        getCommand("milestone").setTabCompleter(new MilestoneCommand());

        generateEnglishTranslations();
        generateDutchTranslations();
        generateGermanTranslations();
        generateFrenchTranslations();

        langFile = fileManager.getConfig("lang/"+configfileConfiguration.getString("language")+".yml");

        getLogger().log(Level.INFO,"Loading milestones");
        storage.getMilestones().whenComplete((milestones, throwable) -> {
            for (Milestone storageMilestone : milestones) {
                milestoneMap.put(storageMilestone.getOnlineTime() * 1000L,storageMilestone);
            }
            getLogger().log(Level.INFO,milestoneMap.size() + " milestones loaded");
        });


        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(),this);
        Bukkit.getPluginManager().registerEvents(new PlayerQuitListener(),this);

        new UpdateChecker(this, 47894).getVersion(version -> {
            if (!this.getDescription().getVersion().equalsIgnoreCase(version)) {
                getLogger().info("There is a new update available of TDRPlaytime.");
                getLogger().info("Download it here https://www.spigotmc.org/resources/tdrplaytime.47894/");
            }
        });

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            long onlineTime = Playtime.getInstance().getStorage().getPlayTimeByUUID(onlinePlayer.getUniqueId().toString()).get();
            Playtime.getInstance().getPlayerOnlineTime().put(onlinePlayer.getUniqueId(),onlineTime);
            Playtime.getInstance().getLastCheckedTime().put(onlinePlayer.getUniqueId(),System.currentTimeMillis());
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(getInstance(), () -> {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                update(onlinePlayer.getUniqueId(),true);
            }
        },0,20 * 60);


        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
            getLogger().log(Level.INFO,"PlaceholderAPI expansion implemented");
            metrics.addCustomChart(new SimplePie("addons_use",() -> "PlaceholderAPI"));

            new PlaceholderAPIExpansion().register();
        }

        metrics.addCustomChart(new SimplePie("database_type",() -> config.get().getString("database").toLowerCase()));
        metrics.addCustomChart(new SimplePie("uses_milestones",() -> String.valueOf(milestoneMap.size() >1)));
        metrics.addCustomChart(new SimplePie("language",() -> config.get().getString("language")));

    }



    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            update(onlinePlayer.getUniqueId(),true);
        }

        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        storage.stop();

                        playerOnlineTime.clear();
                        lastCheckedTime.clear();
                        milestoneMap.clear();
                        keyMessageMap.clear();
                    }
                },
                50
        );

    }

    public void update(UUID uuid,boolean save){
        long extraTime = System.currentTimeMillis() - lastCheckedTime.get(uuid);
        lastCheckedTime.replace(uuid,System.currentTimeMillis());
        long newtime = playerOnlineTime.get(uuid) + extraTime;
        checkMileStones(uuid,playerOnlineTime.get(uuid),newtime);
        playerOnlineTime.replace(uuid,newtime);
        if(save){
            storage.savePlayTime(uuid.toString(),playerOnlineTime.get(uuid));
        }
    }

    private void checkMileStones(UUID uuid, Long oldtime, long newtime) {
        for (Long i = oldtime; i < newtime; i++) {
            if(milestoneMap.containsKey(i)){
                milestoneMap.get(i).apply(Bukkit.getPlayer(uuid));
            }
        }
    }

    public String getMessage(String key){
        if(keyMessageMap.containsKey(key)){
            return keyMessageMap.get(key);
        }
        if(langFile.get().getString(key) == null){
            return  ChatColor.RED + "No translation found for : " + key;
        }
        keyMessageMap.put(key,ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(langFile.get().getString(key))));
        return ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(langFile.get().getString(key)));
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

    public Map<Long, Milestone> getMilestoneMap() {
        return milestoneMap;
    }


    public void generateEnglishTranslations(){
        getLogger().info("Generate English translations");
        FileManager.Config config = fileManager.getConfig("lang/en_GB.yml");
        config.get().addDefault("version",1.0);
        config.get().addDefault("only.player.command","&cThis is a player only command!");
        //playtime command messages
        config.get().addDefault("command.playtime.timemessage","&8[&6PlayTime&8] &7Your playtime is &6%D% &7day(s) &6%H% &7hour(s) &6%M% &7minute(s) &6%S% &7second(s)");
        config.get().addDefault("command.playtime.usertimemessage","&8[&6PlayTime&8] &7%NAME% 's playtime is &6%D% &7day(s) &6%H% &7hour(s) &6%M% &7minute(s) &6%S% &7second(s)");
        config.get().addDefault("command.playtime.resettimeconfirm","&cUser time reset!");
        config.get().addDefault("command.playtime.resettimeussage","&cUse : /playtime reset <username>!");
        //milestone command messages
        config.get().addDefault("command.milestone.mustbenumber","&cThe time parameter must be a number!");
        config.get().addDefault("command.milestone.createusage","&cUse : /milestone create <name> <time in seconds>!");
        config.get().addDefault("command.milestone.milestonenotexist","&cThe milestone <name> doesn't exist!");

        config.get().addDefault("command.milestone.additemusage","&cUse : /milestone additem <milestone>! The item in your hand wil be added!");
        config.get().addDefault("command.milestone.addcommandusage","&cUse : /milestone addcommand <milestone> <command>!");

        config.get().addDefault("command.milestone.milestonecreated","&aThe milestone is created!");
        config.get().addDefault("command.milestone.itemadded","&aYou added succesfull a item to the milestone!");
        config.get().addDefault("command.milestone.commandadded","&aYou added succesfull a command to the milestone!");

        config.copyDefaults(true).save();
        config.save();
    }

    public void generateDutchTranslations(){
        getLogger().info("Generate Dutch translations");
        FileManager.Config config = fileManager.getConfig("lang/nl_NL.yml");
        config.get().addDefault("version",1.0);
        config.get().addDefault("only.player.command","&cDit is een command die alleen een speler kan gebruiken!");
        //playtime command messages
        config.get().addDefault("command.playtime.timemessage","&8[&6PlayTime&8] &7Jouw speeltijd is &6%D% &7dag(en) &6%H% &7uur &6%M% &7minuut(en) &6%S% &7seconde(n)");
        config.get().addDefault("command.playtime.usertimemessage","&8[&6PlayTime&8] &7%NAME% ''s speeltijd is &6%D% &7dag(en) &6%H% &7uur &6%M% &7minuut(en) &6%S% &7seconde(n)");
        config.get().addDefault("command.playtime.resettimeconfirm","&cDe tijd van de speler is gereset!");
        config.get().addDefault("command.playtime.resettimeussage","&cGebruik : /playtime reset <username>!");
        //milestone command messages
        config.get().addDefault("command.milestone.mustbenumber","&cDe tijd parameter moet een nummer zijn!");
        config.get().addDefault("command.milestone.createusage","&cGebruik : /milestone create <name> <time in seconds>!");
        config.get().addDefault("command.milestone.milestonenotexist","&cDe mijlpaal <name> bestaat niet!");

        config.get().addDefault("command.milestone.additemusage","&cGebruik : /milestone additem <milestone>! Het item in je hand wordt dan toegevoegd!");
        config.get().addDefault("command.milestone.addcommandusage","&cGebruik : /milestone addcommand <milestone> <command>!");

        config.get().addDefault("command.milestone.milestonecreated","&aDe mijlpaal is aangemaakt!");
        config.get().addDefault("command.milestone.itemadded","&aJe hebt succesvol een item toegevoegd aan de mijlpaal!");
        config.get().addDefault("command.milestone.commandadded","&aJe hebt succesvol een command toegevoegd aan de mijlpaal!");

        config.copyDefaults(true).save();
        config.save();
    }

    public void generateGermanTranslations(){
        getLogger().info("Generate German translations");
        FileManager.Config config = fileManager.getConfig("lang/de_DE.yml");
        config.get().addDefault("version",1.0);
        config.get().addDefault("only.player.command","&cDies ist ein Kommando nur für Spieler!");
        //playtime command messages
        config.get().addDefault("command.playtime.timemessage","&8[&6PlayTime&8] &7Deine Spielzeit ist &6%D% &7Tag(e) &6%H% &7Stunde(n) &6%M% &7Minute(n) &6%S% &7Sekunde(n)");
        config.get().addDefault("command.playtime.usertimemessage","&8[&6PlayTime&8] &7%NAME% ''s Spielzeit ist &6%D% &7Tag(e) &6%H% &7Stunde(n) &6%M% &7Minute(n) &6%S% &7Sekunde(n)");
        config.get().addDefault("command.playtime.resettimeconfirm","&cDie Zeit des Spielers ist zurückgesetzt!");
        config.get().addDefault("command.playtime.resettimeussage","&cBenutz : /playtime reset <username>!");
        //milestone command messages
        config.get().addDefault("command.milestone.mustbenumber","&cDer Zeitparameter muss eine Anzahl sein!");
        config.get().addDefault("command.milestone.createusage","&cBenutz: /milestone create <name> <time in seconds>!");
        config.get().addDefault("command.milestone.milestonenotexist","&cDer Meilenstein <name> existiert nicht!");

        config.get().addDefault("command.milestone.additemusage","&cBenutz : /milestone additem <milestone>! Der Item in Ihrer Hand wird dann hinzugefügt werden!");
        config.get().addDefault("command.milestone.addcommandusage","&cBenutz : /milestone addcommand <milestone> <command>!");

        config.get().addDefault("command.milestone.milestonecreated","&aDer Meilenstein ist geschaffen!");
        config.get().addDefault("command.milestone.itemadded","&aSie haben dem Meilenstein erfolgreich einen Artikel hinzugefügt!");
        config.get().addDefault("command.milestone.commandadded","&aSie haben dem Meilenstein erfolgreich einen Kommando hinzugefügt!");

        config.copyDefaults(true).save();
        config.save();
    }

    public void generateFrenchTranslations(){
        getLogger().info("Generate French translations");
        FileManager.Config config = fileManager.getConfig("lang/fr_FR.yml");
        config.get().addDefault("version",1.0);
        config.get().addDefault("only.player.command","&cCeci est une commande réservée au joueur!");
        //playtime command messages
        config.get().addDefault("command.playtime.timemessage","&8[&6PlayTime&8] &7Votre temps de jeu est &6%D% &7jour(s) &6%H% &7heure(s) &6%M% &7minute(s) &6%S% &7second(s)");
        config.get().addDefault("command.playtime.usertimemessage","&8[&6PlayTime&8] &7%NAME% ''s récréation est &6%D% &7jour(s) &6%H% &7heure(s) &6%M% &7minute(s) &6%S% &7second(s)");
        config.get().addDefault("command.playtime.resettimeconfirm","&cRéinitialisation de l'heure de l'utilisateur!");
        config.get().addDefault("command.playtime.resettimeussage","&cUtilisez : /playtime reset <username>!");
        //milestone command messages
        config.get().addDefault("command.milestone.mustbenumber","&cLe paramètre de temps doit être un nombre!");
        config.get().addDefault("command.milestone.createusage","&cUtilisez : /milestone create <name> <time in seconds>!");
        config.get().addDefault("command.milestone.milestonenotexist","&cLe jalon <name> n'existe pas!");

        config.get().addDefault("command.milestone.additemusage","&cUtilisez: /milestone additem <milestone>! L'article que vous avez en main sera alors ajouté!");
        config.get().addDefault("command.milestone.addcommandusage","&cUtilisez : /milestone addcommand <milestone> <command>!");

        config.get().addDefault("command.milestone.milestonecreated","&aLe jalon est créé!");
        config.get().addDefault("command.milestone.itemadded","&aVous avez ajouté avec succès un élément au jalon!");
        config.get().addDefault("command.milestone.commandadded","&aVous avez ajouté avec succès une commande au jalon!");

        config.copyDefaults(true).save();
        config.save();
    }
}

