package nl.thedutchruben.playtime.modules.milestones.commands;

import nl.thedutchruben.mccore.spigot.commands.Command;
import nl.thedutchruben.mccore.spigot.commands.Default;
import nl.thedutchruben.mccore.spigot.commands.SubCommand;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.objects.Milestone;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Command(command = "milestone", description = "Main milestone command", permission = "playtime.milestone", console = true)
public class MileStoneCommand {

    @Default
    @SubCommand(
            subCommand = "create",
            description = "Create a new milestone",
            usage = "<name> <time>" ,
            permission = "playtime.milestone.create",
            console = true,
            minParams = 2)
    public void create(CommandSender commandSender, List<String> args) {
        Milestone milestone = new Milestone();
        milestone.setMilestoneName(args.get(0));
        milestone.setOnlineTime(Long.parseLong(args.get(1)));
        Playtime.getInstance().getStorage().saveMilestone(milestone).thenAcceptAsync(aBoolean -> {
            if(aBoolean){
                commandSender.sendMessage("Milestone created");
            }else {
                commandSender.sendMessage("Milestone not created");
            }
        });
    }

    @SubCommand(
            subCommand = "delete",
            description = "Delete a milestone",
            usage = "<milestone>",
            permission = "playtime.milestone.delete",
            minParams = 1,
            console = true
    )
    public void delete(CommandSender commandSender, List<String> args) {
        Milestone milestone = Milestone.getMilestone(args.get(0));
        if(milestone == null){
            commandSender.sendMessage("Milestone not found");
            return;
        }
        Playtime.getInstance().getStorage().deleteMilestone(milestone).thenAcceptAsync(aBoolean -> {
            if(aBoolean){
                commandSender.sendMessage("Milestone deleted");
            }else {
                commandSender.sendMessage("Milestone not deleted");
            }
        });
    }

    @SubCommand(
            subCommand = "list",
            description = "List all milestones",
            permission = "playtime.milestone.list",
            console = true
    )
    public void list(CommandSender commandSender, List<String> args) {
        List<Milestone> milestones = Playtime.getInstance().getMilestones();
        commandSender.sendMessage("Milestones: ");
        for (Milestone milestone : milestones) {
            commandSender.sendMessage("  ");
            commandSender.sendMessage(milestone.getMilestoneName() + " - " + milestone.getOnlineTime());
            commandSender.sendMessage("Rewards: ");
            milestone.getItemStacks().forEach(map -> commandSender.sendMessage(map.toString()));
            commandSender.sendMessage("Commands: ");
            milestone.getCommands().forEach(commandSender::sendMessage);
            commandSender.sendMessage("Messages: ");
            milestone.getMessages().forEach(commandSender::sendMessage);
            commandSender.sendMessage("Firework show: " + milestone.isFireworkShow());
            commandSender.sendMessage("Firework show amount: " + milestone.getFireworkShowAmount());
            commandSender.sendMessage("  ");
        }
    }

    @SubCommand(
            subCommand = "setTime",
            description = "Set the time of a milestone",
            usage = "<milestone>",
            permission = "playtime.milestone.setTime",
            minParams = 2,
            console = true
    )
    public void setTime(CommandSender commandSender, List<String> args) {
        Milestone milestone = Milestone.getMilestone(args.get(0));
        if(milestone == null){
            commandSender.sendMessage("Milestone not found");
            return;
        }

        long time = getTime(commandSender, args.get(1));

        milestone.setOnlineTime(time);

        Playtime.getInstance().getStorage().saveMilestone(milestone).thenAcceptAsync(aBoolean -> {
            if(aBoolean){
                commandSender.sendMessage("Milestone updated");
            }else {
                commandSender.sendMessage("Milestone not updated");
            }
        });
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


    private long getTime(CommandSender commandSender, String time) {
        AtomicLong parsedTime = new AtomicLong();
        Map<String, Integer> timeMap = parseTime(time);

        timeMap.forEach((unit, value) -> {
            switch (unit.toLowerCase(Locale.ROOT)) {
                case "s":
                case "second":
                case "seconds":
                    parsedTime.addAndGet(TimeUnit.SECONDS.toSeconds(value));
                    break;
                case "m":
                case "minute":
                case "minutes":
                    parsedTime.addAndGet(TimeUnit.MINUTES.toSeconds(value));
                    break;
                case "h":
                case "hour":
                case "hours":
                    parsedTime.addAndGet(TimeUnit.HOURS.toSeconds(value));
                    break;
                case "d":
                case "day":
                case "days":
                    parsedTime.addAndGet(TimeUnit.DAYS.toSeconds(value));
                    break;
                case "w":
                case "week":
                case "weeks":
                    parsedTime.addAndGet(TimeUnit.DAYS.toSeconds(value * 7L));
                    break;
                case "mo":
                case "month":
                case "months":
                    parsedTime.addAndGet(TimeUnit.DAYS.toSeconds(value * 30));
                    break;
                case "y":
                case "year":
                case "years":
                    parsedTime.addAndGet(TimeUnit.DAYS.toSeconds(value * 365L));
                    break;
                default:
                    parsedTime.addAndGet(TimeUnit.SECONDS.toSeconds(value));
            }
        });

        return parsedTime.get();
    }
}
