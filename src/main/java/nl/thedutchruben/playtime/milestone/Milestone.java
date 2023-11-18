package nl.thedutchruben.playtime.milestone;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import nl.thedutchruben.mccore.utils.message.MessageUtil;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.mccore.utils.firework.FireworkUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Milestone {
    private transient List<ItemStack> itemStackObjects;

    /**
     * The name of the milestone.
     * -- GETTER --
     *  get the name of the milestone

     */
    @Getter
    @SerializedName("_id")
    private String milestoneName;
    /**
     * The time the player has to be online to get the milestone
     * -- GETTER --
     *  get the time the player has to be online to get the milestone

     */
    @Getter
    @SerializedName("online_time")
    private long onlineTime;
    /**
     * The list of items to give the player.
     */
    @SerializedName("item_stacks")
    private List<Map<String, Object>> itemStacks;
    /**
     * The list of commands to execute
     */
    @SerializedName("commands")
    private List<String> commands;
    /**
     * The list of messages to send
     */
    @SerializedName("messages")
    private List<String> messages;
    /**
     * if there shall be a firework show
     * -- GETTER --
     *  get if there is a firework show

     */
    @Getter
    @SerializedName("firework_show")
    private boolean fireworkShow = false;
    /**
     * The amount of fireworks to spawn
     * -- GETTER --
     *  get the amount of fireworks to spawn

     */
    @Getter
    @SerializedName("firework_show_amount")
    private int fireworkShowAmount = 1;
    /**
     * The seconds between the fireworks
     */
    @SerializedName("firework_show_seconds_between_firework")
    private int fireworkShowSecondsBetween = 0;

    /**
     * Apply the milestone on the player
     *
     * @param player The player to apply the milestone to
     */
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
            Bukkit.getScheduler().runTask(Playtime.getPluginInstance(), () -> {
                for (String command : commands) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            command.replaceAll("%playername%", player.getName())
                                    .replaceAll("%player_name%", player.getName())
                                    .replaceAll("%playeruuid%", player.getUniqueId().toString())
                                    .replaceAll("%player_uuid%", player.getUniqueId().toString()));
                }
            });
        }

        if (messages != null){
            messages.forEach(s -> {
                String formattedString = MessageUtil.translateHexColorCodes("<",">", ChatColor.translateAlternateColorCodes('&',s));
                player.sendMessage(formattedString);
            });
        }

        if (fireworkShow) {
            Bukkit.getScheduler().runTaskAsynchronously(Playtime.getPluginInstance(), () -> {
                for (int i = 0; i < fireworkShowAmount; i++) {
                    Bukkit.getScheduler().runTask(Playtime.getPluginInstance(), () -> {
                        FireworkUtil.spawnRandomFirework(player.getLocation());
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

    /**
     * @return The list of items to give the player.
     */
    public List<Map<String, Object>> getItemStacks() {
        if (itemStacks == null) {
            itemStacks = new ArrayList<>();
        }
        return itemStacks;
    }

    /**
     * @return The list of commands to execute
     */
    public List<String> getCommands() {
        if (commands == null) {
            commands = new ArrayList<>();
        }
        return commands;
    }

    /**
     * set the name of the milestone
     */
    public void setMilestoneName(String milestoneName) {
        this.milestoneName = milestoneName;
    }

    /**
     * set the time the player has to be online to get the milestone
     */
    public void setOnlineTime(long onlineTime) {
        this.onlineTime = onlineTime;
    }

    /**
     * set if there is a firework show
     */
    public void setFireworkShow(boolean fireworkShow) {
        this.fireworkShow = fireworkShow;
    }

    /**
     * set the amount of fireworks to spawn
     */
    public void setFireworkShowAmount(int fireworkShowAmount) {
        this.fireworkShowAmount = fireworkShowAmount;
    }

    /**
     * set the seconds between the fireworks
     */
    public void setFireworkShowSecondsBetween(int fireworkShowSecondsBetween) {
        this.fireworkShowSecondsBetween = fireworkShowSecondsBetween;
    }

    /**
     * get the list of messages to send
     */
    public List<String> getMessages() {
        if(messages == null)
            messages = new ArrayList<>();

        return messages;
    }

    /**
     * set the list of messages to send
     */
    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
}
