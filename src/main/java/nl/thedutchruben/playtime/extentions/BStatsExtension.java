package nl.thedutchruben.playtime.extentions;

import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.Settings;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BStatsExtension {

    public void startBStats(JavaPlugin plugin) {
        Metrics metrics = new Metrics(plugin, 9404);

        String[] pluginNames = {"PlaceholderAPI", "HolographicDisplay", "DecentHolograms", "WorldGuard", "JoinAndQuitMessages"};

        for (String pluginName : pluginNames) {
            Plugin pl = Bukkit.getPluginManager().getPlugin(pluginName);
            if (pl != null) {
                metrics.addCustomChart(new SimplePie("addons_use", () -> pluginName));
            }
        }

        try {

            byte[] buffer;
            try (InputStream inputStream = plugin.getClass().getResourceAsStream("/plugin.yml")) {
                buffer = inputStream != null ? new byte[inputStream.available()] : new byte[0];
            }
            String pluginYml = new String(buffer);


            Pattern pattern = Pattern.compile("downloadSource:\\s*'([^']+)'");

            // Create a matcher with the input string
            Matcher matcher = pattern.matcher(pluginYml);

            // Check if a match is found
            if (matcher.find()) {
                // Extract the downloadSource value from the first group
                String downloadSource = matcher.group(1);

                // Print the result
                metrics.addCustomChart(new SimplePie("download_source", () -> downloadSource));
            } else {
                // Print a message if no match is found
                metrics.addCustomChart(new SimplePie("download_source", () -> "unknown"));

            }

        }catch (Exception e){
            plugin.getLogger().warning(e.getMessage());
        }

        metrics.addCustomChart(new SimplePie("bungeecord",
                () -> String.valueOf(plugin.getServer().spigot().getConfig().getBoolean("settings.bungeecord"))));
        metrics.addCustomChart(new SimplePie("database_type", () -> Playtime.getInstance().getStorage().getName()));
        metrics.addCustomChart(new SimplePie("update_checker",
                () -> String.valueOf(Settings.UPDATE_CHECK.getValue())));
        metrics.addCustomChart(new SimplePie("uses_milestones", () -> String.valueOf(Playtime.getInstance().getMilestones().size() > 1)));
        metrics.addCustomChart(
                new SimplePie("uses_repeating_milestones", () -> String.valueOf(Playtime.getInstance().getRepeatingMilestones().size() > 1)));
        metrics.addCustomChart(new SimplePie("count_afk_time",
                () -> String.valueOf(Settings.AFK_COUNT_TIME.getValue())));
        metrics.addCustomChart(new SimplePie("language", () -> (String) Settings.LANGUAGE.getValue()));
//        metrics.addCustomChart(new SingleLineChart("total_play_time",
//                () -> Math.toIntExact(storage.getTotalPlayTime() / 1000 / 60 / 60)));
//        metrics.addCustomChart(
//                new SingleLineChart("total_players", () -> Math.toIntExact(Playtime.getInstance().getStorage().getTotalPlayers())));

    }
}
