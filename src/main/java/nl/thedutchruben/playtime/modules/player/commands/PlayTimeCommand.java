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
    public void myTime(CommandSender commandSender, List<String> args) {
        if (commandSender instanceof Player) {
            PlaytimeUser user = Playtime.getInstance().getPlaytimeUsers().get(((Player) commandSender).getUniqueId());
            user.updatePlaytime();
            commandSender.sendMessage(Messages.PLAYTIME_INFO_OWN.getMessage(
                    new Replacement("%D%",String.valueOf(user.translateTime()[0])),
                    new Replacement("%H%",String.valueOf(user.translateTime()[1])),
                    new Replacement("%M%",String.valueOf(user.translateTime()[2])),
                    new Replacement("%S%",String.valueOf(user.translateTime()[3]))
            ));
        }else{
            commandSender.sendMessage("You need to be a player to use this command");
        }
    }

    @Fallback(minParams = 1, maxParams = 2)
    @SubCommand(subCommand = "", minParams = 1, maxParams = 2, usage = "<player>", description = "Show a players playtime")
    public void see(CommandSender commandSender, List<String> args) {
        String playerName = args.get(0);

        if (Bukkit.getPlayer(playerName) == null) {
            Playtime.getInstance().getStorage().loadUserByName(playerName).thenAcceptAsync(playtimeUser -> {
                if(playtimeUser == null){
                    commandSender.sendMessage(Messages.PLAYER_DOES_NOT_EXIST.getMessage());
                }else{
                    commandSender.sendMessage(Messages.PLAYTIME_INFO_OTHER.getMessage(new Replacement("%NAME%",playtimeUser.getName()),
                            new Replacement("%D%",String.valueOf(playtimeUser.translateTime()[0])),
                            new Replacement("%H%",String.valueOf(playtimeUser.translateTime()[1])),
                            new Replacement("%M%",String.valueOf(playtimeUser.translateTime()[2])),
                            new Replacement("%S%",String.valueOf(playtimeUser.translateTime()[3]))));
                }

            });

        } else {
            Playtime.getInstance().getPlaytimeUser(playerName).ifPresent(playtimeUser -> {
                commandSender.sendMessage(Messages.PLAYTIME_INFO_OTHER.getMessage(new Replacement("%NAME%",playtimeUser.getName()),
                        new Replacement("%D%",String.valueOf(playtimeUser.translateTime()[0])),
                        new Replacement("%H%",String.valueOf(playtimeUser.translateTime()[1])),
                        new Replacement("%M%",String.valueOf(playtimeUser.translateTime()[2])),
                        new Replacement("%S%",String.valueOf(playtimeUser.translateTime()[3]))));
            });


        }
    }

    @SubCommand(subCommand = "top", permission = "playtime.playtime.top", console = true, description = "Show the top 10 players")
    public void top(CommandSender commandSender, List<String> args) {
        Playtime.getInstance().getStorage().getTopUsers(10,0).whenCompleteAsync((playerMap, throwable) -> {
            for(PlaytimeUser playtimeUser : playerMap){
                commandSender.sendMessage(Messages.PLAYTIME_INFO_OTHER.getMessage(new Replacement("%NAME%",playtimeUser.getName()),
                        new Replacement("%D%",String.valueOf(playtimeUser.translateTime()[0])),
                        new Replacement("%H%",String.valueOf(playtimeUser.translateTime()[1])),
                        new Replacement("%M%",String.valueOf(playtimeUser.translateTime()[2])),
                        new Replacement("%S%",String.valueOf(playtimeUser.translateTime()[3]))));
            }
        });
    }

    @SubCommand(subCommand = "reset", permission = "playtime.playtime.reset", minParams = 2, maxParams = 2, console = true, usage = "<player>", description = "Reset a players playtime")
    public void reset(CommandSender commandSender, List<String> args) {
        String playerName = args.get(1);
        Playtime.getInstance().getStorage().loadUserByName(playerName).thenAcceptAsync(playtimeUser -> {
            if(playtimeUser != null){
                playtimeUser.setPlaytime(0);
                Playtime.getInstance().getPlaytimeUser(playerName).ifPresent(u -> u.setPlaytime(0));
                Playtime.getInstance().getStorage().saveUser(playtimeUser).thenAcceptAsync(u -> {
                    commandSender.sendMessage(Messages.PLAYER_RESET_CONFIRM.getMessage());
                });
            }else{
                commandSender.sendMessage(Messages.PLAYER_DOES_NOT_EXIST.getMessage());

            }
        });
    }

    @SubCommand(subCommand = "add", permission = "playtime.playtime.add", minParams = 3, maxParams = 3, console = true, description = "Add playtime to a user", usage = "<player> <time>")
    public void add(CommandSender commandSender, List<String> args) {
        String playerName = args.get(1);
        String time = args.get(2);

        // Define a pattern to match numbers and letters
        Pattern pattern = Pattern.compile("(\\d+)([A-Za-z]+)");

        // Create a map to store the mappings
        Map<String, Integer> timeMap = new HashMap<>();

        // Use a Matcher to find matches in the input string
        Matcher matcher = pattern.matcher(time);

        // Iterate through matches and populate the map
        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);

            // Map the unit (letter) with the corresponding numeric value
            timeMap.put(unit, value);
        }

       Playtime.getInstance().getPlaytimeUser(playerName).ifPresentOrElse(playtimeUser -> {
            if(timeMap.isEmpty()){
                playtimeUser.addPlaytime(Long.parseLong(time), TimeUnit.SECONDS);
            }else{
                timeMap.forEach((s, integer) -> {
                    switch (s.toUpperCase(Locale.ROOT)){
                        case "S":
                            playtimeUser.addPlaytime(integer, TimeUnit.SECONDS);
                            break;
                        case "M":
                            playtimeUser.addPlaytime(integer, TimeUnit.MINUTES);
                            break;
                        case "H":
                            playtimeUser.addPlaytime(integer, TimeUnit.HOURS);
                            break;
                        case "D":
                            playtimeUser.addPlaytime(integer, TimeUnit.DAYS);
                            break;
                        case "W":
                            playtimeUser.addPlaytime(integer * 7L, TimeUnit.DAYS);
                            break;
                    }
                });
            }
            playtimeUser.save().thenAcceptAsync(test -> {
               commandSender.sendMessage( Messages.TIME_ADDED_TO_USER.getMessage(new Replacement("<player>", playerName), new Replacement("%playtime%",playerName)));
            });
       },() -> {
            Playtime.getInstance().getStorage().loadUserByName(playerName).thenAcceptAsync(playtimeUser -> {
                if(timeMap.isEmpty()){
                    playtimeUser.addPlaytime(Long.parseLong(time), TimeUnit.SECONDS);
                }else{
                    timeMap.forEach((s, integer) -> {
                        switch (s.toUpperCase(Locale.ROOT)){
                            case "S":
                                playtimeUser.addPlaytime(integer, TimeUnit.SECONDS);
                                break;
                            case "M":
                                playtimeUser.addPlaytime(integer, TimeUnit.MINUTES);
                                break;
                            case "H":
                                playtimeUser.addPlaytime(integer, TimeUnit.HOURS);
                                break;
                            case "D":
                                playtimeUser.addPlaytime(integer, TimeUnit.DAYS);
                                break;
                            case "W":
                                playtimeUser.addPlaytime(integer * 7L, TimeUnit.DAYS);
                                break;
                        }
                    });
                }
                playtimeUser.save().thenAcceptAsync(test -> {
                    commandSender.sendMessage( Messages.TIME_ADDED_TO_USER.getMessage(new Replacement("<player>", playerName), new Replacement("%playtime%",playerName)));
                });
            });
       });
    }


    @SubCommand(subCommand = "remove", permission = "playtime.playtime.remove", minParams = 3, maxParams = 3, console = true, usage = "<player> <time>", description = "Remove playtime from a user")
    public void remove(CommandSender commandSender, List<String> args) {
        String playerName = args.get(1);
        String time = args.get(2);

        // Define a pattern to match numbers and letters
        Pattern pattern = Pattern.compile("(\\d+)([A-Za-z]+)");

        // Create a map to store the mappings
        Map<String, Integer> timeMap = new HashMap<>();

        // Use a Matcher to find matches in the input string
        Matcher matcher = pattern.matcher(time);

        // Iterate through matches and populate the map
        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);

            // Map the unit (letter) with the corresponding numeric value
            timeMap.put(unit, value);
        }

        Playtime.getInstance().getPlaytimeUser(playerName).ifPresentOrElse(playtimeUser -> {
            if(timeMap.isEmpty()){
                playtimeUser.removePlaytime(Long.parseLong(time), TimeUnit.SECONDS);
            }else{
                timeMap.forEach((s, integer) -> {
                    switch (s.toUpperCase(Locale.ROOT)){
                        case "S":
                            playtimeUser.removePlaytime(integer, TimeUnit.SECONDS);
                            break;
                        case "M":
                            playtimeUser.removePlaytime(integer, TimeUnit.MINUTES);
                            break;
                        case "H":
                            playtimeUser.removePlaytime(integer, TimeUnit.HOURS);
                            break;
                        case "D":
                            playtimeUser.removePlaytime(integer, TimeUnit.DAYS);
                            break;
                        case "W":
                            playtimeUser.removePlaytime(integer * 7L, TimeUnit.DAYS);
                            break;
                    }
                });
            }
            playtimeUser.save().thenAcceptAsync(test -> {
                commandSender.sendMessage( Messages.TIME_REMOVED_FROM_USER.getMessage(new Replacement("<player>", playerName), new Replacement("%playtime%",playerName)));
            });
        },() -> Playtime.getInstance().getStorage().loadUserByName(playerName).thenAcceptAsync(playtimeUser -> {
            if(timeMap.isEmpty()){
                playtimeUser.removePlaytime(Long.parseLong(time), TimeUnit.SECONDS);
            }else{
                timeMap.forEach((s, integer) -> {
                    switch (s.toUpperCase(Locale.ROOT)){
                        case "S":
                            playtimeUser.removePlaytime(integer, TimeUnit.SECONDS);
                            break;
                        case "M":
                            playtimeUser.removePlaytime(integer, TimeUnit.MINUTES);
                            break;
                        case "H":
                            playtimeUser.removePlaytime(integer, TimeUnit.HOURS);
                            break;
                        case "D":
                            playtimeUser.removePlaytime(integer, TimeUnit.DAYS);
                            break;
                        case "W":
                            playtimeUser.removePlaytime(integer * 7L, TimeUnit.DAYS);
                            break;
                    }
                });
            }
            playtimeUser.save().thenAcceptAsync(test -> {
                commandSender.sendMessage( Messages.TIME_REMOVED_FROM_USER.getMessage(new Replacement("<player>", playerName), new Replacement("%playtime%",playerName)));
            });
        }));
    }

    @SubCommand(subCommand = "pluginInfo", permission = "playtime.playtime.pluginInfo", console = true, description = "Show info about the plugin")
    public void pluginInfo(CommandSender commandSender, List<String> args) {
        commandSender.sendMessage(ChatColor.GREEN + "Playtime by TheDutchRuben");
        commandSender.sendMessage(ChatColor.GREEN + "Version: " + Playtime.getPlugin().getDescription().getVersion());
        commandSender.sendMessage(ChatColor.GREEN + "Author: " + Playtime.getPlugin().getDescription().getAuthors());
        commandSender.sendMessage(ChatColor.GREEN + "Website: " + Playtime.getPlugin().getDescription().getWebsite());
        Playtime.getInstance().getStorage().getMilestones().whenComplete((milestones, throwable) -> {
            commandSender.sendMessage(ChatColor.GREEN + "Milestones: " + milestones.size());
        });
        Playtime.getInstance().getStorage().getRepeatingMilestones().whenComplete((milestones, throwable) -> {
            commandSender.sendMessage(ChatColor.GREEN + "Repeating Milestones: " + milestones.size());
        });
        Playtime.getInstance().getMccore().getUpdate(commandSender, true);
    }

}
