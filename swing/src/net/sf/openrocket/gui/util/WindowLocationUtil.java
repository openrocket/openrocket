package net.sf.openrocket.gui.util;

import java.awt.GraphicsDevice;
import java.awt.Window;

/**
 * Helper class for setting the location of a Swing window. E.g. to check when the window is outside the screen and
 * recenter it if so.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public abstract class WindowLocationUtil {
    /**
     * Sets the location of the window, but don't go outside the screen.
     * @param window the window to move
     * @param x the target x position on the screen
     * @param y the target y position on the screen
     */
    public static void setLocationWithinScreen(Window window, int x, int y) {
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        java.awt.Dimension windowSize = window.getSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        int windowWidth = windowSize.width;
        int windowHeight = windowSize.height;
        int xPos = x;
        int yPos = y;
        if (xPos + windowWidth > screenWidth) {
            xPos = screenWidth - windowWidth;
        }
        if (yPos + windowHeight > screenHeight) {
            yPos = screenHeight - windowHeight;
        }
        window.setLocation(xPos, yPos);
    }

    /**
     * Moves the window to the center of the screen if its location is outside the boundary of the screen.
     * @param window window to move
     */
    public static void moveIfOutsideOfMonitor(Window window) {
        GraphicsDevice currentDevice = window.getGraphicsConfiguration().getDevice();
        if (currentDevice != null && window.isVisible() &&
                !currentDevice.getDefaultConfiguration().getBounds().contains(window.getLocationOnScreen())) {
            int width = currentDevice.getDefaultConfiguration().getBounds().width;
            int height = currentDevice.getDefaultConfiguration().getBounds().height;
            window.setLocation(
                    ((width / 2) - (window.getSize().width / 2)) + currentDevice.getDefaultConfiguration().getBounds().x,
                    ((height / 2) - (window.getSize().height / 2)) + currentDevice.getDefaultConfiguration().getBounds().y
            );
        }
    }

    /**
     * Moves the window to the center of the screen if it is on another monitor as the parent window, or if its location
     * is outside the boundary of the parent's monitor screen.
     * @param window window to move
     * @param parent parent window
     */
    public static void moveIfOutsideOfParentMonitor(Window window, Window parent) {
        GraphicsDevice parentDevice = parent.getGraphicsConfiguration().getDevice();
        GraphicsDevice currentDevice = window.getGraphicsConfiguration().getDevice();
        if (parentDevice != null && (currentDevice == null || currentDevice != parentDevice ||
                (window.isVisible() && !parentDevice.getDefaultConfiguration().getBounds().contains(
                        window.getLocationOnScreen())))) {
            int width = parentDevice.getDefaultConfiguration().getBounds().width;
            int height = parentDevice.getDefaultConfiguration().getBounds().height;
            window.setLocation(
                    ((width / 2) - (window.getSize().width / 2)) + parentDevice.getDefaultConfiguration().getBounds().x,
                    ((height / 2) - (window.getSize().height / 2)) + parentDevice.getDefaultConfiguration().getBounds().y
            );
        }
    }
}
