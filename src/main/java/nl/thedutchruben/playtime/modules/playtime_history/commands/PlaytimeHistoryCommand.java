package nl.thedutchruben.playtime.modules.playtime_history.commands;

import nl.thedutchruben.mccore.spigot.commands.Command;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.Settings;
import nl.thedutchruben.playtime.core.objects.PlaytimeHistory;
import nl.thedutchruben.playtime.core.translations.Messages;
import nl.thedutchruben.playtime.utils.Replacement;
import nl.thedutchruben.playtime.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * Command to view playtime history for yourself or other players
 */
@Command(command = "playtimehistory", aliases = {"pthistory", "timehistory"},
         description = "View playtime history", permission = "playtime.history", console = true)
public class PlaytimeHistoryCommand {

    /**
     * Default command to view your own playtime history or another player's history
     *
     * @param commandSender The sender of the command
     * @param args Command arguments
     */
    public void execute(CommandSender commandSender, String[] args) {
        int limit = Settings.PLAYTIME_HISTORY_MAX_ENTRIES.getValueAsInteger();

        if (args.length == 0) {
            // View own history (only players can do this, not console)
            if (!(commandSender instanceof Player)) {
                commandSender.sendMessage(Messages.ONLY_PLAYER_COMMAND.getMessage());
                return;
            }

            Player player = (Player) commandSender;
            showPlaytimeHistory(player.getUniqueId(), player.getName(), commandSender, limit);
        } else {
            // Check permission for viewing other players' history
            if (!commandSender.hasPermission("playtime.history.others")) {
                commandSender.sendMessage(Messages.ONLY_PLAYER_COMMAND.getMessage());
                return;
            }

            String targetName = args[0];

            // Try to parse custom limit if provided
            if (args.length > 1) {
                try {
                    limit = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    // Ignore invalid number and use default limit
                }
            }

            // Find player UUID from name
            Player targetPlayer = Bukkit.getPlayer(targetName);
            if (targetPlayer != null) {
                showPlaytimeHistory(targetPlayer.getUniqueId(), targetPlayer.getName(), commandSender, limit);
            } else {
                // Player not online, load from storage by name
                int finalLimit = limit;
                Playtime.getInstance().getStorage().loadUserByName(targetName)
                    .thenAccept(playtimeUser -> {
                        if (playtimeUser == null) {
                            commandSender.sendMessage(Messages.PLAYER_DOES_NOT_EXIST.getMessage());
                            return;
                        }

                        showPlaytimeHistoryByName(targetName, commandSender, finalLimit);
                    });
            }
        }
    }

    /**
     * Show playtime history using player UUID
     *
     * @param uuid The UUID of the player
     * @param playerName The player's name for display purposes
     * @param sender The command sender who will receive the history
     * @param limit Maximum number of entries to show
     */
    private void showPlaytimeHistory(UUID uuid, String playerName, CommandSender sender, int limit) {
        Playtime.getInstance().getStorage().getPlaytimeHistory(uuid, limit)
            .thenAccept(history -> displayPlaytimeHistory(history, playerName, sender, limit));
    }

    /**
     * Show playtime history using player name
     *
     * @param playerName The player's name
     * @param sender The command sender who will receive the history
     * @param limit Maximum number of entries to show
     */
    private void showPlaytimeHistoryByName(String playerName, CommandSender sender, int limit) {
        Playtime.getInstance().getStorage().getPlaytimeHistoryByName(playerName, limit)
            .thenAccept(history -> displayPlaytimeHistory(history, playerName, sender, limit));
    }

    /**
     * Display formatted history entries to the command sender
     *
     * @param history The list of history entries to display
     * @param playerName The player's name for display purposes
     * @param sender The command sender who will receive the history
     * @param limit The requested maximum entries
     */
    private void displayPlaytimeHistory(List<PlaytimeHistory> history, String playerName, CommandSender sender, int limit) {
        if (history.isEmpty()) {
            sender.sendMessage(Messages.PLAYTIME_HISTORY_NO_ENTRIES.getMessage(
                    new Replacement("%NAME%", playerName)));
            return;
        }

        // Get the date format from settings
        String dateFormat = Settings.PLAYTIME_HISTORY_DATE_FORMAT.getValueAsString();

        // Send header
        sender.sendMessage(Messages.PLAYTIME_HISTORY_HEADER.getMessage(
                new Replacement("%NAME%", playerName)));

        // Send each history entry
        for (PlaytimeHistory entry : history) {
            String eventType = entry.getEvent().toString();
            String formattedDate = entry.getFormattedDate(dateFormat);
            String formattedTime = TimeUtil.getFormattedTime(entry.getTime());

            sender.sendMessage(Messages.PLAYTIME_HISTORY_ENTRY.getMessage(
                    new Replacement("%EVENT%", eventType),
                    new Replacement("%DATE%", formattedDate),
                    new Replacement("%TIME%", formattedTime)));
        }

        // Send footer with entry count
        sender.sendMessage(Messages.PLAYTIME_HISTORY_FOOTER.getMessage(
                new Replacement("%CURRENT%", String.valueOf(history.size())),
                new Replacement("%TOTAL%", String.valueOf(limit))));
    }
}
