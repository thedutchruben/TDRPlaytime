package nl.thedutchruben.playtime.extentions;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.objects.PlaytimeUser;
import nl.thedutchruben.playtime.core.translations.Messages;
import nl.thedutchruben.playtime.utils.Replacement;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class PlaceholderAPIExtension extends PlaceholderExpansion {

    @Override
    public String getIdentifier() {
        return "tdrplaytime";
    }

    @Override
    public String getAuthor() {
        return Playtime.getPlugin().getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return Playtime.getPlugin().getDescription().getVersion();
    }

    @Override
    public List<String> getPlaceholders() {
        List<String> placeholders = new ArrayList<>();
        placeholders.add("%tdrplaytime_time%");
        placeholders.add("%tdrplaytime_time_days_number%");
        placeholders.add("%tdrplaytime_time_hour_number%");
        placeholders.add("%tdrplaytime_time_minutes_number%");
        placeholders.add("%tdrplaytime_time_seconds_number%");

        for (int i = 1; i <= 10; i++) {
            placeholders.add("%tdrplaytime_top_names_" + i + "%");
            placeholders.add("%tdrplaytime_top_time_" + i + "_days%");
            placeholders.add("%tdrplaytime_top_time_" + i + "_hours%");
            placeholders.add("%tdrplaytime_top_time_" + i + "_minutes%");
            placeholders.add("%tdrplaytime_top_time_" + i + "_seconds%");
        }

        return placeholders;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        String dummyProofParams = params.toLowerCase().replace('-', '_');
        PlaytimeUser user = Playtime.getInstance().getPlaytimeUsers().get(player.getUniqueId());

        if (dummyProofParams.equals("time")) {
            return Messages.PLAYTIME_INFO_OWN.getMessage(
                    new Replacement("%D%", String.valueOf(user.translateTime()[0])),
                    new Replacement("%H%", String.valueOf(user.translateTime()[1])),
                    new Replacement("%M%", String.valueOf(user.translateTime()[2])),
                    new Replacement("%S%", String.valueOf(user.translateTime()[3]))
            );
        }

        if (dummyProofParams.contains("days_number")) {
            return String.valueOf(user.translateTime()[0]);
        } else if (dummyProofParams.contains("hour_number")) {
            return String.valueOf(user.translateTime()[1]);
        } else if (dummyProofParams.contains("minutes_number")) {
            return String.valueOf(user.translateTime()[2]);
        } else if (dummyProofParams.contains("seconds_number")) {
            return String.valueOf(user.translateTime()[3]);
        }

        if (dummyProofParams.contains("top_names_")) {
            int placeNumber = parsePlaceNumber(dummyProofParams);
            return getTopUserName(placeNumber);
        }

        if (dummyProofParams.contains("top_time_")) {
            int placeNumber = parsePlaceNumber(dummyProofParams);
            return getTopUserTime(dummyProofParams, placeNumber);
        }

        return super.onPlaceholderRequest(player, params);
    }

    private int parsePlaceNumber(String params) {
        try {
            String[] parts = params.split("_");
            return Math.max(Integer.parseInt(parts[parts.length - 1]), 1);
        } catch (NumberFormatException e) {
            Bukkit.getLogger().log(Level.WARNING, "Wrong number format");
            return 1;
        }
    }

    private String getTopUserName(int placeNumber) {
        try {
            PlaytimeUser playtimeUser = Playtime.getInstance().getStorage().getTopUser(placeNumber - 1).get();
            return playtimeUser != null ? playtimeUser.getName() : "No user found";
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private String getTopUserTime(String params, int placeNumber) {
        try {
            PlaytimeUser playtimeUser = Playtime.getInstance().getStorage().getTopUser(placeNumber - 1).get();
            if (playtimeUser != null) {
                if (Bukkit.getPlayer(playtimeUser.getUUID()) != null) {
                    playtimeUser = Playtime.getInstance().getPlaytimeUser(playtimeUser.getUUID()).get();
                }
                if (params.endsWith("_days")) {
                    return String.valueOf(playtimeUser.translateTime()[0]);
                } else if (params.endsWith("_hours")) {
                    return String.valueOf(playtimeUser.translateTime()[1]);
                } else if (params.endsWith("_minutes")) {
                    return String.valueOf(playtimeUser.translateTime()[2]);
                } else if (params.endsWith("_seconds")) {
                    return String.valueOf(playtimeUser.translateTime()[3]);
                } else {
                    return String.valueOf(playtimeUser.getTime());
                }
            } else {
                return "0";
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}