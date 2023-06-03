package nl.thedutchruben.playtime.extentions;

import lombok.SneakyThrows;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import nl.thedutchruben.mccore.Mccore;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.utils.TopPlaceCache;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.logging.Level;

public class PlaceholderAPIExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "tdrplaytime";
    }

    @Override
    public @NotNull String getAuthor() {
        return "TheDutchRuben";
    }


    @Override
    public @NotNull String getVersion() {
        return Playtime.getInstance().getDescription().getVersion();
    }

    @SneakyThrows
    @Override
    public String onPlaceholderRequest(Player p, String params) {
        Playtime.getInstance().update(p.getUniqueId(), false);
        long time = Playtime.getInstance().getPlayerOnlineTime().get(p.getUniqueId());
        time = time / 1000;
        int days = (int) (time / 86400);
        time = time - days * 86400L;
        int hours = (int) (time / 3600);
        time = time - hours * 3600L;
        int minutes = (int) (time / 60);
        time = time - minutes * 60L;
        int seconds = (int) time;

        // %tdrplaytime_time%
        if (params.equals("time")) {
            return translateMessage(Playtime.getInstance().getMessage("command.playtime.timemessage"),
                    (Playtime.getInstance().getPlayerOnlineTime().get(p.getUniqueId())));
        }

        // %tdrplaytime_days_number%
        if (params.equals("days_number")) {
            return String.valueOf(days);
        }

        // %tdrplaytime_hour_number%
        if (params.equals("hour_number")) {
            return String.valueOf(hours);
        }

        // %tdrplaytime_total_hour_number%
        if (params.equals("total_hour_number")) {
            return String.valueOf(hours + days * 24);
        }

        // %tdrplaytime_minutes_number%
        if (params.equals("minutes_number")) {
            return String.valueOf(minutes);
        }

        // %tdrplaytime_minutes_number%
        if (params.equals("seconds_number")) {
            return String.valueOf(seconds);
        }

        // %tdrplaytime_top_names_{1-10}%
        if (params.contains("top_names_")) {
            int placeNumber = 1;
            String place = params.split("_")[params.split("_").length - 1];
            try {
                placeNumber = Integer.parseInt(place);
                if (placeNumber <= 1) {
                    placeNumber = 1;
                }
            } catch (NumberFormatException exception) {
                Bukkit.getLogger().log(Level.WARNING, "Wrong number format");
            }
            return Playtime.getInstance().getStorage().getTopPlace(placeNumber - 1);
        }

        // %tdrplaytime_top_time_{1-10}_days%
        if (params.contains("top_time_") && params.endsWith("_days")) {
            int placeNumber = 1;
            String place = params.split("_")[params.split("_").length - 2];
            try {
                placeNumber = Integer.parseInt(place);
                if (placeNumber <= 1) {
                    placeNumber = 1;
                }

            } catch (NumberFormatException exception) {
                Bukkit.getLogger().log(Level.WARNING, "Wrong number format");
            }

            return String.valueOf(
                    getTime(Long.parseLong(Playtime.getInstance().getStorage().getTopPlaceTime(placeNumber - 1)))[0]);
        }

        // %tdrplaytime_top_time_{1-10}_hours%
        if (params.contains("top_time_") && params.endsWith("_hours")) {
            int placeNumber = 1;
            String place = params.split("_")[params.split("_").length - 2];
            try {
                placeNumber = Integer.parseInt(place);
                if (placeNumber <= 1) {
                    placeNumber = 1;
                }

            } catch (NumberFormatException exception) {
                Bukkit.getLogger().log(Level.WARNING, "Wrong number format");
            }

            return String.valueOf(
                    getTime(Long.parseLong(Playtime.getInstance().getStorage().getTopPlaceTime(placeNumber - 1)))[1]);
        }

        // %tdrplaytime_top_time_{1-10}_minutes%
        if (params.contains("top_time_") && params.endsWith("_minutes")) {
            int placeNumber = 1;
            String place = params.split("_")[params.split("_").length - 2];
            try {
                placeNumber = Integer.parseInt(place);
                if (placeNumber <= 1) {
                    placeNumber = 1;
                }

            } catch (NumberFormatException exception) {
                Bukkit.getLogger().log(Level.WARNING, "Wrong number format");
            }

            return String.valueOf(
                    getTime(Long.parseLong(Playtime.getInstance().getStorage().getTopPlaceTime(placeNumber - 1)))[2]);
        }

        // %tdrplaytime_top_time_{1-10}_seconds%
        if (params.contains("top_time_") && params.endsWith("_seconds")) {
            int placeNumber = 1;
            String place = params.split("_")[params.split("_").length - 2];
            try {
                placeNumber = Integer.parseInt(place);
                if (placeNumber <= 1) {
                    placeNumber = 1;
                }

            } catch (NumberFormatException exception) {
                Bukkit.getLogger().log(Level.WARNING, "Wrong number format");
            }

            return String.valueOf(
                    getTime(Long.parseLong(Playtime.getInstance().getStorage().getTopPlaceTime(placeNumber - 1)))[3]);
        }

        // %tdrplaytime_top_time_{1-10}%
        if (params.contains("top_time_")) {
            int placeNumber = 1;
            String place = params.split("_")[params.split("_").length - 1];
            try {
                placeNumber = Integer.parseInt(place);
                if (placeNumber <= 1) {
                    placeNumber = 1;
                }

            } catch (NumberFormatException exception) {
                Bukkit.getLogger().log(Level.WARNING, "Wrong number format");
            }
            return Playtime.getInstance().getStorage().getTopPlaceTime(placeNumber - 1);
        }

        return null;
    }

    public int[] getTime(long time) {
        time = time / 1000;
        int days = (int) (time / 86400);
        time = time - days * 86400L;
        int hours = (int) (time / 3600);
        time = time - hours * 3600L;
        int minutes = (int) (time / 60);
        time = time - minutes * 60L;
        int seconds = (int) time;
        return new int[]{days, hours, minutes, seconds};
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
