package nl.thedutchruben.playtime.modules.milestones.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import nl.thedutchruben.mccore.spigot.commands.Command;
import nl.thedutchruben.mccore.spigot.commands.Default;
import nl.thedutchruben.mccore.spigot.commands.SubCommand;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.events.milestone.MilestoneCreateEvent;
import nl.thedutchruben.playtime.core.events.milestone.MilestoneDeleteEvent;
import nl.thedutchruben.playtime.core.events.milestone.MilestoneUpdateEvent;
import nl.thedutchruben.playtime.modules.milestones.gui.MilestoneInfoGui;
import nl.thedutchruben.playtime.core.objects.Milestone;
import nl.thedutchruben.playtime.core.translations.Messages;
import nl.thedutchruben.playtime.utils.Replacement;
import org.bukkit.Bukkit;
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
        usage = "<name> <time>",
        permission = "playtime.milestone.create",
        console = true,
        minParams = 2,
        maxParams = 2)
    public void create(CommandSender commandSender, List<String> args) {
        Milestone milestone = new Milestone();
        milestone.setMilestoneName(args.get(0));
        long time = getTime(args.get(1));
        milestone.setOnlineTime(time);
        Playtime.getInstance().getStorage().saveMilestone(milestone).thenAcceptAsync(aBoolean -> {
            if (aBoolean) {
                commandSender.sendMessage(Messages.MILESTONE_CREATED.getMessage());
                Playtime.getInstance().getMilestones().add(milestone);
                Bukkit.getPluginManager().callEvent(new MilestoneCreateEvent(milestone));
            } else {
                commandSender.sendMessage(Messages.MILESTONE_COULD_NOT_BE_CREATED.getMessage());
            }
        });
    }

    @SubCommand(
            subCommand = "open",
            description = "Open the milestone GUI",
            usage = "<milestone>",
            permission = "playtime.milestone.open",
            minParams = 1,
            maxParams = 1
    )
    public void open(CommandSender commandSender, List<String> args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Messages.ONLY_PLAYER_COMMAND.getMessage());
            return;
        }
        Player player = (Player) commandSender;
        Milestone milestone = Milestone.getMilestone(args.get(0));
        if (milestone == null) {
            player.sendMessage(Messages.MILESTONE_DOES_NOT_EXIST.getMessage());
            return;
        }
        MilestoneInfoGui milestoneInfoGui = new MilestoneInfoGui(milestone);
        milestoneInfoGui.open(player);
    }

    @SubCommand(
        subCommand = "delete",
        description = "Delete a milestone",
        usage = "<milestone>",
        permission = "playtime.milestone.delete",
        minParams = 1,
        maxParams = 1,
        console = true
    )
    public void delete(CommandSender commandSender, List<String> args) {
        Milestone milestone = Milestone.getMilestone(args.get(0));
        if (milestone == null) {
            commandSender.sendMessage(Messages.MILESTONE_DOES_NOT_EXIST.getMessage());
            return;
        }
        Playtime.getInstance().getStorage().deleteMilestone(milestone).thenAcceptAsync(aBoolean -> {
            if (aBoolean) {
                commandSender.sendMessage(Messages.MILESTONE_REMOVED.getMessage());
                Bukkit.getPluginManager().callEvent(new MilestoneDeleteEvent(milestone));
                Playtime.getInstance().getMilestones().removeIf(milestone1 -> milestone1.getMilestoneName().equals(milestone.getMilestoneName()));
            } else {
                commandSender.sendMessage(Messages.MILESTONE_DOES_NOT_EXIST.getMessage());
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
            TextComponent message = new TextComponent(Messages.MILESTONE_LIST.getMessage(
                new Replacement("%MILESTONE_NAME%", milestone.getMilestoneName()),
                new Replacement("%D%", String.valueOf(TimeUnit.SECONDS.toDays(milestone.getOnlineTime()))),
                new Replacement("%H%", String.valueOf(TimeUnit.SECONDS.toHours(milestone.getOnlineTime()) % 24)),
                new Replacement("%M%", String.valueOf(TimeUnit.SECONDS.toMinutes(milestone.getOnlineTime()) % 60)),
                new Replacement("%S%", String.valueOf(TimeUnit.SECONDS.toSeconds(milestone.getOnlineTime()) % 60))
            ));
            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/milestone info " + milestone.getMilestoneName()));
            commandSender.spigot().sendMessage(message);
        }
    }

    @SubCommand(
        subCommand = "info",
        description = "Get info about a milestone",
        usage = "<milestone>",
        permission = "playtime.milestone.info",
        minParams = 1,
        maxParams = 1,
        console = true
    )
    public void info(CommandSender commandSender, List<String> args) {
        Milestone milestone = Milestone.getMilestone(args.get(0));
        if (milestone == null) {
            commandSender.sendMessage(Messages.MILESTONE_DOES_NOT_EXIST.getMessage());
            return;
        }
        commandSender.sendMessage("Milestone: " + milestone.getMilestoneName());
        commandSender.sendMessage(" Time: " + milestone.getOnlineTime());
        commandSender.sendMessage(" Rewards("+ milestone.getItemStacks().size() +"): ");
        milestone.getItemStacks().forEach(map -> commandSender.sendMessage("  " + map.toString()));
        commandSender.sendMessage(" Commands("+ milestone.getCommands().size() +"): ");
        milestone.getCommands().forEach(command -> commandSender.sendMessage("  " + command));
        commandSender.sendMessage(" Messages("+ milestone.getMessages().size() +"): ");
        milestone.getMessages().forEach(message -> commandSender.sendMessage("  " + message));
        commandSender.sendMessage(" Firework show: " + milestone.isFireworkShow());
        if (milestone.isFireworkShow()) {
            commandSender.sendMessage(" Firework show delay: " + milestone.getFireworkShowSecondsBetween());
            commandSender.sendMessage(" Firework show amount: " + milestone.getFireworkShowAmount());
        }
    }

    @SubCommand(
        subCommand = "test",
        description = "Execute the rewards of a milestone on yourself",
        usage = "<milestone>",
        permission = "playtime.milestone.test",
        minParams = 1,
        maxParams = 1
    )
    public void test(CommandSender commandSender, List<String> args) {
        Milestone milestone = Milestone.getMilestone(args.get(0));
        if (milestone == null) {
            commandSender.sendMessage(Messages.MILESTONE_DOES_NOT_EXIST.getMessage());
            return;
        }
        milestone.apply((Player) commandSender);
        commandSender.sendMessage(Messages.MILESTONE_REWARDS_APPLIED.getMessage());
    }

    @SubCommand(
        subCommand = "addItemToMilestone",
        description = "Add the item in your main hand to the milestone",
        usage = "<milestone>",
        permission = "playtime.milestone.addItemToMilestone",
        minParams = 1,
        maxParams = 1
    )
    public void addItemToMilestone(CommandSender commandSender, List<String> args) {
        Player player = (Player) commandSender;
        Milestone milestone = Milestone.getMilestone(args.get(0));
        if (milestone == null) {
            commandSender.sendMessage(Messages.MILESTONE_DOES_NOT_EXIST.getMessage());
            return;
        }
        milestone.addItemStack(player.getInventory().getItemInMainHand());
        Playtime.getInstance().getStorage().updateMilestone(milestone);
        commandSender.sendMessage(Messages.MILESTONE_ITEM_ADDED.getMessage());
        Bukkit.getPluginManager().callEvent(new MilestoneUpdateEvent(milestone));
    }

    @SubCommand(
        subCommand = "addCommand",
        description = "Add a command to the milestone",
        usage = "<milestone> <command>",
        permission = "playtime.milestone.addCommand",
        minParams = 2,
        maxParams = 2,
        console = true
    )
    public void addCommandToMilestone(CommandSender commandSender, List<String> args) {
        Milestone milestone = Milestone.getMilestone(args.get(0));
        if (milestone == null) {
            commandSender.sendMessage(Messages.MILESTONE_DOES_NOT_EXIST.getMessage());
            return;
        }
        String command = String.join(" ", args.subList(1, args.size()));
        milestone.addCommand(command);
        Playtime.getInstance().getStorage().updateMilestone(milestone);
        commandSender.sendMessage(Messages.MILESTONE_COMMAND_ADDED.getMessage());
        Bukkit.getPluginManager().callEvent(new MilestoneUpdateEvent(milestone));
    }

    @SubCommand(
        subCommand = "removeCommand",
        description = "Remove a command from the milestone",
        usage = "<milestone> <command>",
        permission = "playtime.milestone.removeCommand",
        minParams = 2,
        maxParams = 2,
        console = true
    )
    public void removeCommandFromMilestone(CommandSender commandSender, List<String> args) {
        Milestone milestone = Milestone.getMilestone(args.get(0));
        if (milestone == null) {
            commandSender.sendMessage(Messages.MILESTONE_DOES_NOT_EXIST.getMessage());
            return;
        }
        String command = String.join(" ", args.subList(1, args.size()));
        milestone.removeCommand(command);
        Playtime.getInstance().getStorage().updateMilestone(milestone);
        commandSender.sendMessage(Messages.MILESTONE_COMMAND_REMOVED.getMessage());
        Bukkit.getPluginManager().callEvent(new MilestoneUpdateEvent(milestone));
    }

    @SubCommand(
        subCommand = "togglefirework",
        description = "Toggle the firework for a milestone",
        usage = "<milestone>",
        permission = "playtime.milestone.togglefirework",
        minParams = 1,
        maxParams = 1,
        console = true
    )
    public void toggleFirework(CommandSender commandSender, List<String> args) {
        Milestone milestone = Milestone.getMilestone(args.get(0));
        if (milestone == null) {
            commandSender.sendMessage(Messages.MILESTONE_DOES_NOT_EXIST.getMessage());
            return;
        }
        milestone.setFireworkShow(!milestone.isFireworkShow());
        Playtime.getInstance().getStorage().updateMilestone(milestone);
        commandSender.sendMessage(Messages.MILESTONE_FIREWORK_TOGGLED.getMessage(new Replacement("<state>", milestone.isFireworkShow() ? "enabled" : "disabled")));
        Bukkit.getPluginManager().callEvent(new MilestoneUpdateEvent(milestone));
    }

    @SubCommand(
        subCommand = "setfireworkamount",
        description = "Set the amount of firework for a milestone",
        usage = "<milestone> <amount>",
        permission = "playtime.milestone.setfireworkamount",
        minParams = 2,
        maxParams = 2,
        console = true
    )
    public void setFireworkAmount(CommandSender commandSender, List<String> args) {
        Milestone milestone = Milestone.getMilestone(args.get(0));
        if (milestone == null) {
            commandSender.sendMessage(Messages.MILESTONE_DOES_NOT_EXIST.getMessage());
            return;
        }
        int amount = Integer.parseInt(args.get(1));
        milestone.setFireworkShowAmount(amount);
        Playtime.getInstance().getStorage().updateMilestone(milestone);
        commandSender.sendMessage(Messages.MILESTONE_SET_FIREWORK_AMOUNT.getMessage(new Replacement("<amount>", String.valueOf(amount))));
        Bukkit.getPluginManager().callEvent(new MilestoneUpdateEvent(milestone));
    }

    @SubCommand(
        subCommand = "setfireworkdelay",
        description = "Set the delay between fireworks for a milestone",
        usage = "<milestone> <time in seconds>",
        permission = "playtime.milestone.setfireworkdelay",
        minParams = 3,
        maxParams = 3,
        console = true
    )
    public void setFireworkDelay(CommandSender commandSender, List<String> args) {
        Milestone milestone = Milestone.getMilestone(args.get(0));
        if (milestone == null) {
            commandSender.sendMessage(Messages.MILESTONE_DOES_NOT_EXIST.getMessage());
            return;
        }
        int delay = Integer.parseInt(args.get(1));
        milestone.setFireworkShowSecondsBetween(delay);
        Playtime.getInstance().getStorage().updateMilestone(milestone);
        commandSender.sendMessage(Messages.MILESTONE_SET_FIREWORK_DELAY.getMessage(new Replacement("<amount>", String.valueOf(delay))));
        Bukkit.getPluginManager().callEvent(new MilestoneUpdateEvent(milestone));
    }

    @SubCommand(
        subCommand = "addMessage",
        description = "Add a message to a milestone",
        usage = "<milestone> <message>",
        permission = "playtime.milestone.addMessage",
        minParams = 2,
        maxParams = 2,
        console = true
    )
    public void addMessage(CommandSender commandSender, List<String> args) {
        Milestone milestone = Milestone.getMilestone(args.get(0));
        if (milestone == null) {
            commandSender.sendMessage(Messages.MILESTONE_DOES_NOT_EXIST.getMessage());
            return;
        }
        String message = String.join(" ", args.subList(1, args.size()));
        milestone.addMessage(message);
        Playtime.getInstance().getStorage().updateMilestone(milestone);
        commandSender.sendMessage(Messages.MILESTONE_MESSAGE_ADDED.getMessage());
        Bukkit.getPluginManager().callEvent(new MilestoneUpdateEvent(milestone));
    }

    @SubCommand(
        subCommand = "removeMessage",
        description = "Remove a message from a milestone",
        usage = "<milestone> <message>",
        permission = "playtime.milestone.removeMessage",
        minParams = 2,
        maxParams = 2,
        console = true
    )
    public void removeMessage(CommandSender commandSender, List<String> args) {
        Milestone milestone = Milestone.getMilestone(args.get(0));
        if (milestone == null) {
            commandSender.sendMessage(Messages.MILESTONE_DOES_NOT_EXIST.getMessage());
            return;
        }
        String message = String.join(" ", args.subList(1, args.size()));
        milestone.removeMessage(message);
        Playtime.getInstance().getStorage().updateMilestone(milestone);
        commandSender.sendMessage(Messages.MILESTONE_MESSAGE_REMOVED.getMessage());
        Bukkit.getPluginManager().callEvent(new MilestoneUpdateEvent(milestone));
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