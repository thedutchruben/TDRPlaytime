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
    PLAYTIME_INFO_OWN("command.playtime.timemessage", "&8[&6PlayTime&8] &7Your playtime is &6%D% &7day(s) &6%H% &7hour(s) &6%M% &7minute(s) &6%S% &7second(s)"),
    PLAYTIME_INFO_OTHER("command.playtime.usertimemessage", "&8[&6PlayTime&8] &7%NAME% 's playtime is &6%D% &7day(s) &6%H% &7hour(s) &6%M% &7minute(s) &6%S% &7second(s)"),
    PLAYER_RESET_CONFIRM("command.playtime.resettimeconfirm", "&cUser time reset!"),
    MILESTONE_MUST_BE_NUMBER("command.milestone.mustbenumber", "&cThe time parameter must be a number!"),
    MILESTONE_NOT_EXIST("command.milestone.milestonenotexist", "&cThe milestone <name> doesn't exist!"),
    MILESTONE_CREATED("command.milestone.milestonecreated", "&aThe milestone is created!"),
    ITEM_ADDED("command.milestone.itemadded", "&aYou added successfully an item to the milestone!"),
    COMMAND_ADDED("command.milestone.commandadded", "&aYou added successfully a command to the milestone!"),
    FIREWORK_TOGGLED("command.milestone.fireworktoggled", "&aYou <state> the firework for the milestone"),
    SET_FIREWORK_AMOUNT("command.milestone.setfireworkamount", "&aYou set the firework amount to <amount>"),
    SET_FIREWORK_DELAY("command.milestone.setfireworkdelay", "&aYou set the firework delay to <amount>"),
    REPEATING_MILESTONE_REMOVED("command.milestone.repeatingmilestoneremoved", "&aYou have successfully removed the repeating milestone!"),
    MILESTONE_REMOVED("command.milestone.milestoneremoved", "&aYou have successfully removed the milestone!"),
    ITEM_REMOVED("command.milestone.itemremoved", "&aYou removed an item from the milestone!"),
    COMMAND_REMOVED("command.milestone.commandremoved", "&aYou removed a command from the milestone!"),
    TIME_ADDED("command.playtime.timeadded", "&aYou have successfully added playtime to <player>"),
    TIME_REMOVED("command.playtime.timeremoved", "&aYou have successfully removed playtime from <player>"),
    MILESTONE_LIST("command.milestone.list", "%MILESTONE_NAME% Time: Days: %D% Hours: %H% ,Minute's: %M% Seconds's: %S%"),
    MILESTONE_INFO("command.milestone.info", "%MILESTONE_NAME% Time: Days: %D% Hours: %H% ,Minute's: %M% Seconds's: %S% Rewards: Commands(%REWARD_COMMAND_COUNT%): %REWARD_COMMAND% Items(%REWARD_ITEMS_COUNT%): %REWARD_ITEMS%"),
    PLAYTIME_IMPORTED("command.playtime.imported", "&aYou have successfully imported <count> players!"),
    MESSAGE_ADDED("command.milestone.messageadded", "&aYou have successfully added a message to a milestone!"),
    MESSAGE_REMOVED("command.milestone.messageremoved", "&aYou have removed a message from a milestone!"),
    MILESTONE_REWARDS_APPLIED("command.milestone.rewardsapplied", "&aYou have successfully applied the rewards!"),
    PLAYER_DOES_NOT_EXIST("command.playtime.playerdoesnotexist", "&cThe player does not exist!"),
    MILESTONE_DOES_NOT_EXIST("command.milestone.milestonedoesnotexist", "&cThe milestone does not exist!"),
    MILESTONE_COULD_NOT_BE_CREATED("command.milestone.milestonecouldnotbecreated", "&cThe milestone could not be created!");

    @Getter
    private static final Map<String, String> messages = new HashMap<>();
    private final String path;
    private final String fallBack;

    Messages(String path, String fallBack) {
        this.path = path;
        this.fallBack = fallBack;
    }

    /**
     * Setup the default messages
     */
    public static void setupDefaults() {
        YamlConfiguration file = Playtime.getInstance().getFileManager().getConfig("lang/translations.yml").get();
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
        String message = messages.computeIfAbsent(path, k -> {
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
