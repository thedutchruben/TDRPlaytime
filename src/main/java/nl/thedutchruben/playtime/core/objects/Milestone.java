package nl.thedutchruben.playtime.core.objects;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import nl.thedutchruben.mccore.utils.firework.FireworkUtil;
import nl.thedutchruben.mccore.utils.message.MessageUtil;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.events.milestone.MilestoneReceiveEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Milestone {
    private transient List<ItemStack> _itemStackObjects;

    /**
     * The name of the milestone.
     * -- GETTER --
     * get the name of the milestone
     * -- SETTER --
     *  set the name of the milestone

     */
    @Setter
    @Getter
    @SerializedName("_id")
    private String milestoneName;
    /**
     * The time the player has to be online to get the milestone
     * -- GETTER --
     * get the time the player has to be online to get the milestone
     * -- SETTER --
     *  set the time the player has to be online to get the milestone

     */
    @Setter
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
     * -- SETTER --
     *  set the list of messages to send

     */
    @Setter
    @SerializedName("messages")
    private List<String> messages;
    /**
     * if there shall be a firework show
     * -- GETTER --
     * get if there is a firework show
     * -- SETTER --
     *  set if there is a firework show

     */
    @Setter
    @Getter
    @SerializedName("firework_show")
    private boolean fireworkShow = false;
    /**
     * The amount of fireworks to spawn
     * -- GETTER --
     * get the amount of fireworks to spawn
     * -- SETTER --
     *  set the amount of fireworks to spawn

     */
    @Setter
    @Getter
    @SerializedName("firework_show_amount")
    private int fireworkShowAmount = 1;
    /**
     * The seconds between the fireworks
     * -- SETTER --
     *  set the seconds between the fireworks

     */
    @Setter
    @SerializedName("firework_show_seconds_between_firework")
    private int fireworkShowSecondsBetween = 0;

    /**
     * Apply the milestone on the player
     *
     * @param player The player to apply the milestone to
     */
    public void apply(Player player) {
        Bukkit.getScheduler().runTask(Playtime.getPlugin(),() -> Bukkit.getPluginManager().callEvent(new MilestoneReceiveEvent(this,Playtime.getInstance().getPlaytimeUser(player.getUniqueId()).get())));
        if (itemStacks != null) {
            if (_itemStackObjects == null) {
                _itemStackObjects = new ArrayList<>();
                for (Map<String, Object> itemStack : itemStacks) {
                    _itemStackObjects.add(ItemStack.deserialize(itemStack));
                }
            }
            if(!_itemStackObjects.isEmpty()){
                Bukkit.getScheduler().runTask(Playtime.getPlugin(),() -> player.getInventory().addItem(_itemStackObjects.toArray(ItemStack[]::new)));
            }
        }

        if (commands != null) {
            Bukkit.getScheduler().runTask(Playtime.getPlugin(), () -> commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    command.replaceAll("%playername%", player.getName())
                            .replaceAll("%player_name%", player.getName())
                            .replaceAll("%playeruuid%", player.getUniqueId().toString())
                            .replaceAll("%player_uuid%", player.getUniqueId().toString()))));
        }

        if (messages != null) {
            messages.forEach(s -> {
                String formattedString = MessageUtil.translateHexColorCodes("<", ">", ChatColor.translateAlternateColorCodes('&', s));
                player.sendMessage(formattedString);
            });
        }

        if (fireworkShow) {
            Bukkit.getScheduler().runTaskAsynchronously(Playtime.getPlugin(), () -> {
                for (int i = 0; i < fireworkShowAmount; i++) {
                    Bukkit.getScheduler().runTask(Playtime.getPlugin(), () -> FireworkUtil.spawnRandomFirework(player.getLocation()));
                    try {
                        Thread.sleep(fireworkShowSecondsBetween * 1000L);
                    } catch (InterruptedException e) {
                        Playtime.getPlugin().getLogger().warning("Error while sleeping the thread :" + e.getMessage());
                    }
                }
            });

        }

    }

    public void addItemStack(ItemStack itemStack) {
        if (itemStacks == null) {
            itemStacks = new ArrayList<>();
        }
        itemStacks.add(itemStack.serialize());
        _itemStackObjects = null;
    }

    public void addCommand(String command) {
        if (commands == null) {
            commands = new ArrayList<>();
        }
        commands.add(command);
    }

    public void addMessage(String message) {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        messages.add(message);
    }

    public void removeItemStack(ItemStack itemStack) {
        if (itemStacks == null) {
            return;
        }
        itemStacks.remove(itemStack.serialize());
        _itemStackObjects = null;
    }

    public void removeCommand(String command) {
        if (commands == null) {
            return;
        }
        commands.remove(command);
    }

    public void removeMessage(String message) {
        if (messages == null) {
            return;
        }
        messages.remove(message);
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
     * get the list of messages to send
     */
    public List<String> getMessages() {
        if (messages == null)
            messages = new ArrayList<>();

        return messages;
    }

    public static Milestone getMilestone(String name){
        return Playtime.getInstance()
                .getMilestones().stream()
                .filter(milestone -> milestone.getMilestoneName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }
}
