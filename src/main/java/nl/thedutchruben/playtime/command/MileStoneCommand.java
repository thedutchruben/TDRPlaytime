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
import java.util.Optional;

@Command(command = "milestone", description = "Milestones command", permission = "playtime.milestones", console = true)
public class MileStoneCommand {

    @SubCommand(subCommand = "create", usage = "<string> <time>", minParams = 3, maxParams = 3, description = "Create a milestone")
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
    @SubCommand(subCommand = "list", description = "Show a list of all the milestones" , console = true)
    public void list(CommandSender sender, List<String> args) {
        Playtime.getInstance().getMilestoneMap().forEach((aLong, milestone) -> {
            for (String s : Playtime.getInstance().getLangFile().get().getStringList("command.milestone.list")) {
                sender.sendMessage(
                        translateMessage(s.replaceAll("%MILESTONE_NAME%", milestone.getMilestoneName()), aLong));
            }
        });

    }

    @SubCommand(subCommand = "test", usage = "<milestone>", minParams = 2, maxParams = 2, description = "Test milestone rewards on yourself")
    public void test(CommandSender sender, List<String> args) {
        Milestone milestone = getMilestone(sender,args.get(1));
        if(milestone == null) return;
        milestone.apply((Player) sender);
    }

    @SubCommand(subCommand = "remove", usage = "<milestone>", minParams = 2, maxParams = 2, description = "Remove a milestone")
    public void remove(CommandSender sender, List<String> args) {
        Milestone milestone = getMilestone(sender,args.get(1));
        if(milestone == null) return;
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

    @SubCommand(subCommand = "info", usage = "<milestone>", minParams = 2, maxParams = 2, description = "Show the info of a milestone")
    public void info(CommandSender sender, List<String> args) {

        Milestone milestone = getMilestone(sender, args.get(1));
        if(milestone == null) return;
        for (String s : Playtime.getInstance().getLangFile().get().getStringList("command.milestone.info")) {
            if (s.contains("%REWARD_COMMAND%")) {
                if (milestone.getCommands().isEmpty()) {
                    sender.sendMessage(ChatColor.DARK_RED + "     No commands found");
                } else {
                    for (String command : milestone.getCommands()) {
                        TextComponent textComponent = new TextComponent("     " + command);
                        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{
                                new TextComponent(ChatColor.DARK_RED + "Click remove the command")}));
                        textComponent.setClickEvent(
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/milestone removeCommand "
                                        + milestone.getMilestoneName() + " \"" + command + "\""));
                        sender.spigot().sendMessage(textComponent);
                    }
                }
            } else if (s.contains("%REWARD_MESSAGES%")) {
                if (milestone.getCommands().isEmpty()) {
                    sender.sendMessage(ChatColor.DARK_RED + "     No messages found");
                } else {
                    for (String message : milestone.getMessages()) {
                        TextComponent textComponent = new TextComponent("     " + message);
                        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{
                                new TextComponent(ChatColor.DARK_RED + "Click remove the message")}));
                        textComponent.setClickEvent(
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/milestone removeMessage "
                                        + milestone.getMilestoneName() + " \"" + message + "\""));
                        sender.spigot().sendMessage(textComponent);
                    }
                }
            } else if (s.contains("%REWARD_ITEMS%")) {
                if (milestone.getItemStacks().isEmpty()) {
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
                                "/milestone removeItem " + milestone.getMilestoneName() + " " + name));
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

    @SubCommand(subCommand = "addItem", usage = "<milestone>", minParams = 2, maxParams = 2, description = "Add the item in your hand to the milestone")
    public void addItemToMilestone(CommandSender sender, List<String> args) {
        Milestone milestone = getMilestone(sender,args.get(1));
        if(milestone == null) return;
        milestone.getItemStacks().add(((Player) sender).getInventory().getItemInMainHand().serialize());
        Playtime.getInstance().getStorage().saveMileStone(milestone).whenComplete((unused, throwable) -> {
            sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.itemadded"));
        });

    }

    @SubCommand(subCommand = "removeItem", usage = "<milestone> <string>", minParams = 3, maxParams = 3, description = "Remove the item from the milestone")
    public void removeItemFromMilestone(CommandSender sender, List<String> args) {
        Milestone milestone = getMilestone(sender,args.get(1));
        if(milestone == null) return;
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

    @SubCommand(subCommand = "addMessage", usage = "<milestone> <string>", minParams = 3, maxParams = 3, description = "Add a message to the milestone")
    public void addMessageToMilestone(CommandSender sender, List<String> args) {
        Milestone milestone = getMilestone(sender,args.get(1));
        if(milestone == null) return;
        milestone.getMessages().add(args.get(2));
        Playtime.getInstance().getStorage().saveMileStone(milestone).whenComplete((unused, throwable) -> {
            sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.messageadded"));
        });
    }

    @SubCommand(subCommand = "removeMessage", usage = "<milestone> <string>", minParams = 3, maxParams = 3, description = "Remove a message from the milestone")
    public void removeMessageFromMilestone(CommandSender sender, List<String> args) {
        Milestone milestone = getMilestone(sender,args.get(1));
        if(milestone == null) return;
        milestone.getMessages().remove(args.get(2));
        Playtime.getInstance().getStorage().saveMileStone(milestone).whenComplete((unused, throwable) -> {
            sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.messageremoved"));
        });
    }

    @SubCommand(subCommand = "addCommand", usage = "<milestone> <string>", minParams = 3, maxParams = 3, description = "Add a command to the milestone")
    public void addCommandToMilestone(CommandSender sender, List<String> args) {
        Milestone milestone = getMilestone(sender,args.get(1));
        if(milestone == null) return;
        milestone.getCommands().add(args.get(2));
        Playtime.getInstance().getStorage().saveMileStone(milestone).whenComplete((unused, throwable) -> {
            sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.commandadded"));
        });
    }

    @SubCommand(subCommand = "removeCommand", usage = "<milestone> <string>", minParams = 3, maxParams = 3, description = "Remove a command from the milestone")
    public void removeCommandFromMilestone(CommandSender sender, List<String> args) {
        Milestone milestone = getMilestone(sender,args.get(1));
        if(milestone == null) return;
        milestone.getCommands().remove(args.get(2));
        Playtime.getInstance().getStorage().saveMileStone(milestone).whenComplete((unused, throwable) -> {
            sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.commandremoved"));
        });

    }

    @SubCommand(subCommand = "toggleFirework", usage = "<milestone>", minParams = 2, maxParams = 2, description = "Toggle the firework show on the milestone")
    public void toggleFirework(CommandSender sender, List<String> args) {
        Milestone milestone = getMilestone(sender,args.get(1));
        if(milestone == null) return;
        milestone.setFireworkShow(!milestone.isFireworkShow());
        Playtime.getInstance().getStorage().saveMileStone(milestone).whenComplete((unused, throwable) -> {
            sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.fireworktoggled")
                    .replaceAll("<state>", getState(milestone.isFireworkShow())));
        });
    }

    @SubCommand(subCommand = "setFireworkAmount", usage = "<milestone> <integer>", minParams = 3, maxParams = 3, description = "Set the amount of fireworks to show")
    public void setFireworkAmount(CommandSender sender, List<String> args) {
        Milestone milestone = getMilestone(sender,args.get(1));
        if(milestone == null) return;
        milestone.setFireworkShowAmount(Integer.parseInt(args.get(2)));
        Playtime.getInstance().getStorage().saveMileStone(milestone).whenComplete((unused, throwable) -> {
            sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.setfireworkamount",
                    new Replacement("<amount>", args.get(2))));
        });
    }

    @SubCommand(subCommand = "setFireworkDelay", usage = "<milestone> <integer>", minParams = 3, maxParams = 3, description = "Set the delay between the fireworks")
    public void setFireworkDelay(CommandSender sender, List<String> args) {
        Milestone milestone = getMilestone(sender,args.get(1));
        if(milestone == null) return;
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

    public Milestone getMilestone(CommandSender sender,String args){
        Optional<Milestone> optionalMilestone = Playtime.getInstance().getMilestoneMap().values().stream()
                .filter(milestone1 -> milestone1.getMilestoneName().equalsIgnoreCase(args)).findFirst();
        if(optionalMilestone.isPresent()){
            return optionalMilestone.get();
        }
        sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.milestonenotexist",new Replacement("<name>",args)));
        return null;
    }
}
