package nl.thedutchruben.playtime.modules.milestones.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import nl.thedutchruben.mccore.spigot.commands.Command;
import nl.thedutchruben.mccore.spigot.commands.Default;
import nl.thedutchruben.mccore.spigot.commands.SubCommand;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.events.repeatingmilestone.RepeatingMilestoneCreateEvent;
import nl.thedutchruben.playtime.core.events.repeatingmilestone.RepeatingMilestoneDeleteEvent;
import nl.thedutchruben.playtime.core.events.repeatingmilestone.RepeatingMilestoneUpdateEvent;
import nl.thedutchruben.playtime.core.objects.Milestone;
import nl.thedutchruben.playtime.core.objects.RepeatingMilestone;
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

@Command(command = "repeatingmilestone", description = "Main repeatingmilestone command", permission = "playtime.repeatingmilestone", console = true)
public class RepeatingMilestoneCommand {

    @SubCommand(
            subCommand = "create",
            description = "Create a new reapeating milestone",
            usage = "<name> <time>",
            permission = "playtime.repeatingmilestone.create",
            console = true,
            minParams = 3,
            maxParams = 3)
    public void create(CommandSender commandSender, List<String> args) {
        RepeatingMilestone repeatingMilestone = new RepeatingMilestone();
        repeatingMilestone.setMilestoneName(args.get(1));
        long time = getTime(args.get(2));
        repeatingMilestone.setOnlineTime(time);
        Playtime.getInstance().getStorage().saveRepeatingMilestone(repeatingMilestone).thenAcceptAsync(aBoolean -> {
            if (aBoolean) {
                commandSender.sendMessage(Messages.REPEATING_MILESTONE_CREATED.getMessage());
                Bukkit.getPluginManager().callEvent(new RepeatingMilestoneCreateEvent(repeatingMilestone));
            } else {
                commandSender.sendMessage(Messages.REPEATING_MILESTONE_COULD_NOT_BE_CREATED.getMessage());
            }
        });
    }

    @SubCommand(
            subCommand = "delete",
            description = "Delete a repeating milestone",
            usage = "<repeatingMilestone>",
            permission = "playtime.repeatingmilestone.delete",
            minParams = 2,
            maxParams = 2,
            console = true
    )
    public void delete(CommandSender commandSender, List<String> args) {
        RepeatingMilestone milestone = RepeatingMilestone.get(args.get(1));
        if (milestone == null) {
            commandSender.sendMessage(Messages.REPEATING_MILESTONE_DOES_NOT_EXIST.getMessage());
            return;
        }
        Playtime.getInstance().getStorage().deleteRepeatingMilestone(milestone).thenAcceptAsync(aBoolean -> {
            if (aBoolean) {
                commandSender.sendMessage(Messages.REPEATING_MILESTONE_DELETED.getMessage());
                Bukkit.getPluginManager().callEvent(new RepeatingMilestoneDeleteEvent(milestone));
            } else {
                commandSender.sendMessage(Messages.REPEATING_MILESTONE_DOES_NOT_EXIST.getMessage());
            }
        });
    }

    @Default
    @SubCommand(
        subCommand = "list",
        description = "List all milestones",
        permission = "playtime.repeatingmilestone.list",
        console = true
    )
    public void list(CommandSender commandSender, List<String> args) {
        List<RepeatingMilestone> repeatingMilestones = Playtime.getInstance().getRepeatingMilestones();
        commandSender.sendMessage("Repeating milestones: ");
        for (RepeatingMilestone milestone : repeatingMilestones) {
            TextComponent message = new TextComponent(Messages.REPEATING_MILESTONE_LIST.getMessage(
                    new Replacement("%REPEATING_MILESTONE_NAME%", milestone.getMilestoneName()),
                    new Replacement("%D%", String.valueOf(TimeUnit.SECONDS.toDays(milestone.getOnlineTime()))),
                    new Replacement("%H%", String.valueOf(TimeUnit.SECONDS.toHours(milestone.getOnlineTime()) % 24)),
                    new Replacement("%M%", String.valueOf(TimeUnit.SECONDS.toMinutes(milestone.getOnlineTime()) % 60)),
                    new Replacement("%S%", String.valueOf(TimeUnit.SECONDS.toSeconds(milestone.getOnlineTime()) % 60))
            ));
            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/repeatingmilestone info " + milestone.getMilestoneName()));
            commandSender.spigot().sendMessage(message);
        }
    }

