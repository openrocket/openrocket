package net.sf.openrocket.file.rasaero.importt;

import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Color;

/**
 * Handles the 2D outline color handling from RASAero.
 * Currently, this is always black, but may change in the future.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public abstract class ColorHandler {

    /**
     * Change the color of the component based on the color string from the RASAero document.
     * @param component the component to change the color of
     * @param color the color string from the RASAero document
     */
    public static void applyRASAeroColor(RocketComponent component, String color) {
        if ("Black".equals(color)) {
            component.setColor(Color.BLACK);
            return;
        }
        // Default to black
        component.setColor(Color.BLACK);
    }
}
