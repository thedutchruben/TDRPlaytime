package nl.thedutchruben.playtime.core.translations;

import lombok.Getter;
import nl.thedutchruben.mccore.utils.message.MessageUtil;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.utils.Replacement;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.Map;

public enum Messages {


    PLAYTIME_INFO_OWN("command.playtime.timemessage","&8[&6PlayTime&8] &7Your playtime is &6%D% &7day(s) &6%H% &7hour(s) &6%M% &7minute(s) &6%S% &7second(s)"),
    PLAYTIME_INFO_OTHER("command.playtime.usertimemessage","&8[&6PlayTime&8] &7%NAME% ''s playtime is &6%D% &7day(s) &6%H% &7hour(s) &6%M% &7minute(s) &6%S% &7second(s)"),
    PLAYER_RESET_CONFIRM("command.playtime.resettimeconfirm",""),
    PLAYER_DOES_NOT_EXIST("command.playtime.player_does_not_exist",""),
    TIME_ADDED_TO_USER("command.playtime.timeadded",""),
    TIME_REMOVED_FROM_USER("command.playtime.timeremoved","");


    private final String path;
    private final String fallBack;
    @Getter
    private static final Map<String, String> messages = new HashMap<>();
    Messages(String path,String fallBack) {
        this.path = path;
        this.fallBack = fallBack;
    }

    /**
     * Get the message fully tranlated
     * @param replacements
     * @return
     */
    public String getMessage(Replacement... replacements){
        YamlConfiguration file = Playtime.getInstance().getFileManager().getConfig("lang/translations.yml").get();
        if(!messages.containsKey(path)){
            messages.put(path,file.getString(path,fallBack));
        }
        String message = MessageUtil.translateHexColorCodes("<", ">",ChatColor.translateAlternateColorCodes('&',messages.get(path)));
        for(Replacement replacement : replacements){
            message = message.replace(replacement.getFrom(),replacement.getTo());
        }
        return message;
    }

}
