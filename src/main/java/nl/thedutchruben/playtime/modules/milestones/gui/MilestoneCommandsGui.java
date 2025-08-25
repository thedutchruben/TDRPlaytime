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

public class MilestoneCommandsGui {

    public static final String GUI_TITLE = "Milestone Commands";
    private GUI gui;
    private Milestone milestone;
    private MilestoneInfoGui parentGui;
    private int currentPage = 0;
    private static final int COMMANDS_PER_PAGE = 28; // 4 rows of 7 commands

    public MilestoneCommandsGui(Milestone milestone, MilestoneInfoGui parentGui) {
        this.milestone = milestone;
        this.parentGui = parentGui;
        buildGui();
    }

    private void buildGui() {
        gui = new GUI(6, GUI_TITLE + " - Page " + (currentPage + 1));

        // Add border
        GUIItem border = new GUIItem(new ItemBuilder(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                .displayname(" ")
                .build());
        gui = gui.addBorder(border);

        // Display current commands
        List<String> commands = milestone.getCommands();
        if (commands != null) {
            int startIndex = currentPage * COMMANDS_PER_PAGE;
            int endIndex = Math.min(startIndex + COMMANDS_PER_PAGE, commands.size());

            int slot = 10; // Start at slot 10 (second row, second column)
            for (int i = startIndex; i < endIndex; i++) {
                String command = commands.get(i);
                final int index = i;

                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.WHITE + "Command: " + ChatColor.GRAY + command);
                lore.add("");
                lore.add(ChatColor.YELLOW + "Available placeholders:");
                lore.add(ChatColor.GRAY + "%playername% - Player's name");
                lore.add(ChatColor.GRAY + "%player_name% - Player's name");
                lore.add(ChatColor.GRAY + "%playeruuid% - Player's UUID");
                lore.add(ChatColor.GRAY + "%player_uuid% - Player's UUID");
                lore.add("");
                lore.add(ChatColor.RED + "Right click to remove");

                gui = gui.setItem(slot, new ItemBuilder(Material.COMMAND_BLOCK)
                        .displayname(ChatColor.YELLOW + "Command #" + (i + 1))
                        .lore(lore)
                        .build(), (event -> {
                    if (event.isRightClick()) {
                        String removedCommand = milestone.getCommands().get(index);
                        milestone.removeCommand(index);
                        Playtime.getInstance().getStorage().updateMilestone(milestone);
                        Bukkit.getPluginManager().callEvent(new MilestoneUpdateEvent(milestone));
                        buildGui(); // Refresh the GUI
                        event.getWhoClicked().sendMessage(ChatColor.GREEN + "Command removed: " + ChatColor.GRAY + removedCommand);
                    }
                }));

                // Move to next slot (skip border slots)
                slot++;
                if (slot % 9 == 8) { // End of row
                    slot += 2; // Skip to next row, skip border
                }
                if (slot >= 44) break; // Don't go past row 4
            }
        }

        // Add command button
        gui = gui.setItem(40, new ItemBuilder(Material.GREEN_CONCRETE)
                .displayname(ChatColor.GREEN + "Add Command")
                .lore(ChatColor.GRAY + "Click to add a new command")
                .lore("")
                .lore(ChatColor.YELLOW + "Available placeholders:")
                .lore(ChatColor.GRAY + "%playername% - Player's name")
                .lore(ChatColor.GRAY + "%player_name% - Player's name")
                .lore(ChatColor.GRAY + "%playeruuid% - Player's UUID")
                .lore(ChatColor.GRAY + "%player_uuid% - Player's UUID")
                .build(), (event -> {
            event.getWhoClicked().closeInventory();
            Player player = (Player) event.getWhoClicked();
            
            ChatInputManager.getInstance().requestInput(player,
                "Enter the command to add (without /):\n" +
                ChatColor.GRAY + "Available placeholders:\n" +
                ChatColor.GRAY + "%playername% or %player_name% - Player's name\n" +
                ChatColor.GRAY + "%playeruuid% or %player_uuid% - Player's UUID\n" +
                ChatColor.GRAY + "Example: give %playername% diamond 5",
                parentGui,
                (p, input) -> {
                    if (input.isEmpty()) {
                        p.sendMessage(ChatColor.RED + "Command cannot be empty!");
                        this.open(p);
                        return;
                    }
                    
                    // Remove leading slash if present
                    if (input.startsWith("/")) {
                        input = input.substring(1);
                    }
                    
                    // Check if command already exists
                    if (milestone.getCommands().contains(input)) {
                        p.sendMessage(ChatColor.RED + "This command already exists in the milestone!");
                        this.open(p);
                        return;
                    }
                    
                    milestone.addCommand(input);
                    Playtime.getInstance().getStorage().updateMilestone(milestone);
                    Bukkit.getPluginManager().callEvent(new MilestoneUpdateEvent(milestone));
                    
                    p.sendMessage(ChatColor.GREEN + "Command added: " + ChatColor.GRAY + input);
                    this.buildGui(); // Refresh to show new command
                    this.open(p);
                }
            );
        }));

        // Navigation buttons
        if (currentPage > 0) {
            gui = gui.setItem(48, new ItemBuilder(Material.ARROW)
                    .displayname(ChatColor.YELLOW + "Previous Page")
                    .build(), (event -> {
                currentPage--;
                buildGui();
            }));
        }

        List<String> commands2 = milestone.getCommands();
        int totalCommands = commands2 != null ? commands2.size() : 0;
        int totalPages = (int) Math.ceil((double) totalCommands / COMMANDS_PER_PAGE);

        if (currentPage < totalPages - 1) {
            gui = gui.setItem(50, new ItemBuilder(Material.ARROW)
                    .displayname(ChatColor.YELLOW + "Next Page")
                    .build(), (event -> {
                currentPage++;
                buildGui();
            }));
        }

        // Back button
        gui = gui.setItem(49, new ItemBuilder(Material.BARRIER)
                .displayname(ChatColor.RED + "Back")
                .lore(ChatColor.GRAY + "Return to milestone editor")
                .build(), (event -> {
            parentGui.refresh();
            parentGui.open((Player) event.getWhoClicked());
        }));
    }

    public void open(Player player) {
        gui.open(player);
    }
}