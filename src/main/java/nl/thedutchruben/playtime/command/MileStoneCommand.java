package nl.thedutchruben.playtime.command;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import nl.thedutchruben.mccore.spigot.commands.Command;
import nl.thedutchruben.mccore.spigot.commands.Default;
import nl.thedutchruben.mccore.spigot.commands.SubCommand;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.milestone.Milestone;
import nl.thedutchruben.playtime.utils.Replacement;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

@Command(command = "milestone", description = "Milestones command", permission = "playtime.milestones", console = true)
public class MileStoneCommand {

    @SubCommand(subCommand = "create", usage = "<string> <time>", minParams = 3, maxParams = 3)
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
    @SubCommand(subCommand = "list")
    public void list(CommandSender sender, List<String> args) {
        Playtime.getInstance().getMilestoneMap().forEach((aLong, milestone) -> {
            for (String s : Playtime.getInstance().getLangFile().get().getStringList("command.milestone.list")) {
                sender.sendMessage(
                        translateMessage(s.replaceAll("%MILESTONE_NAME%", milestone.getMilestoneName()), aLong));
            }
        });

    }

    @Default
    @SubCommand(subCommand = "test", usage = "<milestone>", minParams = 2, maxParams = 2, console = false)
    public void test(CommandSender sender, List<String> args) {
        Milestone milestone = Playtime.getInstance().getMilestoneMap().values().stream()
                .filter(milestone1 -> milestone1.getMilestoneName().equalsIgnoreCase(args.get(1))).findFirst().get();
        milestone.apply((Player) sender);
    }

    @SubCommand(subCommand = "remove", usage = "<milestone>", minParams = 2, maxParams = 2)
    public void remove(CommandSender sender, List<String> args) {
        Milestone milestone = Playtime.getInstance().getMilestoneMap().values().stream()
                .filter(milestone1 -> milestone1.getMilestoneName().equalsIgnoreCase(args.get(1))).findFirst().get();
        Playtime.getInstance().getStorage().removeMileStone(milestone).whenComplete((unused, throwable) -> {
            sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.milestoneremoved"));
            Playtime.getInstance().getMilestoneMap().clear();
            Playtime.getInstance().getStorage().getMilestones().whenComplete((milestones, throwable1) -> {
                for (Milestone storageMilestone : milestones) {
                    Playtime.getInstance().getMilestoneMap().put(storageMilestone.getOnlineTime() * 1000L,
                            storageMilestone);
                }
            });
        });
    }

