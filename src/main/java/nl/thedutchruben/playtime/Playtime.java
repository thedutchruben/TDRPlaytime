package nl.thedutchruben.playtime;

import com.earth2me.essentials.Essentials;
import lombok.SneakyThrows;
import nl.thedutchruben.mccore.Mccore;
import nl.thedutchruben.mccore.config.UpdateCheckerConfig;
import nl.thedutchruben.mccore.spigot.commands.CommandRegistry;
import nl.thedutchruben.mccore.utils.config.FileManager;
import nl.thedutchruben.mccore.utils.message.MessageUtil;
import nl.thedutchruben.playtime.database.MysqlDatabase;
import nl.thedutchruben.playtime.database.Storage;
import nl.thedutchruben.playtime.database.YamlDatabase;
import nl.thedutchruben.playtime.events.playtime.PlayTimeCheckEvent;
import nl.thedutchruben.playtime.events.playtime.PlayTimeUpdatePlayerEvent;
import nl.thedutchruben.playtime.extentions.PlaceholderAPIExpansion;
import nl.thedutchruben.playtime.milestone.Milestone;
import nl.thedutchruben.playtime.milestone.RepeatingMilestone;
import nl.thedutchruben.playtime.utils.Replacement;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

/**
 * @author Ruben
 * @version 1.0
 * @since 1.0
 * <p>
 * This class is the main class of the plugin.
 * It is responsible for loading the plugin,
 * registering commands, events, and other stuff.
 * It also handles the database.
 * </p>
 */
public final class Playtime extends JavaPlugin {

    /**
     * The instance of the plugin.
     */
    private static Playtime instance;
    /**
     * The player's playtime
     */
    private final Map<UUID, Long> playerOnlineTime = new HashMap<>();
    /**
     * The player's last checked data.
     */
    private final Map<UUID, LastCheckedData> lastCheckedTime = new HashMap<>();
    /**
     * The filemanager of the plugin.
     */
    private final FileManager fileManager = new FileManager(this);
    /**
     * A map filled with the milestones of the server
     */
    private Map<Long, Milestone> milestoneMap = new HashMap<>();
    /**
     * A list with the repeating milestones.
     */
    private List<RepeatingMilestone> repeatedMilestoneList = new ArrayList<>();
    /**
     * The stores message's of the plugin.
     */
    private final Map<String, String> keyMessageMap = new HashMap<>();
    /**
     * The storage of the plugin.
     */
    private Storage storage;
    /**
     * The language file
     */
    private FileManager.Config langFile;
    /**
     * The stores task for checking data
     */
    private BukkitTask checkTask;
    /**
     * The setting for counting afk time
     */
    private final boolean countAfkTime = fileManager.getConfig("config.yml").get().getBoolean("settings.afk.countAfkTime",
            true);

    /**
     * Count the amount of milestones got.
     */
    private int milestoneGot = 0;

    /**
     * Count the amount of repeating milestones got.
     */
    private int repeatingMilestoneGot = 0;
    /**
     * Count the amount of playtime earned.
     */
    private int playTimeEarned = 0;

    /**
     * The mccore instance
     */

    private Mccore mccore;

    /**
     * Get the instance of the plugin.
     *
     * @return The instance of the plugin.
     */
    public static Playtime getInstance() {
        return instance;
    }

    @SneakyThrows
    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        // Register the metrics of the plugin.
        Metrics metrics = new Metrics(this, 9404);

        // Set up the configs of the plugin.
        FileManager.Config config = fileManager.getConfig("config.yml");
        FileConfiguration configfileConfiguration = config.get();
        configfileConfiguration.addDefault("language", "en_GB");
        configfileConfiguration.addDefault("settings.update_check", true);
        configfileConfiguration.addDefault("settings.cacheTime", 5);
        configfileConfiguration.addDefault("settings.afk.countAfkTime", true);
        configfileConfiguration.addDefault("settings.afk.useEssentialsApi", false);
        configfileConfiguration.addDefault("settings.afk.events.chatResetAfkTime", true);
        configfileConfiguration.addDefault("settings.afk.events.inventoryClickResetAfkTime", true);
        configfileConfiguration.addDefault("settings.afk.events.interactResetAfkTime", true);

