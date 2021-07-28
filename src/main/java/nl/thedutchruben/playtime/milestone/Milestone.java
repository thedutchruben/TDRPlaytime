package nl.thedutchruben.playtime.milestone;

import com.google.gson.annotations.SerializedName;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.utils.FireworkUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Milestone {
    private transient List<ItemStack> itemStackObjects;

    @SerializedName("_id")
    private String milestoneName;
    @SerializedName("online_time")
    private long onlineTime;
    @SerializedName("item_stacks")
    private List<Map<String, Object>> itemStacks;
    @SerializedName("commands")
    private List<String> commands;
    @SerializedName("firework_show")
    private boolean fireworkShow = false;
    @SerializedName("firework_show_amount")
    private int fireworkShowAmount = 1;
    @SerializedName("firework_show_seconds_between_firework")
    private int fireworkShowSecondsBetween = 0;


    public void apply(Player player) {
        if (itemStacks != null) {
            if (itemStackObjects == null) {
                itemStackObjects = new ArrayList<>();
                for (Map<String, Object> itemStack : itemStacks) {
                    itemStackObjects.add(ItemStack.deserialize(itemStack));
                }
            }
            for (ItemStack itemStack : itemStackObjects) {
                player.getInventory().addItem(itemStack);
            }
        }

        if (commands != null) {
            for (String command : commands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%playername%", player.getName()).replace("%playeruuid%", player.getUniqueId().toString()));
            }
        }

        if (fireworkShow) {
            Bukkit.getScheduler().runTaskAsynchronously(Playtime.getInstance(), () -> {
                for (int i = 0; i < fireworkShowAmount; i++) {
                    Bukkit.getScheduler().runTask(Playtime.getInstance(), () -> {
                        FireworkUtil.spawn(player.getLocation());
                    });
                    try {
                        Thread.sleep(fireworkShowSecondsBetween * 1000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

        }

    }

    public List<Map<String, Object>> getItemStacks() {
        if (itemStacks == null) {
            itemStacks = new ArrayList<>();
        }
        return itemStacks;
    }

    public List<String> getCommands() {
        if (commands == null) {
            commands = new ArrayList<>();
        }
        return commands;
    }

    public String getMilestoneName() {
        return milestoneName;
    }

    public void setMilestoneName(String milestoneName) {
        this.milestoneName = milestoneName;
    }

    public long getOnlineTime() {
        return onlineTime;
    }

    public void setOnlineTime(long onlineTime) {
        this.onlineTime = onlineTime;
    }
}
