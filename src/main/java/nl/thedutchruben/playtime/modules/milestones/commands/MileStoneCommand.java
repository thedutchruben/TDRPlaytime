package nl.thedutchruben.playtime.modules.milestones.commands;

import nl.thedutchruben.mccore.spigot.commands.Command;
import nl.thedutchruben.mccore.spigot.commands.Default;
import nl.thedutchruben.mccore.spigot.commands.SubCommand;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.objects.Milestone;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

    @SubCommand(
            subCommand = "create",
            description = "Create a new milestone",
            usage = "<name> <time>" ,
            permission = "playtime.milestone.create",
            console = true,
            minParams = 3,
            maxParams = 3)
    public void create(CommandSender commandSender, List<String> args) {
        Milestone milestone = new Milestone();
        milestone.setMilestoneName(args.get(1));
        long time = getTime(args.get(2));
        milestone.setOnlineTime(time);
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
            minParams = 2,
            maxParams = 2,
            console = true
    )
    public void delete(CommandSender commandSender, List<String> args) {
        Milestone milestone = Milestone.getMilestone(args.get(1));
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

    @Default
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
            subCommand = "remove",
            description = "Remove a milestone",
            usage = "<milestone>",
            permission = "playtime.milestone.remove",
            minParams = 2,
            maxParams = 2,
            console = true
    )
    public void remove(CommandSender commandSender, List<String> args) {
        Milestone milestone = Milestone.getMilestone(args.get(1));
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
            subCommand = "info",
            description = "Get info about a milestone",
            usage = "<milestone>",
            permission = "playtime.milestone.info",
            minParams = 2,
            maxParams = 2,
            console = true
    )
    public void info(CommandSender commandSender, List<String> args) {
        Milestone milestone = Milestone.getMilestone(args.get(1));
        if(milestone == null){
            commandSender.sendMessage("Milestone not found");
            return;
        }
        commandSender.sendMessage("Milestone: " + milestone.getMilestoneName());
        commandSender.sendMessage("Time: " + milestone.getOnlineTime());
        commandSender.sendMessage("Rewards: ");
        milestone.getItemStacks().forEach(map -> commandSender.sendMessage(map.toString()));
        commandSender.sendMessage("Commands: ");
        milestone.getCommands().forEach(commandSender::sendMessage);
        commandSender.sendMessage("Messages: ");
        milestone.getMessages().forEach(commandSender::sendMessage);
        commandSender.sendMessage("Firework show: " + milestone.isFireworkShow());
        commandSender.sendMessage("Firework show amount: " + milestone.getFireworkShowAmount());
    }

    @SubCommand(
            subCommand = "setTime",
            description = "Set the time of a milestone",
            usage = "<milestone>",
            permission = "playtime.milestone.setTime",
            minParams = 3,
            maxParams = 3,
            console = true
    )
    public void setTime(CommandSender commandSender, List<String> args) {
        Milestone milestone = Milestone.getMilestone(args.get(1));
        if(milestone == null){
            commandSender.sendMessage("Milestone not found");
            return;
        }

        long time = getTime(args.get(2));
        System.out.println(time);
        milestone.setOnlineTime(time);

        Playtime.getInstance().getStorage().updateMilestone(milestone).thenAcceptAsync(aBoolean -> {
            if(aBoolean){
                commandSender.sendMessage("Milestone updated");
            }else {
                commandSender.sendMessage("Milestone not updated");
            }
        });
    }

    @SubCommand(
            subCommand = "test",
            description = "Execute the rewards of a milestone on yourself",
            usage = "<milestone>",
            permission = "playtime.milestone.test",
            minParams = 2,
            maxParams = 2,
            console = false
    )
    public void test(CommandSender commandSender, List<String> args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("This command can only be used by players.");
            return;
        }
        Milestone milestone = Milestone.getMilestone(args.get(1));
        if (milestone == null) {
            commandSender.sendMessage("Milestone not found");
            return;
        }
        milestone.apply((Player) commandSender);
        commandSender.sendMessage("Milestone rewards executed.");
    }

    @SubCommand(
            subCommand = "addItemToMilestone",
            description = "Add the item in your main hand to the milestone",
            usage = "<milestone>",
            permission = "playtime.milestone.addItemToMilestone",
            minParams = 2,
            maxParams = 2
    )
    public void addItemToMilestone(CommandSender commandSender, List<String> args) {
        Player player = (Player) commandSender;
        Milestone milestone = Milestone.getMilestone(args.get(1));
        if (milestone == null) {
            commandSender.sendMessage("Milestone not found");
            return;
        }
        milestone.addItemStack(player.getInventory().getItemInMainHand());
        Playtime.getInstance().getStorage().updateMilestone(milestone);
        commandSender.sendMessage("Item added to milestone.");
    }

    @SubCommand(
            subCommand = "addCommandToMilestone",
            description = "Add a command to the milestone",
            usage = "<milestone> <command>",
            permission = "playtime.milestone.addCommandToMilestone",
            minParams = 3,
            maxParams = 3,
            console = true
    )
    public void addCommandToMilestone(CommandSender commandSender, List<String> args) {
        Milestone milestone = Milestone.getMilestone(args.get(1));
        if (milestone == null) {
            commandSender.sendMessage("Milestone not found");
            return;
        }
        String command = String.join(" ", args.subList(1, args.size()));
        milestone.getMessages().removeIf(s -> s.equalsIgnoreCase(command));
        Playtime.getInstance().getStorage().updateMilestone(milestone);
        commandSender.sendMessage("Command added to milestone.");
    }

    @SubCommand(
            subCommand = "togglefirework",
            description = "Toggle the firework for a milestone",
            usage = "<milestone>",
            permission = "playtime.milestone.togglefirework",
            minParams = 2,
            maxParams = 2,
            console = true
    )
    public void toggleFirework(CommandSender commandSender, List<String> args) {
        Milestone milestone = Milestone.getMilestone(args.get(1));
        if (milestone == null) {
            commandSender.sendMessage("Milestone not found");
            return;
        }
        milestone.setFireworkShow(!milestone.isFireworkShow());
        Playtime.getInstance().getStorage().updateMilestone(milestone);
        commandSender.sendMessage("Firework show toggled.");
    }

    @SubCommand(
            subCommand = "setfireworkamount",
            description = "Set the amount of firework for a milestone",
            usage = "<milestone> <amount>",
            permission = "playtime.milestone.setfireworkamount",
            minParams = 3,
            maxParams = 3,
            console = true
    )
    public void setFireworkAmount(CommandSender commandSender, List<String> args) {
        Milestone milestone = Milestone.getMilestone(args.get(1));
        if (milestone == null) {
            commandSender.sendMessage("Milestone not found");
            return;
        }
        int amount = Integer.parseInt(args.get(1));
        milestone.setFireworkShowAmount(amount);
        Playtime.getInstance().getStorage().updateMilestone(milestone);
        commandSender.sendMessage("Firework amount set.");
    }

    @SubCommand(
            subCommand = "setfireworkdelay",
            description = "Set the delay between fireworks for a milestone",
            usage = "<milestone> <time in seconds>",
            permission = "playtime.milestone.setfireworkdelay",
            minParams = 4,
            maxParams = 4,
            console = true
    )
    public void setFireworkDelay(CommandSender commandSender, List<String> args) {
        Milestone milestone = Milestone.getMilestone(args.get(1));
        if (milestone == null) {
            commandSender.sendMessage("Milestone not found");
            return;
        }
        int delay = Integer.parseInt(args.get(1));
        milestone.setFireworkShowSecondsBetween(delay);
        Playtime.getInstance().getStorage().updateMilestone(milestone);
        commandSender.sendMessage("Firework delay set.");
    }

    @SubCommand(
            subCommand = "addMessage",
            description = "Add a message to a milestone",
            usage = "<milestone> <message>",
            permission = "playtime.milestone.addMessage",
            minParams = 3,
            maxParams = 3,
            console = true
    )
    public void addMessage(CommandSender commandSender, List<String> args) {
        Milestone milestone = Milestone.getMilestone(args.get(1));
        if (milestone == null) {
            commandSender.sendMessage("Milestone not found");
            return;
        }
        String message = String.join(" ", args.subList(1, args.size()));
        milestone.addMessage(message);
        Playtime.getInstance().getStorage().updateMilestone(milestone);
        commandSender.sendMessage("Message added to milestone.");
    }

    @SubCommand(
            subCommand = "removeMessage",
            description = "Remove a message from a milestone",
            usage = "<milestone> <message>",
            permission = "playtime.milestone.removeMessage",
            minParams = 3,
            maxParams = 3,
            console = true
    )
    public void removeMessage(CommandSender commandSender, List<String> args) {
        Milestone milestone = Milestone.getMilestone(args.get(1));
        if (milestone == null) {
            commandSender.sendMessage("Milestone not found");
            return;
        }
        String message = String.join(" ", args.subList(1, args.size()));
        milestone.removeMessage(message);
        Playtime.getInstance().getStorage().updateMilestone(milestone);
        commandSender.sendMessage("Message removed from milestone.");
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


    private long getTime(String time) {
        AtomicLong parsedTime = new AtomicLong();
        Map<String, Integer> timeMap = parseTime(time);
        System.out.println(timeMap);
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
