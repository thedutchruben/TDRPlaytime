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
    /**
     * @return
     */
    @Override
    public String getIdentifier() {
        return "tdrplaytime";
    }

    /**
     * @return
     */
    @Override
    public String getAuthor() {
        return "TheDutchRuben";
    }

    /**
     * @return
     */
    @Override
    public String getVersion() {
        return "1.0";
    }


    @Override
    public List<String> getPlaceholders() {
        List<String> placeholders = new ArrayList<>();

        placeholders.add("%tdrplaytime_time%");
        placeholders.add("%tdrplaytime_time_days_number%");
        placeholders.add("%tdrplaytime_time_hour_number%");
        placeholders.add("%tdrplaytime_time_minutes_number%");
        placeholders.add("%tdrplaytime_time%");
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

        // %tdrplaytime_time%
        if (dummyProofParams.equals("time")) {
            return Messages.PLAYTIME_INFO_OWN.getMessage(
                    new Replacement("%D%", String.valueOf(user.translateTime()[0])),
                    new Replacement("%H%", String.valueOf(user.translateTime()[1])),
                    new Replacement("%M%", String.valueOf(user.translateTime()[2])),
                    new Replacement("%S%", String.valueOf(user.translateTime()[3]))
            );
        }

        if ("days_number".contains(dummyProofParams)) {
            return String.valueOf(user.translateTime()[0]);
        } else if ("hour_number".contains(dummyProofParams)) {
            return String.valueOf(user.translateTime()[1]);
        } else if ("minutes_number".contains(dummyProofParams)) {
            return String.valueOf(user.translateTime()[2]);
        } else if ("seconds_number".contains(dummyProofParams)) {
            return String.valueOf(user.translateTime()[3]);
        }


        // %tdrplaytime_top_names_{1-10}%
        if (dummyProofParams.contains("top_names_")) {
            int placeNumber = 1;
            String place = dummyProofParams.split("_")[dummyProofParams.split("_").length - 1];
            try {
                placeNumber = Integer.parseInt(place);
                if (placeNumber <= 1) {
                    placeNumber = 1;
                }
            } catch (NumberFormatException exception) {
                Bukkit.getLogger().log(Level.WARNING, "Wrong number format");
            }
            try {
                PlaytimeUser playtimeUser = Playtime.getInstance().getStorage().getTopUser(placeNumber - 1).get();
                if (playtimeUser != null) {
                    return playtimeUser.getName();
                } else {
                    return "No user found";
                }

            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        if (dummyProofParams.contains("top_time_")) {
            int placeNumber = 1;
            String[] parts = dummyProofParams.split("_");

            try {
                placeNumber = Math.max(Integer.parseInt(parts[parts.length - 2]), 1);
            } catch (NumberFormatException exception) {
                placeNumber = Math.max(Integer.parseInt(parts[parts.length - 1]), 1);
            }

            try {
                PlaytimeUser playtimeUser = Playtime.getInstance().getStorage().getTopUser(placeNumber - 1).get();
                if (playtimeUser != null) {
                    if (Bukkit.getPlayer(playtimeUser.getUUID()) != null) {
                        playtimeUser = Playtime.getInstance().getPlaytimeUser(playtimeUser.getUUID()).get();
                    }
                    if (dummyProofParams.endsWith("_days")) {
                        return String.valueOf(playtimeUser.translateTime()[0]);
                    } else if (dummyProofParams.endsWith("_hours")) {
                        return String.valueOf(playtimeUser.translateTime()[1]);
                    } else if (dummyProofParams.endsWith("_minutes")) {
                        return String.valueOf(playtimeUser.translateTime()[2]);
                    } else if (dummyProofParams.endsWith("_seconds")) {
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

        return super.onPlaceholderRequest(player, params);
    }

}
