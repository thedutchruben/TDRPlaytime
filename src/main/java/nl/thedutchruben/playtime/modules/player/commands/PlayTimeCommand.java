package nl.thedutchruben.playtime.modules.player.commands;

import nl.thedutchruben.mccore.spigot.commands.Command;
import nl.thedutchruben.mccore.spigot.commands.Default;
import nl.thedutchruben.mccore.spigot.commands.SubCommand;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.translations.Messages;
import nl.thedutchruben.playtime.utils.Replacement;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@Command(command = "playtime", description = "Main playtime command", permission = "playtime.playtime", console = true)
public class PlayTimeCommand {

    @Default
    @SubCommand(subCommand = "", description = "Show your own playtime")
    public void myTime(CommandSender commandSender, List<String> args) {
        if (commandSender instanceof Player) {
            Playtime.getInstance().getPlaytimeUsers().get(((Player) commandSender).getUniqueId()).updatePlaytime();
            commandSender.sendMessage(Messages.PLAYTIME_INFO_OWN.getMessage(new Replacement("%playtime%",String.valueOf(Playtime.getInstance().getPlaytimeUsers().get(((Player) commandSender).getUniqueId()).getTime()))));
        }else{
            commandSender.sendMessage("You need to be a player to use this command");
        }
    }

}
