package nl.thedutchruben.playtime.modules.player.commands;

import nl.thedutchruben.mccore.spigot.commands.Command;
import nl.thedutchruben.mccore.spigot.commands.Default;
import nl.thedutchruben.mccore.spigot.commands.Fallback;
import nl.thedutchruben.mccore.spigot.commands.SubCommand;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.objects.PlaytimeUser;
import nl.thedutchruben.playtime.core.translations.Messages;
import nl.thedutchruben.playtime.utils.Replacement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Command(command = "playtime", description = "Main playtime command", permission = "playtime.playtime", console = true)
public class PlayTimeCommand {

    @Default
    @SubCommand(subCommand = "", description = "Show your own playtime")
    public void myTime(CommandSender sender, List<String> args) {
        if (sender instanceof Player) {
            PlaytimeUser user = Playtime.getInstance().getPlaytimeUsers().get(((Player) sender).getUniqueId());
            user.updatePlaytime();
            sender.sendMessage(Messages.PLAYTIME_INFO_OWN.getMessage(
                    new Replacement("%D%", String.valueOf(user.translateTime()[0])),
                    new Replacement("%H%", String.valueOf(user.translateTime()[1])),
                    new Replacement("%M%", String.valueOf(user.translateTime()[2])),
                    new Replacement("%S%", String.valueOf(user.translateTime()[3]))
            ));
        } else {
            sender.sendMessage("You need to be a player to use this command");
        }
    }

    @Fallback(minParams = 1, maxParams = 2)
    @SubCommand(subCommand = "", minParams = 1, maxParams = 2, usage = "<player>", description = "Show a player's playtime")
    public void see(CommandSender sender, List<String> args) {
        String playerName = args.get(0);
        Player player = Bukkit.getPlayer(playerName);

        if (player == null) {
            Playtime.getInstance().getStorage().loadUserByName(playerName).thenAcceptAsync(user -> {
                if (user == null) {
                    sender.sendMessage(Messages.PLAYER_DOES_NOT_EXIST.getMessage());
                } else {
                    sendPlaytimeInfo(sender, user);
                }
            });
        } else {
            Playtime.getInstance().getPlaytimeUser(playerName).ifPresent(user -> sendPlaytimeInfo(sender, user));
        }
    }

    @SubCommand(subCommand = "top", permission = "playtime.playtime.top", console = true, description = "Show the top 10 players")
    public void top(CommandSender sender, List<String> args) {
        Playtime.getInstance().getStorage().getTopUsers(10, 0).whenCompleteAsync((users, throwable) -> {
            users.forEach(user -> sendPlaytimeInfo(sender, user));
        });
    }

    @SubCommand(subCommand = "reset", permission = "playtime.playtime.reset", minParams = 2, maxParams = 2, console = true, usage = "<player>", description = "Reset a player's playtime")
    public void reset(CommandSender sender, List<String> args) {
        String playerName = args.get(1);
        Playtime.getInstance().getStorage().loadUserByName(playerName).thenAcceptAsync(user -> {
            if (user != null) {
                user.setPlaytime(0);
                Playtime.getInstance().getPlaytimeUser(playerName).ifPresent(u -> u.setPlaytime(0));
                Playtime.getInstance().getStorage().saveUser(user).thenAcceptAsync(u -> {
                    sender.sendMessage(Messages.PLAYER_RESET_CONFIRM.getMessage());
                });
            } else {
                sender.sendMessage(Messages.PLAYER_DOES_NOT_EXIST.getMessage());
            }
        });
    }

    @SubCommand(subCommand = "add", permission = "playtime.playtime.add", minParams = 3, maxParams = 3, console = true, description = "Add playtime to a user", usage = "<player> <time>")
    public void add(CommandSender sender, List<String> args) {
        String playerName = args.get(1);
        String time = args.get(2);
        Map<String, Integer> timeMap = parseTime(time);

        Playtime.getInstance().getPlaytimeUser(playerName).ifPresentOrElse(user -> {
            addPlaytime(user, timeMap, time);
            user.save().thenAcceptAsync(test -> {
                sender.sendMessage(Messages.TIME_ADDED.getMessage(new Replacement("<player>", playerName), new Replacement("%playtime%", playerName)));
            });
        }, () -> {
            Playtime.getInstance().getStorage().loadUserByName(playerName).thenAcceptAsync(user -> {
                addPlaytime(user, timeMap, time);
                user.save().thenAcceptAsync(test -> {
                    sender.sendMessage(Messages.TIME_ADDED.getMessage(new Replacement("<player>", playerName), new Replacement("%playtime%", playerName)));
                });
            });
        });
    }

