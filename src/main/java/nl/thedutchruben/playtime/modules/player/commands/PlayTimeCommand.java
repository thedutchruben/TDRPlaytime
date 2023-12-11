package nl.thedutchruben.playtime.modules.player.commands;

import nl.thedutchruben.mccore.spigot.commands.Command;
import nl.thedutchruben.mccore.spigot.commands.Default;
import nl.thedutchruben.mccore.spigot.commands.SubCommand;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.objects.PlaytimeUser;
import nl.thedutchruben.playtime.core.translations.Messages;
import nl.thedutchruben.playtime.utils.Replacement;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

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

    @SubCommand(subCommand = "", minParams = 1, usage = "<player>", description = "Show a players playtime")
    public void see(CommandSender commandSender, List<String> args) {
        String playerName = args.get(0);

        if (Bukkit.getPlayer(playerName) == null) {
            Playtime.getInstance().getStorage().loadUserByName(playerName).thenAcceptAsync(playtimeUser -> {
                commandSender.sendMessage(Messages.PLAYTIME_INFO_OTHER.getMessage(new Replacement("%NAME%",playtimeUser.getName()),
                        new Replacement("%D%",String.valueOf(playtimeUser.translateTime()[0])),
                        new Replacement("%H%",String.valueOf(playtimeUser.translateTime()[1])),
                        new Replacement("%M%",String.valueOf(playtimeUser.translateTime()[2])),
                        new Replacement("%S%",String.valueOf(playtimeUser.translateTime()[3]))));
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
                    //todo send reset message
                });
            }else{
                // todo user does not exist message
            }
        });
//        commandSender.sendMessage(Playtime.getInstance().getMessage("command.playtime.resettimeconfirm"));
    }



    /**
     * Sorts a given HashMap by values in descending order and returns the top 10 entries.
     *
     * @param passedMap The input HashMap to be sorted.
     * @return A LinkedHashMap containing the top 10 entries sorted by values in descending order.
     */
    public LinkedHashMap<String, Long> sortHashMapByValues(HashMap<String, Long> passedMap) {
        // Use Java streams to sort the entries by values in descending order and limit to the top 10
        return passedMap.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        // Merge function to resolve conflicts (not used in this case)
                        (e1, e2) -> e1,
                        // Use LinkedHashMap to preserve the order of insertion
                        LinkedHashMap::new
                ));
    }
}