        configfileConfiguration.addDefault("settings.update_checktime", 0.5);
        config.copyDefaults(true).save();

        FileManager.Config database = fileManager.getConfig("database.yml");
        FileConfiguration fileConfiguration = database.get();
        fileConfiguration.addDefault("database", "yaml");
        fileConfiguration.addDefault("mysql.hostname", "localhost");
        fileConfiguration.addDefault("mysql.port", 3306);
        fileConfiguration.addDefault("mysql.user", "root");
        fileConfiguration.addDefault("mysql.password", "password");
        fileConfiguration.addDefault("mysql.database", "playtime");
        fileConfiguration.addDefault("mysql.ssl", "false");
        fileConfiguration.addDefault("mysql.table_prefix", "");

        database.copyDefaults(true).save();

        // Select the database type
        if (Objects.requireNonNull(database.get().getString("database")).equalsIgnoreCase("mysql")) {
            storage = new MysqlDatabase();
        } else {
            storage = new YamlDatabase();
        }
        keyMessageMap.clear();
        config.save();
        database.save();
        // Set up the database.
        boolean data = storage.setup();
        if (data) {
            // Register the mc core
            mccore = new Mccore(this, "tdrplaytime", "623a25c0ea9f206b0ba31f3f", Mccore.PluginType.SPIGOT);
            // Generate the language files.
            generateEnglishTranslations();
            generateDutchTranslations();
            generateGermanTranslations();

            // get the language file
            langFile = fileManager.getConfig("lang/" + configfileConfiguration.getString("language") + ".yml");

            // Load all the milestone's
            getLogger().log(Level.INFO, "Loading milestones");
            storage.getMilestones().whenComplete((milestones, throwable) -> {
                for (Milestone storageMilestone : milestones) {
                    milestoneMap.put(storageMilestone.getOnlineTime() * 1000L, storageMilestone);
                }
                getLogger().log(Level.INFO, milestoneMap.size() + " milestones loaded");
            });

            // Load all the repeating milestones.
            getLogger().log(Level.INFO, "Loading repeating milestones");
            storage.getRepeatingMilestones().whenComplete((repeatingMilestones, throwable) -> {
                repeatedMilestoneList.addAll(repeatingMilestones);
                getLogger().log(Level.INFO, repeatedMilestoneList.size() + " repeating milestones loaded");
            });

            // Start the update checker if enabled.
            if (configfileConfiguration.getBoolean("settings.update_check", true)) {
                mccore.startUpdateChecker(new UpdateCheckerConfig("tdrplaytime.checkupdate",
                        (int) (configfileConfiguration.getDouble("settings.update_checktime") * 20 * 60 * 60)));
            }

            // Load the user data from the database.
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                long onlineTime = 0;
                try {
                    onlineTime = Playtime.getInstance().getStorage()
                            .getPlayTimeByUUID(onlinePlayer.getUniqueId().toString()).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                Playtime.getInstance().getPlayerOnlineTime().put(onlinePlayer.getUniqueId(), onlineTime);
                Playtime.getInstance().getLastCheckedTime().put(onlinePlayer.getUniqueId(),
                        new LastCheckedData(System.currentTimeMillis(), onlinePlayer.getLocation()));
            }

            // Start the checkTask
            checkTask = Bukkit.getScheduler().runTaskTimerAsynchronously(getInstance(), () -> {
                Bukkit.getPluginManager().callEvent(new PlayTimeCheckEvent(true));
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    update(onlinePlayer.getUniqueId(), true);
                }
            }, 0, 20 * 30);

            // Register tab completion for milestones.
            CommandRegistry.getTabCompletable().put("milestone", commandSender -> {
                Set<String> events = new HashSet<>();
                for (Milestone milestone : getMilestoneMap().values()) {
                    events.add(milestone.getMilestoneName());
                }
                return events;
            });