    @SubCommand(
        subCommand = "info",
        description = "Get info about a milestone",
        usage = "<repeatingMilestone>",
        permission = "playtime.repeatingmilestone.info",
        minParams = 2,
        maxParams = 2,
        console = true
    )
    public void info(CommandSender commandSender, List<String> args) {
        RepeatingMilestone repeatingMilestone = RepeatingMilestone.get(args.get(1));
        if (repeatingMilestone == null) {
            commandSender.sendMessage(Messages.REPEATING_MILESTONE_DOES_NOT_EXIST.getMessage());
            return;
        }
        commandSender.sendMessage("Repeating milestone: " + repeatingMilestone.getMilestoneName());
        commandSender.sendMessage(" Time: " + repeatingMilestone.getOnlineTime());
        commandSender.sendMessage(" Rewards("+ repeatingMilestone.getItemStacks().size() +"): ");
        repeatingMilestone.getItemStacks().forEach(map -> commandSender.sendMessage("  " + map.toString()));
        commandSender.sendMessage(" Commands("+ repeatingMilestone.getCommands().size() +"): ");
        repeatingMilestone.getCommands().forEach(command -> commandSender.sendMessage("  " + command));
        commandSender.sendMessage(" Messages("+ repeatingMilestone.getMessages().size() +"): ");
        repeatingMilestone.getMessages().forEach(message -> commandSender.sendMessage("  " + message));
        commandSender.sendMessage(" Firework show: " + repeatingMilestone.isFireworkShow());
        if (repeatingMilestone.isFireworkShow()) {
            commandSender.sendMessage(" Firework show delay: " + repeatingMilestone.getFireworkShowSecondsBetween());
            commandSender.sendMessage(" Firework show amount: " + repeatingMilestone.getFireworkShowAmount());
        }
    }

    @SubCommand(
        subCommand = "test",
        description = "Execute the rewards of a repeatingmilestone on yourself",
        usage = "<repeatingMilestone>",
        permission = "playtime.repeatingmilestone.test",
        minParams = 2,
        maxParams = 2
    )
    public void test(CommandSender commandSender, List<String> args) {
        RepeatingMilestone repeatingMilestone = RepeatingMilestone.get(args.get(1));
        if (repeatingMilestone == null) {
            commandSender.sendMessage(Messages.REPEATING_MILESTONE_DOES_NOT_EXIST.getMessage());
            return;
        }
        repeatingMilestone.apply((Player) commandSender);
        commandSender.sendMessage(Messages.MILESTONE_REWARDS_APPLIED.getMessage());
    }

    @SubCommand(
        subCommand = "addItemToMilestone",
        description = "Add the item in your main hand to the milestone",
        usage = "<repeatingMilestone>",
        permission = "playtime.repeatingmilestone.addItemToMilestone",
        minParams = 2,
        maxParams = 2
    )
    public void addItemToMilestone(CommandSender commandSender, List<String> args) {
        Player player = (Player) commandSender;
        RepeatingMilestone repeatingMilestone = RepeatingMilestone.get(args.get(1));

        if (repeatingMilestone == null) {
            commandSender.sendMessage(Messages.REPEATING_MILESTONE_DOES_NOT_EXIST.getMessage());
            return;
        }

        repeatingMilestone.addItemStack(player.getInventory().getItemInMainHand());
        Playtime.getInstance().getStorage().updateRepeatingMilestone(repeatingMilestone);
        commandSender.sendMessage(Messages.REPEATING_MILESTONE_ITEM_ADDED.getMessage());
        Bukkit.getPluginManager().callEvent(new RepeatingMilestoneUpdateEvent(repeatingMilestone));
    }

