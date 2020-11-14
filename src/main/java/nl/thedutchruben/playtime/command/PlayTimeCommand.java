package nl.thedutchruben.playtime.command;

import nl.thedutchruben.playtime.Playtime;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class PlayTimeCommand implements CommandExecutor, TabCompleter {
    /**
     * Executes the given command, returning its success.
     * <br>
     * If false is returned, then the "usage" plugin.yml entry for this command
     * (if defined) will be sent to the player.
     *
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage(ChatColor.RED + "This is a player only command!");
            return false;
        }
        if(args.length == 0 ){
            Playtime.getInstance().update(((Player) sender).getUniqueId());
            sender.sendMessage(translateMessage("&8[&6PlayTime&8] &7Your playtime is &6%D% &7day(s) &6%H% &7hour(s) &6%M% &7minute(s) &6%S% &7second(s)",Playtime.getInstance().getPlayerOnlineTime().get(((Player) sender).getUniqueId())));
        }else{
            switch (args[0]){
                case "top":
                case "reset":
                default:
                if(args[0].length() <= 16){
                    if(Bukkit.getPlayer(args[0]) == null){
                        sender.sendMessage(translateMessage("&8[&6PlayTime&8] &7<NAME> 's playtime is &6%D% &7day(s) &6%H% &7hour(s) &6%M% &7minute(s) &6%S% &7second(s)".replaceAll(
                                "<NAME>",Bukkit.getPlayer(args[0]).getName())
                                ,Playtime.getInstance().getStorage().getPlayTimeByName(args[0])));

                    }else{
                        Playtime.getInstance().update(Bukkit.getPlayer(args[0]).getUniqueId());
                        sender.sendMessage(translateMessage("&8[&6PlayTime&8] &7<NAME> 's playtime is &6%D% &7day(s) &6%H% &7hour(s) &6%M% &7minute(s) &6%S% &7second(s)".replaceAll("<NAME>",Bukkit.getPlayer(args[0]).getName()),Playtime.getInstance().getPlayerOnlineTime().get((Bukkit.getPlayer(args[0])).getUniqueId())));
                    }
                }else{

                }
            }
        }
        return true;
    }


    public String translateMessage(String message, long time) {
        time = time / 1000;
        int days = (int) (time / 86400);
        time = time - days * 86400;
        int hours = (int) (time / 3600);
        time = time - hours * 3600;
        int minutes = (int) (time / 60);
        time = time - minutes * 60;
        int seconds = (int) time;
        return ChatColor.translateAlternateColorCodes('&',message.replace("%H%", String.valueOf(hours)).replace("%M%", String.valueOf(minutes)).replace("%S%", String.valueOf(seconds)).replace("%D%", String.valueOf(days)));
    }


    /**
     * Requests a list of possible completions for a command argument.
     *
     * @param sender  Source of the command.  For players tab-completing a
     *                command inside of a command block, this will be the player, not
     *                the command block.
     * @param command Command which was executed
     * @param alias   The alias used
     * @param args    The arguments passed to the command, including final
     *                partial argument to be completed and command label
     * @return A List of possible completions for the final argument, or null
     * to default to the command executor
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
