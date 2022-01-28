package nl.thedutchruben.playtime;

import de.jeff_media.updatechecker.UpdateChecker;
import de.jeff_media.updatechecker.UserAgentBuilder;
import lombok.SneakyThrows;
import nl.thedutchruben.mccore.Mccore;
import nl.thedutchruben.mccore.utils.config.FileManager;
import nl.thedutchruben.playtime.command.MilestoneCommand;
import nl.thedutchruben.playtime.command.PlayTimeCommand;
import nl.thedutchruben.playtime.command.RepeatingMilestoneCommand;
import nl.thedutchruben.playtime.database.MysqlDatabase;
import nl.thedutchruben.playtime.database.Storage;
import nl.thedutchruben.playtime.database.YamlDatabase;
import nl.thedutchruben.playtime.events.PlayTimeCheckEvent;
import nl.thedutchruben.playtime.events.PlayTimeUpdatePlayerEvent;
import nl.thedutchruben.playtime.extentions.PlaceholderAPIExpansion;
import nl.thedutchruben.playtime.milestone.Milestone;
import nl.thedutchruben.playtime.milestone.RepeatingMilestone;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public final class Playtime extends JavaPlugin {
    private static Playtime instance;
    private final Map<UUID, Long> playerOnlineTime = new HashMap<>();
    private final Map<UUID, Long> lastCheckedTime = new HashMap<>();
    private Map<Long, Milestone> milestoneMap = new HashMap<>();
    private List<RepeatingMilestone> repeatedMilestoneList = new ArrayList<>();
    private Map<String, String> keyMessageMap = new HashMap<>();
    private Storage storage;
    private final FileManager fileManager = new FileManager(this);
    private FileManager.Config langFile;
    private BukkitTask checkTask;

    public static Playtime getInstance() {
        return instance;
    }

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
        configfileConfiguration.addDefault("language", "en_GB");
        configfileConfiguration.addDefault("settings.update_check", true);
        configfileConfiguration.addDefault("settings.update_checktime", 0.5);
        config.copyDefaults(true).save();

        FileManager.Config database = fileManager.getConfig("database.yml");
        FileConfiguration fileConfiguration = database.get();
        fileConfiguration.options().header("TDR Playtime Plugin Database\n" +
                "You can use the following database types : yaml/mysql");
        fileConfiguration.addDefault("database", "yaml");
        fileConfiguration.addDefault("mysql.hostname", "localhost");
        fileConfiguration.addDefault("mysql.port", 3306);
        fileConfiguration.addDefault("mysql.user", "root");
        fileConfiguration.addDefault("mysql.password", "password");
        fileConfiguration.addDefault("mysql.database", "playtime");
        fileConfiguration.addDefault("mysql.table_prefix", "");

        database.copyDefaults(true).save();

        if (Objects.requireNonNull(database.get().getString("database")).equalsIgnoreCase("mysql")) {
            storage = new MysqlDatabase();
        } else {
            storage = new YamlDatabase();
        }
        config.save();
        database.save();
        boolean data = storage.setup();
        if(data){
            new Mccore(this);
            getCommand("playtime").setExecutor(new PlayTimeCommand());
            getCommand("playtime").setTabCompleter(new PlayTimeCommand());
            getCommand("milestone").setExecutor(new MilestoneCommand());
            getCommand("milestone").setTabCompleter(new MilestoneCommand());
            getCommand("repeatingmilestone").setExecutor(new RepeatingMilestoneCommand());
            getCommand("repeatingmilestone").setTabCompleter(new RepeatingMilestoneCommand());

            generateEnglishTranslations();
            generateDutchTranslations();
            generateGermanTranslations();

            langFile = fileManager.getConfig("lang/" + configfileConfiguration.getString("language") + ".yml");

            getLogger().log(Level.INFO, "Loading milestones");
            storage.getMilestones().whenComplete((milestones, throwable) -> {
                for (Milestone storageMilestone : milestones) {
                    milestoneMap.put(storageMilestone.getOnlineTime() * 1000L, storageMilestone);
                }
                getLogger().log(Level.INFO, milestoneMap.size() + " milestones loaded");
            });

            getLogger().log(Level.INFO, "Loading repeating milestones");

            storage.getRepeatingMilestones().whenComplete((repeatingMilestones, throwable) -> {
                for (RepeatingMilestone repeatingMilestone : repeatingMilestones) {
                    repeatedMilestoneList.add(repeatingMilestone);
                }
                getLogger().log(Level.INFO, repeatedMilestoneList.size() + " repeating milestones loaded");
            });

            if (configfileConfiguration.getBoolean("settings.update_check", true)) {
                UpdateChecker.init(this, "https://thedutchruben.nl/api/projects/version/tdrplaytime") // A link to a URL that contains the latest version as String
                        .setDownloadLink("https://www.spigotmc.org/resources/tdrplaytime-milestones-mysql.47894/") // You can either use a custom URL or the Spigot Resource ID
                        .setDonationLink("https://www.paypal.com/paypalme/RGSYT")
                        .setChangelogLink(47894) // Same as for the Download link: URL or Spigot Resource ID
                        .setNotifyOpsOnJoin(true) // Notify OPs on Join when a new version is found (default)
                        .setNotifyByPermissionOnJoin("thedutchruben.updatechecker") // Also notify people on join with this permission
                        .setUserAgent(new UserAgentBuilder().addPluginNameAndVersion())
                        .checkEveryXHours(configfileConfiguration.getDouble("settings.update_checktime", 0.5)) // Check every 30 minutes
                        .suppressUpToDateMessage(true)
                        .checkNow(); // And check right now
            }


            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                long onlineTime = 0;
                try {
                    onlineTime = Playtime.getInstance().getStorage().getPlayTimeByUUID(onlinePlayer.getUniqueId().toString()).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                Playtime.getInstance().getPlayerOnlineTime().put(onlinePlayer.getUniqueId(), onlineTime);
                Playtime.getInstance().getLastCheckedTime().put(onlinePlayer.getUniqueId(), System.currentTimeMillis());
            }

            checkTask = Bukkit.getScheduler().runTaskTimerAsynchronously(getInstance(), () -> {
                Bukkit.getPluginManager().callEvent(new PlayTimeCheckEvent(true));
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    update(onlinePlayer.getUniqueId(), true);
                }
            }, 0, 20);


            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                getLogger().log(Level.INFO, "PlaceholderAPI expansion implemented");
                metrics.addCustomChart(new SimplePie("addons_use", () -> "PlaceholderAPI"));
                new PlaceholderAPIExpansion().register();
            }

            if (Bukkit.getPluginManager().getPlugin("Multiverse-Core") != null) {
                metrics.addCustomChart(new SimplePie("addons_use", () -> "Multiverse-Core"));
            }

            if (Bukkit.getPluginManager().getPlugin("MultiWorld") != null) {
                metrics.addCustomChart(new SimplePie("addons_use", () -> "MultiWorld"));
            }

            if (Bukkit.getPluginManager().getPlugin("mcMMO") != null) {
                metrics.addCustomChart(new SimplePie("addons_use", () -> "mcMMO"));
            }

            if (Bukkit.getPluginManager().getPlugin("HolographicDisplay") != null) {
                metrics.addCustomChart(new SimplePie("addons_use", () -> "HolographicDisplay"));
            }


            metrics.addCustomChart(new SimplePie("bungeecord", () -> String.valueOf(getServer().spigot().getConfig().getBoolean("settings.bungeecord"))));
            metrics.addCustomChart(new SimplePie("database_type", () -> storage.getName()));
            metrics.addCustomChart(new SimplePie("update_checker", () -> String.valueOf(configfileConfiguration.getBoolean("settings.update_check", true))));
            metrics.addCustomChart(new SimplePie("uses_milestones", () -> String.valueOf(milestoneMap.size() > 1)));
            metrics.addCustomChart(new SimplePie("uses_repeating_milestones", () -> String.valueOf(repeatedMilestoneList.size() > 1)));

            metrics.addCustomChart(new SimplePie("language", () -> config.get().getString("language")));
            metrics.addCustomChart(new SingleLineChart("total_play_time", () -> Math.toIntExact(storage.getTotalPlayTime() / 1000 / 60 / 60)));
            metrics.addCustomChart(new SingleLineChart("total_players", () -> Math.toIntExact(storage.getTotalPlayers())));

        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if(checkTask != null){
            checkTask.cancel();
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                forceSave(onlinePlayer.getUniqueId());
            }

            storage.stop();
            playerOnlineTime.clear();
            lastCheckedTime.clear();
            milestoneMap.clear();
            repeatedMilestoneList.clear();
            keyMessageMap.clear();
        }

    }

    public void update(UUID uuid, boolean save) {
        if(lastCheckedTime.get(uuid) == null) return;
        playerOnlineTime.putIfAbsent(uuid, 0L);
        long extraTime = System.currentTimeMillis() - lastCheckedTime.get(uuid);
        lastCheckedTime.replace(uuid, System.currentTimeMillis());
        long newtime = playerOnlineTime.get(uuid) + extraTime;
        Bukkit.getScheduler().runTaskAsynchronously(getInstance(), () -> {
            if (Bukkit.getPlayer(uuid) != null) {
                Bukkit.getPluginManager().callEvent(new PlayTimeUpdatePlayerEvent(Bukkit.getPlayer(uuid), playerOnlineTime.get(uuid), newtime));
            }
        });
        checkMileStones(uuid, playerOnlineTime.get(uuid), newtime);
        playerOnlineTime.replace(uuid, newtime);
        if (save) {
            storage.savePlayTime(uuid.toString(), playerOnlineTime.get(uuid));
        }

    }

    public void forceSave(UUID uuid) {

        long extraTime = System.currentTimeMillis() - lastCheckedTime.get(uuid);
        lastCheckedTime.replace(uuid, System.currentTimeMillis());
        long newtime = playerOnlineTime.get(uuid) + extraTime;
        checkMileStones(uuid, playerOnlineTime.get(uuid), newtime);
        playerOnlineTime.replace(uuid, newtime);
        storage.savePlayTime(uuid.toString(), playerOnlineTime.get(uuid));

    }

    private void checkMileStones(UUID uuid, Long oldtime, long newtime) {
        for (Long i = oldtime; i < newtime; i++) {
            for (RepeatingMilestone repeatingMilestone : repeatedMilestoneList) {
                if (i % (repeatingMilestone.getOnlineTime() * 1000) == 1) {
                    if (repeatingMilestone.isOverrideMe() && milestoneMap.containsKey(i)) {
                        return;
                    }
                    repeatingMilestone.apply(Bukkit.getPlayer(uuid));
                }
            }
            if (milestoneMap.containsKey(i)) {
                milestoneMap.get(i).apply(Bukkit.getPlayer(uuid));
            }
        }
    }

    public String getMessage(String key) {
        if (keyMessageMap.containsKey(key)) {
            return keyMessageMap.get(key);
        }
        if (langFile.get().getString(key) == null) {
            return ChatColor.RED + "No translation found for : " + key;
        }
        keyMessageMap.put(key, ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(langFile.get().getString(key))));
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

    public List<RepeatingMilestone> getRepeatedMilestoneList() {
        return repeatedMilestoneList;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public Map<Long, Milestone> getMilestoneMap() {
        return milestoneMap;
    }


    public void generateEnglishTranslations() {
        FileManager.Config config = fileManager.getConfig("lang/en_GB.yml");
        if (!config.get().contains("version")) {
            getLogger().info("Generate English translations");
            config.get().addDefault("version", 1.0);
            config.get().addDefault("only.player.command", "&cThis is a player only command!");
            //playtime command messages
            config.get().addDefault("command.playtime.timemessage", "&8[&6PlayTime&8] &7Your playtime is &6%D% &7day(s) &6%H% &7hour(s) &6%M% &7minute(s) &6%S% &7second(s)");
            config.get().addDefault("command.playtime.usertimemessage", "&8[&6PlayTime&8] &7%NAME% 's playtime is &6%D% &7day(s) &6%H% &7hour(s) &6%M% &7minute(s) &6%S% &7second(s)");
            config.get().addDefault("command.playtime.resettimeconfirm", "&cUser time reset!");
            config.get().addDefault("command.playtime.resettimeussage", "&cUse : /playtime reset <username>!");
            //milestone command messages
            config.get().addDefault("command.milestone.mustbenumber", "&cThe time parameter must be a number!");
            config.get().addDefault("command.milestone.createusage", "&cUse : /milestone create <name> <time in seconds>!");
            config.get().addDefault("command.milestone.milestonenotexist", "&cThe milestone <name> doesn't exist!");

            config.get().addDefault("command.milestone.additemusage", "&cUse : /milestone additem <milestone>! The item in your hand wil be added!");
            config.get().addDefault("command.milestone.addcommandusage", "&cUse : /milestone addcommand <milestone> <command>!");

            config.get().addDefault("command.milestone.milestonecreated", "&aThe milestone is created!");
            config.get().addDefault("command.milestone.itemadded", "&aYou added succesfull a item to the milestone!");
            config.get().addDefault("command.milestone.commandadded", "&aYou added succesfull a command to the milestone!");
            config.copyDefaults(true).save();
            config.save();
        }

        if (config.get().getDouble("version") < 1.1) {
            getLogger().info("Updating English translations");
            config.get().set("version", 1.1);
            config.get().addDefault("command.milestone.togglefireworkusage", "&cUse : /milestone togglefirework <milestone>!");
            config.get().addDefault("command.milestone.setfireworkamountusage", "&cUse : /milestone setfireworkamount <milestone> <amount>!");
            config.get().addDefault("command.milestone.setfireworkdelayusage", "&cUse : /milestone setfireworkdelay <milestone> <delay in seconds>!");

            config.get().addDefault("command.milestone.fireworktoggled", "&aYou <state> the firework for the milestone");
            config.get().addDefault("command.milestone.setfireworkamount", "&aYou set the firework amount to <amount>");
            config.get().addDefault("command.milestone.setfireworkdelay", "&aYou set the firework amount to <amount>");


            config.copyDefaults(true).save();
            config.save();
        }

    }

    public void generateDutchTranslations() {
        getLogger().info("Generate Dutch translations");
        FileManager.Config config = fileManager.getConfig("lang/nl_NL.yml");
        if (!config.get().contains("version")) {
            config.get().addDefault("version", 1.0);
            config.get().addDefault("only.player.command", "&cDit is een command die alleen een speler kan gebruiken!");
            //playtime command messages
            config.get().addDefault("command.playtime.timemessage", "&8[&6PlayTime&8] &7Jouw speeltijd is &6%D% &7dag(en) &6%H% &7uur &6%M% &7minuut(en) &6%S% &7seconde(n)");
            config.get().addDefault("command.playtime.usertimemessage", "&8[&6PlayTime&8] &7%NAME% ''s speeltijd is &6%D% &7dag(en) &6%H% &7uur &6%M% &7minuut(en) &6%S% &7seconde(n)");
            config.get().addDefault("command.playtime.resettimeconfirm", "&cDe tijd van de speler is gereset!");
            config.get().addDefault("command.playtime.resettimeussage", "&cGebruik : /playtime reset <username>!");
            //milestone command messages
            config.get().addDefault("command.milestone.mustbenumber", "&cDe tijd parameter moet een nummer zijn!");
            config.get().addDefault("command.milestone.createusage", "&cGebruik : /milestone create <name> <time in seconds>!");
            config.get().addDefault("command.milestone.milestonenotexist", "&cDe mijlpaal <name> bestaat niet!");

            config.get().addDefault("command.milestone.additemusage", "&cGebruik : /milestone additem <milestone>! Het item in je hand wordt dan toegevoegd!");
            config.get().addDefault("command.milestone.addcommandusage", "&cGebruik : /milestone addcommand <milestone> <command>!");

            config.get().addDefault("command.milestone.milestonecreated", "&aDe mijlpaal is aangemaakt!");
            config.get().addDefault("command.milestone.itemadded", "&aJe hebt succesvol een item toegevoegd aan de mijlpaal!");
            config.get().addDefault("command.milestone.commandadded", "&aJe hebt succesvol een command toegevoegd aan de mijlpaal!");
            config.copyDefaults(true).save();
            config.save();
        }

        if (config.get().getDouble("version") < 1.1) {
            getLogger().info("Updating Dutch translations");
            config.get().set("version", 1.1);
            config.get().addDefault("command.milestone.togglefireworkusage", "&cGebruik : /milestone togglefirework <mijlpaal>!");
            config.get().addDefault("command.milestone.setfireworkamountusage", "&cGebruik : /milestone setfireworkamount <milestone> <aantal>!");
            config.get().addDefault("command.milestone.setfireworkdelayusage", "&cGebruik : /milestone setfireworkdelay <mijlpaal> <vertraging in seconden>!");

            config.get().addDefault("command.milestone.fireworktoggled", "&aJe <state> het vuurwerk voor de mijlpaal");
            config.get().addDefault("command.milestone.setfireworkamount", "&aJe stelt het vuurwerk aantal in op <amount>");
            config.get().addDefault("command.milestone.setfireworkdelay", "&aJe stelt het vuurwerk vertraging in op <amount>");


            config.copyDefaults(true).save();
            config.save();
        }

    }

    public void generateGermanTranslations() {
        getLogger().info("Generate German translations");
        FileManager.Config config = fileManager.getConfig("lang/de_DE.yml");
        if (!config.get().contains("version")) {
            config.get().addDefault("version", 1.0);
            config.get().addDefault("only.player.command", "&cDies ist ein Kommando nur für Spieler!");
            //playtime command messages
            config.get().addDefault("command.playtime.timemessage", "&8[&6PlayTime&8] &7Deine Spielzeit ist &6%D% &7Tag(e) &6%H% &7Stunde(n) &6%M% &7Minute(n) &6%S% &7Sekunde(n)");
            config.get().addDefault("command.playtime.usertimemessage", "&8[&6PlayTime&8] &7%NAME% ''s Spielzeit ist &6%D% &7Tag(e) &6%H% &7Stunde(n) &6%M% &7Minute(n) &6%S% &7Sekunde(n)");
            config.get().addDefault("command.playtime.resettimeconfirm", "&cDie Zeit des Spielers ist zurückgesetzt!");
            config.get().addDefault("command.playtime.resettimeussage", "&cBenutz : /playtime reset <username>!");
            //milestone command messages
            config.get().addDefault("command.milestone.mustbenumber", "&cDer Zeitparameter muss eine Anzahl sein!");
            config.get().addDefault("command.milestone.createusage", "&cBenutz: /milestone create <name> <time in seconds>!");
            config.get().addDefault("command.milestone.milestonenotexist", "&cDer Meilenstein <name> existiert nicht!");

            config.get().addDefault("command.milestone.additemusage", "&cBenutz : /milestone additem <milestone>! Der Item in Ihrer Hand wird dann hinzugefügt werden!");
            config.get().addDefault("command.milestone.addcommandusage", "&cBenutz : /milestone addcommand <milestone> <command>!");

            config.get().addDefault("command.milestone.milestonecreated", "&aDer Meilenstein ist geschaffen!");
            config.get().addDefault("command.milestone.itemadded", "&aSie haben dem Meilenstein erfolgreich einen Artikel hinzugefügt!");
            config.get().addDefault("command.milestone.commandadded", "&aSie haben dem Meilenstein erfolgreich einen Kommando hinzugefügt!");

            config.copyDefaults(true).save();
            config.save();
        }
        if (config.get().getDouble("version") < 1.1) {
            getLogger().info("Updating German translations");
            config.get().set("version", 1.1);
            config.get().addDefault("command.milestone.togglefireworkusage", "&cVerwendung : /milestone togglefirework <Meilenstein>!");
            config.get().addDefault("command.milestone.setfireworkamountusage", "&cVerwenden Sie : /milestone setfireworkamount <Meilenstein> <Nummer>!");
            config.get().addDefault("command.milestone.setfireworkdelayusage", "&cVerwendung: /milestone setfireworkdelay <Meilenstein> <Verzögerung in Sekunden>!");

            config.get().addDefault("command.milestone.fireworktoggled", "&aSie <state> das Feuerwerk für den Meilenstein");
            config.get().addDefault("command.milestone.setfireworkamount", "&aSie stellen die Feuerwerksnummer auf <amount>");
            config.get().addDefault("command.milestone.setfireworkdelay", "&aDu hast die Feuerwerksverzögerung auf <amount> eingestellt");


            config.copyDefaults(true).save();
            config.save();
        }
    }

    public void setMilestoneMap(Map<Long, Milestone> milestoneMap) {
        this.milestoneMap = milestoneMap;
    }

    public void setRepeatedMilestoneList(List<RepeatingMilestone> repeatedMilestoneList) {
        this.repeatedMilestoneList = repeatedMilestoneList;
    }

    public Map<String, String> getKeyMessageMap() {
        return keyMessageMap;
    }
}

