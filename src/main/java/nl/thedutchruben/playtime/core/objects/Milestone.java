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
     * The name of the milestone
     * -- GETTER --
     * get the name of the milestone
     * -- SETTER --
     * set the name of the milestone
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
     * set the time the player has to be online to get the milestone
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
     * set the list of messages to send
     */
    @Setter
    @SerializedName("messages")
    private List<String> messages;

    /**
     * if there shall be a firework show
     * -- GETTER --
     * get if there is a firework show
     * -- SETTER --
     * set if there is a firework show
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
     * set the amount of fireworks to spawn
     */
    @Setter
    @Getter
    @SerializedName("firework_show_amount")
    private int fireworkShowAmount = 1;

    /**
     * The seconds between the fireworks
     * -- SETTER --
     * set the seconds between the fireworks
     */
    @Setter
    @Getter
    @SerializedName("firework_show_seconds_between_firework")
    private int fireworkShowSecondsBetween = 0;

    /**
     * Conditions that must be met for the reward to be granted
     * -- GETTER --
     * get the reward conditions
     * -- SETTER --
     * set the reward conditions
     */
    @Setter
    @Getter
    @SerializedName("conditions")
    private RewardCondition conditions;


    /**
     * Permission required to receive this reward
     * -- GETTER --
     * get the required permission
     * -- SETTER --
     * set the required permission
     */
    @Setter
    @Getter
    @SerializedName("required_permission")
    private String requiredPermission;

    public static Milestone getMilestone(String name) {
        return Playtime.getInstance()
                .getMilestones().stream()
                .filter(milestone -> milestone.getMilestoneName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }

    /**
     * Apply the milestone on the player
     *
     * @param player The player to apply the milestone to
     */
    public void apply(Player player) {
        // Check permission requirement
        if (requiredPermission != null && !requiredPermission.isEmpty()) {
            if (!player.hasPermission(requiredPermission)) {
                return; // Player doesn't have required permission
            }
        }

        // Get player's playtime data
        PlaytimeUser user = Playtime.getInstance().getPlaytimeUsers().get(player.getUniqueId());
        if (user == null) {
            return;
        }

        // Check conditions
        if (conditions != null && conditions.hasConditions()) {
            if (!conditions.check(player, user)) {
                return; // Conditions not met
            }
        }

        Bukkit.getScheduler().runTask(Playtime.getPlugin(), () ->
                Bukkit.getPluginManager().callEvent(
                        new MilestoneReceiveEvent(this, user))
        );

        if (itemStacks != null && _itemStackObjects == null) {
            _itemStackObjects = new ArrayList<>();
            for (Map<String, Object> itemStack : itemStacks) {
                _itemStackObjects.add(ItemStack.deserialize(itemStack));
            }
        }

        if (_itemStackObjects != null && !_itemStackObjects.isEmpty()) {
            Bukkit.getScheduler().runTask(Playtime.getPlugin(), () ->
                    player.getInventory().addItem(_itemStackObjects.toArray(new ItemStack[0]))
            );
        }

        if (commands != null) {
            Bukkit.getScheduler().runTask(Playtime.getPlugin(), () ->
                    commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            command.replaceAll("%playername%", player.getName())
                                    .replaceAll("%player_name%", player.getName())
                                    .replaceAll("%playeruuid%", player.getUniqueId().toString())
                                    .replaceAll("%player_uuid%", player.getUniqueId().toString()))
                    )
            );
        }

        if (messages != null) {
            messages.forEach(s ->
                    player.sendMessage(MessageUtil.translateHexColorCodes("<", ">", ChatColor.translateAlternateColorCodes('&', s)))
            );
        }

        if (fireworkShow) {
            Bukkit.getScheduler().runTaskAsynchronously(Playtime.getPlugin(), () -> {
                for (int i = 0; i < fireworkShowAmount; i++) {
                    Bukkit.getScheduler().runTask(Playtime.getPlugin(), () ->
                            FireworkUtil.spawnRandomFirework(player.getLocation())
                    );
                    try {
                        Thread.sleep(fireworkShowSecondsBetween * 1000L);
                    } catch (InterruptedException e) {
                        Playtime.getPlugin().getLogger().warning("Error while sleeping the thread: " + e.getMessage());
                    }
                }
            });
        }
    }

    /**
     * Adds an ItemStack to the milestone.
     *
     * @param itemStack The ItemStack to add.
     */
    public void addItemStack(ItemStack itemStack) {
        if (itemStacks == null) {
            itemStacks = new ArrayList<>();
        }
        itemStacks.add(itemStack.serialize());
        _itemStackObjects = null;
    }

    /**
     * Adds a command to the milestone.
     *
     * @param command The command to add.
     */
    public void addCommand(String command) {
        if (commands == null) {
            commands = new ArrayList<>();
        }
        commands.add(command);
    }

    /**
     * Adds a message to the milestone.
     *
     * @param message The message to add.
     */
    public void addMessage(String message) {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        messages.add(message);
    }

    /**
     * Removes an ItemStack from the milestone.
     *
     * @param itemStack The ItemStack to remove.
     */
    public void removeItemStack(ItemStack itemStack) {
        if (itemStacks == null) {
            return;
        }
        itemStacks.remove(itemStack.serialize());
        _itemStackObjects = null;
    }

    public long getOnlineTimeInMilliseconds(){
        return onlineTime * 1000;
    }

    /**
     * Removes a command from the milestone.
     *
     * @param command The command to remove.
     */
    public void removeCommand(String command) {
        if (commands == null) {
            return;
        }
        commands.remove(command);
    }

    /**
     * Removes a message from the milestone.
     *
     * @param message The message to remove.
     */
    public void removeMessage(String message) {
        if (messages == null) {
            return;
        }
        messages.remove(message);
    }

    /**
     * Gets the item stacks as ItemStack objects
     */
    public List<ItemStack> getItemStackObjects() {
        if (itemStacks != null && _itemStackObjects == null) {
            _itemStackObjects = new ArrayList<>();
            for (Map<String, Object> itemStack : itemStacks) {
                _itemStackObjects.add(ItemStack.deserialize(itemStack));
            }
        }
        return _itemStackObjects;
    }

    /**
     * Gets the raw item stacks map
     */
    public List<Map<String, Object>> getItemStacks() {
        return itemStacks;
    }

    /**
     * Sets the item stacks
     */
    public void setItemStacks(List<Map<String, Object>> itemStacks) {
        this.itemStacks = itemStacks;
        this._itemStackObjects = null;
    }

    /**
     * Gets the commands list
     */
    public List<String> getCommands() {
        return commands;
    }

    /**
     * Sets the commands list
     */
    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    /**
     * Gets the messages list
     */
    public List<String> getMessages() {
        return messages;
    }

    /**
     * Removes an ItemStack at the specified index
     */
    public void removeItemStack(int index) {
        if (itemStacks != null && index >= 0 && index < itemStacks.size()) {
            itemStacks.remove(index);
            _itemStackObjects = null;
        }
    }

    /**
     * Removes a command at the specified index
     */
    public void removeCommand(int index) {
        if (commands != null && index >= 0 && index < commands.size()) {
            commands.remove(index);
        }
    }

    /**
     * Removes a message at the specified index
     */
    public void removeMessage(int index) {
        if (messages != null && index >= 0 && index < messages.size()) {
            messages.remove(index);
        }
    }
}