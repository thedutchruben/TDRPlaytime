package nl.thedutchruben.playtime.milestone;

import lombok.Data;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Data
public class Milestone {
    private long onlineTime;
    private List<ItemStack> itemStacks;
    private List<String> commands;


}