    @SubCommand(
        subCommand = "addCommand",
        description = "Add a command to the milestone",
        usage = "<repeatingMilestone> <command>",
        permission = "playtime.repeatingmilestone.addCommand",
        minParams = 3,
        maxParams = 3,
        console = true
    )
    public void addCommandToMilestone(CommandSender commandSender, List<String> args) {
        RepeatingMilestone repeatingMilestone = RepeatingMilestone.get(args.get(1));

        if (repeatingMilestone == null) {
            commandSender.sendMessage(Messages.REPEATING_MILESTONE_DOES_NOT_EXIST.getMessage());
            return;
        }

        String command = String.join(" ", args.subList(1, args.size()));
        repeatingMilestone.addCommand(command);
        Playtime.getInstance().getStorage().updateRepeatingMilestone(repeatingMilestone);
        commandSender.sendMessage(Messages.REPEATING_MILESTONE_COMMAND_ADDED.getMessage());
        Bukkit.getPluginManager().callEvent(new RepeatingMilestoneUpdateEvent(repeatingMilestone));
    }

    @SubCommand(
        subCommand = "removeCommand",
        description = "Remove a command from the milestone",
        usage = "<repeatingMilestone> <command>",
        permission = "playtime.repeatingmilestone.removeCommand",
        minParams = 3,
        maxParams = 3,
        console = true
    )
    public void removeCommandFromMilestone(CommandSender commandSender, List<String> args) {
        RepeatingMilestone repeatingMilestone = RepeatingMilestone.get(args.get(1));

        if (repeatingMilestone == null) {
            commandSender.sendMessage(Messages.REPEATING_MILESTONE_DOES_NOT_EXIST.getMessage());
            return;
        }

        String command = String.join(" ", args.subList(1, args.size()));
        repeatingMilestone.removeCommand(command);
        Playtime.getInstance().getStorage().updateRepeatingMilestone(repeatingMilestone);
        commandSender.sendMessage(Messages.REPEATING_MILESTONE_ITEM_REMOVED.getMessage());
        Bukkit.getPluginManager().callEvent(new RepeatingMilestoneUpdateEvent(repeatingMilestone));
    }

    @SubCommand(
        subCommand = "togglefirework",
        description = "Toggle the firework for a milestone",
        usage = "<repeatingMilestone>",
        permission = "playtime.repeatingmilestone.togglefirework",
        minParams = 2,
        maxParams = 2,
        console = true
    )
    public void toggleFirework(CommandSender commandSender, List<String> args) {
        RepeatingMilestone repeatingMilestone = RepeatingMilestone.get(args.get(1));

        if (repeatingMilestone == null) {
            commandSender.sendMessage(Messages.REPEATING_MILESTONE_DOES_NOT_EXIST.getMessage());
            return;
        }

        repeatingMilestone.setFireworkShow(!repeatingMilestone.isFireworkShow());
        Playtime.getInstance().getStorage().updateRepeatingMilestone(repeatingMilestone);
        commandSender.sendMessage(Messages.REPEATING_MILESTONE_FIREWORK_TOGGLED.getMessage(new Replacement("<state>", repeatingMilestone.isFireworkShow() ? "enabled" : "disabled")));
        Bukkit.getPluginManager().callEvent(new RepeatingMilestoneUpdateEvent(repeatingMilestone));
    }

