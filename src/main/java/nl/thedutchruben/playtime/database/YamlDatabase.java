package nl.thedutchruben.playtime.database;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.milestone.Milestone;
import nl.thedutchruben.playtime.milestone.RepeatingMilestone;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class YamlDatabase extends Storage{
    private Gson gson;

    @Override
    public String getName() {
        return "yaml";
    }

    @Override
    public void setup() {
        this.gson = new GsonBuilder()
                .disableHtmlEscaping().setPrettyPrinting().create();
    }

    @Override
    public void stop() {
        this.gson = null;
    }

    @Override
    public CompletableFuture<Long> getPlayTimeByUUID(String uuid) {
        return CompletableFuture.supplyAsync(() -> Playtime.getInstance().getFileManager().getConfig("players/" + uuid + ".yaml").get().getLong("onlinetime",0));
    }

    @Override
    public CompletableFuture<Long> getPlayTimeByName(String name) {
        return CompletableFuture.supplyAsync(() -> Playtime.getInstance().getFileManager().getConfig("players/" + Bukkit.getOfflinePlayer(name).getUniqueId().toString() + ".yaml").get().getLong("onlinetime",0));
    }

    @Override
    public CompletableFuture savePlayTime(String uuid, long playtime) {
        return CompletableFuture.supplyAsync(() -> {
            Playtime.getInstance().getFileManager().getConfig("players/" + uuid + ".yaml").get().set("onlinetime",playtime);
            Playtime.getInstance().getFileManager().getConfig("players/" + uuid + ".yaml").save();
            return this;
        });

    }

    @Override
    public CompletableFuture<Map<String, Long>> getTopTenList() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String,Long> hashMap = new HashMap<>();
            for (final File fileEntry : Objects.requireNonNull(new File(Playtime.getInstance().getDataFolder(), "players/").listFiles())) {
                YamlConfiguration config = Playtime.getInstance().getFileManager().getConfig("players/" + fileEntry.getName().replace(".yaml","") + ".yaml").get();
                if(config != null){
                    if(config.contains("onlinetime")){
                        hashMap.put(Bukkit.getPlayer
                                        (UUID.fromString(fileEntry.getName().replace(".yaml",""))).getName(),
                                config.getLong("onlinetime"));
                    }
                }
            }
            return hashMap;
        });
    }

    @Override
    public long getTotalPlayTime() {
        return 0;
    }

    @Override
    public CompletableFuture<Void> createMilestone(Milestone milestone) {
        return saveMileStone(milestone);
    }

    @Override
    public CompletableFuture<Void> saveMileStone(Milestone milestone) {
        return CompletableFuture.supplyAsync(() -> {
            Playtime.getInstance().getFileManager().getConfig("milestones/" + milestone.getMilestoneName() + ".yaml").get().set("data",this.gson.toJson(milestone,Milestone.class));
            Playtime.getInstance().getFileManager().getConfig("milestones/" + milestone.getMilestoneName() + ".yaml").save();
            return null;
        });

    }

    @Override
    public CompletableFuture<List<Milestone>> getMilestones() {
        return CompletableFuture.supplyAsync(() -> {
            List<Milestone> milestones = new ArrayList<>();
            for (final File fileEntry : Objects.requireNonNull(new File(Playtime.getInstance().getDataFolder(), "milestones/").listFiles())) {
                YamlConfiguration config = Playtime.getInstance().getFileManager().getConfig("milestones/" + fileEntry.getName()).get();
                if(config != null){
                    milestones.add(this.gson.fromJson(config.getString("data"),Milestone.class));
                }
            }
            return milestones;
        });
    }

    @Override
    public CompletableFuture<Void> createRepeatingMilestone(RepeatingMilestone milestone) {
        return saveRepeatingMileStone(milestone);
    }

    @Override
    public CompletableFuture<Void> saveRepeatingMileStone(RepeatingMilestone milestone) {
        return CompletableFuture.supplyAsync(() -> {
            Playtime.getInstance().getFileManager().getConfig("repeatingmilestones/" + milestone.getMilestoneName() + ".yaml").get().set("data",this.gson.toJson(milestone,Milestone.class));
            Playtime.getInstance().getFileManager().getConfig("repeatingmilestones/" + milestone.getMilestoneName() + ".yaml").save();
            return null;
        });
    }

    @Override
    public CompletableFuture<List<RepeatingMilestone>> getRepeatingMilestones() {
        return CompletableFuture.supplyAsync(() -> {
            List<RepeatingMilestone> milestones = new ArrayList<>();
            for (final File fileEntry : Objects.requireNonNull(new File(Playtime.getInstance().getDataFolder(), "repeatingmilestones/").listFiles())) {
                YamlConfiguration config = Playtime.getInstance().getFileManager().getConfig("repeatingmilestones/" + fileEntry.getName()).get();
                if(config != null){
                    milestones.add(this.gson.fromJson(config.getString("data"),RepeatingMilestone.class));
                }
            }
            return milestones;
        });
    }

    @Override
    public CompletableFuture<Void> reset(String name) {
        return CompletableFuture.supplyAsync(() -> {
            String uuid = Bukkit.getPlayer(name).getUniqueId().toString();
            Playtime.getInstance().getFileManager().getConfig("players/" + uuid + ".yaml").get().set("onlinetime",0);
            Playtime.getInstance().getFileManager().getConfig("players/" + uuid + ".yaml").save();
            return null;
        });
    }
}
