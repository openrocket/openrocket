package net.sf.openrocket.gui.util;

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
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        // Alternating row colors
        if (!isSelected) {
            if (index % 2 == 0) {
                label.setBackground(Color.WHITE);
            } else {
                label.setBackground(new Color(245, 245, 245));
            }
        }
        // Text color
        if (isSelected) {
            label.setForeground(Color.WHITE);
        } else {
            label.setForeground(Color.BLACK);
        }
        return label;
    }
}
