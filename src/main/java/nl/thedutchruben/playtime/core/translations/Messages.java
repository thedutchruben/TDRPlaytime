package nl.thedutchruben.playtime.core.translations;

import lombok.Getter;
import nl.thedutchruben.mccore.utils.message.MessageUtil;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.utils.Replacement;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum Messages {

    ONLY_PLAYER_COMMAND("only.player.command", "&cThis is a player only command!"),

    PLAYTIME_INFO_OWN("command.playtime.time_message", "&8[&6PlayTime&8] &7Your playtime is &6%D% &7day(s) &6%H% &7hour(s) &6%M% &7minute(s) &6%S% &7second(s)"),
    PLAYTIME_INFO_OTHER("command.playtime.user_time_message", "&8[&6PlayTime&8] &7%NAME% 's playtime is &6%D% &7day(s) &6%H% &7hour(s) &6%M% &7minute(s) &6%S% &7second(s)"),
    PLAYER_RESET_CONFIRM("command.playtime.reset_time_confirm", "&cUser time reset!"),
    TIME_ADDED("command.playtime.time_added", "&aYou have successfully added playtime to <player>"),
    TIME_REMOVED("command.playtime.time_removed", "&aYou have successfully removed playtime from <player>"),
    PLAYTIME_IMPORTED("command.playtime.imported", "&aYou have successfully imported <count> players!"),
    PLAYER_DOES_NOT_EXIST("command.playtime.player_does_not_exist", "&cThe player does not exist!"),

    MILESTONE_CREATED("command.milestone.created", "&aThe milestone is created!"),
    MILESTONE_ITEM_ADDED("command.milestone.item_added", "&aYou added successfully an item to the milestone!"),
    MILESTONE_COMMAND_ADDED("command.milestone.command_added", "&aYou added successfully a command to the milestone!"),
    MILESTONE_FIREWORK_TOGGLED("command.milestone.firework_toggled", "&aYou <state> the firework for the milestone"),
    MILESTONE_SET_FIREWORK_AMOUNT("command.milestone.set_firework_amount", "&aYou set the firework amount to <amount>"),
    MILESTONE_SET_FIREWORK_DELAY("command.milestone.set_firework_delay", "&aYou set the firework delay to <amount>"),
    MILESTONE_REMOVED("command.milestone.removed", "&aYou have successfully removed the milestone!"),
    MILESTONE_COMMAND_REMOVED("command.milestone.command_removed", "&aYou removed a command from the milestone!"),
    MILESTONE_LIST("command.milestone.list", "%MILESTONE_NAME% Time: Days: %D% Hours: %H% ,Minute's: %M% ,Seconds's: %S%"),
    MILESTONE_INFO("command.milestone.info", "%MILESTONE_NAME% Time: Days: %D% Hours: %H% ,Minute's: %M% ,Seconds's: %S% Rewards: Commands(%REWARD_COMMAND_COUNT%): %REWARD_COMMAND% Items(%REWARD_ITEMS_COUNT%): %REWARD_ITEMS%"),
    MILESTONE_MESSAGE_ADDED("command.milestone.message_added", "&aYou have successfully added a message to a milestone!"),
    MILESTONE_MESSAGE_REMOVED("command.milestone.message_removed", "&aYou have removed a message from a milestone!"),
    MILESTONE_REWARDS_APPLIED("command.milestone.rewards_applied", "&aYou have successfully applied the rewards!"),
    MILESTONE_DOES_NOT_EXIST("command.milestone.milestone_does_not_exist", "&cThe milestone does not exist!"),
    MILESTONE_COULD_NOT_BE_CREATED("command.milestone.could_not_be_created", "&cThe milestone could not be created!"),

    REPEATING_MILESTONE_REMOVED("command.repeating_milestone.removed", "&aYou have successfully removed the repeating milestone!"),
    REPEATING_MILESTONE_DOES_NOT_EXIST("command.repeating_milestone.does_not_exist", "&cThe repeating milestone does not exist!"),
    REPEATING_MILESTONE_COULD_NOT_BE_CREATED("command.repeating_milestone.could_not_be_created", "&cThe repeating milestone could not be created!"),
    REPEATING_MILESTONE_CREATED("command.repeating_milestone.created", "&aYou have successfully created a repeating milestone!"),
    REPEATING_MILESTONE_DELETED("command.repeating_milestone.deleted", "&aYou have successfully deleted a repeating milestone!"),
    REPEATING_MILESTONE_ITEM_ADDED("command.repeating_milestone.item_added", "&aYou have successfully added an item to the repeating milestone!"),
    REPEATING_MILESTONE_ITEM_REMOVED("command.repeating_milestone.item_removed", "&aYou have successfully removed an item from the repeating milestone!"),
    REPEATING_MILESTONE_COMMAND_ADDED("command.repeating_milestone.command_added", "&aYou have successfully added a command to the repeating milestone!"),
    REPEATING_MILESTONE_FIREWORK_TOGGLED("command.repeating_milestone.firework_toggled", "&aYou <state> the firework for the repeating milestone"),
    REPEATING_MILESTONE_SET_FIREWORK_AMOUNT("command.repeating_milestone.set_firework_amount", "&aYou set the firework amount to <amount>"),
    REPEATING_MILESTONE_SET_FIREWORK_DELAY("command.repeating_milestone.set_firework_delay", "&aYou set the firework delay to <amount>"),

    // Playtime history messages
    PLAYTIME_HISTORY_HEADER("command.playtime_history.header", "&8[&6PlayTime&8] &7Playtime History for &6%NAME%"),
    PLAYTIME_HISTORY_ENTRY("command.playtime_history.entry", "&8[&6PlayTime&8] &7%EVENT% on &6%DATE% &7for &6%TIME%"),
    PLAYTIME_HISTORY_FOOTER("command.playtime_history.footer", "&8[&6PlayTime&8] &7Showing &6%CURRENT%&7/&6%TOTAL% &7entries"),
    PLAYTIME_HISTORY_NO_ENTRIES("command.playtime_history.no_entries", "&8[&6PlayTime&8] &cNo playtime history found for &6%NAME%"),

    PLAYER_NOW_AFK("afk.player_now_afk", "&8[&6PlayTime&8] &7%player% is now AFK"),
    PLAYER_NO_LONGER_AFK("afk.player_no_longer_afk", "&8[&6PlayTime&8] &7%player% is no longer AFK"),
    PLAYER_AFK_STATUS("afk.player_afk_status", "&8[&6PlayTime&8] &7%player% has been AFK for %D% day(s) %H% hour(s) %M% minute(s) %S% second(s)"),
    PLAYER_AFK_TOTAL("afk.player_afk_total", "&8[&6PlayTime&8] &7%player% has spent a total of %D% day(s) %H% hour(s) %M% minute(s) %S% second(s) AFK"),
    PLAYER_ACTIVE_TIME("afk.player_active_time", "&8[&6PlayTime&8] &7%player%'s active playtime is %D% day(s) %H% hour(s) %M% minute(s) %S% second(s)"),
    PLAYER_MANUALLY_SET_AFK("afk.player_manually_set_afk", "&8[&6PlayTime&8] &7You have been marked as AFK"),
    PLAYER_MANUALLY_SET_NOT_AFK("afk.player_manually_set_not_afk", "&8[&6PlayTime&8] &7You are no longer AFK"),
    ;

    @Getter
    private static final Map<String, String> messages = new HashMap<>();
    private final String path;
    private final String fallBack;

    Messages(String path, String fallBack) {
        this.path = path;
        this.fallBack = fallBack;
    }

    /**
     * Set up the default messages
     */
    public static void setupDefaults() {
        YamlConfiguration file = Playtime.getInstance().getFileManager().getConfig("translations.yml").get();
        for (Messages value : Messages.values()) {
            if (!file.contains(value.path)) {
                file.set(value.path, value.fallBack);
            }
        }

        Playtime.getInstance().getFileManager().getConfig("lang/translations.yml").save();
    }

    /**
     * Get the message from the path
     *
     * @param replacements The replacements to replace in the message
     * @return The message
     */
    public String getMessage(Replacement... replacements) {
        String message = messages.computeIfAbsent(path, a -> {
            YamlConfiguration file = Playtime.getInstance().getFileManager().getConfig("lang/translations.yml").get();
            return file.getString(path, fallBack);
        });
        message = MessageUtil.translateHexColorCodes("<", ">", ChatColor.translateAlternateColorCodes('&', message));
        for (Replacement replacement : replacements) {
            message = message.replace(replacement.getFrom(), replacement.getTo());
        }
        return message;
    }
}