            // Register tab completion for repeating milestones.
            CommandRegistry.getTabCompletable().put("repeatingmilestone", commandSender -> {
                Set<String> events = new HashSet<>();
                for (RepeatingMilestone milestone : getRepeatedMilestoneList()) {
                    events.add(milestone.getMilestoneName());
                }
                return events;
            });

            // Register placeholder api expansion.
            if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                getLogger().log(Level.INFO, "PlaceholderAPI expansion implemented");
                metrics.addCustomChart(new SimplePie("addons_use", () -> "PlaceholderAPI"));
                new PlaceholderAPIExpansion().register();
            }

            if (Bukkit.getPluginManager().getPlugin("HolographicDisplay") != null) {
                metrics.addCustomChart(new SimplePie("addons_use", () -> "HolographicDisplay"));
            }

            if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
                metrics.addCustomChart(new SimplePie("addons_use", () -> "WorldGuard"));
            }

            if (Bukkit.getPluginManager().getPlugin("JoinAndQuitMessages") != null) {
                metrics.addCustomChart(new SimplePie("addons_use", () -> "JoinAndQuitMessages"));
            }

            metrics.addCustomChart(new SimplePie("download_source", DownloadSource.MODRINTH::name));

            metrics.addCustomChart(new SimplePie("bungeecord",
                    () -> String.valueOf(getServer().spigot().getConfig().getBoolean("settings.bungeecord"))));
            metrics.addCustomChart(new SimplePie("database_type", () -> storage.getName()));
            metrics.addCustomChart(new SimplePie("update_checker",
                    () -> String.valueOf(configfileConfiguration.getBoolean("settings.update_check", true))));
            metrics.addCustomChart(new SimplePie("uses_milestones", () -> String.valueOf(milestoneMap.size() > 1)));
            metrics.addCustomChart(
                    new SimplePie("uses_repeating_milestones", () -> String.valueOf(repeatedMilestoneList.size() > 1)));
            metrics.addCustomChart(new SimplePie("count_afk_time",
                    () -> String.valueOf(configfileConfiguration.getBoolean(String.valueOf(countAfkTime), true))));
            metrics.addCustomChart(new SimplePie("language", () -> config.get().getString("language")));
            metrics.addCustomChart(new SingleLineChart("total_play_time",
                    () -> Math.toIntExact(storage.getTotalPlayTime() / 1000 / 60 / 60)));
            metrics.addCustomChart(
                    new SingleLineChart("total_players", () -> Math.toIntExact(storage.getTotalPlayers())));

            metrics.addCustomChart(new SingleLineChart("milestone_got", () -> {
                int milestoneCount = Math.toIntExact(milestoneGot);
                milestoneGot = 0;
                return milestoneCount;
            }));

            metrics.addCustomChart(new SingleLineChart("repeating_milestone_got", () -> {
                int milestoneCount = Math.toIntExact(repeatingMilestoneGot);
                repeatingMilestoneGot = 0;
                return milestoneCount;
            }));

            metrics.addCustomChart(new SingleLineChart("playtime_earned", () -> {
                int milestoneCount = Math.toIntExact(playTimeEarned);
                playTimeEarned = 0;
                return milestoneCount;
            }));

        } else {
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (checkTask != null) {
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


    /**
     * Update the playtime of a player.
     *
     * @param uuid The uuid of the player.
     * @param save Whether to save the playtime to the database.
     */
    public void update(UUID uuid, boolean save) {
        if (lastCheckedTime.get(uuid) == null)
            return;
        LastCheckedData lastCheckedData = lastCheckedTime.get(uuid);
        playerOnlineTime.putIfAbsent(uuid, 0L);
        long extraTime = System.currentTimeMillis() - lastCheckedData.getTime();
        lastCheckedTime.replace(uuid,
                new LastCheckedData(System.currentTimeMillis(), Objects.requireNonNull(Bukkit.getPlayer(uuid)).getLocation()));
        if(isAfk(Bukkit.getPlayer(uuid), lastCheckedData)){
            return;
        }
        playTimeEarned += extraTime;
        long newtime = playerOnlineTime.get(uuid) + extraTime;
        Bukkit.getScheduler().runTaskAsynchronously(getInstance(), () -> {
            if (Bukkit.getPlayer(uuid) != null) {
                Bukkit.getPluginManager().callEvent(
                        new PlayTimeUpdatePlayerEvent(Bukkit.getPlayer(uuid), playerOnlineTime.get(uuid), newtime));
            }
        });
        checkMileStones(uuid, playerOnlineTime.get(uuid), newtime);
        playerOnlineTime.replace(uuid, newtime);
        if (save) {
            storage.savePlayTime(uuid.toString(), playerOnlineTime.get(uuid));
        }

    }


    /**
     * Check if a player is afk
     * @param player The player to check
     * @param lastCheckedData The last checked data of the player
     * @return Whether the player is afk
     */
    public boolean isAfk(Player player, LastCheckedData lastCheckedData){
        if(countAfkTime){
            return false;
        }
        FileManager.Config config = fileManager.getConfig("config.yml");
        if(config.get().getBoolean("settings.afk.useEssentialsApi", false)){
            if (Bukkit.getPluginManager().getPlugin("Essentials") != null) {
                Essentials essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
                return essentials.getUser(player).isAfk();
            }
        }


        return player != null && lastCheckedData.getLocation().getX() == player.getLocation().getX()
                && lastCheckedData.getLocation().getY() == player.getLocation().getY()
                && lastCheckedData.getLocation().getZ() == player.getLocation().getZ();
    }

    /**
     * Force save the playtime of a player.
     * Doesn't check if the player is afk.
     *
     * @param uuid The uuid of the player.
     */
    public void forceSave(UUID uuid) {

        long extraTime = System.currentTimeMillis() - lastCheckedTime.get(uuid).getTime();
        lastCheckedTime.replace(uuid,
                new LastCheckedData(System.currentTimeMillis(), Objects.requireNonNull(Bukkit.getPlayer(uuid)).getLocation()));
        long newtime = playerOnlineTime.get(uuid) + extraTime;
        checkMileStones(uuid, playerOnlineTime.get(uuid), newtime);
        playerOnlineTime.replace(uuid, newtime);
        storage.savePlayTime(uuid.toString(), playerOnlineTime.get(uuid));

    }

    /**
     * Check if a player has reached a milestone.
     *
     * @param uuid    The uuid of the player.
     * @param oldtime The old playtime of the player.
     * @param newtime The new playtime of the player.
     */
    private void checkMileStones(UUID uuid, Long oldtime, long newtime) {
        for (Long i = oldtime; i < newtime; i++) {
            for (RepeatingMilestone repeatingMilestone : repeatedMilestoneList) {
                if (i % (repeatingMilestone.getOnlineTime() * 1000) == 1) {
                    if (repeatingMilestone.isOverrideMe() && milestoneMap.containsKey(i)) {
                        return;
                    }
                    repeatingMilestone.apply(Bukkit.getPlayer(uuid));
                    repeatingMilestoneGot++;
                }
            }
            if (milestoneMap.containsKey(i)) {
                milestoneMap.get(i).apply(Bukkit.getPlayer(uuid));
                milestoneGot++;
            }
        }
    }

    /**
     * Get the message from the language file.
     *
     * @param key The key of the message.
     * @return The message.
     */
    public String getMessage(String key, Replacement... replacements) {

        if (!keyMessageMap.containsKey(key)) {
            if (langFile.get().getString(key) == null) {
                return ChatColor.RED + "No translation found for : " + key;
            }
            keyMessageMap.put(key,
                    MessageUtil.translateHexColorCodes("<",">",ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(langFile.get().getString(key)))));
        }
        String message = keyMessageMap.get(key);

        for (Replacement replacement : replacements) {
            message = message.replaceAll(replacement.getFrom(), replacement.getTo());
        }

        return message;
    }

    /**
     * Get the mccore instance
     * @return The mccore instance
     */
    public Mccore getMccore() {
        return mccore;
    }

    /**
     * The storage of the plugin
     *
     * @return The storage of the plugin.
     */
    public Storage getStorage() {
        return storage;
    }

    /**
     * The last checked time of a player.
     *
     * @return The last checked time.
     */
    public Map<UUID, LastCheckedData> getLastCheckedTime() {
        return lastCheckedTime;
    }

    /**
     * The online time of a player.
     *
     * @return The online time.
     */
    public Map<UUID, Long> getPlayerOnlineTime() {
        return playerOnlineTime;
    }

    /**
     * Get the repeating milestones.
     * @return A list with the repeating milestones.
     */
    public List<RepeatingMilestone> getRepeatedMilestoneList() {
        return repeatedMilestoneList;
    }

    /**
     * Set the repeating milestones.
     * @param repeatedMilestoneList A list with the repeating milestones.
     */
    public void setRepeatedMilestoneList(List<RepeatingMilestone> repeatedMilestoneList) {
        this.repeatedMilestoneList = repeatedMilestoneList;
    }

    /**
     * Get the filemanager of the plugin.
     *
     * @return The filemanager of the plugin.
     */
    public FileManager getFileManager() {
        return fileManager;
    }

    /**
     * Get the milestone map.
     *
     * @return The milestone map.
     */
    public Map<Long, Milestone> getMilestoneMap() {
        return milestoneMap;
    }

    /**
     * Set the milestone map.
     * @param milestoneMap
     */
    public void setMilestoneMap(Map<Long, Milestone> milestoneMap) {
        this.milestoneMap = milestoneMap;
    }

    /**
     *
     * @return
     */
    public FileManager.Config getLangFile() {
        return langFile;
    }

    /**
     * Generate the English translations.
     */
    public void generateEnglishTranslations() {
        FileManager.Config config = fileManager.getConfig("lang/en_GB.yml");
        if (!config.get().contains("version")) {
            getLogger().info("Generate English translations");
            config.get().addDefault("version", 1.0);
            config.get().addDefault("only.player.command", "&cThis is a player only command!");
            // playtime command messages
            config.get().addDefault("command.playtime.timemessage",
                    "&8[&6PlayTime&8] &7Your playtime is &6%D% &7day(s) &6%H% &7hour(s) &6%M% &7minute(s) &6%S% &7second(s)");
            config.get().addDefault("command.playtime.usertimemessage",
                    "&8[&6PlayTime&8] &7%NAME% 's playtime is &6%D% &7day(s) &6%H% &7hour(s) &6%M% &7minute(s) &6%S% &7second(s)");
            config.get().addDefault("command.playtime.resettimeconfirm", "&cUser time reset!");
            // milestone command messages
            config.get().addDefault("command.milestone.mustbenumber", "&cThe time parameter must be a number!");
            config.get().addDefault("command.milestone.milestonenotexist", "&cThe milestone <name> doesn't exist!");

            config.get().addDefault("command.milestone.milestonecreated", "&aThe milestone is created!");
            config.get().addDefault("command.milestone.itemadded", "&aYou added succesfull a item to the milestone!");
            config.get().addDefault("command.milestone.commandadded",
                    "&aYou added succesfull a command to the milestone!");
            config.copyDefaults(true).save();
            config.save();
        }

        if (config.get().getDouble("version") < 1.1) {
            getLogger().info("Updating English translations to version 1.1");
            config.get().set("version", 1.1);

            config.get().addDefault("command.milestone.fireworktoggled",
                    "&aYou <state> the firework for the milestone");
            config.get().addDefault("command.milestone.setfireworkamount", "&aYou set the firework amount to <amount>");
            config.get().addDefault("command.milestone.setfireworkdelay", "&aYou set the firework amount to <amount>");

            config.copyDefaults(true).save();
            config.save();
        }

        if (config.get().getDouble("version") < 1.2) {
            getLogger().info("Updating English translations to version 1.2");
            config.get().set("version", 1.2);
            config.get().addDefault("command.defaults.enabled", "Enabled");
            config.get().addDefault("command.defaults.disabled", "Disabled");

            config.get().addDefault("command.milestone.repeatingmilestoneremoved",
                    "&aYou have successfully removed the repeating milestone!");
            config.get().addDefault("command.milestone.milestoneremoved",
                    "&aYou have successfully removed the milestone!");

            config.get().addDefault("command.milestone.itemremoved", "&aYou removed an item from the milestone!");
            config.get().addDefault("command.milestone.commandremoved", "&aYou removed an command from the milestone!");

            config.get().addDefault("command.playtime.timeadded", "&aYou have successfully added playtime to <player>");
            config.get().addDefault("command.playtime.timeremoved",
                    "&aYou have successfully removed playtime from <player>");
            config.get().addDefault("command.milestone.list",
                    Arrays.asList("%MILESTONE_NAME%", " Time: Days: %D% Hours: %H% ,Minute's: %M% Seconds's: %S%"));
            config.get().addDefault("command.milestone.info",
                    Arrays.asList("%MILESTONE_NAME%", " Time: Days: %D% Hours: %H% ,Minute's: %M% Seconds's: %S%",
                            " Rewards:", "    Commands(%REWARD_COMMAND_COUNT%):", "%REWARD_COMMAND%",
                            "    Items(%REWARD_ITEMS_COUNT%):", "%REWARD_ITEMS%"));

            config.copyDefaults(true).save();
            config.save();
        }

        if (config.get().getDouble("version") < 1.3) {
            getLogger().info("Updating English translations to version 1.3");
            config.get().set("version", 1.3);
            config.get().addDefault("command.playtime.imported", "&aYou have successfully imported <count> players!");
            config.copyDefaults(true).save();
            config.save();
        }

    }

    /**
     * Generate the Dutch translations.
     */
    public void  generateDutchTranslations() {
        FileManager.Config config = fileManager.getConfig("lang/nl_NL.yml");
        if (!config.get().contains("version")) {
            getLogger().info("Generate Dutch translations");
            config.get().addDefault("version", 1.0);
            config.get().addDefault("only.player.command", "&cDit is een command die alleen een speler kan gebruiken!");
            // playtime command messages
            config.get().addDefault("command.playtime.timemessage",
                    "&8[&6PlayTime&8] &7Jouw speeltijd is &6%D% &7dag(en) &6%H% &7uur &6%M% &7minuut(en) &6%S% &7seconde(n)");
            config.get().addDefault("command.playtime.usertimemessage",
                    "&8[&6PlayTime&8] &7%NAME% ''s speeltijd is &6%D% &7dag(en) &6%H% &7uur &6%M% &7minuut(en) &6%S% &7seconde(n)");
            config.get().addDefault("command.playtime.resettimeconfirm", "&cDe tijd van de speler is gereset!");
            // milestone command messages
            config.get().addDefault("command.milestone.mustbenumber", "&cDe tijd parameter moet een nummer zijn!");
            config.get().addDefault("command.milestone.milestonenotexist", "&cDe mijlpaal <name> bestaat niet!");

            config.get().addDefault("command.milestone.milestonecreated", "&aDe mijlpaal is aangemaakt!");
            config.get().addDefault("command.milestone.itemadded",
                    "&aJe hebt succesvol een item toegevoegd aan de mijlpaal!");
            config.get().addDefault("command.milestone.commandadded",
                    "&aJe hebt succesvol een command toegevoegd aan de mijlpaal!");
            config.copyDefaults(true).save();
            config.save();
        }

        if (config.get().getDouble("version") < 1.1) {
            getLogger().info("Updating Dutch translations to 1.1");
            config.get().set("version", 1.1);

            config.get().addDefault("command.milestone.fireworktoggled", "&aJe <state> het vuurwerk voor de mijlpaal");
            config.get().addDefault("command.milestone.setfireworkamount",
                    "&aJe stelt het vuurwerk aantal in op <amount>");
            config.get().addDefault("command.milestone.setfireworkdelay",
                    "&aJe stelt het vuurwerk vertraging in op <amount>");

            config.copyDefaults(true).save();
            config.save();
        }

        if (config.get().getDouble("version") < 1.2) {
            getLogger().info("Updating Dutch translations to version 1.2");
            config.get().set("version", 1.2);
            config.get().addDefault("command.defaults.enabled", "Aan");
            config.get().addDefault("command.defaults.disabled", "Uit");

            config.get().addDefault("command.milestone.repeatingmilestoneremoved",
                    "&aJe hebt successvol de herhalende mijlpaal verwijderd!");
            config.get().addDefault("command.milestone.milestoneremoved",
                    "&aJe hebt successvol de mijlpaal verwijderd!");

            config.get().addDefault("command.milestone.itemremoved", "&aJe hebt een item uit de mijlpaal verwijderd!");
            config.get().addDefault("command.milestone.commandremoved",
                    "&aJe hebt een opdracht uit de mijlpaal verwijderd!");

            config.get().addDefault("command.playtime.timeadded",
                    "&aJe hebt met succes speeltijd toegevoegd aan <player>");
            config.get().addDefault("command.playtime.timeremoved",
                    "&aJe hebt de speeltijd met succes verwijderd van <player>");
            config.get().addDefault("command.milestone.list",
                    Arrays.asList("%MILESTONE_NAME%", " Tijd: Dagen: %D% Uren: %H%, Minuten: %M% Seconden: %S%"));
            config.get().addDefault("command.milestone.info",
                    Arrays.asList("%MILESTONE_NAME%", " Tijd: Dagen: %D% Uren: %H%, Minuten: %M% Seconden: %S%",
                            " Beloningen:", "    Commando's(%REWARD_COMMAND_COUNT%):", "%REWARD_COMMAND%",
                            "    Artikelen(%REWARD_ITEMS_COUNT%):", "%REWARD_ITEMS%"));

            config.copyDefaults(true).save();
            config.save();
        }

        if (config.get().getDouble("version") < 1.3) {
            getLogger().info("Updating Dutch translations to version 1.3");
            config.get().set("version", 1.3);
            config.get().addDefault("command.playtime.imported", "&aJe hebt met success <count> spelers over gezet!");
            config.copyDefaults(true).save();
            config.save();
        }

    }

    /**
     * Generate the German translations.
     */
    public void generateGermanTranslations() {
        FileManager.Config config = fileManager.getConfig("lang/de_DE.yml");
        if (!config.get().contains("version")) {
            getLogger().info("Generate German translations");
            config.get().addDefault("version", 1.0);
            config.get().addDefault("only.player.command", "&cDies ist ein Kommando nur für Spieler!");
            // playtime command messages
            config.get().addDefault("command.playtime.timemessage",
                    "&8[&6PlayTime&8] &7Deine Spielzeit ist &6%D% &7Tag(e) &6%H% &7Stunde(n) &6%M% &7Minute(n) &6%S% &7Sekunde(n)");
            config.get().addDefault("command.playtime.usertimemessage",
                    "&8[&6PlayTime&8] &7%NAME% ''s Spielzeit ist &6%D% &7Tag(e) &6%H% &7Stunde(n) &6%M% &7Minute(n) &6%S% &7Sekunde(n)");
            config.get().addDefault("command.playtime.resettimeconfirm", "&cDie Zeit des Spielers ist zurückgesetzt!");
            // milestone command messages
            config.get().addDefault("command.milestone.mustbenumber", "&cDer Zeitparameter muss eine Anzahl sein!");
            config.get().addDefault("command.milestone.milestonenotexist", "&cDer Meilenstein <name> existiert nicht!");

            config.get().addDefault("command.milestone.milestonecreated", "&aDer Meilenstein ist geschaffen!");
            config.get().addDefault("command.milestone.itemadded",
                    "&aSie haben dem Meilenstein erfolgreich einen Artikel hinzugefügt!");
            config.get().addDefault("command.milestone.commandadded",
                    "&aSie haben dem Meilenstein erfolgreich einen Kommando hinzugefügt!");

            config.copyDefaults(true).save();
            config.save();
        }
        if (config.get().getDouble("version") < 1.1) {
            getLogger().info("Updating German translations to 1.1");
            config.get().set("version", 1.1);

            config.get().addDefault("command.milestone.fireworktoggled",
                    "&aSie <state> das Feuerwerk für den Meilenstein");
            config.get().addDefault("command.milestone.setfireworkamount",
                    "&aSie stellen die Feuerwerksnummer auf <amount>");
            config.get().addDefault("command.milestone.setfireworkdelay",
                    "&aDu hast die Feuerwerksverzögerung auf <amount> eingestellt");

            config.copyDefaults(true).save();
            config.save();
        }

        if (config.get().getDouble("version") < 1.2) {
            getLogger().info("Updating German translations to version 1.2");
            config.get().set("version", 1.2);
            config.get().addDefault("command.defaults.enabled", "An");
            config.get().addDefault("command.defaults.disabled", "aus");

            config.get().addDefault("command.milestone.repeatingmilestoneremoved",
                    "&aSie haben den sich wiederholenden Meilenstein erfolgreich entfernt!");
            config.get().addDefault("command.milestone.milestoneremoved",
                    "&aSie haben den Meilenstein erfolgreich entfernt!");

            config.get().addDefault("command.milestone.itemremoved",
                    "&aSie haben ein Element aus dem Meilenstein entfernt!");
            config.get().addDefault("command.milestone.commandremoved",
                    "&aSie haben eine Aufgabe aus dem Meilenstein entfernt!");

            config.get().addDefault("command.playtime.timeadded",
                    "&aSie haben <player> erfolgreich Spielzeit hinzugefügt");
            config.get().addDefault("command.playtime.timeremoved",
                    "&aSie haben die Spielzeit erfolgreich von <player> entfernt");
            config.get().addDefault("command.milestone.list",
                    Arrays.asList("%MILESTONE_NAME%", " Zeit: Tage: %D% Stunden: %H%, Minuten: %M% Sekunden: %S%"));
            config.get().addDefault("command.milestone.info",
                    Arrays.asList("%MILESTONE_NAME%", " Zeit: Tage: %D% Stunden: %H%, Minuten: %M% Sekunden: %S%",
                            " Belohnung:", "    Befehle(%REWARD_COMMAND_COUNT%):", "%REWARD_COMMAND%",
                            "    Artikel(%REWARD_ITEMS_COUNT%):", "%REWARD_ITEMS%"));

            config.copyDefaults(true).save();
            config.save();
        }
        if (config.get().getDouble("version") < 1.3) {
            getLogger().info("Updating German translations to version 1.3");
            config.get().set("version", 1.3);
            config.get().addDefault("command.playtime.imported",
                    "&aSie haben <count> Spieler erfolgreich transferiert!");
            config.copyDefaults(true).save();
            config.save();
        }
    }

    /** Get the key message map.
     * @return The key message map.
     */
    public Map<String, String> getKeyMessageMap() {
        return keyMessageMap;
    }

    /**
     *
     */
    enum DownloadSource {
        /**
         * Spigot.org
         */
        SPIGOT,
        /**
         * curseforge
         */
        CURSE_FORGE,
        /**
         * github.com
         */
        GITHUB,
        /**
         * <a href="https://hangar.papermc.io/">...</a>
         */
        HANGAR,
        /**
         * modrinth.com
         */
        MODRINTH,
    }

    /**
     * Small simple class to save the temporary data of a player.
     */
    public static class LastCheckedData {
        /**
         * The last time the player was checked.
         */
        private final long time;

        /**
         * The last location of the player
         */
        private final Location location;

        /**
         * Creates a new instance of LastCheckedData.
         *
         * @param time     The last time the player was checked.
         * @param location The last location of the player.
         */
        public LastCheckedData(long time, Location location) {
            this.time = time;
            this.location = location;
        }

        /**
         * Gets the last time the player was checked.
         *
         * @return The last time the player was checked.
         */
        public long getTime() {
            return time;
        }

        /**
         * Gets the last location of the player.
         *
         * @return The last location of the player.
         */
        public Location getLocation() {
            return location;
        }
    }
}
