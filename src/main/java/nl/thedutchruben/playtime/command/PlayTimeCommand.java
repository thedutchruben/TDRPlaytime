package nl.thedutchruben.playtime.command;

import nl.thedutchruben.mccore.commands.Command;
import nl.thedutchruben.mccore.commands.Default;
import nl.thedutchruben.playtime.Playtime;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

@Command(command = "playtime",description = "Main play time command" ,permission = "playtime.playtime")
public class PlaytimeCommand {

    @Default
    public void myTime(CommandSender commandSender, List<String> args){
        Playtime.getInstance().update(((Player) commandSender).getUniqueId(), true);
        commandSender.sendMessage(translateMessage(Playtime.getInstance().getMessage("command.playtime.timemessage"), Playtime.getInstance().getPlayerOnlineTime().get(((Player) commandSender).getUniqueId())));
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

    @SubCommand(subCommand = "reload")
    public void reload(CommandSender commandSender,List<String> args){

    }

}
