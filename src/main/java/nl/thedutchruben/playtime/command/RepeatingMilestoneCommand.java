package nl.thedutchruben.playtime.command;

import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.milestone.RepeatingMilestone;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;
import java.util.regex.Pattern;

public class RepeatingMilestoneCommand implements CommandExecutor, TabCompleter {
    private final Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

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

        if (args.length <= 1) {
            sendHelp(sender);
        } else {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "create":
                    if (args.length == 3) {
                        try {
                            createMilestone(sender, args[1], Integer.parseInt(args[2]));
                        } catch (NumberFormatException e) {
                            sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.mustbenumber"));
                        }
                    } else {
                        sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.createusage"));
                    }
                    break;
                case "additem":
                    if (args[1] != null) {
                        Optional<RepeatingMilestone> milestoneOptional = Playtime.getInstance().getRepeatedMilestoneList().stream().filter(milestone -> milestone.getMilestoneName().equalsIgnoreCase(args[1].replace("_", " "))).findFirst();
                        if (milestoneOptional.isPresent()) {
                            addItemToMilestone((Player) sender, milestoneOptional.get());
                        } else {
                            Optional<RepeatingMilestone> milestoneOptional2 = Playtime.getInstance().getRepeatedMilestoneList().stream().filter(milestone -> milestone.getMilestoneName().equalsIgnoreCase(args[1])).findFirst();
                            if (milestoneOptional2.isPresent()) {
                                addItemToMilestone((Player) sender, milestoneOptional2.get());
                            } else {
                                sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.milestonenotexist").replace("<name>", args[1]));

                            }
                        }
                    } else {
                        sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.additemusage"));

                    }
                    break;

                case "addcommand":
                    if (args.length >= 3) {
                        Optional<RepeatingMilestone> milestoneOptional = Playtime.getInstance().getRepeatedMilestoneList().stream().filter(milestone -> milestone.getMilestoneName().equalsIgnoreCase(args[1].replace("_", " "))).findFirst();
                        if (milestoneOptional.isPresent()) {
                            StringBuilder commandd = new StringBuilder();
                            for (int i = 2; i < args.length; i++) {
                                commandd.append(args[i]).append(" ");
                            }
                            addCommandToMilestone((Player) sender, milestoneOptional.get(), commandd.toString());
                        } else {
                            Optional<RepeatingMilestone> milestoneOptional2 = Playtime.getInstance().getRepeatedMilestoneList().stream().filter(milestone -> milestone.getMilestoneName().equalsIgnoreCase(args[1])).findFirst();
                            if (milestoneOptional2.isPresent()) {
                                addItemToMilestone((Player) sender, milestoneOptional2.get());
                            } else {
                                sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.milestonenotexist").replace("<name>", args[1]));
                            }
                        }
                    } else {
                        sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.addcommandusage"));
                    }
                    break;
                case "togglefirework":
                    if (args[1] != null) {
                        Optional<RepeatingMilestone> milestoneOptional = Playtime.getInstance().getRepeatedMilestoneList().stream().filter(milestone -> milestone.getMilestoneName().equalsIgnoreCase(args[1].replace("_", " "))).findFirst();
                        if (milestoneOptional.isPresent()) {
                            toggleFirework((Player) sender, milestoneOptional.get());
                        } else {
                            Optional<RepeatingMilestone> milestoneOptional2 = Playtime.getInstance().getRepeatedMilestoneList().stream().filter(milestone -> milestone.getMilestoneName().equalsIgnoreCase(args[1])).findFirst();
                            if (milestoneOptional2.isPresent()) {
                                toggleFirework((Player) sender, milestoneOptional2.get());
                            } else {
                                sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.milestonenotexist").replace("<name>", args[1]));

                            }
                        }
                    } else {
                        sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.togglefireworkusage"));

                    }
                    break;
                case "setfireworkamount":
                    if (args[1] != null && args[2] != null && isNumeric(args[2])) {
                        Optional<RepeatingMilestone> milestoneOptional = Playtime.getInstance().getRepeatedMilestoneList().stream().filter(milestone -> milestone.getMilestoneName().equalsIgnoreCase(args[1].replace("_", " "))).findFirst();
                        if (milestoneOptional.isPresent()) {
                            setFireworkAmount((Player) sender, milestoneOptional.get(), Integer.parseInt(args[2]));
                        } else {
                            Optional<RepeatingMilestone> milestoneOptional2 = Playtime.getInstance().getRepeatedMilestoneList().stream().filter(milestone -> milestone.getMilestoneName().equalsIgnoreCase(args[1])).findFirst();
                            if (milestoneOptional2.isPresent()) {
                                setFireworkAmount((Player) sender, milestoneOptional2.get(), Integer.parseInt(args[2]));
                            } else {
                                sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.milestonenotexist").replace("<name>", args[1]));

                            }
                        }
                    } else {
                        sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.setfireworkamountusage"));

                    }
                    break;
                case "setfireworkdelay":
                    if (args[1] != null && args[2] != null && isNumeric(args[2])) {
                        Optional<RepeatingMilestone> milestoneOptional = Playtime.getInstance().getRepeatedMilestoneList().stream().filter(milestone -> milestone.getMilestoneName().equalsIgnoreCase(args[1].replace("_", " "))).findFirst();
                        if (milestoneOptional.isPresent()) {
                            setFireworkDelay((Player) sender, milestoneOptional.get(), Integer.parseInt(args[2]));
                        } else {
                            Optional<RepeatingMilestone> milestoneOptional2 = Playtime.getInstance().getRepeatedMilestoneList().stream().filter(milestone -> milestone.getMilestoneName().equalsIgnoreCase(args[1])).findFirst();
                            if (milestoneOptional2.isPresent()) {
                                setFireworkDelay((Player) sender, milestoneOptional2.get(), Integer.parseInt(args[2]));
                            } else {
                                sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.milestonenotexist").replace("<name>", args[1]));

                            }
                        }
                    } else {
                        sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.setfireworkdelayusage"));

                    }
                    break;
                default:
                    sendHelp(sender);

            }
        }
        return false;
    }

    public void createMilestone(CommandSender sender, String name, int timeInSeconds) {
        RepeatingMilestone milestone = new RepeatingMilestone();
        milestone.setMilestoneName(name);
        milestone.setOnlineTime(timeInSeconds);
        Playtime.getInstance().getRepeatedMilestoneList().add(milestone);
        Playtime.getInstance().getStorage().createRepeatingMilestone(milestone).whenComplete((unused, throwable) -> {
            sender.sendMessage(Playtime.getInstance().getMessage("command.milestone.milestonecreated"));
        });

    }

    public void addItemToMilestone(Player player, RepeatingMilestone milestone) {
        milestone.getItemStacks().add(player.getInventory().getItemInMainHand().serialize());
        Playtime.getInstance().getStorage().saveRepeatingMileStone(milestone).whenComplete((unused, throwable) -> {
            player.sendMessage(Playtime.getInstance().getMessage("command.milestone.itemadded"));
        });

    }

    public void addCommandToMilestone(Player player, RepeatingMilestone milestone, String command) {
        milestone.getCommands().add(command);
        Playtime.getInstance().getStorage().saveRepeatingMileStone(milestone).whenComplete((unused, throwable) -> {
            player.sendMessage(Playtime.getInstance().getMessage("command.milestone.commandadded"));
        });

    }

    public void toggleFirework(Player player, RepeatingMilestone milestone) {
        milestone.setFireworkShow(!milestone.isFireworkShow());
        Playtime.getInstance().getStorage().saveRepeatingMileStone(milestone).whenComplete((unused, throwable) -> {
            player.sendMessage(Playtime.getInstance().getMessage("command.milestone.togglefirework").replaceAll("<state>", getState(milestone.isFireworkShow())));
        });
    }

    public void setFireworkAmount(Player player, RepeatingMilestone milestone, int amount) {
        milestone.setFireworkShowAmount(amount);
        Playtime.getInstance().getStorage().saveRepeatingMileStone(milestone).whenComplete((unused, throwable) -> {
            player.sendMessage(Playtime.getInstance().getMessage("command.milestone.setfireworkamount").replaceAll("<amount>", amount + ""));
        });
    }

    public void setFireworkDelay(Player player, RepeatingMilestone milestone, int delay) {
        milestone.setFireworkShowSecondsBetween(delay);
        Playtime.getInstance().getStorage().saveRepeatingMileStone(milestone).whenComplete((unused, throwable) -> {
            player.sendMessage(Playtime.getInstance().getMessage("command.milestone.setfireworkdelay").replaceAll("<delay>", delay + ""));
        });
    }

    public void sendHelp(CommandSender commandSender) {
//        commandSender.sendMessage("");
    }

    public String getState(boolean b) {
        return b ? "Enabled" : "Disabled";
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
        final List<String> completions = new ArrayList<>();

        Set<String> COMMANDS = new HashSet<>();
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("additem") || args[0].equalsIgnoreCase("addcommand") || args[0].equalsIgnoreCase("togglefirework")
                    || args[0].equalsIgnoreCase("setfireworkamount") || args[0].equalsIgnoreCase("setfireworkdelay") || args[0].equalsIgnoreCase("overridable")) {
                for (RepeatingMilestone value : Playtime.getInstance().getRepeatedMilestoneList()) {
                    COMMANDS.add(value.getMilestoneName().replace(" ", "_"));
                }

                StringUtil.copyPartialMatches(args[1], COMMANDS, completions);
            }

        }
        if (args.length == 1) {
            COMMANDS.add("create");
            COMMANDS.add("additem");
            COMMANDS.add("addcommand");
            COMMANDS.add("help");
            COMMANDS.add("togglefirework");
            COMMANDS.add("setfireworkamount");
            COMMANDS.add("setfireworkdelay");

            StringUtil.copyPartialMatches(args[0], COMMANDS, completions);
        }


        Collections.sort(completions);

        return completions;
    }

    public boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }
}
