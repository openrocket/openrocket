package net.sf.openrocket.gui.util;

import net.sf.openrocket.startup.Application;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import java.awt.Color;
import java.awt.Component;

/**
 * An improved list cell renderer, with alternating row background colors.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class BetterListCellRenderer extends DefaultListCellRenderer {
    private static final SwingPreferences prefs = (SwingPreferences) Application.getPreferences();

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        // Alternating row colors
        if (!isSelected) {
            if (index % 2 == 0) {
                label.setBackground(prefs.getUITheme().getRowBackgroundDarkerColor());
            } else {
                label.setBackground(prefs.getUITheme().getRowBackgroundLighterColor());
            }
        }
        // Text color
        if (isSelected) {
            label.setForeground(prefs.getUITheme().getTextSelectionForegroundColor());
        } else {
            label.setForeground(prefs.getUITheme().getTextColor());
        }
        return label;
    }
}