    @SubCommand(
        subCommand = "setfireworkamount",
        description = "Set the amount of firework for a milestone",
        usage = "<repeatingMilestone> <amount>",
        permission = "playtime.repeatingmilestone.setfireworkamount",
        minParams = 3,
        maxParams = 3,
        console = true
    )
    public void setFireworkAmount(CommandSender commandSender, List<String> args) {
        RepeatingMilestone repeatingMilestone = RepeatingMilestone.get(args.get(1));

        if (repeatingMilestone == null) {
            commandSender.sendMessage(Messages.REPEATING_MILESTONE_DOES_NOT_EXIST.getMessage());
            return;
        }

        int amount = Integer.parseInt(args.get(1));
        repeatingMilestone.setFireworkShowAmount(amount);
        Playtime.getInstance().getStorage().updateRepeatingMilestone(repeatingMilestone);
        commandSender.sendMessage(Messages.REPEATING_MILESTONE_SET_FIREWORK_AMOUNT.getMessage(new Replacement("<amount>", String.valueOf(amount))));
        Bukkit.getPluginManager().callEvent(new RepeatingMilestoneUpdateEvent(repeatingMilestone));
    }

    @SubCommand(
        subCommand = "setfireworkdelay",
        description = "Set the delay between fireworks for a milestone",
        usage = "<repeatingMilestone> <time in seconds>",
        permission = "playtime.repeatingmilestone.setfireworkdelay",
        minParams = 4,
        maxParams = 4,
        console = true
    )
    public void setFireworkDelay(CommandSender commandSender, List<String> args) {
        RepeatingMilestone repeatingMilestone = RepeatingMilestone.get(args.get(1));
        if (repeatingMilestone == null) {
            commandSender.sendMessage(Messages.REPEATING_MILESTONE_DOES_NOT_EXIST.getMessage());
            return;
        }
        int delay = Integer.parseInt(args.get(1));
        repeatingMilestone.setFireworkShowSecondsBetween(delay);
        Playtime.getInstance().getStorage().updateRepeatingMilestone(repeatingMilestone);
        commandSender.sendMessage(Messages.REPEATING_MILESTONE_SET_FIREWORK_DELAY.getMessage(new Replacement("<amount>", String.valueOf(delay))));
        Bukkit.getPluginManager().callEvent(new RepeatingMilestoneUpdateEvent(repeatingMilestone));
    }

    @SubCommand(
        subCommand = "addMessage",
        description = "Add a message to a milestone",
        usage = "<repeatingMilestone> <message>",
        permission = "playtime.repeatingmilestone.addMessage",
        minParams = 3,
        maxParams = 3,
        console = true
    )
    public void addMessage(CommandSender commandSender, List<String> args) {
        RepeatingMilestone repeatingMilestone = RepeatingMilestone.get(args.get(1));
        if (repeatingMilestone == null) {
            commandSender.sendMessage(Messages.REPEATING_MILESTONE_DOES_NOT_EXIST.getMessage());
            return;
        }
        String message = String.join(" ", args.subList(1, args.size()));
        repeatingMilestone.addMessage(message);
        Playtime.getInstance().getStorage().updateRepeatingMilestone(repeatingMilestone);
        commandSender.sendMessage(Messages.REPEATING_MILESTONE_MESSAGE_ADDED.getMessage());
        Bukkit.getPluginManager().callEvent(new RepeatingMilestoneUpdateEvent(repeatingMilestone));
    }

    @SubCommand(
        subCommand = "removeMessage",
        description = "Remove a message from a milestone",
        usage = "<repeatingMilestone> <message>",
        permission = "playtime.repeatingmilestone.removeMessage",
        minParams = 3,
        maxParams = 3,
        console = true
    )
    public void removeMessage(CommandSender commandSender, List<String> args) {
        RepeatingMilestone repeatingMilestone = RepeatingMilestone.get(args.get(1));
        if (repeatingMilestone == null) {
            commandSender.sendMessage(Messages.REPEATING_MILESTONE_DOES_NOT_EXIST.getMessage());
            return;
        }
        String message = String.join(" ", args.subList(1, args.size()));
        repeatingMilestone.removeMessage(message);
        Playtime.getInstance().getStorage().updateRepeatingMilestone(repeatingMilestone);
        commandSender.sendMessage(Messages.REPEATING_MILESTONE_MESSAGE_REMOVED.getMessage());
        Bukkit.getPluginManager().callEvent(new RepeatingMilestoneUpdateEvent(repeatingMilestone));
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