    @SubCommand(subCommand = "remove", permission = "playtime.playtime.remove", minParams = 3, maxParams = 3, console = true, usage = "<player> <time>", description = "Remove playtime from a user")
    public void remove(CommandSender sender, List<String> args) {
        String playerName = args.get(1);
        String time = args.get(2);
        Map<String, Integer> timeMap = parseTime(time);

        Playtime.getInstance().getPlaytimeUser(playerName).ifPresentOrElse(user -> {
            removePlaytime(user, timeMap, time);
            user.save().thenAcceptAsync(test -> {
                sender.sendMessage(Messages.TIME_REMOVED.getMessage(new Replacement("<player>", playerName), new Replacement("%playtime%", playerName)));
            });
        }, () -> {
            Playtime.getInstance().getStorage().loadUserByName(playerName).thenAcceptAsync(user -> {
                removePlaytime(user, timeMap, time);
                user.save().thenAcceptAsync(test -> {
                    sender.sendMessage(Messages.TIME_REMOVED.getMessage(new Replacement("<player>", playerName), new Replacement("%playtime%", playerName)));
                });
            });
        });
    }

    @SubCommand(subCommand = "pluginInfo", permission = "playtime.playtime.pluginInfo", console = true, description = "Show info about the plugin")
    public void pluginInfo(CommandSender sender, List<String> args) {
        sender.sendMessage(ChatColor.GREEN + "Playtime by TheDutchRuben");
        sender.sendMessage(ChatColor.GREEN + "Version: " + Playtime.getPlugin().getDescription().getVersion());
        sender.sendMessage(ChatColor.GREEN + "Author: " + Playtime.getPlugin().getDescription().getAuthors());
        sender.sendMessage(ChatColor.GREEN + "Website: " + Playtime.getPlugin().getDescription().getWebsite());
        Playtime.getInstance().getStorage().getMilestones().whenComplete((milestones, throwable) -> {
            sender.sendMessage(ChatColor.GREEN + "Milestones: " + milestones.size());
        });
        Playtime.getInstance().getStorage().getRepeatingMilestones().whenComplete((milestones, throwable) -> {
            sender.sendMessage(ChatColor.GREEN + "Repeating Milestones: " + milestones.size());
        });
        Playtime.getInstance().getMccore().getUpdate(sender, true);
    }

    private void sendPlaytimeInfo(CommandSender sender, PlaytimeUser user) {
        sender.sendMessage(Messages.PLAYTIME_INFO_OTHER.getMessage(
                new Replacement("%NAME%", user.getName()),
                new Replacement("%D%", String.valueOf(user.translateTime()[0])),
                new Replacement("%H%", String.valueOf(user.translateTime()[1])),
                new Replacement("%M%", String.valueOf(user.translateTime()[2])),
                new Replacement("%S%", String.valueOf(user.translateTime()[3]))
        ));
    }

    private Map<String, Integer> parseTime(String time) {
        Pattern pattern = Pattern.compile("(\\d+)([A-Za-z]+)");
        Matcher matcher = pattern.matcher(time);
        Map<String, Integer> timeMap = new HashMap<>();

        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);
            timeMap.put(unit, value);
        }
        return timeMap;
    }

    private void addPlaytime(PlaytimeUser user, Map<String, Integer> timeMap, String time) {
        if (timeMap.isEmpty()) {
            user.addPlaytime(Long.parseLong(time), TimeUnit.SECONDS);
        } else {
            timeMap.forEach((unit, value) -> {
                switch (unit.toUpperCase(Locale.ROOT)) {
                    case "S": user.addPlaytime(value, TimeUnit.SECONDS); break;
                    case "M": user.addPlaytime(value, TimeUnit.MINUTES); break;
                    case "H": user.addPlaytime(value, TimeUnit.HOURS); break;
                    case "D": user.addPlaytime(value, TimeUnit.DAYS); break;
                    case "W": user.addPlaytime(value * 7L, TimeUnit.DAYS); break;
                }
            });
        }
    }

    private void removePlaytime(PlaytimeUser user, Map<String, Integer> timeMap, String time) {
        if (timeMap.isEmpty()) {
            user.removePlaytime(Long.parseLong(time), TimeUnit.SECONDS);
        } else {
            timeMap.forEach((unit, value) -> {
                switch (unit.toUpperCase(Locale.ROOT)) {
                    case "S": user.removePlaytime(value, TimeUnit.SECONDS); break;
                    case "M": user.removePlaytime(value, TimeUnit.MINUTES); break;
                    case "H": user.removePlaytime(value, TimeUnit.HOURS); break;
                    case "D": user.removePlaytime(value, TimeUnit.DAYS); break;
                    case "W": user.removePlaytime(value * 7L, TimeUnit.DAYS); break;
                }
            });
        }
    }

}