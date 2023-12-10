package nl.thedutchruben.playtime.extentions;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPIExtension extends PlaceholderExpansion {
    /**
     * @return
     */
    @Override
    public @NotNull String getIdentifier() {
        return "tdrplaytime";
    }

    /**
     * @return
     */
    @Override
    public @NotNull String getAuthor() {
        return "TheDutchRuben";
    }

    /**
     * @return
     */
    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }
}
