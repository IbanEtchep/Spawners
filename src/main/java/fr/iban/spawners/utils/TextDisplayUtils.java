package fr.iban.spawners.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;

public class TextDisplayUtils {

    private static final double MIN_RANGE = 0.1;

    public static TextDisplay spawnTextDisplay(Location location) {
        TextDisplay textDisplay = (TextDisplay) location.getWorld().spawnEntity(location, EntityType.TEXT_DISPLAY);
        textDisplay.setBillboard(Display.Billboard.CENTER);

        return textDisplay;
    }

    public static TextDisplay getTextDisplay(Location location) {
        for (Entity nearbyEntity : location.getNearbyEntities(MIN_RANGE, MIN_RANGE, MIN_RANGE)) {
            if (nearbyEntity instanceof TextDisplay textDisplay) {
                return textDisplay;
            }

        }
        return null;
    }

    public static void updateStackSizeDisplay(Location location, int stackSize) {
        location = location.clone().add(0.5, 1.1, 0.5);
        TextDisplay display = TextDisplayUtils.getTextDisplay(location);

        if (stackSize <= 1) {
            if (display != null) {
                display.remove();
            }
            return;
        }

        if (display == null) {
            display = TextDisplayUtils.spawnTextDisplay(location);
        }

        display.text(Component.text("x").append(Component.text(stackSize).color(TextColor.fromHexString("#3498db"))));
    }

}
