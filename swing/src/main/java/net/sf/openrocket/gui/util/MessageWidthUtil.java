package net.sf.openrocket.gui.util;

/**
 * Helper class for setting the message width for e.g. JOptionPane pop-ups using HTML formatting.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public abstract class MessageWidthUtil {
    /**
     * Set the message width of <message> to <px> pixels using HTML formatting.
     * @param message message to be formatted
     * @param px width of the message in pixels
     * @return HTML-formatted message
     *
     */
    public static String setMessageWidth(String message, int px) {
        return String.format("<html><body><p style='width: %dpx'>%s</p></body></html>", px, message);
    }
}
