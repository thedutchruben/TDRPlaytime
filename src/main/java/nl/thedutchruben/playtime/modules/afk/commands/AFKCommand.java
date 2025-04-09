package nl.thedutchruben.playtime.modules.afk.commands;

import nl.thedutchruben.mccore.spigot.commands.Command;
import nl.thedutchruben.mccore.spigot.commands.Default;
import nl.thedutchruben.mccore.spigot.commands.SubCommand;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.afk.AFKManager;
import nl.thedutchruben.playtime.core.objects.PlaytimeUser;
import nl.thedutchruben.playtime.core.translations.Messages;
import nl.thedutchruben.playtime.utils.Replacement;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@Command(command = "afk", description = "AFK command", permission = "playtime.afk")
public class AFKCommand {

    @Default
    public void toggleAFK(CommandSender sender, List<String> args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Messages.ONLY_PLAYER_COMMAND.getMessage());
            return;
        }

        Player player = (Player) sender;
        PlaytimeUser user = Playtime.getInstance().getPlaytimeUser(player.getUniqueId()).orElse(null);

        if (user == null) {
            return;
        }

        if (user.isAfk()) {
            AFKManager.getInstance().setPlayerNotAFK(user);
            player.sendMessage(Messages.PLAYER_MANUALLY_SET_NOT_AFK.getMessage());
        } else {
            AFKManager.getInstance().setPlayerAFK(user);
            player.sendMessage(Messages.PLAYER_MANUALLY_SET_AFK.getMessage());
        }
    }

    @SubCommand(
            subCommand = "status",
            description = "Check if a player is AFK",
            usage = "[player]",
            permission = "playtime.afk.status",
            minParams = 0,
            maxParams = 1
    )
    public void status(CommandSender sender, List<String> args) {
        Player target;

        if (args.size() > 0) {
            // Check another player's AFK status
            target = Bukkit.getPlayer(args.get(0));
            if (target == null) {
                sender.sendMessage(Messages.PLAYER_DOES_NOT_EXIST.getMessage());
                return;
            }
        } else if (sender instanceof Player) {
            // Check own AFK status
            target = (Player) sender;
        } else {
            sender.sendMessage(Messages.ONLY_PLAYER_COMMAND.getMessage());
            return;
        }

        PlaytimeUser user = Playtime.getInstance().getPlaytimeUser(target.getUniqueId()).orElse(null);
        if (user == null) {
            return;
        }

        if (user.isAfk()) {
            long afkDuration = System.currentTimeMillis() - user.getAfkSince();
            int[] time = translateTime(afkDuration);

            sender.sendMessage(Messages.PLAYER_AFK_STATUS.getMessage(
                    new Replacement("%player%", target.getName()),
                    new Replacement("%D%", String.valueOf(time[0])),
                    new Replacement("%H%", String.valueOf(time[1])),
                    new Replacement("%M%", String.valueOf(time[2])),
                    new Replacement("%S%", String.valueOf(time[3]))
            ));
        } else {
            int[] time = user.translateAfkTime();

            sender.sendMessage(Messages.PLAYER_AFK_TOTAL.getMessage(
                    new Replacement("%player%", target.getName()),
                    new Replacement("%D%", String.valueOf(time[0])),
                    new Replacement("%H%", String.valueOf(time[1])),
                    new Replacement("%M%", String.valueOf(time[2])),
                    new Replacement("%S%", String.valueOf(time[3]))
            ));
        }
    }

    @SubCommand(
            subCommand = "active",
            description = "Check a player's active playtime",
            usage = "[player]",
            permission = "playtime.afk.active",
            minParams = 0,
            maxParams = 1
    )
    public void activeTime(CommandSender sender, List<String> args) {
        Player target;

        if (args.size() > 0) {
            // Check another player's active playtime
            target = Bukkit.getPlayer(args.get(0));
            if (target == null) {
                sender.sendMessage(Messages.PLAYER_DOES_NOT_EXIST.getMessage());
                return;
            }
        } else if (sender instanceof Player) {
            // Check own active playtime
            target = (Player) sender;
        } else {
            sender.sendMessage(Messages.ONLY_PLAYER_COMMAND.getMessage());
            return;
        }

        PlaytimeUser user = Playtime.getInstance().getPlaytimeUser(target.getUniqueId()).orElse(null);
        if (user == null) {
            return;
        }

        int[] time = user.translateActiveTime();

        sender.sendMessage(Messages.PLAYER_ACTIVE_TIME.getMessage(
                new Replacement("%player%", target.getName()),
                new Replacement("%D%", String.valueOf(time[0])),
                new Replacement("%H%", String.valueOf(time[1])),
                new Replacement("%M%", String.valueOf(time[2])),
                new Replacement("%S%", String.valueOf(time[3]))
        ));
    }

    /**
     * Translates time in milliseconds to days, hours, minutes, and seconds
     * @param milliseconds Time in milliseconds
     * @return An array of [days, hours, minutes, seconds]
     */
    private int[] translateTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        int days = (int) (seconds / 86400);
        seconds = seconds - days * 86400L;
        int hours = (int) (seconds / 3600);
        seconds = seconds - hours * 3600L;
        int minutes = (int) (seconds / 60);
        seconds = seconds - minutes * 60L;
        return new int[]{days, hours, minutes, (int) seconds};
    }
}