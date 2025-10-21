package nl.thedutchruben.playtime.core.objects;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import nl.thedutchruben.mccore.utils.firework.FireworkUtil;
import nl.thedutchruben.mccore.utils.message.MessageUtil;
import nl.thedutchruben.playtime.Playtime;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Ruben
 * @version 1.0
 */
public class RepeatingMilestone {

    /**
     * The list of items to give the player.
     */
    private transient List<ItemStack> _itemStackObjects;

    /**
     * The name of the milestone.
     */
    @Setter
    @SerializedName("_id")
    private String milestoneName;
    @Setter
    @Getter
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
    @Setter
    @SerializedName("firework_show_seconds_between_firework")
    private int fireworkShowSecondsBetween = 0;
    @Getter
    @SerializedName("normal_milestone_override_me")
    private boolean overrideMe = false;
    @SerializedName("messages")
    private List<String> messages;

    /**
     * Conditions that must be met for the reward to be granted
     */
    @Setter
    @Getter
    @SerializedName("conditions")
    private RewardCondition conditions;

    /**
     * Cooldown period in milliseconds before the reward can be claimed again
     * For repeating milestones, this is in addition to the repeat interval
     */
    @Setter
    @Getter
    @SerializedName("cooldown_millis")
    private long cooldownMillis = 0;

    /**
     * Permission required to receive this reward
     */
    @Setter
    @Getter
    @SerializedName("required_permission")
    private String requiredPermission;

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

        // Check cooldown
        if (cooldownMillis > 0 && Playtime.getInstance().getRewardCooldownManager() != null) {
            if (!Playtime.getInstance().getRewardCooldownManager().canClaim(player.getUniqueId(), milestoneName)) {
                // Player is on cooldown
                return;
            }
        }
        if (itemStacks != null) {
            if (_itemStackObjects == null) {
                _itemStackObjects = new ArrayList<>();
                for (Map<String, Object> itemStack : itemStacks) {
                    _itemStackObjects.add(ItemStack.deserialize(itemStack));
                }
            }

            for (ItemStack itemStack : _itemStackObjects) {
                player.getInventory().addItem(itemStack);
            }
        }

        if (commands != null) {
            Bukkit.getScheduler().runTask(Playtime.getPlugin(), () -> {
                for (String command : commands) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            command.replaceAll("%playername%", player.getName())
                                    .replaceAll("%player_name%", player.getName())
                                    .replaceAll("%playeruuid%", player.getUniqueId().toString())
                                    .replaceAll("%player_uuid%", player.getUniqueId().toString()));
                }
            });
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
                    Bukkit.getScheduler().runTask(Playtime.getPlugin(), () -> {
                        FireworkUtil.spawnRandomFirework(player.getLocation());
                    });
                    try {
                        Thread.sleep(fireworkShowSecondsBetween * 1000L);
                    } catch (InterruptedException e) {
                        Playtime.getPlugin().getLogger().warning("Error while sleeping the thread :" + e.getMessage());
                    }
                }
            });

        }

        // Set cooldown if configured
        if (cooldownMillis > 0 && Playtime.getInstance().getRewardCooldownManager() != null) {
            Playtime.getInstance().getRewardCooldownManager().setCooldown(
                    player.getUniqueId(),
                    milestoneName,
                    "REPEATING_MILESTONE",
                    cooldownMillis
            );
        }
    }

    public List<Map<String, Object>> getItemStacks() {
        if (itemStacks == null) {
            itemStacks = new ArrayList<>();
        }
        return itemStacks;
    }

    public void setItemStacks(List<Map<String, Object>> itemStacks) {
        this.itemStacks = itemStacks;
    }

    public List<String> getCommands() {
        if (commands == null) {
            commands = new ArrayList<>();
        }
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public String getMilestoneName() {
        return milestoneName;
    }

    public List<String> getMessages() {
        if (messages == null)
            messages = new ArrayList<>();

        return messages;
    }

    public long getOnlineTimeInMilliseconds(){
        return onlineTime * 1000;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public void setItemStackObjects(List<ItemStack> itemStackObjects) {
        this._itemStackObjects = itemStackObjects;
    }

    public boolean isFireworkShow() {
        return fireworkShow;
    }

    public void setFireworkShow(boolean fireworkShow) {
        this.fireworkShow = fireworkShow;
    }

    public int getFireworkShowAmount() {
        return fireworkShowAmount;
    }

    public void setFireworkShowAmount(int fireworkShowAmount) {
        this.fireworkShowAmount = fireworkShowAmount;
    }

    public int getFireworkShowSecondsBetween() {
        return fireworkShowSecondsBetween;
    }

    public void setOverrideMe(boolean overrideMe) {
        this.overrideMe = overrideMe;
    }

    /**
     * Add an ItemStack to the milestone
     * @param itemStack The itemStack to add
     */
    public void addItemStack(ItemStack itemStack) {
        getItemStacks().add(itemStack.serialize());
    }

    /**
     * Add a command to the milestone
     * @param command The command to add
     */
    public void addCommand(String command) {
        getCommands().add(command);
    }

    /**
     * Remove a command from the milestone
     * @param command The command to remove
     */
    public void removeCommand(String command) {
        getCommands().remove(command);
    }

    /**
     * Add a message to the milestone
     *
     * @param message The message to add
     */
    public void addMessage(String message) {
        getMessages().add(message);
    }

    /**
     * Remove a message from the milestone
     *
     * @param message The message to remove
     */
    public void removeMessage(String message) {
        getMessages().remove(message);
    }

    /**
     * Get a milestone by name
     *
     * @param name The name of the milestone
     * @see RepeatingMilestone
     * @return The milestone
     */
    public static RepeatingMilestone get(String name) {
        return Playtime.getInstance()
                .getRepeatingMilestones().stream()
                .filter(milestone -> milestone.getMilestoneName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }
}