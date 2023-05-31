package nl.thedutchruben.playtime.utils;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Random;

/**
 * @author Ruben
 * @version 1.0
 * <p>
 * This class is used to spawn firework
 * </p>
 * @since 1.0
 */
public class FireworkUtil {
    /**
     *
     */
    private static final Random random = new Random();

    /**
     * @param location The location to spawn the firework.
     */
    public static void spawn(Location location) {
        FireworkEffect effect = FireworkEffect.builder().trail(getRandomBoolean()).flicker(getRandomBoolean())
                .withColor(Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255)))
                .withFade(Color.fromRGB(random.nextInt(255), random.nextInt(255), random.nextInt(255)))
                .with(FireworkEffect.Type.values()[random.nextInt(FireworkEffect.Type.values().length)]).build();
        Firework fw = location.getWorld().spawn(location, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.clearEffects();
        meta.addEffect(effect);
        meta.setPower(random.nextInt(3));
        fw.setFireworkMeta(meta);
        fw.getScoreboardTags().add("tdrfirework");
    }

    /**
     * @return A random boolean.
     */
    private static boolean getRandomBoolean() {
        return Math.random() < 0.5;
    }
}
