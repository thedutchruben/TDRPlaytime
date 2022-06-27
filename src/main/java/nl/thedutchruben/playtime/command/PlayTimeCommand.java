package nl.thedutchruben.playtime.command;

import lombok.SneakyThrows;
import nl.thedutchruben.mccore.spigot.commands.Command;
import nl.thedutchruben.mccore.spigot.commands.Default;
import nl.thedutchruben.mccore.spigot.commands.SubCommand;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.milestone.Milestone;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Command(command = "playtime",description = "Main play time command" ,permission = "playtime.playtime" ,console = true)
public class PlayTimeCommand {

    @Default
    @SubCommand(subCommand = "")
    public void myTime(CommandSender commandSender, List<String> args){
        if(commandSender instanceof Player){
            Playtime.getInstance().update(((Player) commandSender).getUniqueId(), true);
            commandSender.sendMessage(translateMessage(Playtime.getInstance().getMessage("command.playtime.timemessage"), Playtime.getInstance().getPlayerOnlineTime().get(((Player) commandSender).getUniqueId())));
        }
    }

    @SubCommand(subCommand = "" ,minParams = 1, usage = "<player>")
    public void see(CommandSender commandSender, List<String> args) throws ExecutionException, InterruptedException {
        String playerName = args.get(0);

        if (Bukkit.getPlayer(playerName) == null) {

            commandSender.sendMessage(translateMessage(Playtime.getInstance().getMessage("command.playtime.usertimemessage").replaceAll(
                            "%NAME%", Bukkit.getOfflinePlayer(playerName).getName())
                    , Playtime.getInstance().getStorage().getPlayTimeByName(playerName).get()));

        } else {
            Playtime.getInstance().update(Bukkit.getPlayer(playerName).getUniqueId(), true);
            commandSender.sendMessage(translateMessage(Playtime.getInstance().getMessage("command.playtime.usertimemessage").replaceAll("%NAME%", Bukkit.getPlayer(playerName).getName()),
                    Playtime.getInstance().getPlayerOnlineTime().get((Bukkit.getPlayer(playerName)).getUniqueId())));
        }
    }

    @SubCommand(subCommand = "top",permission = "playtime.playtime.top", console = true)
    public void top(CommandSender commandSender,List<String> args){
        Playtime.getInstance().getStorage().getTopTenList().whenCompleteAsync((stringLongMap, throwable) -> {
            sortHashMapByValues((HashMap<String, Long>) stringLongMap).forEach((s, aLong) -> commandSender.sendMessage(translateMessage(Playtime.getInstance().getMessage("command.playtime.usertimemessage").replaceAll(
                    "%NAME%", s), aLong)));
        });
    }

    @SubCommand(subCommand = "reset",permission = "playtime.playtime.reset", minParams = 2,maxParams = 2, console = true,usage = "<player>")
    public void reset(CommandSender commandSender,List<String> args){
        String playerName = args.get(1);
        if (Bukkit.getPlayer(playerName) != null) {
            Playtime.getInstance().getPlayerOnlineTime().replace(Bukkit.getPlayer(playerName).getUniqueId(), (long) 0);
            Playtime.getInstance().getLastCheckedTime().replace(Bukkit.getPlayer(playerName).getUniqueId(), new Playtime.LastCheckedData(System.currentTimeMillis(),Bukkit.getPlayer(playerName).getLocation()));
        }
        Playtime.getInstance().getStorage().reset(Bukkit.getPlayer(playerName).getName());
        commandSender.sendMessage(Playtime.getInstance().getMessage("command.playtime.resettimeconfirm"));
    }

    @SneakyThrows
    @SubCommand(subCommand = "add",permission = "playtime.playtime.add", minParams = 3,maxParams = 3, console = true,usage = "<player> <time>")
    public void add(CommandSender commandSender,List<String> args){
        String playerName = args.get(1);
        long time = Long.parseLong(args.get(2));
        if (Bukkit.getPlayer(playerName) != null) {
            Playtime.getInstance().getPlayerOnlineTime().replace(Bukkit.getPlayer(playerName).getUniqueId(), Playtime.getInstance().getPlayerOnlineTime().get(Bukkit.getPlayer(playerName).getUniqueId()) + (time*1000));
        }else{
            Playtime.getInstance().getStorage().savePlayTime(Bukkit.getOfflinePlayer(playerName).getUniqueId(),Playtime.getInstance().getStorage().getPlayTimeByName(playerName).get() + time);
        }
        commandSender.sendMessage(Playtime.getInstance().getMessage("command.playtime.timeadded").replace("<player>",playerName));
    }

