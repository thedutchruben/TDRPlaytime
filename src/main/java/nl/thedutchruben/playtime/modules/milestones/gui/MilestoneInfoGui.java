package nl.thedutchruben.playtime.modules.milestones.gui;

import nl.thedutchruben.mccore.spigot.ui.GUI;
import nl.thedutchruben.mccore.spigot.ui.GUIItem;
import nl.thedutchruben.mccore.utils.item.ItemBuilder;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.core.events.milestone.MilestoneUpdateEvent;
import nl.thedutchruben.playtime.core.objects.Milestone;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MilestoneInfoGui {

    public static final String GUI_TITLE = "Edit Milestone";
    private GUI gui;
    private Milestone milestone;

    public MilestoneInfoGui(Milestone milestone) {
        this.milestone = milestone;
        buildGui();
    }

    private void buildGui() {
        gui = new GUI(6, GUI_TITLE);
        GUIItem border = new GUIItem(new ItemBuilder(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                .displayname(" ")
                .build());
        gui = gui.addBorder(border);

        // Milestone Name
        gui = gui.setItem(10, new ItemBuilder(Material.NAME_TAG)
                .displayname(ChatColor.YELLOW + "Milestone Name")
                .lore(ChatColor.WHITE + "Current: " + milestone.getMilestoneName())
                .lore(" ")
                .lore(ChatColor.GRAY + "Click to edit")
                .build(), (event -> {
            event.getWhoClicked().closeInventory();
            Player player = (Player) event.getWhoClicked();
            
            ChatInputManager.getInstance().requestInput(player, 
                "Enter the new milestone name:", 
                this, 
                (p, input) -> {
                    if (input.isEmpty()) {
                        p.sendMessage(ChatColor.RED + "Milestone name cannot be empty!");
                        this.open(p);
                        return;
                    }
                    
                    // Check if milestone name already exists
                    if (Milestone.getMilestone(input) != null && !input.equals(milestone.getMilestoneName())) {
                        p.sendMessage(ChatColor.RED + "A milestone with that name already exists!");
                        this.open(p);
                        return;
                    }
                    
                    String oldName = milestone.getMilestoneName();
                    milestone.setMilestoneName(input);
                    
                    // Save to storage
                    Playtime.getInstance().getStorage().updateMilestone(milestone);
                    Bukkit.getPluginManager().callEvent(new MilestoneUpdateEvent(milestone));
                    
                    p.sendMessage(ChatColor.GREEN + "Milestone name changed from '" + oldName + "' to '" + input + "'");
                    this.refresh();
                    this.open(p);
                }
            );
        }));

        // Online Time
        gui = gui.setItem(11, new ItemBuilder(Material.CLOCK)
                .displayname(ChatColor.YELLOW + "Required Time")
                .lore(ChatColor.WHITE + "Current: " + milestone.getOnlineTime() + " hours")
                .lore(" ")
                .lore(ChatColor.GRAY + "Click to edit with number selector")
                .build(), (event -> {
            new TimeSelectionGui(milestone, this).open((Player) event.getWhoClicked());
        }));

        // Item Stacks Management
        List<String> itemLore = new ArrayList<>();
        itemLore.add(ChatColor.WHITE + "Current items: " + (milestone.getItemStackObjects() != null ? milestone.getItemStackObjects().size() : 0));
        itemLore.add(" ");
        itemLore.add(ChatColor.GRAY + "Click to manage items");
        gui = gui.setItem(12, new ItemBuilder(Material.CHEST)
                .displayname(ChatColor.YELLOW + "Reward Items")
                .lore(itemLore)
                .build(), (event -> {
            new MilestoneItemsGui(milestone, this).open((Player) event.getWhoClicked());
        }));

        // Commands Management
        List<String> commandLore = new ArrayList<>();
        commandLore.add(ChatColor.WHITE + "Current commands: " + (milestone.getCommands() != null ? milestone.getCommands().size() : 0));
        commandLore.add(" ");
        commandLore.add(ChatColor.GRAY + "Click to manage commands");
        gui = gui.setItem(13, new ItemBuilder(Material.COMMAND_BLOCK)
                .displayname(ChatColor.YELLOW + "Commands")
                .lore(commandLore)
                .build(), (event -> {
            new MilestoneCommandsGui(milestone, this).open((Player) event.getWhoClicked());
        }));

        // Messages Management
        List<String> messageLore = new ArrayList<>();
        messageLore.add(ChatColor.WHITE + "Current messages: " + (milestone.getMessages() != null ? milestone.getMessages().size() : 0));
        messageLore.add(" ");
        messageLore.add(ChatColor.GRAY + "Click to manage messages");
        gui = gui.setItem(14, new ItemBuilder(Material.PAPER)
                .displayname(ChatColor.YELLOW + "Messages")
                .lore(messageLore)
                .build(), (event -> {
            new MilestoneMessagesGui(milestone, this).open((Player) event.getWhoClicked());
        }));

        // Firework Show Toggle
        gui = gui.setItem(19, new ItemBuilder(milestone.isFireworkShow() ? Material.FIREWORK_ROCKET : Material.GUNPOWDER)
                .displayname(ChatColor.YELLOW + "Firework Show")
                .lore(ChatColor.WHITE + "Enabled: " + (milestone.isFireworkShow() ? ChatColor.GREEN + "Yes" : ChatColor.RED + "No"))
                .lore(" ")
                .lore(ChatColor.GRAY + "Click to toggle")
                .build(), (event -> {
            milestone.setFireworkShow(!milestone.isFireworkShow());
            Playtime.getInstance().getStorage().updateMilestone(milestone);
            Bukkit.getPluginManager().callEvent(new MilestoneUpdateEvent(milestone));
            buildGui(); // Rebuild to update display
        }));

        // Firework Amount
        if (milestone.isFireworkShow()) {
            gui = gui.setItem(20, new ItemBuilder(Material.FIREWORK_ROCKET)
                    .displayname(ChatColor.YELLOW + "Firework Amount")
                    .lore(ChatColor.WHITE + "Current: " + milestone.getFireworkShowAmount())
                    .lore(" ")
                    .lore(ChatColor.GRAY + "Left click: +1")
                    .lore(ChatColor.GRAY + "Right click: -1")
                    .lore(ChatColor.GRAY + "Shift+Left: +10")
                    .lore(ChatColor.GRAY + "Shift+Right: -10")
                    .build(), (event -> {
                int change = 0;
                if (event.isLeftClick()) {
                    change = event.isShiftClick() ? 10 : 1;
                } else if (event.isRightClick()) {
                    change = event.isShiftClick() ? -10 : -1;
                }
                int newAmount = Math.max(1, milestone.getFireworkShowAmount() + change);
                milestone.setFireworkShowAmount(newAmount);
                Playtime.getInstance().getStorage().updateMilestone(milestone);
                Bukkit.getPluginManager().callEvent(new MilestoneUpdateEvent(milestone));
                buildGui(); // Rebuild to update display
            }));

            // Firework Delay
            gui = gui.setItem(21, new ItemBuilder(Material.REDSTONE_TORCH)
                    .displayname(ChatColor.YELLOW + "Firework Delay")
                    .lore(ChatColor.WHITE + "Current: " + milestone.getFireworkShowSecondsBetween() + " seconds")
                    .lore(" ")
                    .lore(ChatColor.GRAY + "Left click: +1 second")
                    .lore(ChatColor.GRAY + "Right click: -1 second")
                    .lore(ChatColor.GRAY + "Shift+Left: +5 seconds")
                    .lore(ChatColor.GRAY + "Shift+Right: -5 seconds")
                    .build(), (event -> {
                int change = 0;
                if (event.isLeftClick()) {
                    change = event.isShiftClick() ? 5 : 1;
                } else if (event.isRightClick()) {
                    change = event.isShiftClick() ? -5 : -1;
                }
                int newDelay = Math.max(0, milestone.getFireworkShowSecondsBetween() + change);
                milestone.setFireworkShowSecondsBetween(newDelay);
                Playtime.getInstance().getStorage().updateMilestone(milestone);
                Bukkit.getPluginManager().callEvent(new MilestoneUpdateEvent(milestone));
                buildGui(); // Rebuild to update display
            }));
        }

        // Save and Exit
        gui = gui.setItem(49, new ItemBuilder(Material.EMERALD)
                .displayname(ChatColor.GREEN + "Save & Exit")
                .lore(ChatColor.GRAY + "Click to save changes and close")
                .build(), (event -> {
            // Final save to ensure all changes are persisted
            Playtime.getInstance().getStorage().updateMilestone(milestone);
            Bukkit.getPluginManager().callEvent(new MilestoneUpdateEvent(milestone));
            
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().sendMessage(ChatColor.GREEN + "Milestone '" + milestone.getMilestoneName() + "' saved successfully!");
        }));
    }

    public void open(Player player) {
        gui.open(player);
    }

    public void refresh() {
        buildGui();
    }
}