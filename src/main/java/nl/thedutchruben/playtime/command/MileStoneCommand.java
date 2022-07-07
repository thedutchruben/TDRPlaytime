package nl.thedutchruben.playtime.command;


import nl.thedutchruben.mccore.spigot.commands.Command;
import nl.thedutchruben.mccore.spigot.commands.Default;
import nl.thedutchruben.mccore.spigot.commands.SubCommand;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.milestone.Milestone;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@Command(command = "milestone",description = "Milestones command" ,permission = "playtime.milestones" ,console = true)
public class MileStoneCommand {

    @SubCommand(subCommand = "create", usage = "<string> <time>" , minParams = 3 , maxParams = 3)
    public void create(CommandSender sender, List<String> args) {
        Milestone milestone = new Milestone();
        milestone.setMilestoneName(args.get(1));
        milestone.setOnlineTime(Integer.parseInt(args.get(2)));
        Playtime.getInstance().getMilestoneMap().put(Integer.parseInt(args.get(2)) * 1000L, milestone);
        Playtime.getInstance().getStorage().createMilestone(milestone).whenComplete((unused, throwable) -> {
            sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.milestonecreated"));
        });

    }

    @Default
    @SubCommand(subCommand = "list", usage = "" , minParams = 0)
    public void list(CommandSender sender, List<String> args) {
        Playtime.getInstance().getMilestoneMap().forEach((aLong, milestone) -> {
            sender.sendMessage("----");
            sender.sendMessage(milestone.getMilestoneName());
            sender.sendMessage(" Time:" + translateMessage("Days: %D% Hours: %H% ,Minute's: %M% Seconds's: %S%",aLong));
//            sender.spigot().sendMessage(new );
        });

    }

    @SubCommand(subCommand = "info", usage = "<milestone>" , minParams = 2, maxParams = 2)
    public void info(CommandSender sender, List<String> args) {
        Milestone milestone = Playtime.getInstance().getMilestoneMap().values().stream().
                filter(milestone1 -> milestone1.getMilestoneName().equalsIgnoreCase(args.get(1))).findFirst().get();
            sender.sendMessage(milestone.getMilestoneName());
            sender.sendMessage(" Time:" + translateMessage("Days: %D% Hours: %H% ,Minute's: %M% Seconds's: %S%",milestone.getOnlineTime() * 1000));
            sender.sendMessage(" Rewards:");
            sender.sendMessage("    Commands("+milestone.getCommands().size()+"):");
            if(milestone.getCommands().size() == 0){
                sender.sendMessage(ChatColor.DARK_RED + "     No commands found");
            }else{
                for (String command : milestone.getCommands()) {
                    sender.sendMessage("     " + command);
                }
            }
            sender.sendMessage("    Items("+milestone.getItemStacks().size()+"):");
        if(milestone.getItemStacks().size() == 0){
            sender.sendMessage(ChatColor.DARK_RED + "     No items found");
        }else{
            for (String command : milestone.getCommands()) {
                sender.sendMessage("     " + command);
            }
        }


    }

    @SubCommand(subCommand = "addItemToMilestone", usage = "<milestone>" , minParams = 1 , maxParams = 1)
    public void addItemToMilestone(CommandSender sender, List<String> args) {
        Milestone milestone = Playtime.getInstance().getMilestoneMap().values().stream().
                filter(milestone1 -> milestone1.getMilestoneName().equalsIgnoreCase(args.get(1))).findFirst().get();
        milestone.getItemStacks().add(((Player)sender).getInventory().getItemInMainHand().serialize());
        Playtime.getInstance().getStorage().saveMileStone(milestone).whenComplete((unused, throwable) -> {
            sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.itemadded"));
        });

    }

    @SubCommand(subCommand = "addCommandToMilestone", usage = "<milestone> <string>" , minParams = 2 , maxParams = 2)
    public void addCommandToMilestone(CommandSender sender, List<String> args) {
        Milestone milestone = Playtime.getInstance().getMilestoneMap().values().stream().
                filter(milestone1 -> milestone1.getMilestoneName().equalsIgnoreCase(args.get(1))).findFirst().get();
        milestone.getCommands().add(args.get(2));
        Playtime.getInstance().getStorage().saveMileStone(milestone).whenComplete((unused, throwable) -> {
            sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.commandadded"));
        });

    }

    @SubCommand(subCommand = "toggleFirework", usage = "<milestone>" , minParams = 2 , maxParams = 2)
    public void toggleFirework(CommandSender sender, List<String> args) {
        Milestone milestone = Playtime.getInstance().getMilestoneMap().values().stream().
                filter(milestone1 -> milestone1.getMilestoneName().equalsIgnoreCase(args.get(1))).findFirst().get();
        milestone.setFireworkShow(!milestone.isFireworkShow());
        Playtime.getInstance().getStorage().saveMileStone(milestone).whenComplete((unused, throwable) -> {
            sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.fireworktoggled").replaceAll("<state>", getState(milestone.isFireworkShow())));
        });
    }

    @SubCommand(subCommand = "setFireworkAmount", usage = "<milestone> <integer>" , minParams = 3 , maxParams = 3)
    public void setFireworkAmount(CommandSender sender, List<String> args) {
        Milestone milestone = Playtime.getInstance().getMilestoneMap().values().stream().
                filter(milestone1 -> milestone1.getMilestoneName().equalsIgnoreCase(args.get(1))).findFirst().get();
        milestone.setFireworkShowAmount(Integer.parseInt(args.get(2)));
        Playtime.getInstance().getStorage().saveMileStone(milestone).whenComplete((unused, throwable) -> {
            sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.setfireworkamount").replaceAll("<amount>", args.get(2)));
        });
    }

    @SubCommand(subCommand = "setFireworkDelay", usage = "<milestone> <integer>" , minParams = 3 , maxParams = 3)
    public void setFireworkDelay(CommandSender sender, List<String> args) {
        Milestone milestone = Playtime.getInstance().getMilestoneMap().values().stream().
                filter(milestone1 -> milestone1.getMilestoneName().equalsIgnoreCase(args.get(1))).findFirst().get();
        milestone.setFireworkShowSecondsBetween(Integer.parseInt(args.get(2)));
        Playtime.getInstance().getStorage().saveMileStone(milestone).whenComplete((unused, throwable) -> {
            sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.setfireworkdelay").replaceAll("<delay>", args.get(2)));
        });
    }

    public String getState(boolean b) {
        return b ? "Enabled" : "Disabled";
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
}
