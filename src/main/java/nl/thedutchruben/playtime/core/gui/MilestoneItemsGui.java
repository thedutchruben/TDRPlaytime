package nl.thedutchruben.playtime.core.gui;

import nl.thedutchruben.mccore.spigot.ui.GUI;
import nl.thedutchruben.mccore.spigot.ui.GUIItem;
import nl.thedutchruben.mccore.utils.item.ItemBuilder;
import nl.thedutchruben.playtime.core.objects.Milestone;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MilestoneItemsGui {

    public static final String GUI_TITLE = "Milestone Items";
    private GUI gui;
    private Milestone milestone;
    private MilestoneInfoGui parentGui;
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 28; // 4 rows of 7 items

    public MilestoneItemsGui(Milestone milestone, MilestoneInfoGui parentGui) {
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

        // Display current items
        List<ItemStack> items = milestone.getItemStackObjects();
        if (items != null) {
            int startIndex = currentPage * ITEMS_PER_PAGE;
            int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, items.size());

            int slot = 10; // Start at slot 10 (second row, second column)
            for (int i = startIndex; i < endIndex; i++) {
                ItemStack item = items.get(i);
                final int index = i;

                // Create a copy of the item with additional lore
                ItemStack displayItem = item.clone();
                ItemBuilder builder = new ItemBuilder(displayItem);

                List<String> lore = new ArrayList<>();
                if (displayItem.getItemMeta() != null && displayItem.getItemMeta().getLore() != null) {
                    lore.addAll(displayItem.getItemMeta().getLore());
                }
                lore.add("");
                lore.add(ChatColor.RED + "Right click to remove");
                builder.lore(lore);

                gui = gui.setItem(slot, builder.build(), (event -> {
                    if (event.isRightClick()) {
                        milestone.removeItemStack(index);
                        buildGui(); // Refresh the GUI
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

        // Add item button
        gui = gui.setItem(40, new ItemBuilder(Material.GREEN_CONCRETE)
                .displayname(ChatColor.GREEN + "Add Item")
                .lore(ChatColor.GRAY + "Hold the item you want to add")
                .lore(ChatColor.GRAY + "and click this button")
                .build(), (event -> {
            Player player = (Player) event.getWhoClicked();
            ItemStack heldItem = player.getItemInHand();

            if (heldItem != null && heldItem.getType() != Material.AIR) {
                milestone.addItemStack(heldItem.clone());
                player.sendMessage(ChatColor.GREEN + "Item added to milestone!");
                buildGui(); // Refresh the GUI
            } else {
                player.sendMessage(ChatColor.RED + "You must be holding an item to add it!");
            }
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

        List<ItemStack> items2 = milestone.getItemStackObjects();
        int totalItems = items2 != null ? items2.size() : 0;
        int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);

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
