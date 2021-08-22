package nl.thedutchruben.playtime.command;

import lombok.SneakyThrows;
import nl.thedutchruben.playtime.Playtime;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

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
    @SneakyThrows
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Playtime.getInstance().getMessage("only.player.command"));
            return false;
        }
        if (args.length == 0) {
            Playtime.getInstance().update(((Player) sender).getUniqueId(), true);
            sender.sendMessage(translateMessage(Playtime.getInstance().getMessage("command.playtime.timemessage"), Playtime.getInstance().getPlayerOnlineTime().get(((Player) sender).getUniqueId())));
        } else {
            switch (args[0]) {
                case "top":
                    if (sender.hasPermission("playtime.playtime.top")) {
                        Playtime.getInstance().getStorage().getTopTenList().whenCompleteAsync((stringLongMap, throwable) -> {
                            sortHashMapByValues((HashMap<String, Long>) stringLongMap).forEach((s, aLong) -> sender.sendMessage(translateMessage(Playtime.getInstance().getMessage("command.playtime.usertimemessage").replaceAll(
                                    "%NAME%", s), aLong)));
                        });

                    }
                    break;
                case "reset":
                    if (sender.hasPermission("playtime.playtime.reset")) {
                        if (args.length == 2) {
                            if (Bukkit.getPlayer(args[1]) != null) {
                                Playtime.getInstance().getPlayerOnlineTime().replace(Bukkit.getPlayer(args[1]).getUniqueId(), (long) 0);
                                Playtime.getInstance().getLastCheckedTime().replace(Bukkit.getPlayer(args[1]).getUniqueId(), System.currentTimeMillis());
                            }
                            Playtime.getInstance().getStorage().reset(Bukkit.getPlayer(args[1]).getName());
                            sender.sendMessage(Playtime.getInstance().getMessage("command.playtime.resettimeconfirm"));
                        } else {
                            sender.sendMessage(Playtime.getInstance().getMessage("command.playtime.resettimeussage"));
                        }
                    }
                    break;
                case "migratefromminecraft":
                    if (sender.hasPermission("playtime.playtime.migratefromminecraft")) {
                        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                            long playtime = (long) offlinePlayer.getStatistic(Statistic.PLAY_ONE_MINUTE) * 60 * 60 * 1000;
                            Playtime.getInstance().getStorage().savePlayTime(offlinePlayer.getUniqueId().toString(), playtime);
                        }
                        sender.sendMessage(ChatColor.GREEN + "migrated");
                    }
                    break;
                default:
                    if (sender.hasPermission("playtime.playtime.other")) {
                        if (args[0].length() <= 16) {
                            if (Bukkit.getPlayer(args[0]) == null) {

                                sender.sendMessage(translateMessage(Playtime.getInstance().getMessage("command.playtime.usertimemessage").replaceAll(
                                                "%NAME%", Bukkit.getOfflinePlayer(args[0]).getName())
                                        , Playtime.getInstance().getStorage().getPlayTimeByName(args[0]).get()));

                            } else {
                                Playtime.getInstance().update(Bukkit.getPlayer(args[0]).getUniqueId(), true);
                                sender.sendMessage(translateMessage(Playtime.getInstance().getMessage("command.playtime.usertimemessage").replaceAll("%NAME%", Bukkit.getPlayer(args[0]).getName()), Playtime.getInstance().getPlayerOnlineTime().get((Bukkit.getPlayer(args[0])).getUniqueId())));
                            }
                        } else {
                            if (Bukkit.getPlayer(UUID.fromString(args[0])) == null) {
                                sender.sendMessage(translateMessage(Playtime.getInstance().getMessage("command.playtime.usertimemessage").replaceAll(
                                                "%NAME%", Bukkit.getOfflinePlayer(UUID.fromString(args[0])).getName())
                                        , Playtime.getInstance().getStorage().getPlayTimeByUUID(args[0]).get()));

                            } else {
                                Playtime.getInstance().update(UUID.fromString(args[0]), true);
                                sender.sendMessage(translateMessage(Playtime.getInstance().getMessage("command.playtime.usertimemessage").replaceAll("%NAME%", Bukkit.getPlayer(UUID.fromString(args[0])).getName()), Playtime.getInstance().getPlayerOnlineTime().get(UUID.fromString(args[0]))));
                            }
                        }
                    }
            }
        }
        return true;
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


    public LinkedHashMap<String, Long> sortHashMapByValues(
            HashMap<String, Long> passedMap) {
        List<String> mapKeys = new ArrayList<>(passedMap.keySet());
        List<Long> mapValues = new ArrayList<>(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);
        Collections.reverse(mapValues);
        Collections.reverse(mapKeys);

        LinkedHashMap<String, Long> sortedMap =
                new LinkedHashMap<>();

        Iterator<Long> valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            long val = valueIt.next();
            Iterator<String> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                String key = keyIt.next();
                Long comp1 = passedMap.get(key);

                if (comp1.equals(val)) {
                    keyIt.remove();
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap;
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
            if (args[0].equalsIgnoreCase("reset")) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    COMMANDS.add(onlinePlayer.getName());
                }
                StringUtil.copyPartialMatches(args[1], COMMANDS, completions);
            }

        }
        if (args.length == 1) {
            if (sender.hasPermission("playtime.playtime.top"))
                COMMANDS.add("top");
            if (sender.hasPermission("playtime.playtime.reset"))
                COMMANDS.add("reset");
            if (sender.hasPermission("playtime.playtime.migratefromminecraft"))
                COMMANDS.add("migratefromminecraft");
            if (sender.hasPermission("playtime.playtime.other"))
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    COMMANDS.add(onlinePlayer.getName());
                }
            StringUtil.copyPartialMatches(args[0], COMMANDS, completions);
        }


        Collections.sort(completions);

        return completions;
    }
}
