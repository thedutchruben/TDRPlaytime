package nl.thedutchruben.playtime.core.gui;

import nl.thedutchruben.mccore.spigot.ui.GUI;
import nl.thedutchruben.mccore.spigot.ui.GUIItem;
import nl.thedutchruben.mccore.utils.item.ItemBuilder;
import nl.thedutchruben.playtime.core.objects.Milestone;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MilestoneMessagesGui {

    public static final String GUI_TITLE = "Milestone Messages";
    private GUI gui;
    private Milestone milestone;
    private MilestoneInfoGui parentGui;
    private int currentPage = 0;
    private static final int MESSAGES_PER_PAGE = 28; // 4 rows of 7 messages

    public MilestoneMessagesGui(Milestone milestone, MilestoneInfoGui parentGui) {
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

        // Display current messages
        List<String> messages = milestone.getMessages();
        if (messages != null) {
            int startIndex = currentPage * MESSAGES_PER_PAGE;
            int endIndex = Math.min(startIndex + MESSAGES_PER_PAGE, messages.size());

            int slot = 10; // Start at slot 10 (second row, second column)
            for (int i = startIndex; i < endIndex; i++) {
                String message = messages.get(i);
                final int index = i;

                List<String> lore = new ArrayList<>();

                // Split long messages into multiple lines for better display
                String displayMessage = message;
                if (displayMessage.length() > 40) {
                    displayMessage = displayMessage.substring(0, 37) + "...";
                }

                lore.add(ChatColor.WHITE + "Message:");
                lore.add(ChatColor.GRAY + displayMessage);
                lore.add("");
                lore.add(ChatColor.YELLOW + "Supports color codes:");
                lore.add(ChatColor.GRAY + "&a, &b, &c, etc.");
                lore.add(ChatColor.GRAY + "Hex colors: <#FF0000>");
                lore.add("");
                lore.add(ChatColor.RED + "Right click to remove");

                gui = gui.setItem(slot, new ItemBuilder(Material.PAPER)
                        .displayname(ChatColor.YELLOW + "Message #" + (i + 1))
                        .lore(lore)
                        .build(), (event -> {
                    if (event.isRightClick()) {
                        milestone.removeMessage(index);
                        buildGui(); // Refresh the GUI
                        event.getWhoClicked().sendMessage(ChatColor.GREEN + "Message removed!");
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

        // Add message button
        gui = gui.setItem(40, new ItemBuilder(Material.GREEN_CONCRETE)
                .displayname(ChatColor.GREEN + "Add Message")
                .lore(ChatColor.GRAY + "Click to add a new message")
                .lore("")
                .lore(ChatColor.YELLOW + "Supports color codes:")
                .lore(ChatColor.GRAY + "&a, &b, &c, etc.")
                .lore(ChatColor.GRAY + "Hex colors: <#FF0000>")
                .build(), (event -> {
            event.getWhoClicked().closeInventory();
            event.getWhoClicked().sendMessage(ChatColor.YELLOW + "Type the message to add in chat:");
            event.getWhoClicked().sendMessage(ChatColor.GRAY + "You can use color codes like &a, &b, &c, etc.");
            event.getWhoClicked().sendMessage(ChatColor.GRAY + "You can also use hex colors like <#FF0000>");
            // TODO: Add chat listener for message input
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

        List<String> messages2 = milestone.getMessages();
        int totalMessages = messages2 != null ? messages2.size() : 0;
        int totalPages = (int) Math.ceil((double) totalMessages / MESSAGES_PER_PAGE);

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