    @SubCommand(subCommand = "info", usage = "<milestone>", minParams = 2, maxParams = 2)
    public void info(CommandSender sender, List<String> args) {
        Milestone milestone = Playtime.getInstance().getMilestoneMap().values().stream()
                .filter(milestone1 -> milestone1.getMilestoneName().equalsIgnoreCase(args.get(1))).findFirst().get();
        for (String s : Playtime.getInstance().getLangFile().get().getStringList("command.milestone.info")) {
            if (s.contains("%REWARD_COMMAND%")) {
                if (milestone.getCommands().size() == 0) {
                    sender.sendMessage(ChatColor.DARK_RED + "     No commands found");
                } else {
                    for (String command : milestone.getCommands()) {
                        TextComponent textComponent = new TextComponent("     " + command);
                        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{
                                new TextComponent(ChatColor.DARK_RED + "Click remove the command")}));
                        textComponent.setClickEvent(
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/milestone removeCommandFromMilestone "
                                        + milestone.getMilestoneName() + " \"" + command + "\""));
                        sender.spigot().sendMessage(textComponent);
                    }
                }
            } else if (s.contains("%REWARD_ITEMS%")) {
                if (milestone.getItemStacks().size() == 0) {
                    sender.sendMessage(ChatColor.DARK_RED + "     No items found");
                } else {
                    for (Map<String, Object> itemStack : milestone.getItemStacks()) {
                        ItemStack itemStack1 = ItemStack.deserialize(itemStack);
                        String name = "";
                        if (itemStack1.hasItemMeta()) {
                            if (itemStack1.getItemMeta().hasDisplayName()) {
                                name = itemStack1.getItemMeta().getDisplayName();
                            } else {
                                name = itemStack1.getType().name();
                            }
                        } else {
                            name = itemStack1.getType().name();
                        }
                        TextComponent textComponent = new TextComponent(
                                "     " + name + " x " + itemStack1.getAmount());
                        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{
                                new TextComponent(ChatColor.DARK_RED + "Click remove the item")}));

                        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/milestone removeItemFromMilestone " + milestone.getMilestoneName() + " " + name));
                        sender.spigot().sendMessage(textComponent);
                    }

                }
            } else {
                sender.sendMessage(
                        translateMessage(
                                s.replaceAll("%REWARD_ITEMS_COUNT%", String.valueOf(milestone.getItemStacks().size()))
                                        .replaceAll("%REWARD_COMMAND_COUNT%",
                                                String.valueOf(milestone.getCommands().size()))
                                        .replaceAll("%MILESTONE_NAME%", milestone.getMilestoneName()),
                                milestone.getOnlineTime() * 1000));

            }
        }
    }

    @SubCommand(subCommand = "addItemToMilestone", usage = "<milestone>", minParams = 2, maxParams = 2)
    public void addItemToMilestone(CommandSender sender, List<String> args) {
        Milestone milestone = Playtime.getInstance().getMilestoneMap().values().stream()
                .filter(milestone1 -> milestone1.getMilestoneName().equalsIgnoreCase(args.get(1))).findFirst().get();
        milestone.getItemStacks().add(((Player) sender).getInventory().getItemInMainHand().serialize());
        Playtime.getInstance().getStorage().saveMileStone(milestone).whenComplete((unused, throwable) -> {
            sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.itemadded"));
        });

    }

    @SubCommand(subCommand = "removeItemFromMilestone", usage = "<milestone> <string>", minParams = 3, maxParams = 3)
    public void removeItemFromMilestone(CommandSender sender, List<String> args) {
        Milestone milestone = Playtime.getInstance().getMilestoneMap().values().stream()
                .filter(milestone1 -> milestone1.getMilestoneName().equalsIgnoreCase(args.get(1))).findFirst().get();
        for (Map<String, Object> itemStack : milestone.getItemStacks()) {
            ItemStack itemStack1 = ItemStack.deserialize(itemStack);
            String name = "";
            if (itemStack1.hasItemMeta()) {
                if (itemStack1.getItemMeta().hasDisplayName()) {
                    name = itemStack1.getItemMeta().getDisplayName();
                } else {
                    name = itemStack1.getType().name();
                }
            } else {
                name = itemStack1.getType().name();
            }
            if (name.equalsIgnoreCase(args.get(2))) {
                milestone.getItemStacks().remove(itemStack);
                Playtime.getInstance().getStorage().saveMileStone(milestone).whenComplete((unused, throwable) -> {
                    sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.itemremoved"));
                });
                return;
            }
        }

    }

    @SubCommand(subCommand = "addCommandToMilestone", usage = "<milestone> <string>", minParams = 3, maxParams = 3)
    public void addCommandToMilestone(CommandSender sender, List<String> args) {
        Milestone milestone = Playtime.getInstance().getMilestoneMap().values().stream()
                .filter(milestone1 -> milestone1.getMilestoneName().equalsIgnoreCase(args.get(1))).findFirst().get();
        milestone.getCommands().add(args.get(2));
        Playtime.getInstance().getStorage().saveMileStone(milestone).whenComplete((unused, throwable) -> {
            sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.commandadded"));
        });

    }

    @SubCommand(subCommand = "removeCommandFromMilestone", usage = "<milestone> <string>", minParams = 3, maxParams = 3)
    public void removeCommandFromMilestone(CommandSender sender, List<String> args) {
        Milestone milestone = Playtime.getInstance().getMilestoneMap().values().stream()
                .filter(milestone1 -> milestone1.getMilestoneName().equalsIgnoreCase(args.get(1))).findFirst().get();
        System.out.println(milestone.getCommands());
        System.out.println(args.get(2));
        milestone.getCommands().remove(args.get(2));
        Playtime.getInstance().getStorage().saveMileStone(milestone).whenComplete((unused, throwable) -> {
            sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.commandremoved"));
        });

    }

    @SubCommand(subCommand = "toggleFirework", usage = "<milestone>", minParams = 2, maxParams = 2)
    public void toggleFirework(CommandSender sender, List<String> args) {
        Milestone milestone = Playtime.getInstance().getMilestoneMap().values().stream()
                .filter(milestone1 -> milestone1.getMilestoneName().equalsIgnoreCase(args.get(1))).findFirst().get();
        milestone.setFireworkShow(!milestone.isFireworkShow());
        Playtime.getInstance().getStorage().saveMileStone(milestone).whenComplete((unused, throwable) -> {
            sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.fireworktoggled")
                    .replaceAll("<state>", getState(milestone.isFireworkShow())));
        });
    }

    @SubCommand(subCommand = "setFireworkAmount", usage = "<milestone> <integer>", minParams = 3, maxParams = 3)
    public void setFireworkAmount(CommandSender sender, List<String> args) {
        Milestone milestone = Playtime.getInstance().getMilestoneMap().values().stream()
                .filter(milestone1 -> milestone1.getMilestoneName().equalsIgnoreCase(args.get(1))).findFirst().get();
        milestone.setFireworkShowAmount(Integer.parseInt(args.get(2)));
        Playtime.getInstance().getStorage().saveMileStone(milestone).whenComplete((unused, throwable) -> {
            sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.setfireworkamount",
                    new Replacement("<amount>", args.get(2))));
        });
    }

    @SubCommand(subCommand = "setFireworkDelay", usage = "<milestone> <integer>", minParams = 3, maxParams = 3)
    public void setFireworkDelay(CommandSender sender, List<String> args) {
        Milestone milestone = Playtime.getInstance().getMilestoneMap().values().stream()
                .filter(milestone1 -> milestone1.getMilestoneName().equalsIgnoreCase(args.get(1))).findFirst().get();
        milestone.setFireworkShowSecondsBetween(Integer.parseInt(args.get(2)));
        Playtime.getInstance().getStorage().saveMileStone(milestone).whenComplete((unused, throwable) -> {
            sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.setfireworkdelay",
                    new Replacement("<delay>", args.get(2))));
        });
    }

    public String getState(boolean b) {
        return b ? Playtime.getInstance().getMessage("command.defaults.enabled")
                : Playtime.getInstance().getMessage("command.defaults.disabled");
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
        return ChatColor.translateAlternateColorCodes('&',
                message.replace("%H%", String.valueOf(hours)).replace("%M%", String.valueOf(minutes))
                        .replace("%S%", String.valueOf(seconds)).replace("%D%", String.valueOf(days)));
    }
}