    @SneakyThrows
    @SubCommand(subCommand = "remove",permission = "playtime.playtime.remove", minParams = 3,maxParams = 3, console = true,usage = "<player> <time>")
    public void remove(CommandSender commandSender,List<String> args){
        String playerName = args.get(1);
        long time = Long.parseLong(args.get(2));
        if (Bukkit.getPlayer(playerName) != null) {
            Playtime.getInstance().getPlayerOnlineTime().replace(Bukkit.getPlayer(playerName).getUniqueId(), Playtime.getInstance().getPlayerOnlineTime().get(Bukkit.getPlayer(playerName).getUniqueId()) - (time*1000));
        }else{
            Playtime.getInstance().getStorage().savePlayTime(Bukkit.getOfflinePlayer(playerName).getUniqueId(),Playtime.getInstance().getStorage().getPlayTimeByName(playerName).get() -  (time*1000));
        }
        commandSender.sendMessage(Playtime.getInstance().getMessage("command.playtime.timeremoved").replace("<player>",playerName));
    }

    @SubCommand(subCommand = "reload",permission = "playtime.playtime.reload", console = true)
    public void reload(CommandSender commandSender,List<String> args) throws ExecutionException, InterruptedException {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            Playtime.getInstance().forceSave(onlinePlayer.getUniqueId());
        }
        Playtime.getInstance().getMilestoneMap().clear();
        Playtime.getInstance().getRepeatedMilestoneList().clear();
        Playtime.getInstance().getPlayerOnlineTime().clear();
        Playtime.getInstance().getLastCheckedTime().clear();
        Playtime.getInstance().getStorage().getMilestones().whenComplete((milestones, throwable) -> {
            for (Milestone milestone : milestones) {
                Playtime.getInstance().getMilestoneMap().put(milestone.getOnlineTime()* 1000L,milestone);
            }
        });

        Playtime.getInstance().getStorage().getRepeatingMilestones().whenComplete((milestones, throwable) -> {
            Playtime.getInstance().getRepeatedMilestoneList().addAll(milestones);
        });

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            long onlineTime = Playtime.getInstance().getStorage().getPlayTimeByUUID(onlinePlayer.getUniqueId().toString()).get();
            Playtime.getInstance().getPlayerOnlineTime().put(onlinePlayer.getUniqueId(), onlineTime);
            Playtime.getInstance().getLastCheckedTime().put(onlinePlayer.getUniqueId(), new Playtime.LastCheckedData(System.currentTimeMillis(), onlinePlayer.getLocation()));
        }

        Playtime.getInstance().getKeyMessageMap().clear();
        commandSender.sendMessage(ChatColor.GREEN + "Reloaded");
    }
    public String translateMessage(String message, long time) {
        time = time / 1000;
        int days = (int) (time / 86400);
        time = time - days * 86400L;
        int hours = (int) (time / 3600);
        time = time - hours * 3600L;
        int minutes = (int) (time / 60);
        time = time - minutes * 60L;
        int seconds = (int) time;
        return ChatColor.translateAlternateColorCodes('&', message.replace("%H%", String.valueOf(hours)).replace("%M%", String.valueOf(minutes)).replace("%S%", String.valueOf(seconds)).replace("%D%", String.valueOf(days)));
    }

    public LinkedHashMap<String, Long> sortHashMapByValues(
            HashMap<String, Long> passedMap) {
        List<String> mapKeys = new ArrayList<>(passedMap.keySet());
        List<Long> mapValues = new ArrayList<>(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);
        Collections.reverse(mapValues);
        Collections.reverse(mapKeys);

        LinkedHashMap<String, Long> sortedMap =
                new LinkedHashMap<>();

        Iterator<Long> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            long val = valueIt.next();
            Iterator<String> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                String key = keyIt.next();
                Long comp1 = passedMap.get(key);

                if (comp1.equals(val)) {
                    keyIt.remove();
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap;
    }
}
