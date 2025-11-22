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
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class TimeSelectionGui {

    public static final String GUI_TITLE = "Select Required Time";
    private GUI gui;
    private Milestone milestone;
    private MilestoneInfoGui parentGui;
    private long selectedHours;
    private long selectedMinutes;

    public TimeSelectionGui(Milestone milestone, MilestoneInfoGui parentGui) {
        this.milestone = milestone;
        this.parentGui = parentGui;
        this.selectedHours = milestone.getOnlineTime();
        this.selectedMinutes = 0; // Extract minutes if the current time has decimal hours

        // If the current time has minutes, extract them
        if (selectedHours > 0) {
            // Assuming onlineTime is stored in hours, convert to minutes for calculation
            long totalMinutes = selectedHours * 60;
            selectedHours = totalMinutes / 60;
            selectedMinutes = totalMinutes % 60;
        }

        buildGui();
    }

    private void buildGui() {
        gui = new GUI(6, GUI_TITLE);

        // Add border
        GUIItem border = new GUIItem(new ItemBuilder(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                .displayname(" ")
                .build());
        gui = gui.addBorder(border);

        // Current time display
        gui = gui.setItem(13, new ItemBuilder(Material.CLOCK)
                .displayname(ChatColor.GOLD + "Current Selection")
                .lore(ChatColor.WHITE + "Hours: " + selectedHours)
                .lore(ChatColor.WHITE + "Minutes: " + selectedMinutes)
                .lore(ChatColor.WHITE + "Total: " + formatTime(selectedHours, selectedMinutes))
                .build());

        // Hours section
        gui = gui.setItem(19, new ItemBuilder(Material.ORANGE_CONCRETE)
                .displayname(ChatColor.GOLD + "Hours")
                .lore(ChatColor.GRAY + "Current: " + selectedHours)
                .build());

        // Hour adjustment buttons
        gui = gui.setItem(10, createNumberHead(1), (event -> {
            if (event.isLeftClick()) {
                selectedHours = Math.max(0, selectedHours + (event.isShiftClick() ? 10 : 1));
            } else if (event.isRightClick()) {
                selectedHours = Math.max(0, selectedHours - (event.isShiftClick() ? 10 : 1));
            }
            buildGui();
        }));

        gui = gui.setItem(11, createNumberHead(5), (event -> {
            if (event.isLeftClick()) {
                selectedHours += 5;
            } else if (event.isRightClick()) {
                selectedHours = Math.max(0, selectedHours - 5);
            }
            buildGui();
        }));

        gui = gui.setItem(12, createNumberHead(10), (event -> {
            if (event.isLeftClick()) {
                selectedHours += 10;
            } else if (event.isRightClick()) {
                selectedHours = Math.max(0, selectedHours - 10);
            }
            buildGui();
        }));

        gui = gui.setItem(14, createNumberHead(24), (event -> {
            if (event.isLeftClick()) {
                selectedHours += 24;
            } else if (event.isRightClick()) {
                selectedHours = Math.max(0, selectedHours - 24);
            }
            buildGui();
        }));

        gui = gui.setItem(15, createNumberHead(100), (event -> {
            if (event.isLeftClick()) {
                selectedHours += 100;
            } else if (event.isRightClick()) {
                selectedHours = Math.max(0, selectedHours - 100);
            }
            buildGui();
        }));

        gui = gui.setItem(16, createNumberHead(500), (event -> {
            if (event.isLeftClick()) {
                selectedHours += 500;
            } else if (event.isRightClick()) {
                selectedHours = Math.max(0, selectedHours - 500);
            }
            buildGui();
        }));

        // Minutes section
        gui = gui.setItem(37, new ItemBuilder(Material.LIME_CONCRETE)
                .displayname(ChatColor.GREEN + "Minutes")
                .lore(ChatColor.GRAY + "Current: " + selectedMinutes)
                .build());

        // Minute adjustment buttons
        gui = gui.setItem(28, createNumberHead(1), (event -> {
            if (event.isLeftClick()) {
                selectedMinutes = Math.min(59, selectedMinutes + (event.isShiftClick() ? 10 : 1));
            } else if (event.isRightClick()) {
                selectedMinutes = Math.max(0, selectedMinutes - (event.isShiftClick() ? 10 : 1));
            }
            buildGui();
        }));

        gui = gui.setItem(29, createNumberHead(5), (event -> {
            if (event.isLeftClick()) {
                selectedMinutes = Math.min(59, selectedMinutes + 5);
            } else if (event.isRightClick()) {
                selectedMinutes = Math.max(0, selectedMinutes - 5);
            }
            buildGui();
        }));

        gui = gui.setItem(30, createNumberHead(10), (event -> {
            if (event.isLeftClick()) {
                selectedMinutes = Math.min(59, selectedMinutes + 10);
            } else if (event.isRightClick()) {
                selectedMinutes = Math.max(0, selectedMinutes - 10);
            }
            buildGui();
        }));

        gui = gui.setItem(32, createNumberHead(15), (event -> {
            if (event.isLeftClick()) {
                selectedMinutes = Math.min(59, selectedMinutes + 15);
            } else if (event.isRightClick()) {
                selectedMinutes = Math.max(0, selectedMinutes - 15);
            }
            buildGui();
        }));

        gui = gui.setItem(33, createNumberHead(30), (event -> {
            if (event.isLeftClick()) {
                selectedMinutes = Math.min(59, selectedMinutes + 30);
            } else if (event.isRightClick()) {
                selectedMinutes = Math.max(0, selectedMinutes - 30);
            }
            buildGui();
        }));

        // Preset time buttons
        gui = gui.setItem(20, new ItemBuilder(Material.EMERALD)
                .displayname(ChatColor.GREEN + "1 Hour")
                .lore(ChatColor.GRAY + "Click to set to 1 hour")
                .build(), (event -> {
            selectedHours = 1;
            selectedMinutes = 0;
            buildGui();
        }));

        gui = gui.setItem(21, new ItemBuilder(Material.EMERALD)
                .displayname(ChatColor.GREEN + "5 Hours")
                .lore(ChatColor.GRAY + "Click to set to 5 hours")
                .build(), (event -> {
            selectedHours = 5;
            selectedMinutes = 0;
            buildGui();
        }));

        gui = gui.setItem(22, new ItemBuilder(Material.EMERALD)
                .displayname(ChatColor.GREEN + "10 Hours")
                .lore(ChatColor.GRAY + "Click to set to 10 hours")
                .build(), (event -> {
            selectedHours = 10;
            selectedMinutes = 0;
            buildGui();
        }));

        gui = gui.setItem(23, new ItemBuilder(Material.EMERALD)
                .displayname(ChatColor.GREEN + "24 Hours")
                .lore(ChatColor.GRAY + "Click to set to 1 day")
                .build(), (event -> {
            selectedHours = 24;
            selectedMinutes = 0;
            buildGui();
        }));

        gui = gui.setItem(24, new ItemBuilder(Material.EMERALD)
                .displayname(ChatColor.GREEN + "100 Hours")
                .lore(ChatColor.GRAY + "Click to set to 100 hours")
                .build(), (event -> {
            selectedHours = 100;
            selectedMinutes = 0;
            buildGui();
        }));

        // Reset button
        gui = gui.setItem(31, new ItemBuilder(Material.BARRIER)
                .displayname(ChatColor.RED + "Reset")
                .lore(ChatColor.GRAY + "Set time to 0")
                .build(), (event -> {
            selectedHours = 0;
            selectedMinutes = 0;
            buildGui();
        }));

        // Confirm button
        gui = gui.setItem(48, new ItemBuilder(Material.LIME_CONCRETE)
                .displayname(ChatColor.GREEN + "Confirm")
                .lore(ChatColor.WHITE + "Set time to: " + formatTime(selectedHours, selectedMinutes))
                .build(), (event -> {
            // Convert back to the format expected by milestone (hours with decimal for minutes)
            double totalHours = selectedHours + (selectedMinutes / 60.0);
            milestone.setOnlineTime((long) Math.ceil(totalHours)); // Round up to ensure we don't lose precision

            // Save to storage
            Playtime.getInstance().getStorage().updateMilestone(milestone);
            Bukkit.getPluginManager().callEvent(new MilestoneUpdateEvent(milestone));

            event.getWhoClicked().sendMessage(ChatColor.GREEN + "Time set to " + formatTime(selectedHours, selectedMinutes));
            parentGui.refresh();
            parentGui.open((Player) event.getWhoClicked());
        }));

        // Cancel button
        gui = gui.setItem(50, new ItemBuilder(Material.RED_CONCRETE)
                .displayname(ChatColor.RED + "Cancel")
                .lore(ChatColor.GRAY + "Return without saving")
                .build(), (event -> {
            parentGui.open((Player) event.getWhoClicked());
        }));
    }

    private ItemStack createNumberHead(int number) {
        Material headMaterial = getNumberHeadMaterial(number);
        List<String> lore = Arrays.asList(
                ChatColor.WHITE + "Value: " + number,
                "",
                ChatColor.GRAY + "Left click: Add " + number,
                ChatColor.GRAY + "Right click: Subtract " + number,
                ChatColor.GRAY + "Shift click: Multiply by 10"
        );

        return new ItemBuilder(headMaterial)
                .displayname(ChatColor.YELLOW + "+" + number + " / -" + number)
                .lore(lore)
                .build();
    }

    private Material getNumberHeadMaterial(int number) {
        // Use different colored concrete blocks for different numbers
        switch (number) {
            case 1: return Material.WHITE_CONCRETE;
            case 5: return Material.YELLOW_CONCRETE;
            case 10: return Material.ORANGE_CONCRETE;
            case 15: return Material.MAGENTA_CONCRETE;
            case 24: return Material.BLUE_CONCRETE;
            case 30: return Material.PURPLE_CONCRETE;
            case 100: return Material.RED_CONCRETE;
            case 500: return Material.BLACK_CONCRETE;
            default: return Material.GRAY_CONCRETE;
        }
    }

    private String formatTime(long hours, long minutes) {
        if (hours == 0 && minutes == 0) {
            return "0 minutes";
        } else if (hours == 0) {
            return minutes + " minute" + (minutes == 1 ? "" : "s");
        } else if (minutes == 0) {
            return hours + " hour" + (hours == 1 ? "" : "s");
        } else {
            return hours + " hour" + (hours == 1 ? "" : "s") + " and " +
                   minutes + " minute" + (minutes == 1 ? "" : "s");
        }
    }

    public void open(Player player) {
        gui.open(player);
    }
}